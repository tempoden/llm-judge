package com.github.tempoden.llmjudge.backend.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Content {
    @JsonProperty("model_path")
    public String modelPath;
    @JsonProperty("data")
    public List<DataEntry> data;

    // implicitly used by parser
    Content() {}

    Content(String modelPath, List<DataEntry> data) {
        this.modelPath = modelPath;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content that)) return false;
        return Objects.equals(modelPath, that.modelPath) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelPath, data);
    }
}
