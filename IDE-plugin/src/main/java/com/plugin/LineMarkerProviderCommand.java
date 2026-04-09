package com.plugin;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.ValueArgument;
import org.jetbrains.uast.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class LineMarkerProviderCommand extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        addClassMarker(element, result);
        addConstructorCallMarker(element, result);
        addObjectMarker(element, result);
        addInHandle(element, result);
    }

    private void addClassMarker(PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        UClass uCommand = UastContextKt.toUElement(element, UClass.class);

        if (uCommand == null || uCommand.getName() == null) return;

        PsiClass psiClass = uCommand.getJavaPsi();

        if (psiClass.isInterface()) return;

        if (isCommand(psiClass)) {

            PsiElement identifier = psiClass.getNameIdentifier();
            if (identifier == null) identifier = element;

            GlobalSearchScope scope = ScopeBuilder.getModuleScope(element);

            Collection<PsiElement> targetsEmission = EmissionSearcher.findEmission(uCommand, scope);
            NavigationGutterIconBuilder<PsiElement> emissionBuilder =
                    NavigationGutterIconBuilder.create(AllIcons.Actions.Find)
                            .setTargets(targetsEmission)
                            .setTooltipText("Go to emission");

            result.add(emissionBuilder.createLineMarkerInfo(identifier));

            Collection<PsiElement> targetsProcessing = ProcessingSearcher.findProcessing(uCommand, scope);
            NavigationGutterIconBuilder<PsiElement> processingBuilder =
                    NavigationGutterIconBuilder.create(AllIcons.Actions.Execute)
                            .setTargets(targetsProcessing)
                            .setTooltipText("Go to processing");

            result.add(processingBuilder.createLineMarkerInfo(identifier));
        }
    }

    private void addConstructorCallMarker(PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        UElement uElement = UastContextKt.toUElement(element);
        if (!(uElement instanceof UCallExpression callExpression)) return;
        PsiMethod constructor = callExpression.resolve();
        if (constructor != null && constructor.isConstructor()) {
            PsiClass constructedClass = constructor.getContainingClass();
            if (constructedClass != null && isCommand(constructedClass)) {
                UClass uCommand = UastContextKt.toUElement(constructedClass, UClass.class);
                if (uCommand != null) {
                    GlobalSearchScope scope = ScopeBuilder.getProductionScope(element);
                    Collection<PsiElement> targets = ProcessingSearcher.findProcessing(uCommand, scope);

                    if (!targets.isEmpty()) {
                        NavigationGutterIconBuilder<PsiElement> builder =
                                NavigationGutterIconBuilder.create(AllIcons.Actions.Execute)
                                        .setTargets(targets)
                                        .setTooltipText("Go to processing");
                        result.add(builder.createLineMarkerInfo(element));
                    }
                }
            }
        }
    }

    private void addObjectMarker(PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        UElement uElement = UastContextKt.toUElement(element);
        if (uElement instanceof USimpleNameReferenceExpression ref) {
            UElement parent = uElement.getUastParent();
            if (parent == null) return;

            UCallExpression call = UastUtils.getParentOfType(uElement, UCallExpression.class);
            if (call != null) {
                if (call.getValueArguments().contains(uElement) || call.getValueArguments().contains(parent)) {
                    PsiElement res = ref.resolve();
                    UClass uCommand = UastContextKt.toUElement(res, UClass.class);
                    if (uCommand != null && isCommand(uCommand.getJavaPsi())) {
                        GlobalSearchScope scope = ScopeBuilder.getProductionScope(element);
                        Collection<PsiElement> targets = ProcessingSearcher.findProcessing(uCommand, scope);
                        if (!targets.isEmpty()) {
                            NavigationGutterIconBuilder<PsiElement> builder =
                                    NavigationGutterIconBuilder.create(AllIcons.Actions.Execute)
                                            .setTargets(targets)
                                            .setTooltipText("Go to processing");
                            result.add(builder.createLineMarkerInfo(element));
                        }
                    }
                }
            }
        }
    }

    private void addInHandle(PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        UElement uElement = UastContextKt.toUElement(element);

        if (!(uElement instanceof USimpleNameReferenceExpression ref)) return;

        PsiElement resolved = ref.resolve();
        if (!(resolved instanceof PsiClass targetCommand)) return;

        UClass uCommand = UastUtils.getParentOfType(ref, UClass.class);
        if (uCommand == null) return;

        if (!isCommandsHandler(uCommand, targetCommand)) return;

        GlobalSearchScope scope = ScopeBuilder.getModuleScope(element);
        List<PsiElement> targets = EmissionSearcher.findEmission(
                UastContextKt.toUElement(targetCommand, UClass.class),
                scope
        );

        if (targets != null && !targets.isEmpty()) {
            result.add(NavigationGutterIconBuilder.create(AllIcons.Actions.Find)
                    .setTargets(targets)
                    .setTooltipText("Go to emission")
                    .createLineMarkerInfo(element));
        }

    }

    private boolean isCommand(PsiClass psiClass) {
        for (PsiClass superClass : psiClass.getSupers()) {
            String name = superClass.getName();

            if (name != null && name.contains("Command") && !name.contains("CommandsFlowHandler")) return true;
        }

        return false;
    }

    private boolean isCommandsHandler(UClass uClass, PsiClass psiClass) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiType commandType = factory.createType(psiClass);

        for (UTypeReferenceExpression interfaceRef : uClass.getUastSuperTypes()) {
            PsiType type = interfaceRef.getType();

            if (type instanceof PsiClassType classType) {
                PsiClassType.ClassResolveResult resolveResult = classType.resolveGenerics();
                PsiClass iClass = resolveResult.getElement();

                if (iClass != null && "CommandsFlowHandler".equals(iClass.getName())) {

                    PsiType[] params = classType.getParameters();

                    if (params.length > 0) {
                        PsiType firstParam = params[0];

                        if (firstParam.isAssignableFrom(commandType)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}