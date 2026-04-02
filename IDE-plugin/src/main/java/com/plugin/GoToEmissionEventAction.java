package com.plugin;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch.SearchParameters;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtTypeReference;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_ELEMENT;

public class GoToEmissionEventAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Editor editor = anActionEvent.getData(EDITOR);
        PsiElement target = anActionEvent.getData(PSI_ELEMENT);

        if (project == null || editor == null || !(target instanceof KtClassOrObject ktClass)) return;

        List<PsiElement> emissionPlaces = EventEmissionSearcher.findEmissions(ktClass);

        // show the result
        if (emissionPlaces.size() == 1) {
            // if only one emission place go there
            ((Navigatable) emissionPlaces.get(0)).navigate(true);
        } else {
            // if multiple places - show a list
            NavigationUtil.getPsiElementPopup(
                    emissionPlaces.toArray(new PsiElement[0]),
                    "Emissions Event (" + emissionPlaces.size() + ")"
            ).showInBestPositionFor(editor);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(false);

        Project project = e.getProject();
        Editor editor = e.getData(EDITOR);
        PsiElement target = e.getData(PSI_ELEMENT);

        if (project == null || editor == null || target == null) return;

        if (!(target instanceof KtClassOrObject ktClass)) return;

        String className = ktClass.getName();
        if (className == null) return;

        if (className.endsWith("Update")) return;

        boolean isEventName = ktClass.getName() != null && ktClass.getName().endsWith("Event");
        boolean isEventSuperType = ktClass.getSuperTypeList() != null && ktClass.getSuperTypeList().getText().contains("Event");

        if (isEventName || isEventSuperType) {
            e.getPresentation().setEnabledAndVisible(true);
        }
    }
}
