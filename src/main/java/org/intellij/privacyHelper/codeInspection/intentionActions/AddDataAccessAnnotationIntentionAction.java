package org.intellij.privacyHelper.codeInspection.intentionActions;

import com.intellij.codeInspection.util.IntentionFamilyName;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.NotNull;

public class AddDataAccessAnnotationIntentionAction extends AddAnnotationBaseIntentionAction {
    AddDataAccessAnnotationIntentionAction() {
        preFilledAnnotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataAccess);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "AddDataAccessAnnotationIntentionAction";
    }
}
