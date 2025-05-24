package com.github.tempoden.llmjudge.backend.parsing;

import org.jetbrains.annotations.NotNull;

import java.io.Reader;

public interface DataParser {
    Content parse(@NotNull Reader src);
}
