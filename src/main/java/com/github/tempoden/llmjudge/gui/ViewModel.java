package com.github.tempoden.llmjudge.gui;

import com.github.tempoden.llmjudge.backend.Model;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableModel;

public class ViewModel {

    private JButton pythonPathButton;
    private JLabel pythonPathLabel;

    private JButton jsonButton;
    private JLabel jsonLabel;

    private JLabel modelPathLabel;

    private JButton controlButton;

    private JTable table;

    public ViewModel(@NotNull JButton pythonPathButton,
                     @NotNull JLabel pythonPathLabel,
                     @NotNull JButton jsonButton,
                     @NotNull JLabel jsonLabel,
                     @NotNull JLabel modelPathLabel,
                     @NotNull JButton controlButton,
                     @NotNull JTable table)  {
        this.pythonPathButton = pythonPathButton;
        this.pythonPathLabel = pythonPathLabel;

        this.jsonButton = jsonButton;
        this.jsonLabel = jsonLabel;

        this.modelPathLabel = modelPathLabel;

        this.controlButton = controlButton;

        this.table = table;
    }

    public void resetTable(@NotNull TableModel tableModel) {
        ApplicationManager.getApplication().invokeLater(
                () -> table.setModel(tableModel)
        );
    }

}
