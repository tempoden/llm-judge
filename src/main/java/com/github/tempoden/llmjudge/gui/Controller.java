package com.github.tempoden.llmjudge.gui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.function.Consumer;

public class Controller {

    private final JButton pythonPathButton;
    private final JLabel pythonPathLabel;

    private final JButton jsonButton;
    private final JLabel jsonLabel;

    private final JLabel modelPathLabel;

    private final JButton controlButton;

    private final JComboBox<String> poolBox;
    private final JSpinner nSpinner;

    private final JTable table;

    public Controller(@NotNull JButton pythonPathButton,
                      @NotNull JLabel pythonPathLabel,
                      @NotNull JButton jsonButton,
                      @NotNull JLabel jsonLabel,
                      @NotNull JLabel modelPathLabel,
                      @NotNull JButton controlButton,
                      @NotNull JComboBox<String> poolBox,
                      @NotNull JSpinner nSpinner,
                      @NotNull JTable table)  {
        this.pythonPathButton = pythonPathButton;
        this.pythonPathLabel = pythonPathLabel;

        this.jsonButton = jsonButton;
        this.jsonLabel = jsonLabel;

        this.modelPathLabel = modelPathLabel;

        this.controlButton = controlButton;
        this.controlButton.setEnabled(false);

        this.poolBox = poolBox;
        this.nSpinner = nSpinner;

        this.table = table;
    }

    public void setPythonPath(@NotNull String pythonPath) {
        ApplicationManager.getApplication().invokeLater(
            () -> this.pythonPathLabel.setText("Selected python interpreter: " + pythonPath)
        );
    }

    public void setJsonPath(@NotNull String jsonPath) {
        ApplicationManager.getApplication().invokeLater(
            () -> this.jsonLabel.setText("Selected JSON: " + jsonPath)
        );
    }

    public void setModelPath(@NotNull String modelPath) {
        ApplicationManager.getApplication().invokeLater(
            () -> this.modelPathLabel.setText("Local model: " + modelPath)
        );
    }

    public void resetTable(@NotNull TableModel tableModel) {
        ApplicationManager.getApplication().invokeLater(
            () -> table.setModel(tableModel)
        );
    }

    public void enableStart(@NotNull Runnable func) {
        ApplicationManager.getApplication().invokeLater(
            () -> controlButton.setAction(
                new AbstractAction("Start") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // temporarily disable the button by unbinding action
                        // until cancellation is initialized
                        controlButton.setAction(null);
                        func.run();
                    }
                }
            )
        );
    }

    public void enableCancel(@NotNull Runnable cancelFunc) {
        ApplicationManager.getApplication().invokeLater(
            () -> controlButton.setAction(
                new AbstractAction("Cancel") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // temporarily disable the button by unbinding action
                        // until cancellation is initialized
                        controlButton.setAction(null);
                        cancelFunc.run();
                        showCancelDialog();
                    }
                }
            )
        );
    }

    public void setPoolSize(@NotNull Consumer<String> poolSizeHandler) {
        ApplicationManager.getApplication().invokeLater(
            () -> poolBox.addActionListener(
                e -> poolSizeHandler.accept(Objects.requireNonNull(this.poolBox.getSelectedItem()).toString())
            )
        );
    }

    public void setJudgeReqCount(@NotNull Consumer<Integer> judgeReqCountHandler){
        ApplicationManager.getApplication().invokeLater(
            () -> nSpinner.getModel().addChangeListener(
                e -> judgeReqCountHandler.accept(Integer.parseInt(nSpinner.getValue().toString()))
            )
        );
    }

    public void choseJSON(@NotNull Consumer<String> jsonPathHandler) {
        ApplicationManager.getApplication().invokeLater(
            () -> jsonButton.addActionListener(
                e -> {
                    FileChooserDescriptor descriptor = getJSONFileChooserDescriptor();

                    // Found one issue which still maintains in the Community edition
                    // https://youtrack.jetbrains.com/issue/IDEA-347300/Selecting-a-file-in-the-file-chooser-leads-to-Slow-operations-are-prohibited-on-EDT-error
                    FileChooser.chooseFile(descriptor, null, null,
                        virtualFile -> jsonPathHandler.accept(virtualFile.getCanonicalPath()));
                }
            )
        );
    }

    private static @NotNull FileChooserDescriptor getJSONFileChooserDescriptor() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                // Show directories and .json files only
                return file.isDirectory() || file.getName().endsWith(".json");
            }
        };
        descriptor.setTitle("Select File with Model and Test Data");
        return descriptor;
    }

    public void chosePython(@NotNull Consumer<String> pythonPathHandler) {
        ApplicationManager.getApplication().invokeLater(
            () -> pythonPathButton.addActionListener(
                e -> {
                    FileChooserDescriptor descriptor = getPythonFileChooserDescriptor();

                    FileChooser.chooseFile(descriptor, null, null,
                        virtualFile ->  pythonPathHandler.accept(virtualFile.getCanonicalPath()));
                }
            )
        );
    }

    private static @NotNull FileChooserDescriptor getPythonFileChooserDescriptor() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                // Show directories and (python.* or *.exe) files only
                return file.isDirectory() || file.getName().contains("python") || file.getName().endsWith(".exe");
            }
        };
        descriptor.setTitle("Select a Python Interpreter to Use");
        return descriptor;
    }

    public void showFinishDialog() {
        ApplicationManager.getApplication().invokeLater(
            () -> Messages.showMessageDialog(
                    "LLM-judge has successfully evaluated scores for the provided model",
                    "Success",
                    Messages.getInformationIcon()
            ));
    }

    public void showCancelDialog() {
        ApplicationManager.getApplication().invokeLater(
            () -> Messages.showMessageDialog(
                    "Model evaluation was cancelled",
                    "Operation Cancelled",
                    Messages.getInformationIcon()
            ));
    }

    public void showErrorDialog(@NotNull String msg) {
        ApplicationManager.getApplication().invokeLater(
            () -> Messages.showMessageDialog(
                    "LLM-judge was terminated with an error: " + msg,
                    "Evaluation Error",
                    Messages.getErrorIcon()
            ));
    }

    public void disableUI() {
        ApplicationManager.getApplication().invokeLater(
            () -> {
                this.pythonPathButton.setEnabled(false);
                this.jsonButton.setEnabled(false);
                this.controlButton.setEnabled(false);
                this.nSpinner.setEnabled(false);
                this.poolBox.setEnabled(false);
        });
    }

    // Initially, I thought that I may wrap all this button enabling/disabling
    // into an AutoClosable, and use it like a context manager. But in the end,
    // decided that it may be an overcomplication, so I left these two parts
    // as a separate methods.

    public void disableSettings() {
        ApplicationManager.getApplication().invokeLater(
            () -> {
                this.pythonPathButton.setEnabled(false);
                this.jsonButton.setEnabled(false);
                this.nSpinner.setEnabled(false);
                this.poolBox.setEnabled(false);
        });
    }

    public void enableSettings() {
        ApplicationManager.getApplication().invokeLater(
            () -> {
                this.pythonPathButton.setEnabled(true);
                this.jsonButton.setEnabled(true);
                this.nSpinner.setEnabled(true);
                this.poolBox.setEnabled(true);
        });
    }
}
