package com.github.tempoden.llmjudge.backend.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DataEntry {
    @JsonProperty("input")
    public String input;
    @JsonProperty("reference_output")
    public String referenceOutput;

    // implicitly used by parser
    DataEntry() {}

    DataEntry(String input, String referenceOutput) {
        this.input = input;
        this.referenceOutput = referenceOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataEntry that)) return false;
        return Objects.equals(input, that.input) &&
                Objects.equals(referenceOutput, that.referenceOutput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, referenceOutput);
    }
}
