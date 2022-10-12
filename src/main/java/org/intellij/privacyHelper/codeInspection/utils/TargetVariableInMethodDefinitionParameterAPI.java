package org.intellij.privacyHelper.codeInspection.utils;

import org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

public class TargetVariableInMethodDefinitionParameterAPI extends SensitiveAPI {


    TargetVariableInMethodDefinitionParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                                 String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                                 AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                                 boolean isThirdPartyAPI, String thirdPartyName,
                                                 PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                                 boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_DEFINITION_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

    TargetVariableInMethodDefinitionParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                                 AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                                 boolean isThirdPartyAPI, String thirdPartyName,
                                                 PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                                                 boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_DEFINITION_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }


    TargetVariableInMethodDefinitionParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                                 String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                                 AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                                 boolean isThirdPartyAPI, String thirdPartyName,
                                                 PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                                                 boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_DEFINITION_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

}
