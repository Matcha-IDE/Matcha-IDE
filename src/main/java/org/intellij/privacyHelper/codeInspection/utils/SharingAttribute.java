package org.intellij.privacyHelper.codeInspection.utils;

public class SharingAttribute {
    public static final String SharedWithThirdParty_True = "SharedWithThirdParty.True";
    public static final String SharedWithThirdParty_False = "SharedWithThirdParty.False";

    // exceptions
    public static final String OnlySharedWithServiceProviders_True = "OnlySharedWithServiceProviders.True"; // “Service provider” means an entity that processes user data on behalf of the developer and based on the developer’s instructions.
    public static final String OnlySharedWithServiceProviders_False = "OnlySharedWithServiceProviders.False";
    public static final String OnlySharedForLegalPurposes_True = "OnlySharedForLegalPurposes.True"; // Transferring user data for specific legal purposes, such as in response to a legal obligation or government requests.
    public static final String OnlySharedForLegalPurposes_False = "OnlySharedForLegalPurposes.False";
    public static final String OnlyInitiatedByUser_True = "OnlyInitiatedByUser.True";
    public static final String OnlyInitiatedByUser_False = "OnlyInitiatedByUser.False";
    public static final String OnlyAfterGettingUserConsent_True = "OnlyAfterGettingUserConsent.True";
    public static final String OnlyAfterGettingUserConsent_False = "OnlyAfterGettingUserConsent.False";
    public static final String OnlyTransferringAnonymousData_True = "OnlyTransferringAnonymousData.True";
    public static final String OnlyTransferringAnonymousData_False = "OnlyTransferringAnonymousData.False";

    // purpose categories
    public static final String ForAppFunctionality = "SharedFor.AppFunctionality";
    public static final String ForAnalytics = "SharedFor.Analytics";
    public static final String ForDeveloperCommunications = "SharedFor.DeveloperCommunications";
    public static final String ForAdvertisingOrMarketing = "SharedFor.AdvertisingOrMarketing";
    public static final String ForFraudPreventionAndSecurityAndCompliance = "SharedFor.FraudPreventionAndSecurityAndCompliance";
    public static final String ForPersonalization = "SharedFor.Personalization";
    public static final String ForAccountManagement = "SharedFor.AccountManagement";
}
