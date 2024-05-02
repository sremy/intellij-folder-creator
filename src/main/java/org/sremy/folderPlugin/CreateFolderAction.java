package org.sremy.folderPlugin;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CreateFolderAction extends AnAction {

    private final Collection<String> subfolderNames;

    public CreateFolderAction() {
        subfolderNames = getListOfSubFolders();
    }

    protected abstract List<String> getListOfSubFolders();

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null)
            return;


        VirtualFile[] virtualFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (virtualFiles == null) {
            showMessageInfo(project, "Please select a folder in which we will create sub-folders");
            return;
        }
        List<Path> folderPathList = Arrays.stream(virtualFiles).map(CreateFolderAction::getFolder).toList();
        System.out.println(folderPathList);
        VirtualFile rootVirtualFile = ProjectUtil.guessProjectDir(project);
        String relativeProjectPath = folderPathList.stream()
                .map(p -> rootVirtualFile != null ? rootVirtualFile.toNioPath().relativize(p) : p)
                .map(Path::toString)
                .collect(Collectors.joining(", "));

        System.out.println(relativeProjectPath);

        try {
            folderPathList.forEach(this::createTemplateVirtualFolders);
            Arrays.stream(virtualFiles).forEach(vf -> vf.refresh(true, true));
        } catch (Exception ex) {
            showMessageWarning(project, "Exception during the creation of the sub-folders: " + ex);
            return;
        }

        try {
            doActionInFolder(folderPathList);
        } catch (Exception ex) {
            showMessageWarning(project, "Exception during the action on the folder: " + ex);
        }

        NotificationGroupManager.getInstance()
                .getNotificationGroup("Subfolders created")
                .createNotification("Subfolders created in " + relativeProjectPath, NotificationType.INFORMATION)
                .notify(project);

//        printVariablesInEachMethod(e);
    }

    private static void printVariablesInEachMethod(AnActionEvent e) {
        // PSI File
        PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);

        if (psiFile != null && psiFile.getFileType().equals(JavaFileType.INSTANCE)) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiMethod[] methods = psiJavaFile.getClasses()[0].getMethods();

            Arrays.stream(methods).forEach(m -> m.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitLocalVariable(@NotNull PsiLocalVariable variable) {
                    super.visitLocalVariable(variable);
                    System.out.println("In method " + m.getName() + ", Found a variable " + variable.getName() + " type: " + variable.getType().getCanonicalText() +  " at offset " +
                            variable.getTextRange().getStartOffset());
                }
            }));
        }
    }

    protected void doActionInFolder(List<Path> selectedFolders) throws Exception {
        // Implement it in subclass
    }

    // IntelliJ VFS API
    private void createTemplateVirtualFolders(Path path) {
        subfolderNames.forEach(name -> {
            try {
                VfsUtil.createDirectories(path.resolve(name).toAbsolutePath().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Java Files API
    private static void createTemplateFolders(Path path) {
        Collection<String> subfolderNames = List.of("inputs", "mocks", "output");
        subfolderNames.forEach(name -> {
            try {
                Files.createDirectories(path.resolve(name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull
    private static Path getFolder(VirtualFile virtualFile) {
        if (virtualFile.isDirectory()) {
            return virtualFile.toNioPath();
        }
        return virtualFile.getParent().toNioPath();
    }

    private static void showMessageInfo(Project project, String message) {
        Messages.showMessageDialog(project, message, "Folders Plugin", Messages.getInformationIcon());
    }
    private static void showMessageError(Project project, String message) {
        Messages.showMessageDialog(project, message, "Folders Plugin", Messages.getErrorIcon());
    }

    private static void showMessageWarning(Project project, String message) {
        Messages.showMessageDialog(project, message, "Folders Plugin", Messages.getWarningIcon());
    }
}
