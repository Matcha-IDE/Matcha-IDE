package org.intellij.privacyHelper.codeInspection.utils;

/**
 * Created by tianshi on 11/13/17.
 */
public enum PersonalDataAPIType {
    PERSONAL_DATA_FROM_RETURN_VALUE,
    PERSONAL_DATA_FROM_CALLBACK,
    PERSONAL_DATA_FROM_INTENT,
    PERSONAL_DATA_IN_METHOD_CALL_PARAMETER,
    PERSONAL_DATA_IN_METHOD_DEFINITION_PARAMETER,
    PERSONAL_DATA_IN_CALLER
}
