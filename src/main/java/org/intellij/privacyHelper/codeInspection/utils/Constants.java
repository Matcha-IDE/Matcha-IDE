package org.intellij.privacyHelper.codeInspection.utils;

import java.util.*;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.*;
import static org.intellij.privacyHelper.codeInspection.utils.SharingAttribute.*;
import static org.intellij.privacyHelper.codeInspection.utils.CollectionAttribute.*;

/**
 * Created by tianshi on 12/27/17.
 */
public class Constants {
    public static final Map<String, String> DESCRIPTION_MAPPING = Collections.unmodifiableMap(new HashMap<>() {{
        put(fieldDataTransmissionAccessIdList, "Use the Id of an existing @DataAccess annotation to represent where you obtains the data that gets sent out here. Ids defined in this project are listed below, separated by comma:\n\n%s");
        put(fieldDataTransmissionCollectionAttributeList, "Specify data transmission attributes to help determine whether it needs to be disclosed as data collection on the label");
        put(fieldDataTransmissionSharingAttributeList, "Specify data transmission attributes to help determine whether it needs to be disclosed as data sharing on the label");
        put("SharedWithThirdParty", "'True' if the data collected from your app is transferred to a third party, otherwise 'False'.\n\nAccording to Google's definition, data transferred to a third party needs to be disclosed as data sharing on your privacy label.");
        put("OnlyTransferringAnonymousData",
                "'True' if transferring the data to a third party that has been fully anonymized so that it can no longer be associated with an individual user; otherwise 'False'.\nData sharing is exempt from disclosure if 'OnlyTransferringAnonymousData' is 'True'.");
        put("OnlyInitiatedByUser",
                "'True' if transferring the data to a third party based on a specific user-initiated action, where the user reasonably expects the data to be shared; otherwise 'False'.\nData sharing is exempt from disclosure if 'OnlyInitiatedByUser' is 'True'.");
        put("OnlyAfterGettingUserConsent", "'True' if transferring the data to a third party based on a prominent in-app disclosure and consent that meets the requirements described in Google Play's User Data policy; otherwise 'False'.\n\nData sharing is exempt from disclosure if 'OnlyAfterGettingUserConsentWithProminentDisclosure' is 'True'.");
        put("OnlySharedForLegalPurposes", "'True' if transferring the data to a third party for specific legal purposes, such as in response to a legal obligation or government requests; otherwise 'False'.\n\nData sharing is exempt from disclosure if 'OnlySharedForLegalPurposes' is 'True'.");
        put("SharedFor", "Insert one or multiple of the following sharing purposes options:\n\n" +
                "'AppFunctionality': Used for features that are available in the app\n" +
                "'Analytics': Used to collect data about how users use the app or how it performs\n" +
                "'DeveloperCommunications': Used to send news or notifications about the app or the developer.\n" +
                "'AdvertisingOrMarketing': Used to display or target ads or marketing communications, or measuring ad performance\n" +
                "'FraudPreventionAndSecurityAndCompliance': Used for fraud prevention, security, or compliance with laws.\n" +
                "'Personalization': Used to customize your app, such as showing recommended content or suggestions.\n" +
                "'AccountManagement': Used for the setup or management of a user’s account with the developer.");
        put("OnlySharedWithServiceProviders", "'True' if transferring the data to a 'service provider' that processes it on behalf of the developer; otherwise 'False'.\n\n'Service provider' means an entity that processes user data on behalf of the developer and based on the developer’s instructions.\n\nData sharing is exempt from disclosure if 'OnlySharedWithServiceProviders' is 'True'.");

        put("TransmittedOffDevice", "'True' if the data is transmitted from your app off a user’s device, otherwise 'False'.\n\nAccording to Google's definition, data transmitted off the device needs to be disclosed as data collection on your privacy label.");
        put("OptionalCollection", "'True' if a user has the ability to opt into or opt out of data collection, e.g., a user has control over its collection and can use the app without providing it; or where a user chooses whether to manually provide that data type, otherwise 'False'");
        put("CollectedFor", "Insert one or multiple of the following collection purposes options:\n" +
                "'AppFunctionality': Used for features that are available in the app\n\n" +
                "'Analytics': Used to collect data about how users use the app or how it performs\n" +
                "'DeveloperCommunications': Used to send news or notifications about the app or the developer.\n" +
                "'AdvertisingOrMarketing': Used to display or target ads or marketing communications, or measuring ad performance\n" +
                "'FraudPreventionAndSecurityAndCompliance': Used for fraud prevention, security, or compliance with laws.\n" +
                "'Personalization': Used to customize your app, such as showing recommended content or suggestions.\n" +
                "'AccountManagement': Used for the setup or management of a user’s account with the developer.");
        put("EncryptionInTransit", "'True' if the network traffic is correctly encrypted; otherwise 'False'.\n\nYou should follow best industry standards to safely encrypt your app’s data in transit. Common encryption protocols include TLS (Transport Layer Security) and HTTPS. ");
        put("UserToUserEncryption", "'True' if the data is sent off device, but is unreadable by you or anyone other than the sender and recipient as a result of end-to-end encryption; otherwise 'False'.\n\nData collection is exempt from disclosure if EndToEndEncryption' is 'True'.");
        put("NotStoredInBackend", "'True' if the data is only stored in memory and retained for no longer than necessary to service the specific request in real-time; otherwise 'False'.\n\nData collection is exempt from disclosure if NotStoredInBackend' is 'True'.");

        put(PersonalDataGroup.Location_ApproximateLocation.toString(), "User or device physical location to an area greater than or equal to 3 square kilometers, such as the city a user is in, or location provided by Android’s ACCESS_COARSE_LOCATION permission.");
        put(PersonalDataGroup.Location_PreciseLocation.toString(), "User or device physical location within an area less than 3 square kilometers, such as location provided by Android’s ACCESS_FINE_LOCATION permission.");
        put(PersonalDataGroup.PersonalInfo_Name.toString(), "How a user refers to themselves, such as their first or last name, or nickname.");
        put(PersonalDataGroup.PersonalInfo_EmailAddress.toString(), "A user’s email address.");
        put(PersonalDataGroup.PersonalInfo_UserIds.toString(), "Identifiers that relate to an identifiable person. For example, an account ID, account number, or account name. ");
        put(PersonalDataGroup.PersonalInfo_Address.toString(), "A user’s address, such as a mailing or home address.");
        put(PersonalDataGroup.PersonalInfo_PhoneNumber.toString(), "A user’s phone number.");
        put(PersonalDataGroup.PersonalInfo_RaceAndEthnicity.toString(), "Information about a user’s race or ethnicity.");
        put(PersonalDataGroup.PersonalInfo_PoliticalOrReligiousBeliefs.toString(), "Information about a user’s political or religious beliefs.");
        put(PersonalDataGroup.PersonalInfo_SexualOrientation.toString(), "Information about a user’s sexual orientation.");
        put(PersonalDataGroup.PersonalInfo_OtherPersonalInfo.toString(), "Any other personal information such as date of birth, gender identity, veteran status, etc.");
        put(PersonalDataGroup.FinancialInfo_UserPaymentInfo.toString(), "Information about a user’s financial accounts such as credit card number.");
        put(PersonalDataGroup.FinancialInfo_PurchaseHistory.toString(), "Information about purchases or transactions a user has made.");
        put(PersonalDataGroup.FinancialInfo_CreditScore.toString(), "Information about a user’s credit score.");
        put(PersonalDataGroup.FinancialInfo_OtherFinancialInfo.toString(), "Any other financial information such as user salary or debts.");
        put(PersonalDataGroup.HealthAndFitness_HealthInfo.toString(), "Information about a user's health, such as medical records or symptoms.");
        put(PersonalDataGroup.HealthAndFitness_FitnessInfo.toString(), "Information about a user's fitness, such as exercise or other physical activity.");
        put(PersonalDataGroup.Messages_Emails.toString(), "A user’s emails including the email subject line, sender, recipients, and the content of the email.");
        put(PersonalDataGroup.Messages_SmsOrMms.toString(), "A user’s text messages including the sender, recipients, and the content of the message.");
        put(PersonalDataGroup.Messages_InAppMessages.toString(), "Any other types of messages. For example, instant messages or chat content.");
        put(PersonalDataGroup.PhotosAndVideos_Photos.toString(), "A user’s photos.");
        put(PersonalDataGroup.PhotosAndVideos_Videos.toString(), "A user’s videos.");
        put(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings.toString(), "A user’s voice such as a voicemail or a sound recording.");
        put(PersonalDataGroup.AudioFiles_MusicFiles.toString(), "A user’s music files.");
        put(PersonalDataGroup.AudioFiles_OtherUserAudioFiles.toString(), "Any other user-created or user-provided audio files.");
        put(PersonalDataGroup.FilesAndDocs_FilesAndDocs.toString(), "A user’s files or documents, or information about their files or documents such as file names.");
        put(PersonalDataGroup.Calendar_CalendarEvents.toString(), "Information from a user’s calendar such as events, event notes, and attendees.");
        put(PersonalDataGroup.Contacts_Contacts.toString(), "Information about the user’s contacts such as contact names, message history, and social graph information like usernames, contact recency, contact frequency, interaction duration and call history.");
        put(PersonalDataGroup.AppActivity_AppInteractions.toString(), "Information about how a user interacts with the app. For example, the number of times they visit a page or sections they tap on.");
        put(PersonalDataGroup.AppActivity_InAppSearchHistory.toString(), "Information about what a user has searched for in your app.");
        put(PersonalDataGroup.AppActivity_InstalledApps.toString(), "Information about the apps installed on a user's device.");
        put(PersonalDataGroup.AppActivity_OtherUserGeneratedContent.toString(), "Any other user-generated content not listed here, or in any other section. For example, user bios, notes, or open-ended responses.");
        put(PersonalDataGroup.AppActivity_OtherUserActivities.toString(), "Any other user activity or actions in-app not listed here such as gameplay, likes, and dialog options.");
        put(PersonalDataGroup.WebBrowsing_WebBrowsingHistory.toString(), "Information about the websites a user has visited.");
        put(PersonalDataGroup.AppInfoAndPerformance_CrashLogs.toString(), "Crash log data from your app. For example, the number of times your app has crashed, stack traces, or other information directly related to a crash.");
        put(PersonalDataGroup.AppInfoAndPerformance_Diagnostics.toString(), "Information about the performance of your app. For example battery life, loading time, latency, framerate, or any technical diagnostics.");
        put(PersonalDataGroup.AppInfoAndPerformance_OtherAppPerformanceData.toString(), "Any other app performance data not listed here.");
        put(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds.toString(), "Identifiers that relate to an individual device, browser or app. For example, an IMEI number, MAC address, Widevine Device ID, Firebase installation ID, or advertising identifier.");
    }});

    public static final String SOURCE_ANNOTATION_REQUIRED =  "The %s " + String.format(
           "should be annotated with @%s, @%s or @%s ", DataAccess, MultipleAccess, NotPersonalDataAccess) +
            "to specify how personal data is used\n\n%s";
    public static final String SINK_ANNOTATION_REQUIRED = "The %s " + String.format(
            "should be annotated with @%s, @%s or @%s to explain how data leaves the app.\n", DataTransmission,
            MultipleTransmission, NotPersonalDataTransmission);
    public static final String DEFINE_VARIABLE = "Please declare a variable to hold this value for adding privacy " +
            "annotation (auto quickfixes provided)";
    public static final String INCORRECT_ANNOTATION = "The annotation contains incomplete or invalid value(s). Please use the quickfix to navigate to the annotation and check the detailed error explanations";
    public static final String CHECK_THE_ANNOTATION = "Check errors in the annotation";
    public static final String NEED_ONE_VALUE = "One of the following values is required: %s";
    public static final String CONFLICTING_VALUES = "Conflicting values: %s";
    public static final String NEED_AT_LEAST_ONE_VALUE = "At least one of the following values is required: %s";

    public static final String[] COLLECTION_EXEMPT_VALUES = new String[] {
            UserToUserEncryption_True, NotStoredInBackend_True};
    public static final String[] SHARING_EXEMPT_VALUES = new String[] {
            OnlyAfterGettingUserConsent_True, OnlyInitiatedByUser_True,
            OnlySharedWithServiceProviders_True, OnlySharedForLegalPurposes_True,
            OnlyTransferringAnonymousData_True};

    public static final Map<String, String[][]> ONE_CHOICE_REQUIRED = Collections.unmodifiableMap(new HashMap<>() {{
        put(fieldDataTransmissionCollectionAttributeList, new String[][] {
                {TransmittedOffDevice_True, TransmittedOffDevice_False}});
        put(fieldDataTransmissionSharingAttributeList, new String[][] {
                {SharedWithThirdParty_True, SharedWithThirdParty_False}});
    }});

    public static final Map<String, Map<String, String[][]>> CONDITIONAL_AT_LEAST_ONE_CHOICE_REQUIRED =
            Collections.unmodifiableMap(new HashMap<>() {{
                put(fieldDataTransmissionSharingAttributeList, new HashMap<>() {{
                    put(SharedWithThirdParty_True, new String[][] {
                            {OnlySharedWithServiceProviders_True,
                                    OnlySharedWithServiceProviders_False},
                            {OnlySharedForLegalPurposes_True, OnlySharedForLegalPurposes_False},
                            {OnlyInitiatedByUser_True, OnlyInitiatedByUser_False},
                            {OnlyAfterGettingUserConsent_True,
                                    OnlyAfterGettingUserConsent_False},
                            {OnlyTransferringAnonymousData_True, OnlyTransferringAnonymousData_False},
                            {SharingAttribute.ForAppFunctionality, SharingAttribute.ForAnalytics,
                                    SharingAttribute.ForDeveloperCommunications,
                                    SharingAttribute.ForAdvertisingOrMarketing,
                                    SharingAttribute.ForFraudPreventionAndSecurityAndCompliance,
                                    SharingAttribute.ForPersonalization,
                                    SharingAttribute.ForAccountManagement}});
                }});
                put(fieldDataTransmissionCollectionAttributeList, new HashMap<>() {{
                    put(TransmittedOffDevice_True, new String[][] {
                            {EncryptionInTransit_True, EncryptionInTransit_False},
                            {UserRequestDelete_True, UserRequestDelete_False},
                            {UserToUserEncryption_True, UserToUserEncryption_False},
                            {NotStoredInBackend_False, NotStoredInBackend_True},
                            {OptionalCollection_False, OptionalCollection_True},
                            {CollectionAttribute.ForAppFunctionality, CollectionAttribute.ForAnalytics,
                                    CollectionAttribute.ForDeveloperCommunications,
                                    CollectionAttribute.ForAdvertisingOrMarketing,
                                    CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance,
                                    CollectionAttribute.ForPersonalization, CollectionAttribute.ForAccountManagement}});
                }});
            }});

    public static final Map<String, String[][]> AT_LEAST_ONE_CHOICE_REQUIRED =
            Collections.unmodifiableMap(new HashMap<>() {{
//                put(fieldDataTransmissionCollectionAttributeList, new String[][] {
//                        {CollectionAttribute.ForAppFunctionality.toString(), CollectionAttribute.ForAnalytics.toString(),
//                                CollectionAttribute.ForDeveloperCommunications.toString(),
//                                CollectionAttribute.ForAdvertisingOrMarketing.toString(),
//                                CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance.toString(),
//                                CollectionAttribute.ForPersonalization.toString(),
//                                CollectionAttribute.ForAccountManagement.toString()}});
            }});

    public static final Map<String, String[][]> CONFLICT_VALUE_GROUP_MAP = Collections.unmodifiableMap(new HashMap<>() {{
        put(fieldDataTransmissionSharingAttributeList, new String[][] {
                {SharedWithThirdParty_False}, {
                OnlyAfterGettingUserConsent_True,
                OnlyAfterGettingUserConsent_False,
                OnlyInitiatedByUser_True, OnlyInitiatedByUser_False,
                OnlySharedWithServiceProviders_True, OnlySharedWithServiceProviders_False,
                OnlySharedForLegalPurposes_True, OnlySharedForLegalPurposes_False,
                OnlyTransferringAnonymousData_True, OnlyTransferringAnonymousData_False,
                SharingAttribute.ForAppFunctionality, SharingAttribute.ForAnalytics,
                SharingAttribute.ForDeveloperCommunications,
                SharingAttribute.ForAdvertisingOrMarketing,
                SharingAttribute.ForFraudPreventionAndSecurityAndCompliance,
                SharingAttribute.ForPersonalization,
                SharingAttribute.ForAccountManagement}});
        put(fieldDataTransmissionCollectionAttributeList, new String[][] {
                {TransmittedOffDevice_False}, {EncryptionInTransit_True, EncryptionInTransit_False,
                UserRequestDelete_True, UserRequestDelete_False, UserToUserEncryption_True,
                UserToUserEncryption_False, NotStoredInBackend_False, NotStoredInBackend_True,
                OptionalCollection_False, OptionalCollection_True,
                CollectionAttribute.ForAppFunctionality, CollectionAttribute.ForAnalytics,
                CollectionAttribute.ForDeveloperCommunications, CollectionAttribute.ForAdvertisingOrMarketing,
                CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance,
                CollectionAttribute.ForPersonalization, CollectionAttribute.ForAccountManagement}
        });
    }});

    public static final String metadata = "metadata";

    public static final String fieldDataAccessId = "id";
    public static final String fieldDataAccessDataType = "dataType";
    public static final String fieldDataTransmissionAccessIdList = "accessId";
    public static final String fieldDataTransmissionCollectionAttributeList = "collectionAttribute";
    public static final String fieldDataTransmissionSharingAttributeList = "sharingAttribute";

    public static final String safetySectionCsvHeader = "Question ID (machine readable),Response ID (machine readable),Response value,Answer requirement,Human-friendly question label";

    public static final String [] safetySectionIsCollectingData = {"PSL_DATA_COLLECTION_COLLECTS_PERSONAL_DATA", "REQUIRED",
            "Does your app collect or share any of the required user data types?"};
    public static final String [] safetySectionEncryptedInTransit = {"PSL_DATA_COLLECTION_ENCRYPTED_IN_TRANSIT", "MAYBE_REQUIRED",
            "Is all of the user data collected by your app encrypted in transit?"};
    public static final String [] safetySectionUserRequestDelete = {"PSL_DATA_COLLECTION_USER_REQUEST_DELETE", "MAYBE_REQUIRED",
            "Do you provide a way for users to request that their data is deleted?"};
    public static final String [] safetySectionFamilyPolicy = {"PSL_DATA_COLLECTION_COMPLIES_FAMILY_POLICY", "OPTIONAL",
            "\"Only answer this question if you've indicated that your app's target age group includes children, or you've opted into the Designed for Families program. If either of the above is true, you are required to follow the Google Play Families Policy (https://support.google.com/googleplay/android-developer/answer/9893335). Do you want to let users know about this commitment in the Data safety section on your store listing?\""};
    public static final String [] safetySectionSecurityReview = {"PSL_INDEPENDENTLY_VALIDATED", "OPTIONAL", "\"Has your app successfully completed an independent security review, according to the Mobile Application Security Assessment (MASA) framework? Only answer \"\"yes\"\" if the review is in good standing.\""};

    public static final String [] safetySectionName = {"PSL_DATA_TYPES_PERSONAL", "PSL_NAME", "MULTIPLE_CHOICE", "Personal info / Name"};
    public static final String [] safetySectionEmail = {"PSL_DATA_TYPES_PERSONAL", "PSL_EMAIL", "MULTIPLE_CHOICE", "Personal info / Email address"};
    public static final String [] safetySectionUserAccount = {"PSL_DATA_TYPES_PERSONAL", "PSL_USER_ACCOUNT", "MULTIPLE_CHOICE", "Personal info / User IDs"};
    public static final String [] safetySectionAddress = {"PSL_DATA_TYPES_PERSONAL", "PSL_ADDRESS", "MULTIPLE_CHOICE", "Personal info / Address"};
    public static final String [] safetySectionPhone = {"PSL_DATA_TYPES_PERSONAL", "PSL_PHONE", "MULTIPLE_CHOICE", "Personal info / Phone number"};
    public static final String [] safetySectionEthnicity = {"PSL_DATA_TYPES_PERSONAL", "PSL_RACE_ETHNICITY", "MULTIPLE_CHOICE", "Personal info / Race and ethnicity"};
    public static final String [] safetySectionReligious = {"PSL_DATA_TYPES_PERSONAL", "PSL_POLITICAL_RELIGIOUS", "MULTIPLE_CHOICE", "Personal info / Political or religious beliefs"};
    public static final String [] safetySectionSexGender = {"PSL_DATA_TYPES_PERSONAL", "PSL_SEXUAL_ORIENTATION_GENDER_IDENTITY", "MULTIPLE_CHOICE", "Personal info / Sexual orientation"};
    public static final String [] safetySectionPersonal = {"PSL_DATA_TYPES_PERSONAL", "PSL_OTHER_PERSONAL", "MULTIPLE_CHOICE", "Personal info / Other info"};
    public static final String [] safetySectionBankAccount = {"PSL_DATA_TYPES_FINANCIAL", "PSL_CREDIT_DEBIT_BANK_ACCOUNT_NUMBER", "MULTIPLE_CHOICE", "Financial info / User payment info"};
    public static final String [] safetySectionPurchaseHistory = {"PSL_DATA_TYPES_FINANCIAL", "PSL_PURCHASE_HISTORY", "MULTIPLE_CHOICE", "Financial info / Purchase history"};
    public static final String [] safetySectionCreditScore = {"PSL_DATA_TYPES_FINANCIAL", "PSL_CREDIT_SCORE", "MULTIPLE_CHOICE", "Financial info / Credit score"};
    public static final String [] safetySectionOtherFinancial = {"PSL_DATA_TYPES_FINANCIAL", "PSL_OTHER", "MULTIPLE_CHOICE", "Financial info / Other financial info"};
    public static final String [] safetySectionApproxLocation = {"PSL_DATA_TYPES_LOCATION", "PSL_APPROX_LOCATION", "MULTIPLE_CHOICE", "Location / Approximate location"};
    public static final String [] safetySectionPreciseLocation = {"PSL_DATA_TYPES_LOCATION", "PSL_PRECISE_LOCATION", "MULTIPLE_CHOICE", "Location / Precise location"};
    public static final String [] safetySectionWebBrowsingHistory = {"PSL_DATA_TYPES_SEARCH_AND_BROWSING", "PSL_WEB_BROWSING_HISTORY", "MULTIPLE_CHOICE", "Web browsing / Web browsing history"};
    public static final String [] safetySectionMessagesEmails = {"PSL_DATA_TYPES_EMAIL_AND_TEXT", "PSL_EMAILS", "MULTIPLE_CHOICE", "Messages / Emails"};
    public static final String [] safetySectionSmsCallLog = {"PSL_DATA_TYPES_EMAIL_AND_TEXT", "PSL_SMS_CALL_LOG", "MULTIPLE_CHOICE", "Messages / SMS or MMS"};
    public static final String [] safetySectionOtherMessages = {"PSL_DATA_TYPES_EMAIL_AND_TEXT", "PSL_OTHER_MESSAGES", "MULTIPLE_CHOICE", "Messages / Other in-app messages"};
    public static final String [] safetySectionPhotos = {"PSL_DATA_TYPES_PHOTOS_AND_VIDEOS", "PSL_PHOTOS", "MULTIPLE_CHOICE", "Photos and videos / Photos"};
    public static final String [] safetySectionVideos = {"PSL_DATA_TYPES_PHOTOS_AND_VIDEOS", "PSL_VIDEOS", "MULTIPLE_CHOICE", "Photos and videos / Videos"};
    public static final String [] safetySectionAudio = {"PSL_DATA_TYPES_AUDIO", "PSL_AUDIO", "MULTIPLE_CHOICE", "Audio files / Voice or sound recordings"};
    public static final String [] safetySectionMusic = {"PSL_DATA_TYPES_AUDIO", "PSL_MUSIC", "MULTIPLE_CHOICE", "Audio files / Music files"};
    public static final String [] safetySectionOtherAudio = {"PSL_DATA_TYPES_AUDIO", "PSL_OTHER_AUDIO", "MULTIPLE_CHOICE", "Audio files / Other audio files"};
    public static final String [] safetySectionHealth = {"PSL_DATA_TYPES_HEALTH_AND_FITNESS", "PSL_HEALTH", "MULTIPLE_CHOICE", "Health and fitness / Health info"};
    public static final String [] safetySectionFitness = {"PSL_DATA_TYPES_HEALTH_AND_FITNESS", "PSL_FITNESS", "MULTIPLE_CHOICE", "Health and fitness / Fitness info"};
    public static final String [] safetySectionContacts = {"PSL_DATA_TYPES_CONTACTS", "PSL_CONTACTS", "MULTIPLE_CHOICE", "Contacts / Contacts"};
    public static final String [] safetySectionCalendar = {"PSL_DATA_TYPES_CALENDAR", "PSL_CALENDAR", "MULTIPLE_CHOICE", "Calendar / Calendar events"};
    public static final String [] safetySectionCrashLogs = {"PSL_DATA_TYPES_APP_PERFORMANCE", "PSL_CRASH_LOGS", "MULTIPLE_CHOICE", "App info and performance / Crash logs"};
    public static final String [] safetySectionPerformanceDiagnostics = {"PSL_DATA_TYPES_APP_PERFORMANCE", "PSL_PERFORMANCE_DIAGNOSTICS", "MULTIPLE_CHOICE", "App info and performance / Diagnostics"};
    public static final String [] safetySectionOtherPerformance = {"PSL_DATA_TYPES_APP_PERFORMANCE", "PSL_OTHER_PERFORMANCE", "MULTIPLE_CHOICE", "App info and performance / Other app performance data"};
    public static final String [] safetySectionFilesAndDocs = {"PSL_DATA_TYPES_FILES_AND_DOCS", "PSL_FILES_AND_DOCS", "MULTIPLE_CHOICE", "Files and docs / Files and docs"};
    public static final String [] safetySectionUserInteraction = {"PSL_DATA_TYPES_APP_ACTIVITY", "PSL_USER_INTERACTION", "MULTIPLE_CHOICE", "App activity / App interactions"};
    public static final String [] safetySectionInAppSearchHistory = {"PSL_DATA_TYPES_APP_ACTIVITY", "PSL_IN_APP_SEARCH_HISTORY", "MULTIPLE_CHOICE", "App activity / In-app search history"};
    public static final String [] safetySectionAppsOnDevice = {"PSL_DATA_TYPES_APP_ACTIVITY", "PSL_APPS_ON_DEVICE", "MULTIPLE_CHOICE", "App activity / Installed apps"};
    public static final String [] safetySectionUserGeneratedContent = {"PSL_DATA_TYPES_APP_ACTIVITY", "PSL_USER_GENERATED_CONTENT", "MULTIPLE_CHOICE", "App activity / Other user-generated content"};
    public static final String [] safetySectionOtherAppActivity = {"PSL_DATA_TYPES_APP_ACTIVITY", "PSL_OTHER_APP_ACTIVITY", "MULTIPLE_CHOICE", "App activity / Other actions"};
    public static final String [] safetySectionDeviceId = {"PSL_DATA_TYPES_IDENTIFIERS", "PSL_DEVICE_ID", "MULTIPLE_CHOICE", "Device or other IDs / Device or other IDs"};

    public static final String[][] safetySectionDataTypes = {safetySectionName, safetySectionEmail,
            safetySectionUserAccount, safetySectionAddress, safetySectionPhone, safetySectionEthnicity,
            safetySectionReligious, safetySectionSexGender, safetySectionPersonal, safetySectionBankAccount,
            safetySectionPurchaseHistory, safetySectionCreditScore, safetySectionOtherFinancial,
            safetySectionApproxLocation, safetySectionPreciseLocation, safetySectionWebBrowsingHistory,
            safetySectionMessagesEmails, safetySectionSmsCallLog, safetySectionOtherMessages, safetySectionPhotos,
            safetySectionVideos, safetySectionAudio, safetySectionMusic, safetySectionOtherAudio, safetySectionHealth,
            safetySectionFitness, safetySectionContacts, safetySectionCalendar, safetySectionCrashLogs,
            safetySectionPerformanceDiagnostics, safetySectionOtherPerformance, safetySectionFilesAndDocs,
            safetySectionUserInteraction, safetySectionInAppSearchHistory, safetySectionAppsOnDevice,
            safetySectionUserGeneratedContent, safetySectionOtherAppActivity, safetySectionDeviceId};

    public static final String [] safetySectionCollected = {"PSL_DATA_USAGE_COLLECTION_AND_SHARING", "PSL_DATA_USAGE_ONLY_COLLECTED", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Is this data collected, shared, or both? / Collected\""};
    public static final String [] safetySectionShared = {"PSL_DATA_USAGE_COLLECTION_AND_SHARING", "PSL_DATA_USAGE_ONLY_SHARED", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Is this data collected, shared, or both? / Shared\""};
    public static final String [] safetySectionEphemeral = {"PSL_DATA_USAGE_EPHEMERAL", "", "MAYBE_REQUIRED", "\"Data usage and handling (%s) / Is this data processed ephemerally?\""};
    public static final String [] safetySectionOptional = {"DATA_USAGE_USER_CONTROL", "PSL_DATA_USAGE_USER_CONTROL_OPTIONAL", "SINGLE_CHOICE", "\"Data usage and handling (%s) / Is this data required for your app, or can users choose whether it's collected? / Users can choose whether this data is collected\""};
    public static final String [] safetySectionRequired = {"DATA_USAGE_USER_CONTROL", "PSL_DATA_USAGE_USER_CONTROL_REQUIRED", "SINGLE_CHOICE", "\"Data usage and handling (%s) / Is this data required for your app, or can users choose whether it's collected? / Data collection is required (users can't turn off this data collection)\""};
    public static final String [] safetySectionCollectionAppFunctionality = {"DATA_USAGE_COLLECTION_PURPOSE", "PSL_APP_FUNCTIONALITY", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data collected? Select all that apply. / App functionality\""};
    public static final String [] safetySectionCollectionAnalytics = {"DATA_USAGE_COLLECTION_PURPOSE", "PSL_ANALYTICS", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data collected? Select all that apply. / Analytics\""};
    public static final String [] safetySectionCollectionDevCommunications = {"DATA_USAGE_COLLECTION_PURPOSE", "PSL_DEVELOPER_COMMUNICATIONS", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data collected? Select all that apply. / Developer communications\""};
    public static final String [] safetySectionCollectionFraudPrevention = {"DATA_USAGE_COLLECTION_PURPOSE", "PSL_FRAUD_PREVENTION_SECURITY", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data collected? Select all that apply. / Fraud prevention, security, and compliance\""};
    public static final String [] safetySectionCollectionAdvertising = {"DATA_USAGE_COLLECTION_PURPOSE", "PSL_ADVERTISING", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data collected? Select all that apply. / Advertising or marketing\""};
    public static final String [] safetySectionCollectionPersonalization = {"DATA_USAGE_COLLECTION_PURPOSE", "PSL_PERSONALIZATION", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data collected? Select all that apply. / Personalization\""};
    public static final String [] safetySectionCollectionAccountManagement = {"DATA_USAGE_COLLECTION_PURPOSE", "PSL_ACCOUNT_MANAGEMENT", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data collected? Select all that apply. / Account management\""};
    public static final String [] safetySectionSharingAppFunctionality = {"DATA_USAGE_SHARING_PURPOSE", "PSL_APP_FUNCTIONALITY", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data shared? Select all that apply. / App functionality\""};
    public static final String [] safetySectionSharingAnalytics = {"DATA_USAGE_SHARING_PURPOSE", "PSL_ANALYTICS", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data shared? Select all that apply. / Analytics\""};
    public static final String [] safetySectionSharingDevCommunications = {"DATA_USAGE_SHARING_PURPOSE", "PSL_DEVELOPER_COMMUNICATIONS", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data shared? Select all that apply. / Developer communications\""};
    public static final String [] safetySectionSharingFraudPrevention = {"DATA_USAGE_SHARING_PURPOSE", "PSL_FRAUD_PREVENTION_SECURITY", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data shared? Select all that apply. / Fraud prevention, security, and compliance\""};
    public static final String [] safetySectionSharingAdvertising = {"DATA_USAGE_SHARING_PURPOSE", "PSL_ADVERTISING", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data shared? Select all that apply. / Advertising or marketing\""};
    public static final String [] safetySectionSharingPersonalization = {"DATA_USAGE_SHARING_PURPOSE", "PSL_PERSONALIZATION", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data shared? Select all that apply. / Personalization\""};
    public static final String [] safetySectionSharingAccountManagement = {"DATA_USAGE_SHARING_PURPOSE", "PSL_ACCOUNT_MANAGEMENT", "MULTIPLE_CHOICE", "\"Data usage and handling (%s) / Why is this user data shared? Select all that apply. / Account management\""};

    public static final String[][] safetySectionDataUsageResponses = {safetySectionCollected, safetySectionShared,
            safetySectionEphemeral, safetySectionOptional, safetySectionRequired,
            safetySectionCollectionAppFunctionality, safetySectionCollectionAnalytics,
            safetySectionCollectionDevCommunications, safetySectionCollectionFraudPrevention,
            safetySectionCollectionAdvertising, safetySectionCollectionPersonalization,
            safetySectionCollectionAccountManagement, safetySectionSharingAppFunctionality,
            safetySectionSharingAnalytics, safetySectionSharingDevCommunications,
            safetySectionSharingFraudPrevention, safetySectionSharingAdvertising,
            safetySectionSharingPersonalization, safetySectionSharingAccountManagement};

}
