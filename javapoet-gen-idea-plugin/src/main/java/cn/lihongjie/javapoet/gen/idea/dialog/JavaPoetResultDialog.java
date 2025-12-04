package cn.lihongjie.javapoet.gen.idea.dialog;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Dialog to display generated JavaPoet code with options to copy or save.
 */
public class JavaPoetResultDialog extends DialogWrapper {

    private final String generatedCode;
    private final Project project;
    private Editor editor;

    public JavaPoetResultDialog(@Nullable Project project, String generatedCode) {
        super(project, true);
        this.project = project;
        this.generatedCode = generatedCode;
        setTitle("Generated JavaPoet Code");
        setSize(900, 700);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create document and editor with full scroll support
        Document document = EditorFactory.getInstance().createDocument(generatedCode);
        editor = EditorFactory.getInstance().createEditor(document, project, FileTypeManager.getInstance().getFileTypeByExtension("java"), false);
        
        // Configure editor settings
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setFoldingOutlineShown(true);
        settings.setIndentGuidesShown(true);
        settings.setUseSoftWraps(false);
        settings.setAdditionalLinesCount(3);
        settings.setAdditionalColumnsCount(3);
        
        // Set Java syntax highlighting
        if (editor instanceof EditorEx && project != null) {
            EditorEx editorEx = (EditorEx) editor;
            editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(
                    project, FileTypeManager.getInstance().getFileTypeByExtension("java")));
        }
        
        // Editor component already has built-in scrolling
        JComponent editorComponent = editor.getComponent();
        editorComponent.setPreferredSize(new Dimension(880, 600));
        
        panel.add(editorComponent, BorderLayout.CENTER);

        // Info label
        JLabel infoLabel = new JLabel("Generated JavaPoet code is shown below. Click 'Copy' to copy to clipboard.");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        panel.add(infoLabel, BorderLayout.NORTH);

        return panel;
    }
    
    @Override
    protected void dispose() {
        if (editor != null && !editor.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
        super.dispose();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                new DialogWrapperAction("Copy to Clipboard") {
                    @Override
                    protected void doAction(java.awt.event.ActionEvent e) {
                        CopyPasteManager.getInstance().setContents(new StringSelection(generatedCode));
                    }
                },
                getOKAction()
        };
    }

    @Override
    protected @Nullable String getDimensionServiceKey() {
        return "JavaPoetResultDialog";
    }
}
