package com.github.tempoden.llmjudge.backend.parsing;

import java.io.Reader;

public interface DataParser {
    Content parse(Reader src);
}
