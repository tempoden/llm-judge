package com.github.tempoden.llmjudge.backend.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Content (
        @JsonProperty("model_path") String modelPath,
        List<DataEntry> data
) {
}
