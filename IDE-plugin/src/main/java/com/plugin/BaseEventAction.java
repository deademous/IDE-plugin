package com.plugin;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import java.util.List;

public abstract class BaseEventAction extends AnAction {

    protected abstract List<PsiElement> findTargets(KtClassOrObject targetClass, GlobalSearchScope scope);
    protected abstract String getPopupTitle();
    protected abstract GlobalSearchScope getScope(PsiElement element);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiElement element = getTargetElement(e);
        if (project == null || element == null) return;

        KtClassOrObject ktClass = KtEventUtil.tryResolveToClass(element);
        if (ktClass == null) return;

        List<PsiElement> targets = findTargets(ktClass, getScope(element));
        if (targets.isEmpty()) return;

        if (targets.size() == 1) {
            ((Navigatable) targets.get(0)).navigate(true);
        } else {
            NavigationUtil.getPsiElementPopup(targets.toArray(new PsiElement[0]), getPopupTitle())
                    .showInBestPositionFor(editor);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiElement element = getTargetElement(e);
        KtClassOrObject ktClass = KtEventUtil.tryResolveToClass(element);
        boolean visible = editor != null && ktClass != null && KtEventUtil.isEventClass(ktClass);
        e.getPresentation().setEnabledAndVisible(visible);
    }

    @Nullable
    private PsiElement getTargetElement(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (editor != null && file != null) {
            return file.findElementAt(editor.getCaretModel().getOffset());
        }
        return null;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
}