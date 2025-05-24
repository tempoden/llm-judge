package com.github.tempoden.llmjudge.backend.runner;

import org.jetbrains.annotations.NotNull;

public interface ModelRunner {
    String queryModel(@NotNull String prompt);
}
