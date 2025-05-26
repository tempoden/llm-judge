package com.github.tempoden.llmjudge.backend;

import com.github.tempoden.llmjudge.backend.concurrency.*;
import com.github.tempoden.llmjudge.backend.parsing.*;
import com.github.tempoden.llmjudge.backend.runner.*;
import com.github.tempoden.llmjudge.backend.scoring.*;
import com.github.tempoden.llmjudge.gui.*;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.TableModel;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class Model {

    private static final Logger LOG = Logger.getInstance(Model.class);

    private final Controller vm;
    private final DataParser parser = new JSONParser();

    private static OpenAIClientAsync client;
    static {
        try {
            client = OpenAIOkHttpClientAsync.builder()
                    .fromEnv()
                    // In some situations, adding timeout may prevent a situation when
                    // request is hanged on OpenAI server side and force its restart,
                    // but it is better not to use it, because it may lead to Exceptions.
                    //
                    // .timeout(Duration.ofSeconds(30))
                    //
                    // Also, this one https://github.com/openai/openai-java/issues/128 seems to not be
                    // fully fixed, and I will report it right after finishing this task.
                    //
                    .build();
        } catch (Exception e) {
            LOG.error("Failed to initialize OpenAI client from env. Please set OPENAI_API_KEY before starting plugin.", e);
        }
    }

    private static final int RESPONSE_COL = 2;
    private static final int SCORE_COL = 3;

    private Content jsonData;
    private String pythonPath = "python";
    private PoolSize poolSize = PoolSize.EIGHT;
    private int judgeReqCount = 3;

    public Model (@NotNull Controller controller) {
        this.vm = controller;

        controller.setPythonPath("default `python` executable from $PATH will be used");
        controller.setJsonPath("");
        controller.choseJSON(this::setModelFile);
        controller.chosePython(this::setPython);
        controller.setPoolSize(this::setPoolSize);
        controller.setJudgeReqCount(this::setJudgeReqCount);

        if (client == null) {
            controller.disableUI();
            controller.showErrorDialog("Failed to initialize OpenAI client from env. Please set OPENAI_API_KEY before starting plugin.");
        }
    }

    public void setPoolSize(@NotNull String poolSize) {
        this.poolSize = PoolSize.fromString(poolSize);
    }

    public void setJudgeReqCount(int reqCount) {
        this.judgeReqCount = reqCount;
    }

    public void setPython(@NotNull String pythonPath) {
        this.pythonPath = pythonPath;
        vm.setPythonPath(pythonPath);
    }

    public void setModelFile(@NotNull String jsonPath) {
        ApplicationManager.getApplication().executeOnPooledThread(
            () -> {
                try {
                    jsonData = parser.parse(new FileReader(jsonPath));
                    TableModel tm = Util.buildTableModel(jsonData.data());
                    vm.resetTable(tm);
                    vm.enableStart(this::runTask);
                    vm.setModelPath(jsonData.modelPath());
                    vm.setJsonPath(jsonPath);
                } catch (FileNotFoundException | ParsingException e) {
                    vm.showErrorDialog("Error opening provided JSON: " + e.getMessage());
                }
            }
        );
    }

    public void runTask() {
        // Make local references to be captured by lambda
        // so unwanted model state changes will not have any effect.
        String pythonPath = this.pythonPath;
        String modelPath = jsonData.modelPath();
        List<DataEntry> data = jsonData.data();
        PoolSize poolSize = this.poolSize;
        int judgeReqCount = this.judgeReqCount;

        CancellationToken token = new CancellationToken();
        vm.enableCancel(token::cancel);
        vm.disableSettings();

        Worker.ErrorHandler errorHandler = e -> {
            // Cancel all work on error
            token.cancel();
            vm.showErrorDialog(e.getMessage());
        };

        ApplicationManager.getApplication().executeOnPooledThread(
            () -> {
                try {
                    TableModel tm = Util.buildTableModel(data);
                    vm.resetTable(tm);

                    if (token.isCancelled()) {
                        return;
                    }

                    ModelRunner runner = new PythonJBRunner(pythonPath, modelPath, token);
                    Scorer scorer = new OpenAIScorer(client, token);

                    ExecutorService exec = getExecutorService(poolSize);

                    List<CompletableFuture<Void>> subtasks = new ArrayList<>(data.size());
                    for (int i = 0; i < data.size(); i++) {
                        CompletableFuture<Void> subtask = CompletableFuture.runAsync(
                            createSubtaskContent(
                                runner, scorer, errorHandler,
                                data.get(i), judgeReqCount,
                                token, tm, i),
                            exec
                        );
                        subtasks.add(subtask);
                    }

                    CompletableFuture<Void> waitAll = CompletableFuture.allOf(subtasks.toArray(CompletableFuture[]::new));
                    if (WaitUtil.waitWithCancel(waitAll, token)) {
                        waitAll.join();
                        subtasks.forEach(CompletableFuture::join);
                        vm.showFinishDialog();
                    } else {
                        waitAll.cancel(true);
                        subtasks.forEach(s -> s.cancel(true));
                    }

                } finally {
                    vm.enableStart(this::runTask);
                    vm.enableSettings();
                }
            }
        );
    }

    private static Worker createSubtaskContent(
            @NotNull ModelRunner runner,
            @NotNull Scorer scorer,
            @NotNull Worker.ErrorHandler errorHandler,
            @NotNull DataEntry workload,
            int judgeReqCount,
            @NotNull CancellationToken token,
            @NotNull TableModel tm,
            int rowId) {

        Worker.StatusCallback runnerStatus =
            (String s) -> ApplicationManager.getApplication()
                .invokeLater(() -> tm.setValueAt(s, rowId, RESPONSE_COL));

        Worker.StatusCallback scorerStatus =
            (String s) -> ApplicationManager.getApplication()
                .invokeLater(() -> tm.setValueAt(s, rowId, SCORE_COL));

        return new Worker(runner, runnerStatus,
                          scorer, scorerStatus,
                          workload, judgeReqCount,
                          token, errorHandler);
    }

    private enum PoolSize {
        ONE("1"),
        FOUR("4"),
        EIGHT("8"),
        SIXTEEN("16"),
        UNBOUNDED("Unbounded");

        private final String value;

        PoolSize(String value) {
            this.value = value;
        }

        public static PoolSize fromString(String str) {
            for (PoolSize limit : values()) {
                if (limit.value.equals(str)) {
                    return limit;
                }
            }
            throw new IllegalArgumentException("Unknown PoolSize: " + str);
        }
    }

    private static final String POOL_NAME = "LLM-Judge";
    private static ExecutorService getExecutorService(@NotNull PoolSize poolSize) {
        return switch (poolSize) {
            case ONE -> AppExecutorUtil.createBoundedApplicationPoolExecutor(POOL_NAME, 1);
            case FOUR -> AppExecutorUtil.createBoundedApplicationPoolExecutor(POOL_NAME, 4);
            case SIXTEEN -> AppExecutorUtil.createBoundedApplicationPoolExecutor(POOL_NAME, 16);
            case UNBOUNDED -> AppExecutorUtil.getAppExecutorService();
            // Make 8 threads default;
            default -> AppExecutorUtil.createBoundedApplicationPoolExecutor(POOL_NAME, 8);
        };
    }
}
