package com.github.tempoden.llmjudge.gui;

import com.github.tempoden.llmjudge.backend.parsing.DataEntry;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.List;

public final class Util {
    public static final String[] columnNames = {"Input", "Reference Output", "Model Output", "Score"};

    static Object[][] toTableModelData(List<DataEntry> data) {
        return data.stream()
                .map(row -> new Object[]{row.input(), row.referenceOutput(), "", ""})
                .toArray(Object[][]::new);
    }

    public static TableModel buildTableModel(List<DataEntry> data) {
        return new DefaultTableModel(toTableModelData(data), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells non-editable
            }
        };
    }
}
