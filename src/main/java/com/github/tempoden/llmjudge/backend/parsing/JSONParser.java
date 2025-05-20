package com.github.tempoden.llmjudge.backend.parsing;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;

public class JSONParser implements DataParser{
    @Override
    public Content parse(Reader src) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(src, Content.class);
        } catch (DatabindException e) {
            throw new ParsingException("Unsupported JSON format", e);
        } catch (StreamReadException e){
            throw new ParsingException("Given data is not a valid JSON", e);
        } catch (IOException e) {
            throw new ParsingException("I/O error", e);
        }
    }
}
