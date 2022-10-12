package org.intellij.privacyHelper.codeInspection.utils;

import org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;


public class TargetVariableInCallerAPI extends SensitiveAPI {


    /**
     *
     * @param displayName string representing the name of the given API
     * @param fullAPIName regex string for the complete method name, including the package name of the library, for template matching
     * @param returnValueTypeCanonicalTextPattern regex string for the complete name of the return value type of the method call for template matching
     * @param parameterTypeCanonicalTextRestriction the types that must be present in a given parameter for this API
     * @param parameterValueTextRestriction any text that must be present in a given parameter for this API
     * @param requiredPermissions permissions required for the method call
     * @param libImplicitVoluntaryPermissions
     * @param isThirdPartyAPI
     * @param thirdPartyName
     * @param config
     * @param targetVariableTracker
     * @param isPersonalDataSource
     * @param isPersonalDataSink
     */
    TargetVariableInCallerAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                              String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                              AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                              PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                              boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_CALLER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }
}
