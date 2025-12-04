package cn.lihongjie.javapoet.gen.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

/**
 * Action to generate JavaPoet code and copy to clipboard.
 */
public class GenerateAndCopyAction extends BaseJavaPoetAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        try {
            String generatedCode = generateCode(e);
            
            // Copy to clipboard
            CopyPasteManager.getInstance().setContents(new StringSelection(generatedCode));
            
            showSuccess(project, "JavaPoet code copied to clipboard!");
            
        } catch (Exception ex) {
            showError(project, "Failed to generate JavaPoet code: " + ex.getMessage());
        }
    }
}
