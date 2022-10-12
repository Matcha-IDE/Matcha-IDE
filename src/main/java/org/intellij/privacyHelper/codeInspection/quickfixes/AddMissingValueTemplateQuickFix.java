package org.intellij.privacyHelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.ANNOTATION_PKG;
import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.SINGLE_VALUE_FIELDS;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.fieldDataTransmissionCollectionAttributeList;

/**
 * Created by tianshi on 2/9/18.
 */
public class AddMissingValueTemplateQuickFix implements LocalQuickFix {
    private static final String inconsistentFieldValueQuickfixName = "Add missing value templates";
    private List<String> missingFieldValues;

    public AddMissingValueTemplateQuickFix(ArrayList<String> missingFieldValues) {
        this.missingFieldValues = missingFieldValues;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Coconut quick-fixes";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return inconsistentFieldValueQuickfixName;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        PsiNameValuePair pair = PsiTreeUtil.getParentOfType(problemDescriptor.getPsiElement(), PsiNameValuePair.class);
        if (pair == null) {
            return;
        }
        if (fieldDataTransmissionCollectionAttributeList.equals(pair.getName())) {
            missingFieldValues = missingFieldValues.stream().map(
                    s -> String.format("%s.collectionAttribute.%s", ANNOTATION_PKG, s)).collect(Collectors.toList());
        } else {
            missingFieldValues = missingFieldValues.stream().map(
                    s -> String.format("%s.sharingAttribute.%s", ANNOTATION_PKG, s)).collect(Collectors.toList());
        }
        Pattern p = Pattern.compile("\\{(.*?)}", Pattern.DOTALL);
        String currentValueString = pair.getValue().getText().strip();
        Matcher matcher = p.matcher(currentValueString);
        if (matcher.find()) {
            currentValueString = matcher.group(0);
            currentValueString = currentValueString.substring(1, currentValueString.length() - 1);
        }
        if (!currentValueString.isEmpty()) {
            missingFieldValues.add(currentValueString);
        }
        String valueArrayString;
        if (SINGLE_VALUE_FIELDS.contains(pair.getName())) {
            valueArrayString = String.format("%s", String.join(",\n", missingFieldValues));
        } else {
            valueArrayString = String.format("{%s}", String.join(",\n", missingFieldValues));
        }
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        pair.setValue(factory.createExpressionFromText(valueArrayString, null));
        JavaCodeStyleManager.getInstance(pair.getProject())
                // Tell it to shorten all class references accordingly
                .shortenClassReferences(pair);
    }
}
