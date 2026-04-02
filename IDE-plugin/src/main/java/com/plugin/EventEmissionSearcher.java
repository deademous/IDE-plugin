package com.plugin;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtTypeReference;

import java.util.ArrayList;
import java.util.List;


public class EventEmissionSearcher {
    public static List<PsiElement> findEmissions(@NotNull KtClassOrObject target) {
        Project project = target.getProject();
        List<PsiElement> emissionPlaces = new ArrayList<>();
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);

        for (PsiReference ref : ReferencesSearch.search(target, GlobalSearchScope.projectScope(project), true).findAll()) {
            PsiElement el = ref.getElement();
            VirtualFile file = el.getContainingFile().getVirtualFile();

            if (file == null) continue;

            // find in source files only
            if (!fileIndex.isInSource(file)) continue;

            // exclude tests
            if (fileIndex.isInTestSourceContent(file)) continue;
            String filePath = file.getPath();
            if (filePath.contains("/test/")) {
                continue;
            }

            // exclude update
            String fileName = file.getName();
            if (fileName.contains("Update")) {
                continue;
            }

            // exclude imports
            if (PsiTreeUtil.getParentOfType(el, KtImportDirective.class) != null) {
                continue;
            }

            // exclude type ref
            if (PsiTreeUtil.getParentOfType(el, KtTypeReference.class) != null) {
                continue;
            }

            emissionPlaces.add(el);
        }
        return emissionPlaces;
    }
}
