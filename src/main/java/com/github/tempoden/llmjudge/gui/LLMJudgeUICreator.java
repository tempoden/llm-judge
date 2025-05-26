package com.github.tempoden.llmjudge.gui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LLMJudgeUICreator {
    public static Controller createContent(JPanel pluginPanel) {
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
        GridBagConstraints leftConstraints = new GridBagConstraints();

        // Common settings
        leftConstraints.insets = new Insets(5, 5, 5, 5); // Padding

        // === Row 0: Big Button ===
        JButton controlButton = new JButton("Run", AllIcons.Actions.Execute);
        leftConstraints.gridx = 0;
        leftConstraints.gridy = 0;
        leftConstraints.gridwidth = 2;
        leftConstraints.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(controlButton, leftConstraints);

        // === Row 1: Label + ComboBox ===
        leftConstraints.gridy = 1;
        leftConstraints.gridwidth = 1;
        leftConstraints.fill = GridBagConstraints.NONE;

        JLabel comboLabel = new JBLabel("ThreadPool size:");
        leftConstraints.gridx = 0;
        leftConstraints.anchor = GridBagConstraints.EAST;
        leftPanel.add(comboLabel, leftConstraints);

        String[] options = Util.POOL_OPTIONS;
        JComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.setSelectedIndex(Util.DEFAULT_POOL_OPTION);
        leftConstraints.gridx = 1;
        leftConstraints.anchor = GridBagConstraints.WEST;
        leftPanel.add(comboBox, leftConstraints);

        // === Row 2: Label + Spinner ===
        leftConstraints.gridy = 2;

        JLabel spinnerLabel = new JBLabel("Judge-LLM calls:");
        leftConstraints.gridx = 0;
        leftConstraints.anchor = GridBagConstraints.EAST;
        leftPanel.add(spinnerLabel, leftConstraints);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(Util.DEFAULT_N, 1, 20, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setEditable(false);         // Prevent typing
            textField.setFocusable(false);        // Optional: remove focus highlight
        }
        leftConstraints.gridx = 1;
        leftConstraints.anchor = GridBagConstraints.WEST;
        leftPanel.add(spinner, leftConstraints);

        withButtons.add(leftPanel, BorderLayout.WEST);

        // Add the button to the top of the pluginPanel
        pluginPanel.add(withButtons, BorderLayout.NORTH);

        // Create the table using the data and column names
        JTable table = new JBTable(new DefaultTableModel(null, Util.columnNames));
        // Make separate cells selectable
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        // Forbid columns reordering
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        // Add the scroll pane (with the table) to the center of the pluginPanel
        pluginPanel.add(scrollPane, BorderLayout.CENTER);

        return new Controller(
                pythonButton,
                pythonLabel,
                jsonButton,
                jsonLabel,
                modelLabel,
                controlButton,
                comboBox,
                spinner,
                table
        );
    }
}
