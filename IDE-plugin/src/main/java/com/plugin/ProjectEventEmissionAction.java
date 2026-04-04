package com.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.kotlin.psi.KtClassOrObject;

import java.util.List;

public class ProjectEventEmissionAction extends BaseEventAction{
    @Override
    protected GlobalSearchScope getScope(PsiElement element) {
        return ScopeBuilder.getProductionScope(element);
    }

    @Override
    protected String getPopupTitle() { return "Event Emissions (Project)"; }

    @Override
    protected List<PsiElement> findTargets(KtClassOrObject targetClass, GlobalSearchScope scope) {
        return EventEmissionSearcher.findEmissions(targetClass, scope);
    }
}
