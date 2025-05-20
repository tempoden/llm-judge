package com.github.tempoden.llmjudge.backend.parsing;

import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JSONParserTest {

    @Test
    public void testParsingHappyPath() {
        StringReader sr = new StringReader("""
                {
                    "model_path": "path/to/model.py",
                    "data": [
                        {
                            "input": "industrial city in germany on the rhine herne canal",
                            "reference_output": "Henrichenburg"
                        },
                        {
                            "input": "german word for pleasure from someone else's pain",
                            "reference_output": "Schadenfreude"
                        },
                        {
                            "input": "who has the talismans in sailor moon s",
                            "reference_output": "Haruka and Michiru"
                        },
                        {
                            "input": "who sang it must have been love but its over now",
                            "reference_output": "Roxette"
                        }
                    ]
                }
                """);

        DataParser parser = new JSONParser();
        Content expected = new Content(
                "path/to/model.py",
                List.of(
                        new DataEntry("industrial city in germany on the rhine herne canal",
                                "Henrichenburg"),
                        new DataEntry("german word for pleasure from someone else's pain",
                                "Schadenfreude"),
                        new DataEntry("who has the talismans in sailor moon s",
                                "Haruka and Michiru"),
                        new DataEntry("who sang it must have been love but its over now",
                                "Roxette")
                )
        );

        assertEquals(expected, parser.parse(sr));
    }

    @Test
    public void testParsingEmpty() {
        StringReader sr = new StringReader("{}");

        DataParser parser = new JSONParser();
        Content expected = new Content();

        assertEquals(expected, parser.parse(sr));
    }

    @Test(expected = ParsingException.class)
    public void testMalformedJSON() {
        StringReader sr = new StringReader("""
                model_path: "path/to/model.py"
                data:
                  - input: "industrial city in germany on the rhine herne canal",
                    reference_output: "Henrichenburg"
                  - input: "german word for pleasure from someone else's pain",
                    reference_output: "Schadenfreude"
                """);

        DataParser parser = new JSONParser();
        parser.parse(sr);
    }

    @Test(expected = ParsingException.class)
    public void testWrongJSONFormat() {
        StringReader sr = new StringReader("""
                {
                    "model_path": "path/to/model.py",
                    "data": [
                        {
                            "input": "industrial city in germany on the rhine herne canal",
                            "reference_output": "Henrichenburg",
                            "answer": "HEYY"
                        },
                    ]
                }
                """);

        DataParser parser = new JSONParser();
        parser.parse(sr);
    }
}
