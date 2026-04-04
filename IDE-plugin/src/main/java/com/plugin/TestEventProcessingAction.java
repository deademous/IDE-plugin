package com.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.kotlin.psi.KtClassOrObject;

import java.util.List;

public class TestEventProcessingAction extends BaseEventAction {
    @Override
    protected List<PsiElement> findTargets(KtClassOrObject targetClass, GlobalSearchScope scope) {
        return EventProcessingSearcher.findProcessing(targetClass, scope);
    }
    @Override
    protected String getPopupTitle() { return "Event Processing (Test)"; }
    @Override
    protected GlobalSearchScope getScope(PsiElement element) {
        return ScopeBuilder.getTestScope(element);
    }
}