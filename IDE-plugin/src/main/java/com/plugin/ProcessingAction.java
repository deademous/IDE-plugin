package com.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.uast.UClass;

import java.util.List;

public class ProcessingAction extends BaseAction {
    @Override
    protected List<PsiElement> findTargets(UClass targetClass, GlobalSearchScope scope) {
        return ProcessingSearcher.findProcessing(targetClass, scope);
    }

    @Override
    protected String getTitle() {
        return "Processing Command";
    }

    @Override
    protected String getOperation() {
        return "Processing";
    }
}
