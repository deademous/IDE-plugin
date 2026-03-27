package com.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProcessingSearcher {

    public static Collection<PsiElement> findProcessing(@NotNull UClass uClass) {
        List<PsiElement> targets = new ArrayList<>();
        Project project = uClass.getJavaPsi().getProject();
        PsiClass classCommand = uClass.getJavaPsi();
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);

        Collection<PsiReference> all_usage = ReferencesSearch.search(classCommand, GlobalSearchScope.projectScope(project))
                .findAll();

        for(PsiReference usage : all_usage) {
            PsiElement element = usage.getElement();
            VirtualFile vFile = element.getContainingFile().getVirtualFile();

            if (vFile != null && fileIndex.isInTestSourceContent(vFile)) continue;

            UElement uElement = UastContextKt.toUElement(element);
            if (uElement == null) continue;

            UClass handlerClass = UastUtils.getParentOfType(uElement, UClass.class);
            if (handlerClass != null && isCommandsHandler(handlerClass, classCommand)) {
                PsiClass psiHandler = handlerClass.getJavaPsi();

                PsiMethod[] methods = psiHandler.findMethodsByName("handle", false);

                if (methods.length > 0) {
                    targets.add(methods[0]);
                }
                else {
                    targets.add(psiHandler);
                }
            }
        }

        return targets;
    }

    private static boolean isCommandsHandler(UClass uClass, PsiClass pClass) {
        for (UTypeReferenceExpression interfaceRef : uClass.getUastSuperTypes()) {
            PsiType type = interfaceRef.getType();

            if (type instanceof PsiClassType classType) {
                PsiClassType.ClassResolveResult resolveResult = classType.resolveGenerics();
                PsiClass iClass = resolveResult.getElement();

                if (iClass != null && "CommandsFlowHandler".equals(iClass.getName())) {
                    PsiType[] params = classType.getParameters();

                    if (params.length > 0) {
                        PsiType firstParam = params[0];

                        PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(pClass.getProject());
                        PsiType commandType = psiElementFactory.createType(pClass);

                        if (firstParam.isAssignableFrom(commandType)) return true;
                    }

                }
            }
        }

        return false;
    }
}
