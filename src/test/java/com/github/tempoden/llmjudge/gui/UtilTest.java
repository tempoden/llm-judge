package com.github.tempoden.llmjudge.gui;

import com.github.tempoden.llmjudge.backend.parsing.DataEntry;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class UtilTest {
    @Test
    public void testTableContentCreation() {
        List<DataEntry> data = List.of(
          new DataEntry("lya", "mya"),
          new DataEntry("nya", "krya")
        );

        Object[][] expected = {
                {"lya", "mya", "", ""},
                {"nya", "krya", "", ""}
        };

        assertArrayEquals(expected, Util.toTableModelData(data));
    }
}
