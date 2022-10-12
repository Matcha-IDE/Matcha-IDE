package org.intellij.privacyHelper.codeInspection.utils;

import org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

/**
 * Created by tianshi on 2/2/18.
 */
public class TargetVariableInMethodCallParameterAPI extends SensitiveAPI {

    public TargetVariableInMethodCallParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                           AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                           PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                           boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_CALL_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

    TargetVariableInMethodCallParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                           String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                           AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                           PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                           boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_CALL_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

    public TargetVariableInMethodCallParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                                  PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_CALL_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

    TargetVariableInMethodCallParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                           String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                           AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                           PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                                           boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_CALL_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

}
