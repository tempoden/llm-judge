package com.github.tempoden.llmjudge.gui;

import com.github.tempoden.llmjudge.backend.parsing.Content;
import com.github.tempoden.llmjudge.backend.parsing.DataParser;
import com.github.tempoden.llmjudge.backend.parsing.JSONParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ThreadLocalRandom;

public class LLMJudgeUICreator {
    public static void createContent(JPanel pluginPanel) {
        pluginPanel.setLayout(new BorderLayout());

        JPanel withButtons = new JPanel();
        withButtons.setLayout(new BorderLayout());
        // ===== Right Panel with 3 rows (2 button/label pairs + bottom label) =====
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton button1 = new JButton("Button 1");
        JLabel label1 = new JBLabel("Label 1");
        JButton button2 = new JButton("Button 2");
        JLabel label2 = new JBLabel("Label 2");
        JLabel bottomLabel = new JBLabel("Bottom label under right buttons");
        bottomLabel.setHorizontalAlignment(SwingConstants.HORIZONTAL);

        // Fix button sizes
        Dimension fixedButtonSize = new Dimension(100, 30);
        button1.setPreferredSize(fixedButtonSize);
        button2.setPreferredSize(fixedButtonSize);

        // --- Row 1: Button1 + Label1 ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0;
        rightPanel.add(button1, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.anchor = GridBagConstraints.LINE_START;
        rightPanel.add(label1, gbc);

        // --- Row 2: Button2 + Label2 ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        rightPanel.add(button2, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.anchor = GridBagConstraints.LINE_START;
        rightPanel.add(label2, gbc);

        // --- Row 3: Bottom Label (spanning both columns) ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        rightPanel.add(bottomLabel, gbc);

        withButtons.add(rightPanel, BorderLayout.CENTER);

        // ===== Left Panel with square button =====
        JPanel leftPanel = new JPanel(new GridBagLayout());
        JButton leftButton = new JButton("Left");

        // Set a temporary square size (will resize after layout)
        Dimension defaultSize = new Dimension(rightPanel.getPreferredSize().width, rightPanel.getPreferredSize().height);
        leftButton.setPreferredSize(defaultSize);

        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.gridx = 0;
        leftConstraints.gridy = 0;
        leftConstraints.anchor = GridBagConstraints.NORTH;
        leftConstraints.fill = GridBagConstraints.NONE;
        leftConstraints.weighty = 1;

        leftPanel.add(leftButton, leftConstraints);
        withButtons.add(leftPanel, BorderLayout.WEST);

        // Add the button to the top of the pluginPanel
        pluginPanel.add(withButtons, BorderLayout.NORTH);

        JScrollPane scrollPane = initJScrollPane();
        // Add the scroll pane (with the table) to the center of the pluginPanel
        pluginPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private static JScrollPane initJScrollPane() {
        DataParser parser = new JSONParser();
        Content content;
        try {
            content = parser.parse(new FileReader("C:\\SHAD\\JB\\llm-judge\\misc\\dataset\\demo-10.json"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        TableModel model = Util.buildTableModel(content.data());

        // Create the table using the data and column names
        JTable table = new JTable(model);
        // Make separate cells selectable
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        // Forbid columns reordering
        table.getTableHeader().setReorderingAllowed(false);

        return new JScrollPane(table);
    }
}
