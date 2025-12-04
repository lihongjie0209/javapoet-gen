package cn.lihongjie.javapoet.gen.idea.action;

import cn.lihongjie.javapoet.gen.idea.toolwindow.JavaPoetToolWindowFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * Action to generate JavaPoet code and show in the tool window.
 */
public class GenerateToToolWindowAction extends BaseJavaPoetAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        try {
            String generatedCode = generateCode(e);
            
            // Get or create tool window
            ToolWindow toolWindow = ToolWindowManager.getInstance(project)
                    .getToolWindow("JavaPoet Generator");
            
            if (toolWindow != null) {
                // Update content and show
                JavaPoetToolWindowFactory.updateContent(project, generatedCode);
                toolWindow.show();
            }
            
        } catch (Exception ex) {
            showError(project, "Failed to generate JavaPoet code: " + ex.getMessage());
        }
    }
}
