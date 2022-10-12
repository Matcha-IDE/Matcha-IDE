package org.intellij.privacyHelper.codeInspection.utils;

public class CollectionAttribute {
    public static final String TransmittedOffDevice_True = "TransmittedOffDevice.True";
    public static final String TransmittedOffDevice_False = "TransmittedOffDevice.False";

    public static final String EncryptionInTransit_True = "EncryptionInTransit.True";
    public static final String EncryptionInTransit_False = "EncryptionInTransit.False";

    public static final String UserRequestDelete_True = "UserRequestDelete.True";
    public static final String UserRequestDelete_False = "UserRequestDelete.False";

    public static final String UserToUserEncryption_True = "UserToUserEncryption.True"; // "not in scope for data collection"
    public static final String UserToUserEncryption_False = "UserToUserEncryption.False";

    // break down the definition of "Ephemeral processing" into more familiar concepts.
    // The formal definition is: Processed “ephemerally” means that the data is only stored in memory,
    // retained for no longer than necessary to service the specific request in real-time, and not used for any other purpose.
    public static final String NotStoredInBackend_False = "NotStoredInBackend.False";
    public static final String NotStoredInBackend_True = "NotStoredInBackend.True";

    // purpose categories
    public static final String ForAppFunctionality = "CollectedFor.AppFunctionality";
    public static final String ForAnalytics = "CollectedFor.Analytics";
    public static final String ForDeveloperCommunications = "CollectedFor.DeveloperCommunications";
    public static final String ForAdvertisingOrMarketing = "CollectedFor.AdvertisingOrMarketing";
    public static final String ForFraudPreventionAndSecurityAndCompliance =
            "CollectedFor.FraudPreventionAndSecurityAndCompliance";
    public static final String ForPersonalization = "CollectedFor.Personalization";
    public static final String ForAccountManagement = "CollectedFor.AccountManagement";

    // more attributes
    public static final String OptionalCollection_False = "OptionalCollection.False";
    public static final String OptionalCollection_True = "OptionalCollection.True";
}
