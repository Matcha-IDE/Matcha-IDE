package org.intellij.privacyHelper.codeInspection.instances;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CollectionAttribute;
import org.intellij.privacyHelper.codeInspection.utils.SafetySectionDataElement;
import org.intellij.privacyHelper.codeInspection.utils.SharingAttribute;

import java.util.*;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.DATA_TYPE_PRIVACY_LABEL_MAPPING;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class AnnotationInstance {
    private final SmartPsiElementPointer<PsiElement> annotationSmartPointer;
    private final AnnotationHolder annotationHolder;

    public AnnotationInstance(SmartPsiElementPointer<PsiElement> psiElementPointer,
                              AnnotationHolder annotationHolder) {
        this.annotationSmartPointer = psiElementPointer;
        this.annotationHolder = annotationHolder;
    }

    public SmartPsiElementPointer<PsiElement> getAnnotationSmartPointer() {
        return annotationSmartPointer;
    }

    public CoconutAnnotationType getAnnotationType() {
        return annotationHolder.mAnnotationType;
    }

    public AnnotationHolder getAnnotationHolder() {
        return annotationHolder;
    }

    public SafetySectionDataElement getSafetySectionDataElement(Project openProject) {
        if (annotationHolder.mAnnotationType == CoconutAnnotationType.DataAccess) {
            return null;
        }
        String[][] privacyLabelDataTypes = Arrays.stream(
                        annotationHolder.getTransmissionDataTypes(openProject))
                .map(d -> DATA_TYPE_PRIVACY_LABEL_MAPPING.get(d)).toArray(String[][]::new);

        List<String> collectionAttributes;
        List<String> sharingAttributes;
        try {
            collectionAttributes =
                    List.of(Arrays.stream(annotationHolder.getInstanceAllValuesNoPkgPrefix(
                            fieldDataTransmissionCollectionAttributeList, 2)).toArray(String[]::new));
            sharingAttributes =
                    List.of(Arrays.stream(annotationHolder.getInstanceAllValuesNoPkgPrefix(
                            fieldDataTransmissionSharingAttributeList, 2)).toArray(String[]::new));
        } catch (IllegalArgumentException e) {
            return null;
        }
        Set<String[]> privacyLabelAttributes = new HashSet<>();
        boolean encryptedInTransit = true;
        boolean userRequestDelete = false;
        if (!collectionAttributes.contains(CollectionAttribute.TransmittedOffDevice_False) &&
                !collectionAttributes.contains(CollectionAttribute.UserToUserEncryption_True)) {
            privacyLabelAttributes.add(safetySectionCollected);
            if (collectionAttributes.contains(CollectionAttribute.EncryptionInTransit_False)) {
                encryptedInTransit = false;
            }
            if (collectionAttributes.contains(CollectionAttribute.UserRequestDelete_True)) {
                userRequestDelete = true;
            }
            if (collectionAttributes.contains(CollectionAttribute.NotStoredInBackend_True)) {
                privacyLabelAttributes.add(safetySectionEphemeral);
            }
            if (collectionAttributes.contains(CollectionAttribute.OptionalCollection_True)) {
                privacyLabelAttributes.add(safetySectionOptional);
            } else {
                privacyLabelAttributes.add(safetySectionRequired);
            }
            if (collectionAttributes.contains(CollectionAttribute.ForAccountManagement)) {
                privacyLabelAttributes.add(safetySectionCollectionAccountManagement);
            }
            if (collectionAttributes.contains(CollectionAttribute.ForAnalytics)) {
                privacyLabelAttributes.add(safetySectionCollectionAnalytics);
            }
            if (collectionAttributes.contains(CollectionAttribute.ForAdvertisingOrMarketing)) {
                privacyLabelAttributes.add(safetySectionCollectionAdvertising);
            }
            if (collectionAttributes.contains(CollectionAttribute.ForAppFunctionality)) {
                privacyLabelAttributes.add(safetySectionCollectionAppFunctionality);
            }
            if (collectionAttributes.contains(CollectionAttribute.ForDeveloperCommunications)) {
                privacyLabelAttributes.add(safetySectionCollectionDevCommunications);
            }
            if (collectionAttributes.contains(CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance)) {
                privacyLabelAttributes.add(safetySectionCollectionFraudPrevention);
            }
            if (collectionAttributes.contains(CollectionAttribute.ForPersonalization)) {
                privacyLabelAttributes.add(safetySectionCollectionPersonalization);
            }
        }
        if (!sharingAttributes.contains(SharingAttribute.SharedWithThirdParty_False) &&
                !sharingAttributes.contains(SharingAttribute.OnlyAfterGettingUserConsent_True) &&
                !sharingAttributes.contains(SharingAttribute.OnlySharedWithServiceProviders_True) &&
                !sharingAttributes.contains(SharingAttribute.OnlySharedForLegalPurposes_True) &&
                !sharingAttributes.contains(SharingAttribute.OnlyInitiatedByUser_True) &&
                !sharingAttributes.contains(SharingAttribute.OnlyTransferringAnonymousData_True)) {
            privacyLabelAttributes.add(safetySectionShared);
            if (sharingAttributes.contains(SharingAttribute.ForAccountManagement)) {
                privacyLabelAttributes.add(safetySectionSharingAccountManagement);
            }
            if (sharingAttributes.contains(SharingAttribute.ForAnalytics)) {
                privacyLabelAttributes.add(safetySectionSharingAnalytics);
            }
            if (sharingAttributes.contains(SharingAttribute.ForAdvertisingOrMarketing)) {
                privacyLabelAttributes.add(safetySectionSharingAdvertising);
            }
            if (sharingAttributes.contains(SharingAttribute.ForAppFunctionality)) {
                privacyLabelAttributes.add(safetySectionSharingAppFunctionality);
            }
            if (sharingAttributes.contains(SharingAttribute.ForDeveloperCommunications)) {
                privacyLabelAttributes.add(safetySectionSharingDevCommunications);
            }
            if (sharingAttributes.contains(SharingAttribute.ForFraudPreventionAndSecurityAndCompliance)) {
                privacyLabelAttributes.add(safetySectionSharingFraudPrevention);
            }
            if (sharingAttributes.contains(SharingAttribute.ForPersonalization)) {
                privacyLabelAttributes.add(safetySectionSharingPersonalization);
            }
        }
        if (!privacyLabelAttributes.isEmpty()) {
            HashMap<String[], Set<String[]>> dataPractices = new HashMap<>();
            for (String[] dataType : privacyLabelDataTypes) {
                dataPractices.put(dataType, privacyLabelAttributes);
            }
            return new SafetySectionDataElement(encryptedInTransit, userRequestDelete, dataPractices);
        } else {
            return null;
        }
    }

    public boolean isValid() {
        return annotationSmartPointer.getRange() != null && annotationSmartPointer.getElement() != null;
    }
}
