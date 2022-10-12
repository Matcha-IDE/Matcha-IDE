package org.intellij.privacyHelper.codeInspection.utils;

import org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils.*;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.*;

import java.util.ArrayList;


/**
 * Contains an array of all Personal Data Source API calls that are monitered by Coconut.
 *
 * The list is accessible through two get methods that each return an array of type SensitiveAPI
 * based on given parameters.
 *
 * Created by tianshi on 4/27/17.
 */
public class  PersonalDataSourceAPIList {

    static SensitiveAPI[] sensitiveAPIs = {
            // TODO: (long-term) for all resources accessed via android\.content\.ContentResolver\.query, check the first parameter to know what type it accessed exactly.
            // TODO: (long-term) automatically extract/require the dev to fill in what field they actually use (e.g. for SmsSource list, they can get data, data sent, smsId, address, type, content, seen, read)

            // TODO (Tiffany): add an entry to capture any access to Calendar data:
            // Example: context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null, null, null)
            // Use TargetValueFromReturnValueAPI class to define the API.
            // The second (fullAPIName) and the third (returnValueTypeCanonicalText) are regex strings for template
            // matching. The former represents the complete method name, including the package name of the library, and
            // the latter represents the complete name of the return value type.
            // Because we also needs the first parameter (CalendarContract.Events.CONTENT_URI) to determine that it is
            // accessing the calendar data, you'll need to specify some constraints for the parameters with regex. See
            // how the Android ID is handled as a reference.
            // Use CalendarAnnotationUtil for the annotation config field. No need to modify that class for now.
            // Use ReturnValueTargetVariableTracker for the targetVariableTracker.
            new TargetValueFromReturnValueAPI(
                    "ContentResolver.query (Calendar)",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{ "android\\.net\\.Uri", ".*", ".*", ".*", ".*"},
                    new String[]{"CalendarContract.*CONTENT_URI.*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.READ_CALENDAR}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Calendar_CalendarEvents),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "ContentResolver.query (SMS)",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{ "android\\.net\\.Uri", ".*", ".*", ".*", ".*"},
                    new String[]{"(Telephony.*CONTENT_URI.*)|(Uri\\.parse\\((.*)sms(.*))", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.READ_SMS}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Messages_SmsOrMms),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "ContentResolver.query (Contacts)",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{ "android\\.net\\.Uri", ".*", ".*", ".*", ".*"},
                    new String[]{"ContactsContract.*CONTENT_URI.*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.READ_CONTACTS}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Contacts_Contacts),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "ContentResolver.query (Call logs)",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{ "android\\.net\\.Uri", ".*", ".*", ".*", ".*"},
                    new String[]{"Calls.*CONTENT_URI.*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.READ_CALL_LOG}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Contacts_Contacts),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "getLastOutgoingCall",
                    "android\\.provider\\.CallLog\\.Calls\\.getLastOutgoingCall",
                    "java\\.lang\\.String",
                    new AndroidPermission[][]{{AndroidPermission.READ_CALL_LOG}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Contacts_Contacts),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "LocationManager.getLastKnownLocation",
                    "android\\.location\\.LocationManager\\.getLastKnownLocation",
                    "android\\.location\\.Location",
                    new AndroidPermission[][]{{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new ReturnValueTargetVariableTracker(),
                    true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),
            new TargetValueFromReturnValueAPI(
                    "FusedLocationProviderApi.getLastLocation",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.getLastLocation",
                    "android\\.location\\.Location",
                    new AndroidPermission[][]{{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new ReturnValueTargetVariableTracker(),
                    true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),
            new TargetVariableFromCallbackAPI(
                    "FusedLocationProviderClient.getLastLocation",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderClient\\.getLastLocation",
                    ".*",
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new GMSTaskLocationCallbackTargetVariableTracker(),
                    true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // public abstract Location getLastLocation (GoogleApiClient client) The best accuracy available while respecting the location permissions will be returned.
            // TODO: (urgent) Android ID has different scope and resettability before/after Android 8.0
            new TargetVariableFromCallbackAPI("LocationManager.requestLocationUpdates",
                    "android\\.location\\.LocationManager\\.requestLocationUpdates",
                    "void",
                    new String[]{"java\\.lang\\.String", ".*", ".*", "android\\.location\\.LocationListener"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(3),true, false,
                    AccessType.RECURRING_COLLECTION
            ), // requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener)
            new TargetVariableFromCallbackAPI("LocationManager.requestLocationUpdates",
                    "android\\.location\\.LocationManager\\.requestLocationUpdates",
                    "void",
                    new String[]{".*", ".*", "android\\.location\\.Criteria", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(3),true, false,
                    AccessType.RECURRING_COLLECTION
            ), // requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("LocationManager.requestLocationUpdates",
                    "android\\.location\\.LocationManager\\.requestLocationUpdates",
                    "void",
                    new String[]{"java\\.lang\\.String", ".*", ".*", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(3),true, false,
                    AccessType.RECURRING_COLLECTION
            ), // requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("LocationManager.requestSingleUpdate",
                    "android\\.location\\.LocationManager\\.requestSingleUpdate",
                    "void",
                    new String[]{"java\\.lang\\.String", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(1),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // requestSingleUpdate(String provider, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("LocationManager.requestSingleUpdate",
                    "android\\.location\\.LocationManager\\.requestSingleUpdate",
                    "void",
                    new String[]{"android\\.location\\.Criteria", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(1),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("FusedLocationProviderClient.requestLocationUpdates",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderClient\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*LocationRequest", ".*LocationCallback", ".*"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new GMSLocationCallbackTargetVariableTracker(1),true, false,
                    AccessType.RECURRING_COLLECTION
            ), // FusedLocationProviderClient.requestLocationUpdates(LocationRequest request, LocationCallback callback, Looper looper)
//            new TargetVariableFromCallbackAPI(), // FusedLocationProviderClient.requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent)
            new TargetVariableFromCallbackAPI("FusedLocationProviderApi.requestLocationUpdates",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*GoogleApiClient", ".*LocationRequest", ".*LocationListener"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new GMSLocationListenerTargetVariableTracker(2),true, false,
                    AccessType.RECURRING_COLLECTION
            ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener)
            new TargetVariableFromCallbackAPI("FusedLocationProviderApi.requestLocationUpdates",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*GoogleApiClient", ".*LocationRequest", ".*LocationCallback", ".*"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new GMSLocationCallbackTargetVariableTracker(2),true, false,
                    AccessType.RECURRING_COLLECTION
            ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationCallback callback, Looper looper)
            new TargetVariableFromCallbackAPI("FusedLocationProviderApi.requestLocationUpdates",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*GoogleApiClient", ".*LocationRequest", ".*LocationListener", ".*"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new GMSLocationListenerTargetVariableTracker(2),true, false,
                    AccessType.RECURRING_COLLECTION
            ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener, Looper looper)
            /*
        new TargetVariableFromCallbackAPI(
                "request location update",
                "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                ".*",
                new String[]{".*GoogleApiClient", ".*LocationRequest", ".*PendingIntent"},
                new String[]{".*", ".*", ".*"},
                new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                new AndroidPermission[][]{}, false, null,
                CoconutAnnotationType.LocationSource,
                new PersonalDataGroup[] {PersonalDataGroup.Location},
                new FusedLocationProviderLocationRequestBasedAnnotationUtil(1),
                new GMSLocationListenerTargetVariableTracker(2),true, false
        ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, PendingIntent callbackIntent)
        */
            //TODO: (long-term) String key = LocationManager.KEY_PROXIMITY_ENTERING; Boolean entering = intent.getBooleanExtra(key, false); when addProximityAlert is called

            new TargetVariableInMethodCallParameterAPI(
                    "ExifInterface.getLatLong",
                    ".*ExifInterface.getLatLong",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.Location_PreciseLocation,
                                    PersonalDataGroup.Location_ApproximateLocation)
                    },
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "Android ID",
                    "android\\.provider\\.Settings\\.Secure\\.getString",
                    "java\\.lang\\.String",
                    new String[]{ ".*", "java\\.lang\\.String"},
                    new String[]{".*", ".*ANDROID_ID"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getString(Secure.ANDROID_ID)
            new TargetValueFromReturnValueAPI(
                    "Android ID",
                    "android\\.provider\\.Settings\\.Secure\\.getString",
                    "java\\.lang\\.String",
                    new String[]{ ".*", "java\\.lang\\.String"},
                    new String[]{".*", "\"android_id\""},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getString("android_id")
            new TargetValueFromReturnValueAPI(
                    "UUID",
                    "java\\.util\\.UUID\\.randomUUID",
                    "java\\.util\\.UUID",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // randomUUID()
            new TargetValueFromReturnValueAPI( // Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones
                    "IMEI for GSM phone, MEID for CDMA phones",
                    "android\\.telephony\\.TelephonyManager\\.getDeviceId",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getDeviceId ()
            new TargetValueFromReturnValueAPI( // Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones
                    "IMEI for GSM phone, MEID for CDMA phones",
                    "android\\.telephony\\.TelephonyManager\\.getDeviceId",
                    "java\\.lang\\.String",
                    new String[] {"int"},
                    new String[] {".*"},
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getDeviceId (int slotIndex)
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the IMEI (International Mobile Equipment Identity). Return null if IMEI is not available
                    "IMEI",
                    "android\\.telephony\\.TelephonyManager\\.getImei",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getImei()
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the IMEI (International Mobile Equipment Identity). Return null if IMEI is not available
                    "IMEI",
                    "android\\.telephony\\.TelephonyManager\\.getImei",
                    "java\\.lang\\.String",
                    new String[] {"int"},
                    new String[] {".*"},
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getImei(int slotIndex)
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the MEID (Mobile Equipment Identifier). Return null if MEID is not available.
                    "MEID",
                    "android\\.telephony\\.TelephonyManager\\.getMeid",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getMeid()
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the MEID (Mobile Equipment Identifier). Return null if MEID is not available.
                    "MEID",
                    "android\\.telephony\\.TelephonyManager\\.getMeid",
                    "java\\.lang\\.String",
                    new String[] {"int"},
                    new String[] {".*"},
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), // getMeid(int slotIndex)
            new TargetValueFromReturnValueAPI( // now return a constant value of 02:00:00:00:00:00 after Android 6.0
                    "Wi-Fi MAC Address",
                    "android\\.net\\.wifi\\.WifiInfo\\.getMacAddress",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {}, // FIXME: AndroidPermission.ACCESS_WIFI_STATE
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),
            new TargetValueFromReturnValueAPI(
                    "Bluetooth MAC Address",
                    "android\\.bluetooth\\.BluetoothAdapter\\.getAddress",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {}, // FIXME: AndroidPermission.BLUETOOTH
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),
            new TargetValueFromReturnValueAPI(
                    "Line1(Phone) number",
                    "android\\.telephony\\.TelephonyManager\\.getLine1Number", // phone number
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),
            new TargetValueFromReturnValueAPI(
                    "Google Instance ID",
                    "com\\.google\\.android\\.gms\\.iid\\.InstanceID\\.getId",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),
            new TargetValueFromReturnValueAPI(
                    "Google Advertising ID",
                    "com\\.google\\.android\\.gms\\.ads\\.identifier\\.AdvertisingIdClient\\.Info\\.getId",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),
            /**
             * Items in the below section are based around any type of audio recording.
             *
             * Currently implemented APIs:
             * AudioRecord (captures audio)
             *
             * Note: MediaRecorder (captures audio) can be used to collect audio data simultaneously, so we coded a
             * separate annotation util for it in a later section.
             *
             * Note: Activity.startActivityForResult can be used to access multiple types of data and is handled
             * separately in a later section.
            */
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*short\\[\\]", ".*int", ".*int"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.RECORD_AUDIO}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: AudioRecord.read(short[] audioData, int offsetInShorts, int sizeInShorts) and AudioRecord.read(short[] audioData, int offsetInShorts, int sizeInShorts, int readMode). readMode specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*float\\[\\]", ".*int", ".*int", ".*int"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.RECORD_AUDIO}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: AudioRecord.read(float[] audioData, int offsetInFloats, int sizeInFloats, int readMode)
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*ByteBuffer", ".*int"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.RECORD_AUDIO}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: AudioRecord.read(ByteBuffer audioBuffer, int sizeInBytes) and AudioRecord.read(ByteBuffer audioBuffer, int sizeInBytes, int readMode)
            //If you need to handle readMode specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*byte\\[\\]", "int", "int"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.RECORD_AUDIO}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: AudioRecord.read(byte[] audioData, int offsetInBytes, int sizeInBytes) and AudioRecord.read(byte[] audioData, int offsetInBytes, int sizeInBytes, int readMode)
            //If you need to handle readMode specifically, create a new, seperate entry for the longer method.



            /**
             * Items in the below section are based around the APIs used to capture images from the camera
             *
             * Currently implemented APIs:
             * CameraCaptureSession (captures metadata, including location data of pictures)
             * ImageReader (captures images)
             *
             * Note: MediaRecorder (captures video) can be used to collect audio data simultaneously, so we coded a
             * separate annotation util for it in a later section.
             *
             * Note: Activity.startActivityForResult can be used to access multiple types of data and is handled
             * separately in a later section.
            */
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.capture",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.capture",
                    "int",
                    new String[]{".*CaptureRequest", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.capture(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.captureBurst",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.captureBurst",
                    "int",
                    new String[]{".*", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"}, //TODO: "List<CaptureBurst>" does not work as a parameter regex here. Neither does "List\\<CaptureBurst\\>" Having this parameter is not essential to the code function, but it would be nice to know how to do this. This applies to other CameraCaptureSession methods as well
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.captureBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.captureBurstRequests",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.captureBurstRequests",
                    "int",
                    new String[]{".*", ".*Executor", ".*CameraCaptureSession\\.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.captureBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.captureSingleRequest",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.captureSingleRequest",
                    "int",
                    new String[]{".*CaptureRequest", ".*Executor", ".*CameraCaptureSession\\.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.captureSingleRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setRepeatingBurst",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setRepeatingBurst",
                    "int",
                    new String[]{".*", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.setRepeatingBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setRepeatingBurstRequests",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setRepeatingBurstRequests",
                    "int",
                    new String[]{".*", ".*Executor", ".*CameraCaptureSession\\.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false,
                    AccessType.RECURRING_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.setRepeatingBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setRepeatingRequest",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setRepeatingRequest",
                    "int",
                    new String[]{".*CaptureRequest", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false,
                    AccessType.RECURRING_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.setRepeatingRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setSingleRepeatingRequest",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setSingleRepeatingRequest",
                    "int",
                    new String[]{".*CaptureRequest", ".*Executor", ".*CameraCaptureSession.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false,
                    AccessType.RECURRING_COLLECTION
            ), //Method(s) handled: CameraCaptureSession.setSingleRepeatingRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetValueFromReturnValueAPI(
                    "ImageReader.acquireNextImage",
                    "android\\.media\\.ImageReader\\.acquireNextImage",
                    "android.media.Image",
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION

            ), //Method(s) handled: ImageReader.acquireNextImage()
            new TargetValueFromReturnValueAPI(
                    "ImageReader.acquireLatestImage",
                    "android\\.media\\.ImageReader\\.acquireLatestImage",
                    "android.media.Image",
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ImageReader.acquireLatestImage()

            new TargetVariableInMethodCallParameterAPI("Camera.setPreviewDisplay",
                    "android\\.hardware\\.Camera\\.setPreviewDisplay",
                    "void",
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), true, false,
                    AccessType.CONTINUOUS_COLLECTION),
            /**
             * This section handles all sensor API calls.
             *
             * Current APIs implemented -
             * SensorManager
             *
             */
            new TargetVariableFromCallbackAPI("SensorManager.registerListener",
                    "android\\.hardware\\.SensorManager\\.registerListener",
                    "boolean",
                    new String[]{".*SensorEventListener", ".*Sensor", ".*int"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.BODY_SENSORS}}, false, null, // FIXME: the second array here means the permission may be needed for some argument values
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.HealthAndFitness_HealthInfo,
                                    PersonalDataGroup.HealthAndFitness_FitnessInfo,
                                    PersonalDataGroup.AppActivity_AppInteractions),
                    },
                    new SensorCallbackTargetVariableTracker(0),true, false,
                    AccessType.RECURRING_COLLECTION
            ), //Method(s) handled: SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs)
            //and SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs)
            //and SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, Handler handler)
            //and SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs, Handler handler)
            new TargetVariableFromCallbackAPI("SensorManager.requestTriggerSensor",
                    "android\\.hardware\\.SensorManager\\.requestTriggerSensor",
                    "boolean",
                    new String[]{".*TriggerEventListener", ".*Sensor"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][] {},
                    new AndroidPermission[][]{{AndroidPermission.BODY_SENSORS}}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.HealthAndFitness_HealthInfo,
                                    PersonalDataGroup.HealthAndFitness_FitnessInfo,
                                    PersonalDataGroup.AppActivity_AppInteractions),
                    },
                    new SensorCallbackTargetVariableTracker(0),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: SensorManager.requestTriggerSensor(TriggerEventListener listener, Sensor sensor)
            new TargetVariableInMethodCallParameterAPI("SensorManager.createDirectChannel",
                    "android\\.hardware\\.SensorManager\\.createDirectChannel",
                    "android\\.hardware\\.SensorDirectChannel",
                    new String[]{"android\\.os\\.MemoryFile"},
                    new String[]{".*"},
                    new AndroidPermission[][] {},
                    new AndroidPermission[][]{{AndroidPermission.BODY_SENSORS}}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.HealthAndFitness_HealthInfo,
                                    PersonalDataGroup.HealthAndFitness_FitnessInfo,
                                    PersonalDataGroup.AppActivity_AppInteractions),
                    },
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.CONTINUOUS_COLLECTION

            ), //Method(s) handled: SensorManager.createDirectChannel(MemoryFile mem)
            new TargetVariableInMethodCallParameterAPI("SensorManager.createDirectChannel",
                    "android\\.hardware\\.SensorManager\\.createDirectChannel",
                    "android\\.hardware\\.SensorDirectChannel",
                    new String[]{"android\\.hardware\\.HardwareBuffer"},
                    new String[]{".*"},
                    new AndroidPermission[][] {},
                    new AndroidPermission[][]{{AndroidPermission.BODY_SENSORS}}, false, null,
                    new PersonalDataAPIAnnotationUtil [] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.HealthAndFitness_HealthInfo,
                                    PersonalDataGroup.HealthAndFitness_FitnessInfo,
                                    PersonalDataGroup.AppActivity_AppInteractions),
                    },
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.CONTINUOUS_COLLECTION
            ), //Method(s) handled: SensorManager.createDirectChannel(HardwareBuffer mem)


            /**
             * This section handles all User File API calls.
             *
             * Current APIs implemented -
             * ShareCompat.IntentReader
             * MediaPlayer
             * ContentResolver
             *
             * Note: Activity.startActivityForResult can be used to access multiple types of data and is handled
             * separately in a later section.
            */
            new TargetValueFromReturnValueAPI("ContentResolver.openAssetFile",
                    "android\\.content\\.ContentResolver\\.openAssetFile",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.CancellationSignal"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.FilesAndDocs_FilesAndDocs),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_MusicFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_OtherUserAudioFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Videos),
                    },
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ContentResolver.openAssetFile(Uri uri, String mode, CancellationSignal signal)
            new TargetValueFromReturnValueAPI("ContentResolver.openAssetFileDescriptor",
                    "android\\.content\\.ContentResolver\\.openAssetFileDescriptor",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.FilesAndDocs_FilesAndDocs),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_MusicFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_OtherUserAudioFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Videos),
                    },
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ContentResolver.openAssetFileDescriptor(Uri uri, String mode) and ContentResolver.openAssetFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.openFile",
                    "android\\.content\\.ContentResolver\\.openFile",
                    "android\\.os\\.ParcelFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.CancellationSignal"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.FilesAndDocs_FilesAndDocs),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_MusicFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_OtherUserAudioFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Videos),
                    },
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ContentResolver.openFile(Uri uri, String mode, CancellationSignal signal)
            new TargetValueFromReturnValueAPI("ContentResolver.openFileDescriptor",
                    "android\\.content\\.ContentResolver\\.openFileDescriptor",
                    "android\\.os\\.ParcelFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.FilesAndDocs_FilesAndDocs),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_MusicFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_OtherUserAudioFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Videos),
                    },
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ContentResolver.openFileDescriptor(Uri uri, String mode) and ContentResolver.openFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.openInputStream",
                    "android\\.content\\.ContentResolver\\.openInputStream",
                    "java\\.io\\.InputStream",
                    new String[]{"android\\.net\\.Uri"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.FilesAndDocs_FilesAndDocs),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_MusicFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_OtherUserAudioFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Videos),
                    },
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ContentResolver.openInputStream(Uri uri)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.openTypedAssetFile",
                    "android\\.content\\.ContentResolver\\.openTypedAssetFile",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.Bundle", "android\\.os\\.CancellationSignal"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.FilesAndDocs_FilesAndDocs),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_MusicFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_OtherUserAudioFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Videos),
                    },
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ContentResolver.openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal)
            new TargetValueFromReturnValueAPI("ContentResolver.openTypedAssetFileDescriptor",
                    "android\\.content\\.ContentResolver\\.openTypedAssetFileDescriptor",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.Bundle"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.FilesAndDocs_FilesAndDocs),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_MusicFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AudioFiles_OtherUserAudioFiles),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Photos),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PhotosAndVideos_Videos),
                    },
                    new ReturnValueTargetVariableTracker(),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ContentResolver.openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts) and ContentResolver.openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts, CancellationSignal cancellationSignal)
            new TargetValueFromReturnValueAPI("BitmapFactory.decodeFile",
                    "android\\.graphics\\.BitmapFactory\\.decodeFile",
                    "android\\.graphics\\.Bitmap",
                    new String[]{".*String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(
                            PersonalDataGroup.FilesAndDocs_FilesAndDocs, PersonalDataGroup.PhotosAndVideos_Photos),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),
            new TargetValueFromReturnValueAPI("BitmapFactory.decodeFile",
                    "android\\.graphics\\.BitmapFactory\\.decodeFile",
                    "android\\.graphics\\.Bitmap",
                    new String[]{".*String", ".*BitmapFactory.Options"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{{AndroidPermission.READ_EXTERNAL_STORAGE}},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(
                            PersonalDataGroup.FilesAndDocs_FilesAndDocs, PersonalDataGroup.PhotosAndVideos_Photos),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            /**
             * This section handles all User Input API calls
             *
             * Current APIs implemented -
             * EditText
             *
            */
            new TargetVariableInCallerAPI("EditText.getText",
                    "android\\.widget\\.EditText\\.getText",
                    "android\\.text\\.Editable",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserInputAnnotationUtil(),
                    new PersonalDataInCallerTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: EditText.getText()
            new TargetVariableInCallerAPI("AutoCompleteTextView.getText",
                    "android\\.widget\\.AutoCompleteTextView\\.getText",
                    "android\\.text\\.Editable",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserInputAnnotationUtil(),
                    new PersonalDataInCallerTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: AutoCompleteTextView.getText()
            new TargetVariableInCallerAPI("ExtractEditText.getText",
                    "android\\.widget\\.ExtractEditText\\.getText",
                    "android\\.text\\.Editable",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserInputAnnotationUtil(),
                    new PersonalDataInCallerTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: ExtractEditText.getText()
            new TargetVariableInCallerAPI("MultiAutoCompleteTextView.getText",
                    "android\\.widget\\.MultiAutoCompleteTextView\\.getText",
                    "android\\.text\\.Editable",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserInputAnnotationUtil(),
                    new PersonalDataInCallerTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: MultiAutoCompleteTextView.getText()
            new TargetValueFromReturnValueAPI("getSelectedItem",
                    ".*getSelectedItem",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserSelectionAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),
            new TargetValueFromReturnValueAPI("getSelectedItemId",
                    ".*getSelectedItemId",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserSelectionAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),
            new TargetValueFromReturnValueAPI("getSelectedItemPosition",
                    ".*getSelectedItemPosition",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserSelectionAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),
            new TargetValueFromReturnValueAPI("getSelectedView",
                    ".*getSelectedItemPosition",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new SensitiveUserSelectionAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            /**
             * Items in the below section handles MediaRecorder APIs used to capture audios from the microphone and
             * videos from the camera
            */
            new TargetVariableInMethodCallParameterAPI(
                    "MediaRecorder.setOutputFile",
                    "android\\.media\\.MediaRecorder\\.setOutputFile",
                    "void",
                    new String[]{".*FileDescriptor"},
                    new String[]{".*"},
                    new AndroidPermission[][]{{AndroidPermission.RECORD_AUDIO, AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new MediaRecorderAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.CONTINUOUS_COLLECTION
            ), //Method(s) handled: MediaRecorder.setOutputFile(FileDescriptor fd)
            new TargetVariableInMethodCallParameterAPI(
                    "MediaRecorder.setOutputFile",
                    "android\\.media\\.MediaRecorder\\.setOutputFile",
                    "void",
                    new String[]{".*String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{{AndroidPermission.RECORD_AUDIO, AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new MediaRecorderAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.CONTINUOUS_COLLECTION
            ), //Method(s) handled: MediaRecorder.setOutputFile(String path)
            new TargetVariableInMethodCallParameterAPI(
                    "MediaRecorder.setOutputFile",
                    "android\\.media\\.MediaRecorder\\.setOutputFile",
                    "void",
                    new String[]{".*File"},
                    new String[]{".*"},
                    new AndroidPermission[][]{{AndroidPermission.RECORD_AUDIO, AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    new MediaRecorderAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.CONTINUOUS_COLLECTION
            ), //Method(s) handled: MediaRecorder.setOutputFile(File file)

            /**
             * Items in the below section handles startActivityForResult APIs used to capture a variety of personal data,
             * including camera data, microphone data, user files, etc.
            */
            new TargetVariableInMethodCallParameterAPI(
                    "startActivityForResult",
                    ".*startActivityForResult",
                    "void",
                    new String[]{".*Intent", ".*"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new StartActivityForResultAnnotationUtil(0),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false,
                    AccessType.ONE_TIME_COLLECTION
            ), //Method(s) handled: Activity.startActivityForResult(Intent intent, int requestCode) and Activity.startActivityForResult(Intent intent, int requestCode, Bundle options)
            //and FragmentActivity.startActivityForResult(Intent intent, int requestCode) and FragmentActivity.startActivityForResult(Intent intent, int requestCode, Bundle options)
            //and ActivityCompat.startActivityForResult(Intent intent, int requestCode) and ActivityCompat.startActivityForResult(Intent intent, int requestCode, Bundle options)
            //Intents for User Data should be - Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT, Intent.ACTION_GET_DOCUMENT, Intent.ACTION_GET_DOCUMENT_TREE

            new TargetValueFromReturnValueAPI(
                    "getPrimaryClip",
                    ".*getPrimaryClip",
                    ".*ClipData",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_AppInteractions),
                            new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_OtherUserActivities),
                    },
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getRunningTasks",
                    ".*getRunningTasks",
                    ".*RunningTaskInfo.*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_InstalledApps),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getRunningAppProcesses",
                    ".*getRunningAppProcesses",
                    ".*RunningAppProcessInfo.*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_InstalledApps),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getInstalledApplications",
                    ".*getInstalledApplications",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_InstalledApps),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getInstalledPackages",
                    ".*getInstalledPackages",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_InstalledApps),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetVariableInMethodDefinitionParameterAPI(
                    "NotificationListenerService.onNotificationPosted",
                    ".*onNotificationPosted",
                    "void",
                    new AndroidPermission[][]{{AndroidPermission.NOTIFICATION}},
                    new AndroidPermission[][]{},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.AppActivity_InstalledApps,
                                    PersonalDataGroup.AppActivity_OtherUserGeneratedContent,
                                    PersonalDataGroup.AppActivity_OtherUserActivities,
                                    PersonalDataGroup.Messages_InAppMessages)
                    },
                    new PersonalDataInMethodDefinitionParameterTargetVariableTracker(0), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetVariableInMethodDefinitionParameterAPI(
                    "NotificationListenerService.onNotificationRemoved",
                    ".*onNotificationRemoved",
                    "void",
                    new AndroidPermission[][]{{AndroidPermission.NOTIFICATION}},
                    new AndroidPermission[][]{},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.AppActivity_InstalledApps,
                                    PersonalDataGroup.AppActivity_OtherUserGeneratedContent,
                                    PersonalDataGroup.AppActivity_OtherUserActivities,
                                    PersonalDataGroup.Messages_InAppMessages)
                    },
                    new PersonalDataInMethodDefinitionParameterTargetVariableTracker(0), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "getActiveNotifications",
                    ".*getActiveNotifications",
                    ".*StatusBarNotification.*\\[\\]",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{{AndroidPermission.NOTIFICATION}},
                    new AndroidPermission[][]{},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.AppActivity_InstalledApps,
                                    PersonalDataGroup.AppActivity_OtherUserGeneratedContent,
                                    PersonalDataGroup.AppActivity_OtherUserActivities,
                                    PersonalDataGroup.Messages_InAppMessages)
                    },
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getSnoozedNotifications",
                    ".*getSnoozedNotifications",
                    ".*StatusBarNotification.*\\[\\]",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{{AndroidPermission.NOTIFICATION}},
                    new AndroidPermission[][]{},
                    false, null,
                    new PersonalDataAPIAnnotationUtil[] {
                            new createSourceAnnotationRequiredByDataTypeUtil(
                                    PersonalDataGroup.AppActivity_InstalledApps,
                                    PersonalDataGroup.AppActivity_OtherUserGeneratedContent,
                                    PersonalDataGroup.AppActivity_OtherUserActivities,
                                    PersonalDataGroup.Messages_InAppMessages)
                    },
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetVariableInMethodDefinitionParameterAPI(
                    "AccessibilityService.onAccessibilityEvent",
                    ".*onAccessibilityEvent",
                    "void",
                    new String[]{".*AccessibilityEvent"},
                    new String[]{".*"},
                    new AndroidPermission[][]{{AndroidPermission.ACCESSIBILITY}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_AppInteractions),
                    new PersonalDataInMethodDefinitionParameterTargetVariableTracker(0), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetVariableInMethodDefinitionParameterAPI(
                    "AccessibilityService.onGesture",
                    ".*onGesture",
                    "boolean",
                    new String[]{".*AccessibilityGestureEvent"},
                    new String[]{".*"},
                    new AndroidPermission[][]{{AndroidPermission.ACCESSIBILITY}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_AppInteractions),
                    new PersonalDataInMethodDefinitionParameterTargetVariableTracker(0), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetVariableInMethodDefinitionParameterAPI(
                    "AccessibilityService.onKeyEvent",
                    ".*onKeyEvent",
                    "boolean",
                    new String[]{".*KeyEvent"},
                    new String[]{".*"},
                    new AndroidPermission[][]{{AndroidPermission.ACCESSIBILITY}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.AppActivity_AppInteractions),
                    new PersonalDataInMethodDefinitionParameterTargetVariableTracker(0), true, false,
                    AccessType.ONE_TIME_COLLECTION
            ),

            new TargetValueFromReturnValueAPI(
                    "getAccounts",
                    ".*getAccounts",
                    ".*Account.*\\[\\]",
                    new AndroidPermission[][]{{AndroidPermission.GET_ACCOUNTS}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PersonalInfo_UserIds),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getAccountsAndVisibilityForPackage",
                    ".*getAccountsAndVisibilityForPackage",
                    ".*",
                    new String[]{".*String", ".*String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.GET_ACCOUNTS}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PersonalInfo_UserIds),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getAccountsByType",
                    ".*getAccountsByType",
                    ".*Account.*\\[\\]",
                    new String[]{".*String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{{AndroidPermission.GET_ACCOUNTS}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PersonalInfo_UserIds),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getAccountsByTypeAndFeatures",
                    ".*getAccountsByTypeAndFeatures",
                    ".*",
                    new String[]{".*String", ".*String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.GET_ACCOUNTS}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PersonalInfo_UserIds),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getAccountsByTypeForPackage",
                    ".*getAccountsByTypeForPackage",
                    ".*",
                    new AndroidPermission[][]{{AndroidPermission.GET_ACCOUNTS}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.PersonalInfo_UserIds),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getAuthToken",
                    ".*getAuthToken",
                    ".*",
                    new AndroidPermission[][]{{AndroidPermission.GET_ACCOUNTS}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),

            new TargetValueFromReturnValueAPI(
                    "getAuthTokenByFeatures",
                    ".*getAuthTokenByFeatures",
                    ".*",
                    new AndroidPermission[][]{{AndroidPermission.GET_ACCOUNTS}},
                    new AndroidPermission[][]{},
                    false, null,
                    new createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds),
                    new ReturnValueTargetVariableTracker(), true, false,
                    AccessType.ONE_TIME_COLLECTION),



            // TODO: AccessibilityService.takeScreenshot (added in API 30)
    };

        public static SensitiveAPI[] getAPIList(boolean isThirdParty) {
        ArrayList<SensitiveAPI> apiList = new ArrayList<>();
        for (SensitiveAPI api : sensitiveAPIs) {
            if (isThirdParty == api.isThirdPartyAPI) {
                apiList.add(api);
            }
        }
        return apiList.toArray(new SensitiveAPI[0]);
    }
}
