package cn.lihongjie.javapoet.gen.idea.toolwindow;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating the JavaPoet Generator tool window.
 */
public class JavaPoetToolWindowFactory implements ToolWindowFactory {

    private static final Map<Project, EditorTextField> editorMap = new HashMap<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JavaPoetToolWindowContent content = new JavaPoetToolWindowContent(project);
        Content windowContent = ContentFactory.getInstance().createContent(
                content.getPanel(), 
                "Generated Code", 
                false
        );
        toolWindow.getContentManager().addContent(windowContent);
        
        // Store reference for updates
        editorMap.put(project, content.getEditor());
    }

    /**
     * Update the content of the tool window.
     */
    public static void updateContent(Project project, String code) {
        EditorTextField editor = editorMap.get(project);
        if (editor != null) {
            editor.setText(code);
        }
    }

    /**
     * Inner class for the tool window content.
     */
    private static class JavaPoetToolWindowContent {
        private final JPanel mainPanel;
        private final EditorTextField editorTextField;

        public JavaPoetToolWindowContent(Project project) {
            mainPanel = new JPanel(new BorderLayout());

            // Create editor first (needed for button actions)
            editorTextField = new EditorTextField();
            editorTextField.setOneLineMode(false);

            // Create toolbar
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            JButton copyButton = new JButton("Copy to Clipboard");
            copyButton.addActionListener(e -> {
                String text = editorTextField.getText();
                if (text != null && !text.isEmpty()) {
                    CopyPasteManager.getInstance().setContents(new StringSelection(text));
                }
            });
            toolbar.add(copyButton);

            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(e -> editorTextField.setText(""));
            toolbar.add(clearButton);

            mainPanel.add(toolbar, BorderLayout.NORTH);

            // Add editor to scroll pane
            JBScrollPane scrollPane = new JBScrollPane(editorTextField);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            // Initial message
            editorTextField.setText("// JavaPoet code will appear here\n// Right-click on a Java file and select 'Generate JavaPoet Code'");
        }

        public JPanel getPanel() {
            return mainPanel;
        }

        public EditorTextField getEditor() {
            return editorTextField;
        }
    }
}
