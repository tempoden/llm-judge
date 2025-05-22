package com.github.tempoden.llmjudge.gui;

import com.github.tempoden.llmjudge.backend.Model;

import javax.swing.*;

public class ViewModel {

    private final Model model;

    private JButton pythonPathButton;
    private JLabel pythonPathLabel;

    private JButton jsonButton;
    private JLabel jsonLabel;

    private JLabel modelPathLabel;

    private JButton controlButton;

    private JScrollPane scrollPane;
    private JTable table;

    public ViewModel(Model model) {
        this.model = model;
        model.register(this);
    }

    public void registerPythonPathButton(JButton pythonPathButton) {
        this.pythonPathButton = pythonPathButton;
    }

    public void registerPythonPathLabel(JLabel pythonPathLabel) {
        this.pythonPathLabel = pythonPathLabel;
    }

    public void registerJsonButton(JButton jsonButton) {
        this.jsonButton = jsonButton;
    }

    public void registerJsonLabel(JLabel jsonLabel) {
        this.jsonLabel = jsonLabel;
    }

    public void registerModelPathLabel(JLabel modelPathLabel) {
        this.modelPathLabel = modelPathLabel;
    }

    public void registerControlButton(JButton controlButton) {
        this.controlButton = controlButton;
    }

    public void registerScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

}
