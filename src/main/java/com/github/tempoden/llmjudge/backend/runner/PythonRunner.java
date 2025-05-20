package com.github.tempoden.llmjudge.backend.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PythonRunner implements ModelRunner {
    private final String interpreterPath;
    private final String modelPath;

    public PythonRunner (String interpreterPath, String modelPath) {
        this.interpreterPath = interpreterPath;
        this.modelPath = modelPath;
    }

    @Override
    public String queryModel(String prompt) {
        ProcessBuilder builder = new ProcessBuilder(List.of(
                interpreterPath,
                modelPath,
                prompt
        ));

        StringBuilder result = new StringBuilder();

        try {
            Process process = builder.start();

            // I do not read from stderr here, but it might be
            // a helpful piece of diagnostic information for the
            // model usage.

            // Read the output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            // Wait for a process to finish
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RunModelException("Non-zero exit code: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            throw new RunModelException("Failed to perform query", e);
        }

        return result.toString();
    }
}
