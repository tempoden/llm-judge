package com.github.tempoden.llmjudge.gui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class ViewModel {

    private final JButton pythonPathButton;
    private final JLabel pythonPathLabel;

    private final JButton jsonButton;
    private final JLabel jsonLabel;

    private final JLabel modelPathLabel;

    public final JButton controlButton;

    private final JTable table;

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

    public void enableStart(Runnable func) {
        ApplicationManager.getApplication().invokeLater(
            () -> {
                controlButton.setAction(
                    new AbstractAction("Start") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // temporarily disable the button by unbinding action
                            // until cancellation is initialized
                            controlButton.setAction(null);
                            func.run();
                        }
                    }
                );
            }
        );
    }

    public void enableCancel(Runnable cancelFunc) {
        ApplicationManager.getApplication().invokeLater(
            () -> {
                controlButton.setAction(
                    new AbstractAction("Cancel") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // temporarily disable the button by unbinding action
                            // until cancellation is initialized
                            controlButton.setAction(null);
                            cancelFunc.run();
                        }
                    }
                );
            }
        );
    }

    public void choseJSON(Consumer<String> jsonPathHandler) {
        ApplicationManager.getApplication().invokeLater(
            () -> jsonButton.addActionListener(
                e -> {
                    FileChooserDescriptor descriptor = getJSONFileChooserDescriptor();

                    // Found one issue which still maintains in the Community edition
                    // https://youtrack.jetbrains.com/issue/IDEA-347300/Selecting-a-file-in-the-file-chooser-leads-to-Slow-operations-are-prohibited-on-EDT-error
                    FileChooser.chooseFile(descriptor, null, null, virtualFile -> {
                        jsonPathHandler.accept(virtualFile.getCanonicalPath());
                    });
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

    public void chosePython(Consumer<String> pythonPathHandler) {
        ApplicationManager.getApplication().invokeLater(
            () -> pythonPathButton.addActionListener(
                e -> {
                    FileChooserDescriptor descriptor = getPythonFileChooserDescriptor();

                    FileChooser.chooseFile(descriptor, null, null, virtualFile -> {
                        pythonPathHandler.accept(virtualFile.getCanonicalPath());
                    });
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
}
