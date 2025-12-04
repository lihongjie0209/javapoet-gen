package cn.lihongjie.javapoet.gen.idea.action;

import cn.lihongjie.javapoet.gen.idea.dialog.JavaPoetResultDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Action to generate JavaPoet code and show in a dialog with options.
 */
public class GenerateJavaPoetAction extends BaseJavaPoetAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        try {
            String generatedCode = generateCode(e);
            
            // Show dialog with generated code
            JavaPoetResultDialog dialog = new JavaPoetResultDialog(project, generatedCode);
            dialog.show();
            
        } catch (Exception ex) {
            showError(project, "Failed to generate JavaPoet code: " + ex.getMessage());
        }
    }
}
