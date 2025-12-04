package cn.lihongjie.javapoet.gen.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Action to generate JavaPoet code and open in a new editor tab.
 */
public class GenerateAndOpenAction extends BaseJavaPoetAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        try {
            VirtualFile sourceFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            String fileName = sourceFile != null ? 
                    sourceFile.getNameWithoutExtension() + "Generator.java" : 
                    "GeneratedJavaPoet.java";
            
            String generatedCode = generateCode(e);
            
            // Create a light virtual file (in-memory, not saved)
            LightVirtualFile virtualFile = new LightVirtualFile(fileName, generatedCode);
            
            // Open in editor
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
            
        } catch (Exception ex) {
            showError(project, "Failed to generate JavaPoet code: " + ex.getMessage());
        }
    }
}
