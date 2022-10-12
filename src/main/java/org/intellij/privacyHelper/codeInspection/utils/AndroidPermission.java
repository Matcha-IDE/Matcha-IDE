package org.intellij.privacyHelper.codeInspection.utils;

/**
 * Created by tianshi on 4/26/17.
 */
public enum AndroidPermission {
        // dangerous permissions
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION,
        READ_CALENDAR,
        WRITE_CALENDAR,
        CAMERA,
        READ_CONTACTS,
        WRITE_CONTACTS,
        GET_ACCOUNTS,
        RECORD_AUDIO,
        READ_PHONE_STATE,
        CALL_PHONE,
        READ_CALL_LOG,
        WRITE_CALL_LOG,
        ADD_VOICEMAIL,
        USE_SIP,
        PROCESS_OUTGOING_CALLS,
        BODY_SENSORS,
        SEND_SMS,
        RECEIVE_SMS,
        READ_SMS,
        RECEIVE_WAP_PUSH,
        RECEIVE_MMS,
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE,
        // other permissions
        INTERNET,
        ACCESS_WIFI_STATE,
        BLUETOOTH,
        NOTIFICATION, ACCESSIBILITY;
}
