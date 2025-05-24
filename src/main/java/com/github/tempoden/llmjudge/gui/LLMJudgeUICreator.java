package com.github.tempoden.llmjudge.gui;

import com.github.tempoden.llmjudge.backend.Model;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LLMJudgeUICreator {
    public static void createContent(JPanel pluginPanel) {
        pluginPanel.setLayout(new BorderLayout());

        JPanel withButtons = new JPanel();
        withButtons.setLayout(new BorderLayout());
        // ===== Right Panel with 3 rows (2 button/label pairs + bottom label) =====
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton pythonButton = new JButton("Set python");
        JLabel pythonLabel = new JBLabel("Label 1");
        JButton jsonButton = new JButton("Set JSON");
        JLabel jsonLabel = new JBLabel("Label 2");
        JLabel modelLabel = new JBLabel("Bottom label under right buttons");
        modelLabel.setHorizontalAlignment(SwingConstants.HORIZONTAL);

        // Fix button sizes
        Dimension fixedButtonSize = new Dimension(100, 30);
        pythonButton.setPreferredSize(fixedButtonSize);
        jsonButton.setPreferredSize(fixedButtonSize);

        // --- Row 1: Button1 + Label1 ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0;
        rightPanel.add(pythonButton, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.anchor = GridBagConstraints.LINE_START;
        rightPanel.add(pythonLabel, gbc);

        // --- Row 2: Button2 + Label2 ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        rightPanel.add(jsonButton, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.anchor = GridBagConstraints.LINE_START;
        rightPanel.add(jsonLabel, gbc);

        // --- Row 3: Bottom Label (spanning both columns) ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        rightPanel.add(modelLabel, gbc);

        withButtons.add(rightPanel, BorderLayout.CENTER);

        // ===== Left Panel with square button =====
        JPanel leftPanel = new JPanel(new GridBagLayout());
        JButton controlButton = new JButton("Start");

        // Set a temporary square size (will resize after layout)
        Dimension defaultSize = new Dimension(rightPanel.getPreferredSize().width, rightPanel.getPreferredSize().height);
        controlButton.setPreferredSize(defaultSize);

        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.gridx = 0;
        leftConstraints.gridy = 0;
        leftConstraints.anchor = GridBagConstraints.NORTH;
        leftConstraints.fill = GridBagConstraints.NONE;
        leftConstraints.weighty = 1;

        leftPanel.add(controlButton, leftConstraints);
        withButtons.add(leftPanel, BorderLayout.WEST);

        // Add the button to the top of the pluginPanel
        pluginPanel.add(withButtons, BorderLayout.NORTH);

        // Create the table using the data and column names
        JTable table = new JTable(new DefaultTableModel(null, Util.columnNames));
        // Make separate cells selectable
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        // Forbid columns reordering
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        // Add the scroll pane (with the table) to the center of the pluginPanel
        pluginPanel.add(scrollPane, BorderLayout.CENTER);

        ViewModel vm = new ViewModel(
                pythonButton,
                pythonLabel,
                jsonButton,
                jsonLabel,
                modelLabel,
                controlButton,
                table
        );

        Model model = new Model(vm);
    }
}
