package com.plugin;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastContextKt;

import java.util.Collection;

public class LineMarkerProviderCommand extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {

        UClass uClass = UastContextKt.toUElement(element, UClass.class);

        if (uClass == null || uClass.getName() == null) return;

        PsiClass psiClass = uClass.getJavaPsi();

        if (psiClass.isInterface()) return;

        if (isCommand(psiClass)) {

            PsiElement identifier = psiClass.getNameIdentifier();
            if (identifier == null) identifier = element;

            GlobalSearchScope scope = ScopeBuilder.getProductionScope(element);

            Collection<PsiElement> targetsEmission = EmissionSearcher.findEmission(uClass, scope);
            NavigationGutterIconBuilder<PsiElement> emissionBuilder =
                    NavigationGutterIconBuilder.create(AllIcons.Actions.Find)
                            .setTargets(targetsEmission)
                            .setTooltipText("Go to emission");

            result.add(emissionBuilder.createLineMarkerInfo(identifier));

           Collection<PsiElement> targetsProcessing = ProcessingSearcher.findProcessing(uClass, scope);
           NavigationGutterIconBuilder<PsiElement> processingBuilder =
                   NavigationGutterIconBuilder.create(AllIcons.Actions.Execute)
                                   .setTargets(targetsProcessing)
                                           .setTooltipText("Go to processing");

            result.add(processingBuilder.createLineMarkerInfo(identifier));
        }

    }
    private boolean isCommand(PsiClass psiClass) {
        for (PsiClass superClass : psiClass.getSupers()) {
            String name = superClass.getName();

            if (name != null && name.contains("Command") && !name.contains("CommandsFlowHandler")) return true;
        }

        return false;
    }
}