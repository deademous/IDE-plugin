package com.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.uast.UClass;

import java.util.List;

public class EmissionAction extends BaseAction {
    @Override
    protected List<PsiElement> findTargets(UClass targetClass, GlobalSearchScope scope) {
        return EmissionSearcher.findEmission(targetClass, scope);
    }

    @Override
    protected String getTitle() {
        return "Emission Command";
    }

    @Override
    protected String getOperation() {
        return "Emission";
    }
}
