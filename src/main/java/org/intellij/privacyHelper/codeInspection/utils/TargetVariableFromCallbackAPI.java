package org.intellij.privacyHelper.codeInspection.utils;

import org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

/**
 * Created by tianshi on 11/13/17.
 */
public class TargetVariableFromCallbackAPI extends SensitiveAPI {


    TargetVariableFromCallbackAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                  String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                  PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_FROM_CALLBACK,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

    TargetVariableFromCallbackAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                  PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_FROM_CALLBACK,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }


    TargetVariableFromCallbackAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalTextPattern,
                                  String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                  PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        super(PersonalDataAPIType.PERSONAL_DATA_FROM_CALLBACK,
                displayName, fullAPIName, returnValueTypeCanonicalTextPattern,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, configs, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink, accessType);
    }

}
