package cn.lihongjie.javapoet.gen.idea.action;

import cn.lihongjie.javapoet.gen.core.JavaPoetGenException;
import cn.lihongjie.javapoet.gen.core.JavaPoetGenerator;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Base action class for generating JavaPoet code.
 */
public abstract class BaseJavaPoetAction extends AnAction {

    protected final JavaPoetGenerator generator = new JavaPoetGenerator();

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Only enable for Java files
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        boolean isJavaFile = psiFile instanceof PsiJavaFile;
        
        // Also check if we have a virtual file selected that ends with .java
        if (!isJavaFile) {
            VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            isJavaFile = virtualFile != null && virtualFile.getName().endsWith(".java");
        }
        
        e.getPresentation().setEnabledAndVisible(isJavaFile);
    }

    /**
     * Generate JavaPoet code from the current context.
     */
    protected String generateCode(AnActionEvent e) throws IOException, JavaPoetGenException {
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            throw new JavaPoetGenException("No file selected");
        }

        String sourceCode = new String(virtualFile.contentsToByteArray(), StandardCharsets.UTF_8);
        return generator.generateFromSource(sourceCode, virtualFile.getName());
    }

    /**
     * Show a notification to the user.
     */
    protected void showNotification(Project project, String title, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("JavaPoet Generator")
                .createNotification(title, content, type)
                .notify(project);
    }

    /**
     * Show an error notification.
     */
    protected void showError(Project project, String message) {
        showNotification(project, "JavaPoet Generator Error", message, NotificationType.ERROR);
    }

    /**
     * Show a success notification.
     */
    protected void showSuccess(Project project, String message) {
        showNotification(project, "JavaPoet Generator", message, NotificationType.INFORMATION);
    }
}
