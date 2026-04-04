package com.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.*;

public class KtEventUtil {

    public static boolean isEventClass(KtClassOrObject ktClass) {
        String name = ktClass.getName();
        if (name == null || name.endsWith("Update") || name.endsWith("Handler")) return false;
        if (name.endsWith("Event")) return true;
        if (ktClass.getSuperTypeList() != null) {
            String cleanSuperType = ktClass.getSuperTypeList().getText().replaceAll("<.*?>", "");
            return cleanSuperType.contains("Event");
        }
        KtClassOrObject parentClass = PsiTreeUtil.getParentOfType(ktClass, KtClassOrObject.class);
        if (parentClass != null) {
            String parentName = parentClass.getName();
            if (parentName != null && parentName.endsWith("Event")) return true;
        }
        return false;
    }

    @Nullable
    public static KtClassOrObject tryResolveToClass(PsiElement element) {
        if (element == null) return null;

        if (element instanceof KtClassOrObject) return (KtClassOrObject) element;

        if (PsiTreeUtil.getParentOfType(element, KtImportDirective.class) != null) return null;

        PsiElement parent = element.getParent();

        if (parent instanceof KtClassOrObject && element == ((KtClassOrObject) parent).getNameIdentifier()) {
            return (KtClassOrObject) parent;
        }

        if (parent instanceof KtReferenceExpression) {
            KtReferenceExpression ref = (KtReferenceExpression) parent;

            PsiReference reference = ref.getReference();
            PsiElement resolved = reference != null ? reference.resolve() : null;

            if (resolved instanceof KtClassOrObject) return (KtClassOrObject) resolved;

            if (resolved instanceof KtConstructor) {
                return ((KtConstructor<?>) resolved).getContainingClassOrObject();
            }
        }

        return null;
    }
}