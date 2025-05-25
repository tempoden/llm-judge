package com.github.tempoden.llmjudge.gui;

import com.github.tempoden.llmjudge.backend.parsing.DataEntry;

import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.List;

public final class Util {

    public static final String[] POOL_OPTIONS = {"1", "4", "8", "16", "Unbounded"};
    public static final int DEFAULT_POOL_OPTION = 2;
    public static final int DEFAULT_N = 3;

    public static final String[] columnNames = {"Input", "Reference Output", "Model Output", "Score"};

    static Object[][] toTableModelData(@NotNull List<DataEntry> data) {
        return data.stream()
                .map(row -> new Object[]{row.input(), row.referenceOutput(), "", ""})
                .toArray(Object[][]::new);
    }

    public static TableModel buildTableModel(@NotNull List<DataEntry> data) {
        return new DefaultTableModel(toTableModelData(data), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells non-editable
            }
        };
    }
}
