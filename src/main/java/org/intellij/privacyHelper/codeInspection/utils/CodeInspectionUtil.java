/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.privacyHelper.codeInspection.utils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.codeInspection.annotations.*;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationMetaData;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationInstance;
import org.intellij.privacyHelper.codeInspection.instances.SensitiveAPIInstance;
import org.intellij.privacyHelper.codeInspection.quickfixes.AddMissingValueTemplateQuickFix;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.getCommonPrefix;
import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.*;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 3/26/17.
 */
public class CodeInspectionUtil {

    private static final String MISSING_FIELDS = "Missing fields: %s";
    private static final String EMPTY_FIELD = "This field is empty. %s";
    private static final String INCOMPLETE_FIELD_VALUE = "This value is incomplete. %s";

    private static final Map<String, String> FIX_GUIDE_MESSAGES = Collections.unmodifiableMap(new HashMap<>() {{
        put(fieldDataAccessId, "You are recommended to choose a meaningful name that represents the data type and " +
                "the purpose for the data accessed here. For example, if the GPS location is accessed for building " +
                "map features for your app, you can create a string resource `R.string.gps_location_for_map`, and then " +
                "set the value of the string resource to the same value of its name or a more extensive explanation");
    }});

    /**
     * Sets the fields and order for annotations of each type. This determines which fields must be completed for each annotation by the user.
     */
    public static Map<String, String []> ANNOTATION_FIELD_ORDER = new HashMap<>();
    public static final List<String> SINGLE_VALUE_FIELDS =
            Arrays.asList(fieldDataAccessId);

    public static Set<String> NO_VALUE_ANNOTATION = new HashSet<>();
    public static Map<String, Map<String, String>> ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING = new HashMap<>();
    public static Map<String, Map<String, String>> ANNOTATION_TYPE_FIELDS_EMPTY_VALUE_PATTERN_MAPPING = new HashMap<>();
    public static Map<String, Set<String>> ANNOTATION_TYPE_REQUIRED_FIELDS_SET = new HashMap<>();
    public static Map<String, String[]> SAFETY_SECTION_ATTRIBUTE_MAPPING =
            Collections.unmodifiableMap(new HashMap<>() {{
                put("isCollected", safetySectionCollected);
                put("isShared", safetySectionShared);
                put("isEphemeral", safetySectionEphemeral);
                put("isOptional", safetySectionOptional);
                put("isRequired", safetySectionRequired);
                put("isCollectedForAppFunctionality", safetySectionCollectionAppFunctionality);
                put("isCollectedForAnalytics", safetySectionCollectionAnalytics);
                put("isCollectedForDevCommunications", safetySectionCollectionDevCommunications);
                put("isCollectedForFraudPrevention", safetySectionCollectionFraudPrevention);
                put("isCollectedForAdvertising", safetySectionCollectionAdvertising);
                put("isCollectedForPersonalization", safetySectionCollectionPersonalization);
                put("isCollectedForAccountManagement", safetySectionCollectionAccountManagement);
                put("isSharedForAppFunctionality", safetySectionSharingAppFunctionality);
                put("isSharedForAnalytics", safetySectionSharingAnalytics);
                put("isSharedForDevCommunications", safetySectionSharingDevCommunications);
                put("isSharedForFraudPrevention", safetySectionSharingFraudPrevention);
                put("isSharedForAdvertising", safetySectionSharingAdvertising);
                put("isSharedForPersonalization", safetySectionSharingPersonalization);
                put("isSharedForAccountManagement", safetySectionSharingAccountManagement);
            }});
    public static Map<String[], String> SAFETY_SECTION_ATTRIBUTE_REVERSE_MAPPING =
            Collections.unmodifiableMap(new HashMap<>() {{
                put(safetySectionCollected, "isCollected");
                put(safetySectionShared, "isShared");
                put(safetySectionEphemeral, "isEphemeral");
                put(safetySectionOptional, "isOptional");
                put(safetySectionRequired, "isRequired");
                put(safetySectionCollectionAppFunctionality, "isCollectedForAppFunctionality");
                put(safetySectionCollectionAnalytics, "isCollectedForAnalytics");
                put(safetySectionCollectionDevCommunications, "isCollectedForDevCommunications");
                put(safetySectionCollectionFraudPrevention, "isCollectedForFraudPrevention");
                put(safetySectionCollectionAdvertising, "isCollectedForAdvertising");
                put(safetySectionCollectionPersonalization, "isCollectedForPersonalization");
                put(safetySectionCollectionAccountManagement, "isCollectedForAccountManagement");
                put(safetySectionSharingAppFunctionality, "isSharedForAppFunctionality");
                put(safetySectionSharingAnalytics, "isSharedForAnalytics");
                put(safetySectionSharingDevCommunications, "isSharedForDevCommunications");
                put(safetySectionSharingFraudPrevention, "isSharedForFraudPrevention");
                put(safetySectionSharingAdvertising, "isSharedForAdvertising");
                put(safetySectionSharingPersonalization, "isSharedForPersonalization");
                put(safetySectionSharingAccountManagement, "isSharedForAccountManagement");
            }});
    public static Map<PersonalDataGroup, String[]> DATA_TYPE_PRIVACY_LABEL_MAPPING =
            Collections.unmodifiableMap(new HashMap<>() {{
                put(PersonalDataGroup.PersonalInfo_Name, safetySectionName);
                put(PersonalDataGroup.PersonalInfo_EmailAddress, safetySectionEmail);
                put(PersonalDataGroup.PersonalInfo_UserIds, safetySectionUserAccount);
                put(PersonalDataGroup.PersonalInfo_Address, safetySectionAddress);
                put(PersonalDataGroup.PersonalInfo_PhoneNumber, safetySectionPhone);
                put(PersonalDataGroup.PersonalInfo_RaceAndEthnicity, safetySectionEthnicity);
                put(PersonalDataGroup.PersonalInfo_PoliticalOrReligiousBeliefs, safetySectionReligious);
                put(PersonalDataGroup.PersonalInfo_SexualOrientation, safetySectionSexGender);
                put(PersonalDataGroup.PersonalInfo_OtherPersonalInfo, safetySectionPersonal);
                put(PersonalDataGroup.FinancialInfo_UserPaymentInfo, safetySectionBankAccount);
                put(PersonalDataGroup.FinancialInfo_PurchaseHistory, safetySectionPurchaseHistory);
                put(PersonalDataGroup.FinancialInfo_CreditScore, safetySectionCreditScore);
                put(PersonalDataGroup.FinancialInfo_OtherFinancialInfo, safetySectionOtherFinancial);
                put(PersonalDataGroup.Location_ApproximateLocation, safetySectionApproxLocation);
                put(PersonalDataGroup.Location_PreciseLocation, safetySectionPreciseLocation);
                put(PersonalDataGroup.WebBrowsing_WebBrowsingHistory, safetySectionWebBrowsingHistory);
                put(PersonalDataGroup.Messages_Emails, safetySectionMessagesEmails);
                put(PersonalDataGroup.Messages_SmsOrMms, safetySectionSmsCallLog);
                put(PersonalDataGroup.Messages_InAppMessages, safetySectionOtherMessages);
                put(PersonalDataGroup.PhotosAndVideos_Photos, safetySectionPhotos);
                put(PersonalDataGroup.PhotosAndVideos_Videos, safetySectionVideos);
                put(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings, safetySectionAudio);
                put(PersonalDataGroup.AudioFiles_MusicFiles, safetySectionMusic);
                put(PersonalDataGroup.AudioFiles_OtherUserAudioFiles, safetySectionOtherAudio);
                put(PersonalDataGroup.HealthAndFitness_HealthInfo, safetySectionHealth);
                put(PersonalDataGroup.HealthAndFitness_FitnessInfo, safetySectionFitness);
                put(PersonalDataGroup.Contacts_Contacts, safetySectionContacts);
                put(PersonalDataGroup.Calendar_CalendarEvents, safetySectionCalendar);
                put(PersonalDataGroup.AppInfoAndPerformance_CrashLogs, safetySectionCrashLogs);
                put(PersonalDataGroup.AppInfoAndPerformance_Diagnostics, safetySectionPerformanceDiagnostics);
                put(PersonalDataGroup.AppInfoAndPerformance_OtherAppPerformanceData, safetySectionOtherPerformance);
                put(PersonalDataGroup.FilesAndDocs_FilesAndDocs, safetySectionFilesAndDocs);
                put(PersonalDataGroup.AppActivity_AppInteractions, safetySectionUserInteraction);
                put(PersonalDataGroup.AppActivity_InAppSearchHistory, safetySectionInAppSearchHistory);
                put(PersonalDataGroup.AppActivity_InstalledApps, safetySectionAppsOnDevice);
                put(PersonalDataGroup.AppActivity_OtherUserGeneratedContent, safetySectionUserGeneratedContent);
                put(PersonalDataGroup.AppActivity_OtherUserActivities, safetySectionOtherAppActivity);
                put(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds, safetySectionDeviceId);
            }});

    public static Map<String[], PersonalDataGroup> DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING =
            Collections.unmodifiableMap(new HashMap<>() {{
                put(safetySectionName, PersonalDataGroup.PersonalInfo_Name);
                put(safetySectionEmail, PersonalDataGroup.PersonalInfo_EmailAddress);
                put(safetySectionUserAccount, PersonalDataGroup.PersonalInfo_UserIds);
                put(safetySectionAddress, PersonalDataGroup.PersonalInfo_Address);
                put(safetySectionPhone, PersonalDataGroup.PersonalInfo_PhoneNumber);
                put(safetySectionEthnicity, PersonalDataGroup.PersonalInfo_RaceAndEthnicity);
                put(safetySectionReligious, PersonalDataGroup.PersonalInfo_PoliticalOrReligiousBeliefs);
                put(safetySectionSexGender, PersonalDataGroup.PersonalInfo_SexualOrientation);
                put(safetySectionPersonal, PersonalDataGroup.PersonalInfo_OtherPersonalInfo);
                put(safetySectionBankAccount, PersonalDataGroup.FinancialInfo_UserPaymentInfo);
                put(safetySectionPurchaseHistory, PersonalDataGroup.FinancialInfo_PurchaseHistory);
                put(safetySectionCreditScore, PersonalDataGroup.FinancialInfo_CreditScore);
                put(safetySectionOtherFinancial, PersonalDataGroup.FinancialInfo_OtherFinancialInfo);
                put(safetySectionApproxLocation, PersonalDataGroup.Location_ApproximateLocation);
                put(safetySectionPreciseLocation, PersonalDataGroup.Location_PreciseLocation);
                put(safetySectionWebBrowsingHistory, PersonalDataGroup.WebBrowsing_WebBrowsingHistory);
                put(safetySectionMessagesEmails, PersonalDataGroup.Messages_Emails);
                put(safetySectionSmsCallLog, PersonalDataGroup.Messages_SmsOrMms);
                put(safetySectionOtherMessages, PersonalDataGroup.Messages_InAppMessages);
                put(safetySectionPhotos, PersonalDataGroup.PhotosAndVideos_Photos);
                put(safetySectionVideos, PersonalDataGroup.PhotosAndVideos_Videos);
                put(safetySectionAudio, PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings);
                put(safetySectionMusic, PersonalDataGroup.AudioFiles_MusicFiles);
                put(safetySectionOtherAudio, PersonalDataGroup.AudioFiles_OtherUserAudioFiles);
                put(safetySectionHealth, PersonalDataGroup.HealthAndFitness_HealthInfo);
                put(safetySectionFitness, PersonalDataGroup.HealthAndFitness_FitnessInfo);
                put(safetySectionContacts, PersonalDataGroup.Contacts_Contacts);
                put(safetySectionCalendar, PersonalDataGroup.Calendar_CalendarEvents);
                put(safetySectionCrashLogs, PersonalDataGroup.AppInfoAndPerformance_CrashLogs);
                put(safetySectionPerformanceDiagnostics, PersonalDataGroup.AppInfoAndPerformance_Diagnostics);
                put(safetySectionOtherPerformance, PersonalDataGroup.AppInfoAndPerformance_OtherAppPerformanceData);
                put(safetySectionFilesAndDocs, PersonalDataGroup.FilesAndDocs_FilesAndDocs);
                put(safetySectionUserInteraction, PersonalDataGroup.AppActivity_AppInteractions);
                put(safetySectionInAppSearchHistory, PersonalDataGroup.AppActivity_InAppSearchHistory);
                put(safetySectionAppsOnDevice, PersonalDataGroup.AppActivity_InstalledApps);
                put(safetySectionUserGeneratedContent, PersonalDataGroup.AppActivity_OtherUserGeneratedContent);
                put(safetySectionOtherAppActivity, PersonalDataGroup.AppActivity_OtherUserActivities);
                put(safetySectionDeviceId, PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds);
            }});

    public static final String ANNOTATION_PKG = "me.tianshili.annotationlib";

    public static final List<String> PREDEFINED_VALUE_FIELDS = List.of(fieldDataAccessDataType,
            fieldDataTransmissionSharingAttributeList, fieldDataTransmissionCollectionAttributeList);

    private static final String resourceStringPrefix = "R.string.";
    private static final String sourceDataStringPrefix = "DataType.";

    static {
            Collections.addAll(NO_VALUE_ANNOTATION,
                    NotPersonalDataAccess.toString(), NotPersonalDataTransmission.toString());
            ANNOTATION_TYPE_REQUIRED_FIELDS_SET.put(DataAccess.toString(),
                    new HashSet<>(Arrays.asList(fieldDataAccessId, fieldDataAccessDataType)));
            ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING.put(DataAccess.toString(),
                    Collections.unmodifiableMap(new HashMap<>() {{
                        put(fieldDataAccessId, resourceStringPrefix);
                        put(fieldDataAccessDataType, sourceDataStringPrefix);
                    }}));
            ANNOTATION_TYPE_FIELDS_EMPTY_VALUE_PATTERN_MAPPING.put(DataAccess.toString(),
                    Collections.unmodifiableMap(new HashMap<>() {{
                        put(fieldDataAccessId, resourceStringPrefix);
                        put(fieldDataAccessDataType, sourceDataStringPrefix);
                        put(fieldDataTransmissionAccessIdList, resourceStringPrefix);
                    }}));
            ANNOTATION_FIELD_ORDER.put(DataAccess.toString(),
                    new String[]{fieldDataAccessId, fieldDataAccessDataType});
            ANNOTATION_TYPE_REQUIRED_FIELDS_SET.put(DataTransmission.toString(),
                    new HashSet<>(Arrays.asList(fieldDataTransmissionAccessIdList,
                            fieldDataTransmissionCollectionAttributeList, fieldDataTransmissionSharingAttributeList)));
            ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING.put(DataTransmission.toString(),
                Collections.unmodifiableMap(new HashMap<>() {{
                    put(fieldDataTransmissionAccessIdList, null);
                    put(fieldDataTransmissionCollectionAttributeList, null);
                    put(fieldDataTransmissionSharingAttributeList, null);
                }}));
            ANNOTATION_TYPE_FIELDS_EMPTY_VALUE_PATTERN_MAPPING.put(DataTransmission.toString(),
                Collections.unmodifiableMap(new HashMap<>() {{
                    put(fieldDataTransmissionAccessIdList, resourceStringPrefix);
                    put(fieldDataTransmissionCollectionAttributeList, " *");
                    put(fieldDataTransmissionSharingAttributeList, " *");
                }}));
        ANNOTATION_FIELD_ORDER.put(DataTransmission.toString(),
                new String[]{fieldDataTransmissionAccessIdList,
                        fieldDataTransmissionCollectionAttributeList, fieldDataTransmissionSharingAttributeList});
    }

    public static boolean checkAnnotationCorrectnessByType(PsiAnnotation annotation, ProblemsHolder holder) {
        Set<String> errorFields = new HashSet<>();
        boolean correctness;
        boolean completeness = checkAnnotationCompletenessByType(errorFields, annotation, holder);
        boolean validity;
        if (parseAnnotation(annotation).mAnnotationType == DataAccess) {
            validity = checkSourceAnnotationValidity(errorFields, annotation, holder);
        } else {
            validity = checkSinkAnnotationValidity(errorFields, annotation, holder);
        }
        correctness = completeness && validity;
        PsiNameValuePair[] nameValuePairs = annotation.getParameterList().getAttributes();
        for (PsiNameValuePair nameValuePair : nameValuePairs) {
            if (nameValuePair.getNameIdentifier() != null) {
                PrivacyPracticesHolder.getInstance(annotation.getProject()).setAnnotationFieldIsCorrect(
                        nameValuePair.getNameIdentifier(),
                        !errorFields.contains(nameValuePair.getName()));
            }
        }
        return correctness;
    }

    private static boolean checkAnnotationCompletenessByType(Set<String> errorFields,
                                                             PsiAnnotation annotation, ProblemsHolder holder) {
        if (annotation == null) {
            return false;
        }
        String annotationTypeString = CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(annotation).toString();
        boolean validAnnotation = true;
        Project openProject = annotation.getProject();
        Map<String, String> annotationFieldInitValuePatternMapping =
                ANNOTATION_TYPE_FIELDS_EMPTY_VALUE_PATTERN_MAPPING.get(annotationTypeString);
        Set<String> annotationRequiredFields = ANNOTATION_TYPE_REQUIRED_FIELDS_SET.get(annotationTypeString);
        PsiNameValuePair[] annotationTypeValuePairs = annotation.getParameterList().getAttributes();
        if (annotationTypeValuePairs.length < annotationFieldInitValuePatternMapping.size()) {
            ArrayList<String> missingFields = new ArrayList<>();
            for (String requiredField : annotationRequiredFields) {
                boolean findField = false;
                for (PsiNameValuePair nameValuePair : annotationTypeValuePairs) {
                    if (requiredField.equals(nameValuePair.getName())) {
                        findField = true;
                        break;
                    }
                }
                if (!findField) {
                    missingFields.add(requiredField);
                }
            }
            if (!missingFields.isEmpty()) {
                validAnnotation = false;
                if (holder != null) {
                    if (annotation.getNameReferenceElement() != null) {
                        holder.registerProblem(annotation.getNameReferenceElement(),
                                String.format(MISSING_FIELDS, String.join(", ", missingFields)),
                                ProblemHighlightType.GENERIC_ERROR, null);
                    }
                }
            }
        }
        for (PsiNameValuePair nameValuePair : annotationTypeValuePairs) {
            if (nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                validAnnotation = false;
                break;
            }
            String name = nameValuePair.getName();
            if (!annotationFieldInitValuePatternMapping.containsKey(name)) {
                validAnnotation = false;
                break;
            }
            if (annotationFieldInitValuePatternMapping.get(name) == null) {
                // No restriction on this field, continue
                continue;
            }
            PsiElement nameIdentifier = nameValuePair.getNameIdentifier();
            PsiElement value = nameValuePair.getValue();
            ArrayList<PsiElement> elementArrayList = new ArrayList<>();
            if (value instanceof PsiArrayInitializerMemberValue) {
                elementArrayList.addAll(Arrays.asList(((PsiArrayInitializerMemberValue) value).getInitializers()));
            } else {
                elementArrayList.add(value);
            }
            boolean isCurrentFieldComplete = true;
            if (elementArrayList.isEmpty()) {
                validAnnotation = false;
                isCurrentFieldComplete = false;
                if (holder != null && nameIdentifier != null) {
                    holder.registerProblem(nameIdentifier,
                            String.format(EMPTY_FIELD, FIX_GUIDE_MESSAGES.getOrDefault(name, "")),
                                    ProblemHighlightType.GENERIC_ERROR, null);
                }
            } else {
                boolean unknownfield = false;
                for (PsiElement exp : elementArrayList) {
                    if (Pattern.matches(annotationFieldInitValuePatternMapping.get(name), exp.getText())) {
                        validAnnotation = false;
                        unknownfield = true;
                        isCurrentFieldComplete = false;
                    }
                }
                if (unknownfield) {
                    if (holder != null && nameIdentifier != null) {
                        holder.registerProblem(nameIdentifier,
                                String.format(INCOMPLETE_FIELD_VALUE, FIX_GUIDE_MESSAGES.getOrDefault(name, "")),
                                ProblemHighlightType.GENERIC_ERROR, null);
                    }
                }
            }
            if (!isCurrentFieldComplete) {
                errorFields.add(name);
            }
        }
        return validAnnotation;
    }

    public static ArrayList<String> getAllUniqueSourceIds(Project project) {
        ArrayList<String> allSourceIds = new ArrayList<>();
        AnnotationInstance[] annotationInstances =
                PrivacyPracticesHolder.getInstance(project).getAnnotationInstances();
        for (AnnotationInstance annotationInstance : annotationInstances) {
            if (annotationInstance.getAnnotationType() != DataAccess) {
                continue;
            }
            String dataId = annotationInstance.getAnnotationHolder().getDataId(false);
            if (!dataId.isEmpty() && !dataId.equals(resourceStringPrefix) && !allSourceIds.contains(dataId)) {
                allSourceIds.add(dataId);
            }
        }
        return allSourceIds;
    }

    public static boolean isUniqueSourceId(Project project, String sourceId) {
        int countMatch = 0;
        AnnotationInstance[] annotationInstances =
                PrivacyPracticesHolder.getInstance(project).getAnnotationInstances();
        for (AnnotationInstance annotationInstance : annotationInstances) {
            if (annotationInstance.getAnnotationType() != DataAccess) {
                continue;
            }
            String dataId = annotationInstance.getAnnotationHolder().getDataId(false);
            if (dataId.equals(sourceId)) {
                countMatch += 1;
            }
        }
        return countMatch <= 1;
    }

    static public PersonalDataGroup[] getDataTypesByAccessIds(String[] transmissionAccessIds, Project project) {
        ArrayList<PersonalDataGroup> dataGroups = new ArrayList<>();
        for (AnnotationInstance instance :
                PrivacyPracticesHolder.getInstance(project).getAnnotationInstances()) {
            if (instance.getAnnotationType() != CoconutAnnotationType.DataAccess) {
                continue;
            }
            String accessId = instance.getAnnotationHolder().getInstanceFirstValue(fieldDataAccessId);
            if (!List.of(transmissionAccessIds).contains(accessId)) {
                continue;
            }
            for (String dataGroupString : instance.getAnnotationHolder().getDataTypes()) {
                PersonalDataGroup dataGroup;
                try {
                    dataGroup = PersonalDataGroup.valueOf(dataGroupString);
                } catch (IllegalArgumentException ignored) {
                    dataGroup = null;
                }
                if (dataGroup != null) {
                    dataGroups.add(dataGroup);
                }
            }
        }
        return dataGroups.toArray(new PersonalDataGroup[0]);
    }

    public static boolean checkSourceAnnotationValidity(Set<String> errorFields, PsiAnnotation annotation, ProblemsHolder holder) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.parseAnnotation(annotation);
        HashMap<String, ArrayList<String>> fieldErrorMessages = new HashMap<>();
        String accessId = annotationHolder.getInstanceFirstValue(fieldDataAccessId).replace("\"", "");
        // check if accessId is not an empty value and it hasn't been used before
        if (!resourceStringPrefix.equals(accessId) && !isUniqueSourceId(annotation.getProject(), accessId)) {
            fieldErrorMessages.put(fieldDataAccessId, new ArrayList<>());
            fieldErrorMessages.get(fieldDataAccessId).add(
                    String.format("%s is already used in other @DataAccess annotations. Please create a unique ID for each @DataAccess annotation.", accessId));
        }
        // Finally, register the problems directly on the annotation element
        if (!fieldErrorMessages.isEmpty()) {
            PsiNameValuePair[] nameValuePairs = annotation.getParameterList().getAttributes();
            for (PsiNameValuePair nameValuePair : nameValuePairs) {
                if (nameValuePair.getNameIdentifier() != null) {
                    if (fieldErrorMessages.containsKey(nameValuePair.getName())) {
                        LocalQuickFix quickFix = null;
                        for (String errorMessage : fieldErrorMessages.get(nameValuePair.getName())) {
                            if (holder != null) {
                                holder.registerProblem(nameValuePair.getNameIdentifier(), errorMessage,
                                        ProblemHighlightType.GENERIC_ERROR, quickFix);
                            }
                        }
                    }
                    if (fieldErrorMessages.containsKey(nameValuePair.getName())) {
                        errorFields.add(nameValuePair.getName());
                    }
                }
            }
        }
        return fieldErrorMessages.isEmpty();
    }

    public static boolean checkSinkAnnotationValidity(Set<String> errorFields, PsiAnnotation annotation, ProblemsHolder holder) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.parseAnnotation(annotation);
        HashMap<String, ArrayList<String>> fieldErrorMessages = new HashMap<>();
        HashMap<String, ArrayList<String>> missingValuePrefixes = new HashMap<>();
        // First, check whether the access ids are valid
        String[] accessIdsInTransmission = Arrays.stream(
                annotationHolder.getInstanceAllValues(fieldDataTransmissionAccessIdList))
                .map(s -> s.replace("\"", "")).toArray(String[]::new);
        ArrayList<String> allSourceIds = getAllUniqueSourceIds(annotation.getProject());
        ArrayList<String> nonexistentIds = new ArrayList<>();
        for (String accessId : accessIdsInTransmission) {
            if (!allSourceIds.contains(accessId)) {
                nonexistentIds.add(accessId);
            }
        }
        if (!nonexistentIds.isEmpty()) {
            String errorMessage = String.format("%s do not match any existing @%s annotation",
                    String.join(", ", nonexistentIds), DataAccess);
            ArrayList<String> newErrorMessageList = new ArrayList<>();
            newErrorMessageList.add(errorMessage);
            fieldErrorMessages.put(fieldDataTransmissionAccessIdList, newErrorMessageList);
        }
        // Second, check if the binary collection/sharing attributes contain exactly one value
        for (Map.Entry<String, String[][]> entry : ONE_CHOICE_REQUIRED.entrySet()) {
            for (String[] requiredValueGroup : entry.getValue()) {
                int choiceCount = 0;
                ArrayList<String> containedValues = new ArrayList<>();
                for (String requiredValue : requiredValueGroup) {
                    if (annotationHolder.hasTransmissionAttribute(entry.getKey(), requiredValue)) {
                        choiceCount += 1;
                        containedValues.add(requiredValue);
                    }
                }
                if (choiceCount == 0) {
                    updateErrorMessageMap(fieldErrorMessages, entry, NEED_ONE_VALUE, String.join(", ", requiredValueGroup));
                    if (!missingValuePrefixes.containsKey(entry.getKey())) {
                        missingValuePrefixes.put(entry.getKey(), new ArrayList<>());
                    }
                    missingValuePrefixes.get(entry.getKey()).add(getCommonPrefix(requiredValueGroup));
                } else if (choiceCount > 1) {
                    updateErrorMessageMap(fieldErrorMessages, entry, CONFLICTING_VALUES, String.join(", ", containedValues));
                }
            }
        }
        for (Map.Entry<String, Map<String, String[][]>> cond : CONDITIONAL_AT_LEAST_ONE_CHOICE_REQUIRED.entrySet()) {
            String field = cond.getKey();
            for (Map.Entry<String, String[][]> entry : cond.getValue().entrySet()) {
                if (!annotationHolder.hasTransmissionAttribute(field, entry.getKey())) {
                    continue;
                }
                for (String[] requiredValueGroup : entry.getValue()) {
                    int choiceCount = 0;
                    for (String requiredValue : requiredValueGroup) {
                        if (annotationHolder.hasTransmissionAttribute(field, requiredValue)) {
                            choiceCount += 1;
                        }
                    }
                    if (choiceCount == 0) {
                        String errorMessage = String.format(NEED_AT_LEAST_ONE_VALUE, String.join(", ", requiredValueGroup));
                        if (fieldErrorMessages.containsKey(field)) {
                            fieldErrorMessages.get(field).add(errorMessage);
                        } else {
                            ArrayList<String> newErrorMessageList = new ArrayList<>();
                            newErrorMessageList.add(errorMessage);
                            fieldErrorMessages.put(field, newErrorMessageList);
                        }
                        if (!missingValuePrefixes.containsKey(field)) {
                            missingValuePrefixes.put(field, new ArrayList<>());
                        }
                        missingValuePrefixes.get(field).add(getCommonPrefix(requiredValueGroup));
                    }
                }
            }

        }
        // Third, check if the other required collection/sharing attributes contain at least one value
        for (Map.Entry<String, String[][]> entry : AT_LEAST_ONE_CHOICE_REQUIRED.entrySet()) {
            for (String[] requiredValueGroup : entry.getValue()) {
                int choiceCount = 0;
                for (String requiredValue : requiredValueGroup) {
                    if (annotationHolder.hasTransmissionAttribute(entry.getKey(), requiredValue)) {
                        choiceCount += 1;
                    }
                }
                if (choiceCount == 0) {
                    String errorMessage = String.format(NEED_AT_LEAST_ONE_VALUE, String.join(", ", requiredValueGroup));
                    if (fieldErrorMessages.containsKey(entry.getKey())) {
                        fieldErrorMessages.get(entry.getKey()).add(errorMessage);
                    } else {
                        ArrayList<String> newErrorMessageList = new ArrayList<>();
                        newErrorMessageList.add(errorMessage);
                        fieldErrorMessages.put(entry.getKey(), newErrorMessageList);
                    }
                    if (!missingValuePrefixes.containsKey(entry.getKey())) {
                        missingValuePrefixes.put(entry.getKey(), new ArrayList<>());
                    }
                    missingValuePrefixes.get(entry.getKey()).add(getCommonPrefix(requiredValueGroup));
                }
            }
        }
        // Fourth, check if there are conflicting values (e.g., specifying both not sharing data and sharing properties)
        for (Map.Entry<String, String[][]> entry : CONFLICT_VALUE_GROUP_MAP.entrySet()) {
            ArrayList<String> potentialConflictingValues = new ArrayList<>();
            for (String[] conflictingValueGroup : entry.getValue()) {
                ArrayList<String> currentValues = new ArrayList<>();
                for (String value : conflictingValueGroup) {
                    if (annotationHolder.hasTransmissionAttribute(entry.getKey(), value)) {
                        currentValues.add(value);
                    }
                }
                if (!currentValues.isEmpty()) {
                    potentialConflictingValues.add(currentValues.size() > 1 ?
                            String.format("{%s}",String.join(", ", currentValues)) : currentValues.get(0));
                }
            }
            if (potentialConflictingValues.size() > 1) {
                updateErrorMessageMap(fieldErrorMessages, entry, CONFLICTING_VALUES, String.join(", ",
                        potentialConflictingValues));
            }
        }
        // Finally, register the problems directly on the annotation element
        if (!fieldErrorMessages.isEmpty()) {
            PsiNameValuePair[] nameValuePairs = annotation.getParameterList().getAttributes();
            for (PsiNameValuePair nameValuePair : nameValuePairs) {
                if (nameValuePair.getNameIdentifier() != null) {
                    if (fieldErrorMessages.containsKey(nameValuePair.getName())) {
                        LocalQuickFix quickFix = null;
                        if (missingValuePrefixes.containsKey(nameValuePair.getName())) {
                            quickFix = new AddMissingValueTemplateQuickFix(missingValuePrefixes.get(nameValuePair.getName()));
                        }
                        for (String errorMessage : fieldErrorMessages.get(nameValuePair.getName())) {
                            if (holder != null) {
                                holder.registerProblem(nameValuePair.getNameIdentifier(), errorMessage,
                                        ProblemHighlightType.GENERIC_ERROR, quickFix);
                            }
                        }
                    }
                    if (fieldErrorMessages.containsKey(nameValuePair.getName())) {
                        errorFields.add(nameValuePair.getName());
                    }
                }
            }
        }
        return fieldErrorMessages.isEmpty();
    }

    private static void updateErrorMessageMap(HashMap<String, ArrayList<String>> fieldErrorMessages,
                                              Map.Entry<String, String[][]> entry, String errorMessageTemplate,
                                              String join) {
        String errorMessage = String.format(errorMessageTemplate, join);
        if (fieldErrorMessages.containsKey(entry.getKey())) {
            fieldErrorMessages.get(entry.getKey()).add(errorMessage);
        } else {
            ArrayList<String> newErrorMessageList = new ArrayList<>();
            newErrorMessageList.add(errorMessage);
            fieldErrorMessages.put(entry.getKey(), newErrorMessageList);
        }
    }

    public static boolean isInTestFile(PsiElement element) {
        PsiDirectory directory = element.getContainingFile().getContainingDirectory();
        while (directory != null) {
            if ("test".equals(directory.getName()) || "androidTest".equals(directory.getName())) {
                return true;
            }
            directory = directory.getParentDirectory();
        }
        return false;
    }

    public static boolean containsConflictAnnotation(PsiAnnotation[] annotations) {
        boolean hasIgnoreSource = false;
        boolean hasIgnoreSink = false;
        boolean hasSource = false;
        boolean hasSink = false;

        for (PsiAnnotation annotation : annotations) {
            CoconutAnnotationType coconutAnnotationType = getAnnotationTypeFromPsiAnnotation(annotation);
            if (coconutAnnotationType == CoconutAnnotationType.NotPersonalDataAccess) {
                hasIgnoreSource = true;
            } else if (coconutAnnotationType == NotPersonalDataTransmission) {
                hasIgnoreSink = true;
            } else if (coconutAnnotationType == CoconutAnnotationType.DataAccess ||
                    coconutAnnotationType == CoconutAnnotationType.MultipleAccess) {
                hasSource = true;
            } else if (coconutAnnotationType == CoconutAnnotationType.DataTransmission ||
                    coconutAnnotationType == CoconutAnnotationType.MultipleTransmission) {
                hasSink = true;
            }
        }
        return (hasIgnoreSource && hasSource) || (hasIgnoreSink && hasSink);
    }

    public static ArrayList<PsiAnnotation> unpackListAnnotation(PsiAnnotation multipleAnnotation) {
        return unpackListAnnotation(multipleAnnotation, null);
    }

    public static ArrayList<PsiAnnotation> unpackListAnnotation(PsiAnnotation multipleAnnotation, String key) {
        ArrayList<PsiAnnotation> annotations = new ArrayList<>();
        if (multipleAnnotation == null) {
            return annotations;
        }
        PsiAnnotationMemberValue value = null;
        if (key == null) {
            value = multipleAnnotation.getParameterList().getAttributes()[0].getValue();
        } else {
            value = multipleAnnotation.findAttributeValue(key);
//            PsiNameValuePair[] nameValuePairs = multipleAnnotation.getParameterList().getAttributes();
//            for (PsiNameValuePair nameValuePair : nameValuePairs) {
//                if (key.equals(nameValuePair.getName())) {
//                    value = nameValuePair.getValue();
//                    break;
//                }
//            }
        }
        if (value != null) {
            if (value instanceof PsiArrayInitializerMemberValue) {
                PsiAnnotationMemberValue[] elements = ((PsiArrayInitializerMemberValue) value).getInitializers();
                for (PsiAnnotationMemberValue element : elements) {
                    if (element instanceof PsiAnnotation) {
                        annotations.add((PsiAnnotation) element);
                    }
                }
            } else {
                if (value instanceof PsiAnnotation) {
                    annotations.add((PsiAnnotation) value);
                }
            }
        }
        return annotations;
    }

    public static ArrayList<String> extractSourceIdsFromSink(PsiAnnotation annotation, boolean hasQuote) {
        ArrayList<PsiAnnotation> sources = CodeInspectionUtil.unpackListAnnotation(annotation, fieldDataTransmissionAccessIdList);
        ArrayList<String> sourceIds = new ArrayList<>();
        for (PsiAnnotation source : sources) {
            AnnotationHolder sourceHolder = CodeInspectionUtil.parseAnnotation(source);
            String sourceId = sourceHolder.getDataId(hasQuote);
            sourceIds.add(sourceId);
        }
        return sourceIds;
    }


    public static Set<String> extractSourceDataGroupsFromSink(AnnotationMetaData annotationMetaData) {
        ArrayList<PsiAnnotation> sources = CodeInspectionUtil.unpackListAnnotation(
                (PsiAnnotation) annotationMetaData.psiAnnotationPointer.getElement(),
                fieldDataTransmissionAccessIdList);
        Set<String> sourceDataTypes = new HashSet<>();
        for (PsiAnnotation source : sources) {
            AnnotationHolder sourceHolder = CodeInspectionUtil.parseAnnotation(source);
            String dataType = sourceHolder.getDataType();
            sourceDataTypes.add(dataType);
        }
        return sourceDataTypes;
    }

    public static void fillAnnotationList(ArrayList<AnnotationHolder> annotationHolderInstanceArrayList,
                                          ArrayList<PsiAnnotation> annotationInstanceArrayList,
                                          ArrayList<AnnotationSpeculation> annotationSpeculationArrayList,
                                          AnnotationHolder annotationHolder, PsiAnnotation annotation,
                                          AnnotationSpeculation[] speculations) {
        for (int i = 0 ; i < speculations.length ; ++i) {
            annotationHolderInstanceArrayList.add(annotationHolder);
            annotationInstanceArrayList.add(annotation);
        }
        Collections.addAll(annotationSpeculationArrayList, speculations);
    }

    public static boolean isMatchedSource(AnnotationHolder speculationHolder, PsiAnnotation annotationInstance) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.parseAnnotation(annotationInstance);
        return speculationHolder.mAnnotationType == annotationHolder.mAnnotationType &&
                speculationHolder.getDataType().equals(annotationHolder.getDataType());
    }

    public static String getCallerString(PsiElement element) {
        PsiMethod method = null;
        if (element instanceof PsiMethodCallExpression) {
            method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        } else if (element instanceof PsiMethod) {
            method = (PsiMethod) element;
        }
        if (method == null) {
            return element.getText();
        }
        MethodSignature methodSignature = parseMethod(method);
        return methodSignature.myFullMethodName + ": " + element.getText();

    }

    public static String getTargetVariableNameString(PsiElement dataEntity) {
        String targetString = "";
        if (dataEntity instanceof PsiField) {
            targetString = String.format("field \"%s\"", ((PsiField) dataEntity).getName());
        } else if (dataEntity instanceof PsiParameter) {
            targetString = String.format("parameter \"%s\"", ((PsiParameter) dataEntity).getName());
        } else if (dataEntity instanceof PsiLocalVariable) {
            targetString = String.format("local variable \"%s\"", ((PsiLocalVariable) dataEntity).getName());
        } else {
            return "";
        }
        return targetString;
    }

    public static boolean isPrivacyAnnotation(PsiAnnotation annotation) {
        return getAnnotationTypeFromPsiAnnotation(annotation) != null;
    }

    public static String getApiCallInstanceText(SensitiveAPIInstance myAPICallInstance) {
        SensitiveAPI sensitiveAPI = myAPICallInstance.sensitiveAPI;
        PsiElement psiElement = myAPICallInstance.getPsiElementPointer().getElement();
        if (psiElement == null) {
            return "(invalid code)";
        }
        String apiText;
        String methodName;
        String parameterText;
        if (sensitiveAPI.personalDataAPIType == PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_DEFINITION_PARAMETER) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            methodName = psiMethod.getName();
            parameterText = Arrays.stream(psiMethod.getParameters()).map(
                            p -> p.getType().toString().replaceAll(".*:", "") + " " + p.getName())
                    .collect(Collectors.joining(", "));
        } else {
            PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) psiElement;
            methodName = psiMethodCallExpression.getMethodExpression().getText();
            parameterText = Arrays.stream(psiMethodCallExpression.getArgumentList().getExpressions()).map(
                    a -> a.getText()).collect(Collectors.joining(", "));
        }
        apiText = String.format("%s(%s)", methodName, parameterText).replace("\n", "").replaceAll(" +", "");
        if (apiText.length() > 80) {
            apiText = String.format("%s(...)", methodName).replace("\n", "").replaceAll(" +", "");
        }
        return apiText;
    }

    //
  // Examples of checkMatchedMethodCall:
  //
  //if (checkMatchedMethodCall(expression, "java.lang.String", "android.provider.Settings.Secure.getString")) {
  //  LOG.info("String android.provider.Settings.Secure.getString is called");
  //}
  //if (checkMatchedMethodCall(expression, "double", "me.tianshili.stepcounter.MainActivity.calculateMedian")) {
  //  LOG.info("double me.tianshili.stepcounter.MainActivity.calculateMedian is called");
  //}
  //if (checkMatchedMethodCall(expression, "com.google.android.gms.ads.AdRequest", "com.google.android.gms.ads.AdRequest.Builder.build")) {
  //  LOG.info("com.google.android.gms.ads.AdRequeset com.google.android.gms.ads.AdRequest.Builder.build is called");
  //}
  //if (checkMatchedMethodCall(expression, "void", "com.google.android.gms.ads.AdView.loadAd")) {
  //  LOG.info("void com.google.android.gms.ads.AdView.loadAd is called");
  //}
    public static class MethodSignature {
        public String myReturnValueTypeCanonicalText;
        public String myFullMethodName;
        PsiType [] myParameterTypeList;
        PsiExpression [] myParameterValueExpressionList;

        int status = 0;
        static final int METHOD_CALL_SIGNATURE = 1;
        static final int METHOD_DEFINICTION_SIGNATURE = 2;

        MethodSignature() {
            myReturnValueTypeCanonicalText = null;
            myFullMethodName = null;
            myParameterValueExpressionList = null;
        }
        MethodSignature(String returnValueTypeCanonicalText,
                        String fullMethodName,
                        PsiType [] parameterTypeCanonicalTextList,
                        PsiExpression [] parameterValueTextList) {
            myReturnValueTypeCanonicalText = returnValueTypeCanonicalText;
            myFullMethodName = fullMethodName;
            myParameterTypeList = parameterTypeCanonicalTextList;
            myParameterValueExpressionList = parameterValueTextList;
            status = METHOD_CALL_SIGNATURE;
        }
        MethodSignature(String returnValueTypeCanonicalText,
                        String fullMethodName,
                        PsiType [] parameterTypeCanonicalTextList) {
            myReturnValueTypeCanonicalText = returnValueTypeCanonicalText;
            myFullMethodName = fullMethodName;
            myParameterTypeList = parameterTypeCanonicalTextList;
            myParameterValueExpressionList = null;
            status = METHOD_DEFINICTION_SIGNATURE;
        }

        boolean isValid() {
            return myReturnValueTypeCanonicalText != null && myFullMethodName != null
                    && (myParameterValueExpressionList != null || status == METHOD_DEFINICTION_SIGNATURE)
                    && myParameterTypeList != null;
        }
        boolean isMatchedMethodCall(String returnValueTypeCanonicalTextPattern,
                                    String fullMethodNamePattern) {
            if (!isValid()) {
                return false;
            }
            return Pattern.matches(returnValueTypeCanonicalTextPattern, myReturnValueTypeCanonicalText) &&
                    Pattern.matches(fullMethodNamePattern, myFullMethodName);
        }
        boolean isMatchedMethodCall(String returnValueTypeCanonicalTextPattern,
                                    String fullMethodNamePattern,
                                    String[] parameterTypeCanonicalTextPatternList) {
            if (!isValid()) {
                return false;
            }
            if (!isMatchedMethodCall(returnValueTypeCanonicalTextPattern, fullMethodNamePattern)) {
                return false;
            }
            // parameterTypeCanonicalTextPatternList could be a sublist of the full list
            if (parameterTypeCanonicalTextPatternList.length > myParameterTypeList.length) {
                return false;
            }
            for (int i = 0 ; i < parameterTypeCanonicalTextPatternList.length ; ++i) {
                if (myParameterTypeList[i] == null) {
                    continue;
                }
                if (!checkForParameterTypeMatch(parameterTypeCanonicalTextPatternList[i], myParameterTypeList[i])) {
                    return false;
                }
            }
            return true;
        }

        /**
         *
         *
         * @param parameterTypeCanonicalTextPattern
         * @param myParameterType
         * @return
         */
        boolean checkForParameterTypeMatch(String parameterTypeCanonicalTextPattern, PsiType myParameterType) {
            boolean match = false;
            if (Pattern.compile(parameterTypeCanonicalTextPattern, Pattern.DOTALL).matcher(myParameterType.getCanonicalText()).matches()) {
                match = true;
            } else {
                for (PsiType superType : myParameterType.getSuperTypes()) {
                    if (checkForParameterTypeMatch(parameterTypeCanonicalTextPattern, superType)) {
                        match = true;
                    }
                }
            }
            return match;
        }

        boolean isMatchedMethodCall(String returnValueTypeCanonicalTextPattern,
                                    String fullMethodNamePattern,
                                    String[] parameterValueTypePatternList,
                                    String[] parameterValueTextPatternList) {
            if (!isValid()) {
                return false;
            }
            if (!isMatchedMethodCall(returnValueTypeCanonicalTextPattern, fullMethodNamePattern, parameterValueTypePatternList)) {
                return false;
            }
            // parameterValueTextPatternList could be a sublist of the full list
            if (parameterValueTextPatternList.length > myParameterValueExpressionList.length) {
                return false;
            }
            for (int i = 0 ; i < parameterValueTextPatternList.length ; ++i) {
                Pattern p = Pattern.compile(parameterValueTextPatternList[i], Pattern.DOTALL);
                if (!p.matcher(myParameterValueExpressionList[i].getText()).matches()) {
                    return false;
                }
            }
            return true;
        }
    }

    public static MethodSignature parseMethod(PsiMethod method) {
        String mReturnValueTypeCanonicalText;
        String mFullMethodName;
        PsiType methodReturnValueType = method.getReturnType();
        if (methodReturnValueType != null) {
            mReturnValueTypeCanonicalText = methodReturnValueType.getCanonicalText();
        } else {
            return new MethodSignature();
        }
        PsiClass methodContainingClass = method.getContainingClass();
        if (methodContainingClass == null) {
            return new MethodSignature();
        }
        mFullMethodName = methodContainingClass.getQualifiedName() + "." + method.getName();
        ArrayList<PsiType> parameterTypeList = new ArrayList<>();
        PsiParameter [] parameters = method.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
            parameterTypeList.add(parameter.getType());
        }
        return new MethodSignature(mReturnValueTypeCanonicalText, mFullMethodName,
                parameterTypeList.toArray(new PsiType[0]));
    }

    static private MethodSignature parseMethodCall(PsiMethodCallExpression expression) {
        String mReturnValueTypeCanonicalText;
        String mPackageAndClassName;
        String mMethodInvocationText;
        String mFullMethodName;
        PsiType expressionType = expression.getType();
        if (expressionType != null) {
            mReturnValueTypeCanonicalText = expressionType.getCanonicalText();
        } else {
            return new MethodSignature();
        }
        // method call expression should have two children: the reference expression of the method and the expression list of the parameter list
        PsiElement[] expressionChildren = expression.getChildren();
        if (expressionChildren.length != 2) {
            return new MethodSignature();
        }
        PsiElement caller = expressionChildren[0].getChildren()[0];
        if (caller instanceof PsiReferenceExpressionImpl) {
            PsiReferenceExpressionImpl callObject = (PsiReferenceExpressionImpl)caller;
            PsiType callObjectType = callObject.getType();
            if (callObjectType != null) {
                mPackageAndClassName = callObjectType.getCanonicalText();
            }
            else {
                // if it's static class
                assert (!callObject.textMatches(""));
                mPackageAndClassName = callObject.getCanonicalText();
            }
        } else if (caller instanceof PsiSuperExpressionImpl) { // e.g. super.onCreate()
            PsiSuperExpressionImpl callSuperExpression = (PsiSuperExpressionImpl)caller;
            mPackageAndClassName = callSuperExpression.getType().getCanonicalText();
        } else if (caller instanceof PsiMethodCallExpressionImpl) { // called by method call return val
            PsiMethodCallExpressionImpl callMethodReturnValue = (PsiMethodCallExpressionImpl)caller;
            mPackageAndClassName = callMethodReturnValue.getType().getCanonicalText();
        } else if (caller instanceof PsiNewExpressionImpl) { // called by new expression return val
            PsiNewExpressionImpl callNewExpression = (PsiNewExpressionImpl) caller;
            mPackageAndClassName = callNewExpression.getType().getCanonicalText();
        } else if (caller instanceof PsiParenthesizedExpressionImpl) {
            PsiParenthesizedExpressionImpl callParenthesizedExpression = (PsiParenthesizedExpressionImpl) caller;
            mPackageAndClassName = callParenthesizedExpression.getType().getCanonicalText();
        } else if (caller instanceof PsiThisExpressionImpl) {
            PsiThisExpressionImpl callThisExpression = (PsiThisExpressionImpl) caller;
            mPackageAndClassName = callThisExpression.getType().getCanonicalText();
        } else {
            //If the method call doesn't have an object or a static class it's being called on (such as if it's
            //defined within the class), enter these branches
            int expressionFirstChildChildrenLen = expressionChildren[0].getChildren().length;
            if (expressionFirstChildChildrenLen == 2 &&
                    PsiTreeUtil.getParentOfType(expression, PsiClass.class) != null &&
                    PsiTreeUtil.getParentOfType(expression, PsiClass.class).getName() != null) {
                //Gets the full package and class name of the containing class (such as "com.coconuttest.tyu91.coconuttest.CameraByIntentTestActivity")
                mPackageAndClassName = ((PsiJavaFile)(PsiTreeUtil.getParentOfType(expression, PsiFile.class))).getPackageName() + "." +
                        PsiTreeUtil.getParentOfType(expression, PsiClass.class).getName();
            //If the method is called from a subfield from within the class (such as if we declare a View.OnClickListener
            //and call the method within the listener, we have to get out of the listener to find the correct package and class
            } else if (expressionFirstChildChildrenLen == 2 &&
                    PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiClass.class) != null &&
                    PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiClass.class).getName() != null) {
                mPackageAndClassName = ((PsiJavaFile) (PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiFile.class))).getPackageName() + "." +
                        PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiClass.class).getName();
            } else {
                return new MethodSignature();
            }
        }
        //One thing we don't handle is if the method is defined in a super class. If a method is defined in the super class
        //and then called in the subclass without using an object or static call (so like just calling startActivityForResult()
        //instead of Activity.startActivityForResult(), which is common), then we don't identify that.
        //TODO: Add in the feature described by the above comment
        mMethodInvocationText = ((PsiReferenceExpressionImpl)expressionChildren[0]).getCanonicalText();
        String[] mMethodInvocationTokens = mMethodInvocationText.split("\\.");
        mFullMethodName = mPackageAndClassName + "." + mMethodInvocationTokens[mMethodInvocationTokens.length - 1];
        PsiExpression[] parameterValueExpressionList = expression.getArgumentList().getExpressions();
        PsiType[] parameterTypeList = expression.getArgumentList().getExpressionTypes();
        return new MethodSignature(mReturnValueTypeCanonicalText, mFullMethodName,
                parameterTypeList, parameterValueExpressionList);
    }

    // also compare parameter types and values
    static public boolean checkMatchedMethodCall(PsiMethodCallExpression expression,
                                                 String returnValueTypeCanonicalTextPattern,
                                                 String fullMethodNamePattern,
                                                 String [] parameterTypeCanonicalTextPatternList,
                                                 String [] parameterValueTextPatternList) {
        MethodSignature methodSignature = parseMethodCall(expression);
        return methodSignature.isMatchedMethodCall(returnValueTypeCanonicalTextPattern, fullMethodNamePattern,
                parameterTypeCanonicalTextPatternList, parameterValueTextPatternList);
    }

    static public boolean checkMatchedMethod(PsiMethod method,
                                             String returnValueTypeCanonicalTextPattern,
                                             String fullMethodNamePattern,
                                             String [] parameterTypeCanonicalTextPatternList) {
        MethodSignature methodSignature = parseMethod(method);
        return methodSignature.isMatchedMethodCall(returnValueTypeCanonicalTextPattern, fullMethodNamePattern, parameterTypeCanonicalTextPatternList);
    }

  static public PsiAnnotation getAnnotationByType(PsiElement targetVariable, CoconutAnnotationType type) {
      PsiModifierList modifierList;
      if (targetVariable instanceof PsiModifierList) {
          modifierList = (PsiModifierList) targetVariable;
      } else {
          modifierList = PsiTreeUtil.getChildOfType(targetVariable, PsiModifierList.class);
      }
      if (modifierList == null) {
          return null;
      }
      for (PsiAnnotation annotation : modifierList.getAnnotations()) {
          if (type == getAnnotationTypeFromPsiAnnotation(annotation)) {
              return annotation;
          }
      }
      return null;
  }

  static public PsiAnnotation [] getAllAnnotations(PsiElement targetVariable) {
      PsiModifierList prevModifierList = PsiTreeUtil.getChildOfType(targetVariable, PsiModifierList.class);
      if (prevModifierList == null) {
          return new PsiAnnotation[0];
      }
      return prevModifierList.getAnnotations();
  }

  static public CoconutAnnotationType getAnnotationTypeFromPsiAnnotation(PsiAnnotation annotation) {
      String typeString = annotation.getChildren()[1].getText();
      CoconutAnnotationType annotationType = null;
      try {
          annotationType = CoconutAnnotationType.valueOf(typeString);
      } catch (IllegalArgumentException ignored) {
      }
      return annotationType;
  }

    /**
     * @param annotation
     * @return
     */
  @NotNull
  static public AnnotationHolder parseAnnotation(PsiAnnotation annotation) {
      CoconutAnnotationType type = getAnnotationTypeFromPsiAnnotation(annotation);
      return new AnnotationHolder(type, annotation);
  }

  public static boolean isIncompleteValue(String value) {
      return Pattern.matches(".*UNKNOWN", value) || Pattern.matches("\" *\"", value) ||
              Pattern.matches(" *", value) || resourceStringPrefix.equals(value) ||
              sourceDataStringPrefix.equals(value);
  }

    /**
     * Creates an empty annotation holder based on the given parameters
     *
     * @param type The type of annotation a holder is needed for
     * @return An empty annotation holder for the given type
     */
    @NotNull
    public static AnnotationHolder createEmptyAnnotationHolderByType(CoconutAnnotationType type) {
        return new AnnotationHolder(type);
    }

    public static int getElementLineNumber(PsiElement element) {
        return getElementLineNumber(element, true);
    }

    public static int getElementLineNumber(PsiElement element, boolean isStart) {
        PsiFile containingFile = element.getContainingFile();
        Project project = containingFile.getProject();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(containingFile);
        assert document != null;
        int textOffset;
        if (isStart) {
            textOffset = element.getTextRange().getStartOffset();
        } else {
            textOffset = element.getTextRange().getEndOffset();
        }
        return document.getLineNumber(textOffset) + 1;
    }


    public static ArrayList<PsiElement> getGlobalAndLocalRefExpsBeforeMethodExp(PsiReferenceExpression expression,
                                                                                PsiMethodCallExpression targetMethodExpression) {
        PsiElement expressionDeclaration = expression.resolve();
        if (expressionDeclaration == null) {
            return new ArrayList<>();
        }
        ArrayList<PsiElement> matchedReferenceExpressions = new ArrayList<>();
        matchedReferenceExpressions.add(expressionDeclaration);
        Collection<PsiReference> allReferences = ReferencesSearch.search(expressionDeclaration).findAll();
        for (PsiReference reference : allReferences) {
            PsiElement referenceContainingMethod = PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class);
            PsiElement targetContainingMethod = PsiTreeUtil.getParentOfType(targetMethodExpression, PsiMethod.class);
            if (referenceContainingMethod == null) {
                matchedReferenceExpressions.add(reference.getElement());
            } else if (reference.getElement().getTextOffset() < targetMethodExpression.getTextOffset() ||
                    !referenceContainingMethod.equals(targetContainingMethod)) {
                matchedReferenceExpressions.add(reference.getElement());
            }
        }
        return matchedReferenceExpressions;
    }
}
