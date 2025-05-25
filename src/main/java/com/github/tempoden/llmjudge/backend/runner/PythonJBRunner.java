package com.github.tempoden.llmjudge.backend.runner;

import com.github.tempoden.llmjudge.backend.concurrency.*;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PythonJBRunner implements ModelRunner {

    private static final Logger LOG = Logger.getInstance(PythonJBRunner.class);

    private final String interpreterPath;
    private final String modelPath;
    private final CancellationToken cancel;

     public PythonJBRunner(@NotNull String interpreterPath,
                           @NotNull String modelPath,
                           @NotNull CancellationToken cancel) {
        this.interpreterPath = interpreterPath;
        this.modelPath = modelPath;
        this.cancel = cancel;
    }

    @Override
    public String queryModel(@NotNull String prompt) {
        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();

        CompletableFuture<Integer> exitCodeFuture = new CompletableFuture<>();
        OSProcessHandler handler;

        try {
            handler = new OSProcessHandler(new GeneralCommandLine(
                    List.of(interpreterPath, modelPath, prompt)
            ));

            handler.addProcessListener(new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    if (ProcessOutputTypes.STDOUT.equals(outputType)) {
                        stdoutBuilder.append(event.getText());
                        LOG.debug("Process Output: " + event.getText().trim());
                    } else if (ProcessOutputTypes.STDERR.equals(outputType)) {
                        stderrBuilder.append(event.getText());
                        LOG.debug("Process stderr: " + event.getText().trim());
                    }
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    LOG.debug("Process terminated with exit code: " + event.getExitCode());
                    exitCodeFuture.complete(event.getExitCode());
                }
            });

            // We should not forget to kill all child processes if there are any
            handler.setShouldDestroyProcessRecursively(true);
            handler.startNotify();

        } catch (ExecutionException e) {
            RunModelException err = new RunModelException(
                    """
                    Failed to execute model '%s' using interpreter '%s'
                    on input '%s'
                    
                    Captured stderr: %s
                    """.formatted(modelPath, interpreterPath, prompt, stderrBuilder.toString()),
                    e
            );
            LOG.error(err);
            throw err;
        }

        if (WaitUtil.waitWithCancel(exitCodeFuture, cancel)) {
            int exitCode = exitCodeFuture.join();
            if (exitCode == 0) {
                return stdoutBuilder.toString();
            } else {
                RunModelException err = new RunModelException(
                        """
                        Interpreter '%s' exited with non-zero code %d
                        when running model '%s' on input '%s'
                        
                        Captured stderr: %s
                        """.formatted(interpreterPath, exitCode,  modelPath, prompt, stderrBuilder.toString())
                );
                LOG.error(err);
                throw err;
            }
        }

        handler.destroyProcess();
        throw new RunCancelledException();
    }
}
