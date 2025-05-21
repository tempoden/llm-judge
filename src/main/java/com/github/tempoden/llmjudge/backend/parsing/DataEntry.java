package com.github.tempoden.llmjudge.backend.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public record DataEntry (
        @NotNull String input,
        @JsonProperty("reference_output") @NotNull String referenceOutput
) {
}
