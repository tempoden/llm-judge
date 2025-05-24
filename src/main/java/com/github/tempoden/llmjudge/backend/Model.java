package com.github.tempoden.llmjudge.backend;

import com.github.tempoden.llmjudge.backend.concurrency.*;
import com.github.tempoden.llmjudge.backend.parsing.*;
import com.github.tempoden.llmjudge.backend.runner.*;
import com.github.tempoden.llmjudge.backend.scoring.*;
import com.github.tempoden.llmjudge.gui.*;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;

import javax.swing.table.TableModel;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class Model {

    private final ViewModel vm;
    private final DataParser parser = new JSONParser();

    private final static OpenAIClientAsync client = OpenAIOkHttpClientAsync.fromEnv();

    private static final int RESPONSE_COL = 2;
    private static final int SCORE_COL = 3;
    private Content jsonData;

    private String pythonPath = "C:\\Users\\tempo\\anaconda3\\python.exe";

    public Model (ViewModel vm) {
        this.vm = vm;

        vm.setPythonPath("default `python` executable from $PATH will be used");
        vm.setJsonPath("");
        vm.choseJSON(this::setModelFile);
        vm.chosePython(this::setPython);
    }

    public void setPython(String pythonPath) {
        this.pythonPath = pythonPath;
        vm.setPythonPath(pythonPath);
    }

    public void setModelFile(String jsonPath) {
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
                    throw new RuntimeException(e);
                }
            }
        );
    }

    public void runTask() {
        String modelPath = jsonData.modelPath();
        List<DataEntry> data = jsonData.data();

        CancellationToken token = new CancellationToken();
        vm.enableCancel(token::cancel);

        ApplicationManager.getApplication().executeOnPooledThread(
            () -> {
                try {
                    TableModel tm = Util.buildTableModel(data);
                    vm.resetTable(tm);

                    if (token.isCancelled()) {
                        return;
                    }

                    ModelRunner runner = new PythonRunner(
                            pythonPath,
                            modelPath);
                    Scorer scorer = new OpenAIScorer(client, token);

                    ExecutorService exec = AppExecutorUtil.createBoundedApplicationPoolExecutor("LLM-Judge", 4);

                    List<CompletableFuture<Void>> subtasks = new ArrayList<>(data.size());
                    for (int i = 0; i < data.size(); i++) {
                        final int rowId = i;
                        Worker.StatusCallback runnerStatus = (String s) -> {
                            ApplicationManager.getApplication().invokeLater(
                                    () -> tm.setValueAt(s, rowId, RESPONSE_COL)
                            );
                        };
                        Worker.StatusCallback scorerStatus = (String s) -> {
                            ApplicationManager.getApplication().invokeLater(
                                    () -> tm.setValueAt(s, rowId, SCORE_COL)
                            );
                        };

                        CompletableFuture<Void> subtask = CompletableFuture.runAsync(
                                new Worker(runner, runnerStatus,
                                        scorer, scorerStatus,
                                        data.get(i), 3,
                                        token
                                ),
                                exec
                        );
                        subtasks.add(subtask);
                    }

                    CompletableFuture<Void> waitAll = CompletableFuture.allOf(subtasks.toArray(CompletableFuture[]::new));
                    if (WaitUtil.waitWithCancel(waitAll, token)) {
                        waitAll.join();
                        subtasks.forEach(CompletableFuture::join);
                    } else {
                        waitAll.cancel(true);
                        subtasks.forEach(s -> s.cancel(true));
                    }

                } finally {
                    vm.enableStart(this::runTask);
                }
            }
        );
    }
}
