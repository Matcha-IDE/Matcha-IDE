package org.intellij.privacyHelper.codeInspection.utils;

import org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

/**
 * Created by tianshi on 4/26/17.
 */
public class TargetValueFromReturnValueAPI extends SensitiveAPI {

    TargetValueFromReturnValueAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                  boolean isThirdPartyAPI, String thirdPartyName,
                                  PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_FROM_RETURN_VALUE,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

    TargetValueFromReturnValueAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                  String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                  boolean isThirdPartyAPI, String thirdPartyName,
                                  PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_FROM_RETURN_VALUE,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern, parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

    TargetValueFromReturnValueAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                  boolean isThirdPartyAPI, String thirdPartyName,
                                  PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_FROM_RETURN_VALUE,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }


    TargetValueFromReturnValueAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                  String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                  boolean isThirdPartyAPI, String thirdPartyName,
                                  PersonalDataAPIAnnotationUtil [] configs, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_FROM_RETURN_VALUE,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern, parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }
}