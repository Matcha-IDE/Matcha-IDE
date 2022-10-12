package org.intellij.privacyHelper.codeInspection.utils;

import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionEngine;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ex.InspectionManagerEx;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.find.FindModel;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlComment;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.GotItTooltip;
import com.intellij.ui.HighlightedRegion;
import com.intellij.ui.JBColor;
import com.intellij.util.CharTable;
import com.intellij.util.IncorrectOperationException;
import kotlin.Unit;
import org.intellij.privacyHelper.codeInspection.inspections.LibraryConfigXmlInspection;
import org.intellij.privacyHelper.codeInspection.instances.ThirdPartyDependencyInstance;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.ideUI.IdeUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 1/18/18.
 */
public class CoconutUIUtil {
    public static final String AddAnnotationTodoTemplate = "Annotate the %s";
    public static final String NetworkKeywordRegex = "net|upload|download|url|http|sock|web|ssl|connection|client|request";
    public static final String ReviewCompleteAnnotationText = "Review complete annotations (no action needed)";
    public static final String VerifyCustomLibText = "Verify custom 3rd-party SDK data usage";
    public static final String VerifiedCustomLibText = "Review verified custom 3rd-party SDK data usage (no action needed)";
    public static final String DefaultLibText = "Review default library data usage (no action needed)";

    public static final String libraryPrivacyTemplate = "<library-privacy\n" +
            "        xmlns=\"https://gist.githubusercontent.com/i7mist\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"https://gist.githubusercontent.com/i7mist https://gist.github.com/i7mist/72e701e0ba6b5ac698782f2dd136f04b/raw/\">\n" +
            "%s\n" +
            "</library-privacy>";

    public static final String comment =
            "The following <library-custom-usage> tag is auto-generated. Each <data> tag represents potential data\n" +
                    "         collection or sharing behavior depending on your library usage. Delete a <data> tag if it doesn't\n" +
                    "         apply to your library usage and keep it otherwise. You can also add new <data> tags if your custom\n" +
                    "         data usage has not been covered in the auto-generated tags. Remember to set \"verified\" to true\n" +
                    "         once you've finished checking all <data>.";
    public static final String commentFormatted =
            "    <!-- The following <library-custom-usage> tag is auto-generated. Each <data> tag represents potential data\n" +
                    "         collection or sharing behavior depending on your library usage. Delete a <data> tag if it doesn't\n" +
                    "         apply to your library usage and keep it otherwise. You can also add new <data> tags if your custom\n" +
                    "         data usage has not been covered in the auto-generated tags. Remember to set \"verified\" to true\n" +
                    "         once you've finished checking all <data>. -->\n";
    public static final String libraryElementTemplate =
            "    <library-custom-usage name=\"%s\" verified=\"false\">\n" +
                    "%s\n" +
                    "    </library-custom-usage>\n";
    public static final String dataElementTemplate = "        <data type=\"%s\" action=\"%s\" ephemeral=\"%s\" required=\"%s\"\n" +
            "            app-functionality=\"%s\" analytics=\"%s\" developer-communications=\"%s\"\n" +
            "            advertising-or-marketing=\"%s\" fraud-prevention-and-security-and-compliance=\"%s\"\n" +
            "            personalization=\"%s\" account-management=\"%s\">\n" +
            "%s\n" +
            "        </data>";

    // FIXME: also search these keywords in the manifest file
    public static final Map<PersonalDataGroup, String[]> dataGroupPermissionMap = Collections.unmodifiableMap(new HashMap<>() {{
        put(PersonalDataGroup.PersonalInfo_Name, new String[] {"BIND_AUTOFILL_SERVICE", "GET_ACCOUNTS"});
        put(PersonalDataGroup.PersonalInfo_EmailAddress, new String[] {"BIND_AUTOFILL_SERVICE", "GET_ACCOUNTS"});
        put(PersonalDataGroup.PersonalInfo_UserIds, new String[] {"BIND_AUTOFILL_SERVICE", "GET_ACCOUNTS"});
        put(PersonalDataGroup.PersonalInfo_Address, new String[] {"BIND_AUTOFILL_SERVICE", "GET_ACCOUNTS"});
        put(PersonalDataGroup.PersonalInfo_PhoneNumber, new String[] {"BIND_AUTOFILL_SERVICE", "GET_ACCOUNTS",
                "READ_CALL_LOG", "READ_PHONE_NUMBERS",
                "READ_PHONE_STATE", "READ_SMS"});
        put(PersonalDataGroup.PersonalInfo_RaceAndEthnicity, new String[] {"BIND_AUTOFILL_SERVICE", "GET_ACCOUNTS"});
        put(PersonalDataGroup.PersonalInfo_PoliticalOrReligiousBeliefs, new String[] {"BIND_AUTOFILL_SERVICE",
                "GET_ACCOUNTS"});
        put(PersonalDataGroup.PersonalInfo_SexualOrientation, new String[] {"BIND_AUTOFILL_SERVICE", "GET_ACCOUNTS"});
        put(PersonalDataGroup.PersonalInfo_OtherPersonalInfo, new String[] {"BIND_AUTOFILL_SERVICE",
                "GET_ACCOUNTS"});
        put(PersonalDataGroup.FinancialInfo_UserPaymentInfo, new String[] {"BIND_AUTOFILL_SERVICE"});
        put(PersonalDataGroup.FinancialInfo_PurchaseHistory, new String[] {});
        put(PersonalDataGroup.FinancialInfo_CreditScore, new String[] {});
        put(PersonalDataGroup.FinancialInfo_OtherFinancialInfo, new String[] {});
        put(PersonalDataGroup.Calendar_CalendarEvents, new String[] {"READ_CALENDAR", "WRITE_CALENDAR"});
        put(PersonalDataGroup.PhotosAndVideos_Photos, new String[] {"READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"});
        put(PersonalDataGroup.PhotosAndVideos_Videos, new String[] {"READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"});
        put(PersonalDataGroup.Contacts_Contacts, new String[] {"ACCEPT_HANDOVER", "ADD_VOICEMAIL", "ANSWER_PHONE_CALLS",
                "CALL_PHONE", "PROCESS_OUTGOING_CALLS", "READ_CALL_LOG", "READ_CONTACTS", "READ_PHONE_NUMBERS",
                "READ_PHONE_STATE", "READ_SMS", "RECEIVE_MMS", "RECEIVE_SMS", "RECEIVE_WAP_PUSH", "SEND_SMS",
                "WRITE_CONTACTS"});
        put(PersonalDataGroup.Location_ApproximateLocation, new String[] {"ACCESS_COARSE_LOCATION", "ACCESS_MEDIA_LOCATION"});
        put(PersonalDataGroup.Location_PreciseLocation, new String[] {"ACCESS_FINE_LOCATION", "ACCESS_MEDIA_LOCATION"});
        put(PersonalDataGroup.HealthAndFitness_HealthInfo, new String[] {"ACTIVITY_RECOGNITION", "BODY_SENSORS"});
        put(PersonalDataGroup.HealthAndFitness_FitnessInfo, new String[] {"ACTIVITY_RECOGNITION", "BODY_SENSORS"});
        put(PersonalDataGroup.Messages_Emails, new String[] {});
        put(PersonalDataGroup.Messages_SmsOrMms, new String[] {"READ_SMS", "RECEIVE_MMS", "RECEIVE_SMS",
                "RECEIVE_WAP_PUSH", "SEND_SMS", "WRITE_SMS"});
        put(PersonalDataGroup.Messages_InAppMessages, new String[] {});
        put(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds, new String[] {"AD_ID", "READ_PRIVILEGED_PHONE_STATE"});
        put(PersonalDataGroup.FilesAndDocs_FilesAndDocs, new String[] {"READ_EXTERNAL_STORAGE",
                "WRITE_EXTERNAL_STORAGE", "MANAGE_EXTERNAL_STORAGE"});
        put(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings, new String[] {"CAPTURE_AUDIO_OUTPUT", "RECORD_AUDIO",
                "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"});
        put(PersonalDataGroup.AudioFiles_MusicFiles, new String[] {"READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"});
        put(PersonalDataGroup.AudioFiles_OtherUserAudioFiles, new String[] {"CAPTURE_AUDIO_OUTPUT", "RECORD_AUDIO",
                "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"});
        put(PersonalDataGroup.AppActivity_AppInteractions, new String[] {"QUERY_ALL_PACKAGES"});
        put(PersonalDataGroup.AppActivity_InstalledApps, new String[] {});
        put(PersonalDataGroup.AppActivity_InAppSearchHistory, new String[] {});
        put(PersonalDataGroup.AppActivity_OtherUserGeneratedContent, new String[] {});
        put(PersonalDataGroup.AppActivity_OtherUserActivities, new String[] {});
        put(PersonalDataGroup.WebBrowsing_WebBrowsingHistory, new String[] {});
        put(PersonalDataGroup.AppInfoAndPerformance_CrashLogs, new String[] {});
        put(PersonalDataGroup.AppInfoAndPerformance_Diagnostics, new String[] {"BATTERY_STATS"});
        put(PersonalDataGroup.AppInfoAndPerformance_OtherAppPerformanceData, new String[] {});
    }});

    public static final Map<PersonalDataGroup, String[]> dataGroupKeywordMap = Collections.unmodifiableMap(new HashMap<>() {{
        put(PersonalDataGroup.PersonalInfo_Name, new String[] {"name"});
        put(PersonalDataGroup.PersonalInfo_EmailAddress, new String[] {"email"});
        put(PersonalDataGroup.PersonalInfo_UserIds, new String[] {"uid", "user id"});
        put(PersonalDataGroup.PersonalInfo_Address, new String[] {"home address", "city", "country",
                "zip code"});
        put(PersonalDataGroup.PersonalInfo_PhoneNumber, new String[] {"phone", "default dialer"});
        put(PersonalDataGroup.PersonalInfo_RaceAndEthnicity, new String[] {"(?<!t)race", "ethnicity", "african",
                "indian", "asian"});
        put(PersonalDataGroup.PersonalInfo_PoliticalOrReligiousBeliefs, new String[] {"political", "religious"});
        put(PersonalDataGroup.PersonalInfo_SexualOrientation, new String[] {"sexual orientation", "gay", "lesbian",
                "transgender", "bisexual", "queer"});
        put(PersonalDataGroup.PersonalInfo_OtherPersonalInfo, new String[] {"birth",
                "nationality", "gender", "male", "female", "non-binary", "veteran"});
        put(PersonalDataGroup.FinancialInfo_UserPaymentInfo, new String[] {"credit card", "billing", "cvv",
                "routing number", "account number", "bank"});
        put(PersonalDataGroup.FinancialInfo_PurchaseHistory, new String[] {"purchase"});
        put(PersonalDataGroup.FinancialInfo_CreditScore, new String[] {"credit score"});
        put(PersonalDataGroup.FinancialInfo_OtherFinancialInfo, new String[] {"salary", "debt"});
        put(PersonalDataGroup.Calendar_CalendarEvents, new String[] {"calendar", "attendee"});
        put(PersonalDataGroup.PhotosAndVideos_Photos, new String[] {"photo", "barcode", "image",
                "picture", "media"});
        put(PersonalDataGroup.PhotosAndVideos_Videos, new String[] {"video", "recording", "media"});
        put(PersonalDataGroup.Contacts_Contacts, new String[] {"contact", "call history",
                "interaction duration"});
        put(PersonalDataGroup.Location_ApproximateLocation, new String[] {"location",
                "city", "country", "ip address"});
        put(PersonalDataGroup.Location_PreciseLocation, new String[] {"location",
                "latitude", "longitude"});
        put(PersonalDataGroup.HealthAndFitness_HealthInfo, new String[] {"health", "medical", "medicine", "symptom",
                "disease", "doctor", "physician", "sleep", "wellness", "therapist", "emergency", "emergencies",
                "period", "pregnancy"});
        put(PersonalDataGroup.HealthAndFitness_FitnessInfo, new String[] {"fitness", "exercise",
                "workout", "sport", "diet", "nutrition"});
        put(PersonalDataGroup.Messages_Emails, new String[] {"email", "sender", "recipient", "subject"});
        put(PersonalDataGroup.Messages_SmsOrMms, new String[] {"message", "sms", "mms", "sender", "recipient",
                "subject"});
        put(PersonalDataGroup.Messages_InAppMessages, new String[] {"message", "chat", "reply", "replies",
                "comment", "sender", "recipient", "subject"});
        put(PersonalDataGroup.DeviceOrOtherIds_DeviceOrOtherIds, new String[] {"mac address", "widevine", "device id",
                "instance id", "app id", "advertising id", "fingerprint", "user agent", "unique id", "token",
                "AdvertisingIdClient"});
        put(PersonalDataGroup.FilesAndDocs_FilesAndDocs, new String[] {"file", "document", "backup", "restore",
                "download", "storage", "media"});
        put(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings, new String[] {"voice", "sound", "recording"});
        put(PersonalDataGroup.AudioFiles_MusicFiles, new String[] {"music", "song"});
        put(PersonalDataGroup.AudioFiles_OtherUserAudioFiles, new String[] {});
        put(PersonalDataGroup.AppActivity_AppInteractions, new String[] {"selected", "visit number", "view number",
                "getItemAtPosition", "getItemIdAtPosition", "AccessibilityService", "TextService",
                "Instrumentation", "shortcut"});
        put(PersonalDataGroup.AppActivity_InstalledApps, new String[] {"installed app"});
        put(PersonalDataGroup.AppActivity_InAppSearchHistory, new String[] {"search"});
        put(PersonalDataGroup.AppActivity_OtherUserGeneratedContent, new String[] {"bios", "note",
                "response"});
        put(PersonalDataGroup.AppActivity_OtherUserActivities, new String[] {"gameplay", "dialog option"});
        put(PersonalDataGroup.WebBrowsing_WebBrowsingHistory, new String[] {"browser", "cookie", "browser cache",
                "browsing cache", "search", "web view"});
        put(PersonalDataGroup.AppInfoAndPerformance_CrashLogs, new String[] {"crash", "stack trace"});
        put(PersonalDataGroup.AppInfoAndPerformance_Diagnostics, new String[] {"ActivityManager",
                "ApplicationErrorReport", "ApplicationExitInfo", "BatteryManager", "Benchmark", "Debug", "HealthStats",
                "Macrobenchmark", "PowerManager", "StrictMode", "battery", "loading time", "latency", "frame rate",
                "diagnostics"});
        put(PersonalDataGroup.AppInfoAndPerformance_OtherAppPerformanceData, new String[] {"performance"});
    }});

    public static void navigateMainEditorToPsiElement(SmartPsiElementPointer element) {
        VirtualFile virtualFile = element.getVirtualFile();
        Project myProject = element.getProject();
        Segment elementRange = element.getRange();
        if (elementRange == null) {
            return;
        }
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(myProject, virtualFile,
                elementRange.getStartOffset());

        IdeUI.submitTask(() -> openFileDescriptor.navigate(false));
    }

    public static void navigateMainEditorToPsiElement(PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        Project myProject = element.getProject();
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(myProject, virtualFile,
                element.getTextRange().getStartOffset());

        IdeUI.submitTask(() -> {
            openFileDescriptor.navigate(false);
        });
    }

    public static void navigateMainEditorToPsiElement(PsiElement element, TooltipShowCallback callback) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        Project myProject = element.getProject();
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(myProject, virtualFile,
                element.getTextRange().getStartOffset());

        IdeUI.submitTask(() -> {
            openFileDescriptor.navigate(false);
            callback.showTooltip();
        });
    }

    public static void navigateMainEditorToPsiElement(SmartPsiElementPointer element, TooltipShowCallback callback) {
        VirtualFile virtualFile = element.getVirtualFile();
        Project myProject = element.getProject();
        Segment elementRange = element.getRange();
        if (elementRange == null) {
            return;
        }
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(myProject, virtualFile,
                elementRange.getStartOffset());

        IdeUI.submitTask(() -> {
            openFileDescriptor.navigate(false);
            callback.showTooltip();
        });
    }

    static Balloon currentBallon = null;
    public static void showTooltip(TooltipInfo [] tooltipInfo) {
        if (tooltipInfo == null || tooltipInfo.length == 0) {
            return;
        }
        if (currentBallon != null) {
            currentBallon.dispose();
        }
        Project project = tooltipInfo[0].project;
        GotItTooltip tooltip = new GotItTooltip(tooltipInfo[0].id, tooltipInfo[0].text, project)
                .withPosition(tooltipInfo[0].position)
                .withShowCount(1);
        if (!tooltipInfo[0].header.isEmpty()) {
            tooltip.withHeader(tooltipInfo[0].header);
        }
        tooltip.setOnBalloonCreated((balloon) -> {
            currentBallon = balloon;
            balloon.addListener(new JBPopupListener() {
                @Override
                public void onClosed(@NotNull LightweightWindowEvent event) {
                    JBPopupListener.super.onClosed(event);
                    currentBallon = null;
                    showTooltip(Arrays.copyOfRange(tooltipInfo, 1, tooltipInfo.length));
                }
            });
            return Unit.INSTANCE;
        });
        if (tooltipInfo[0].target instanceof PsiElement) {
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            PsiElement psiElement = (PsiElement) tooltipInfo[0].target;
            int offset = psiElement.getTextOffset();
            // make the tooltip show next to the modifierList
            if (editor != null) {
                tooltip.show(editor.getContentComponent(), (c, b) -> editor.offsetToXY(offset));
            }
        } else {
            JComponent component = (JComponent) tooltipInfo[0].target;
            tooltip.show(component, (c, b) -> new Point(0, 0));
        }
    }

    public static PsiWhiteSpace nl(PsiElement context)
    {
        PsiWhiteSpace newline;
        while (!(context instanceof ASTNode))
        {
            context = context.getFirstChild ();
            if (context == null)
                return null;
        }

        CharTable
                charTable = SharedImplUtil.findCharTableByTree (
                (ASTNode) context);
        newline = (PsiWhiteSpace) Factory
                .createSingleLeafElement(TokenType.WHITE_SPACE, "\n",
                        charTable, PsiManager.getInstance (context.getProject()));

        return newline;
    }


    public static void pushNotification(Project project, String title, String content, NotificationType type) {
        NotificationGroup NOTIFICATION_GROUP =
                new NotificationGroup("Required Plugins", NotificationDisplayType.BALLOON, true);
        NOTIFICATION_GROUP.createNotification(title, content, type, (notification, hyperlinkEvent) -> {

                        }).notify(project);
    }

    public static String prettifyCamelCaseString(String camelCaseString) {
        return String.join(" ", camelCaseString.split("(?<=[a-z])(?=[A-Z])")).replace(".", " ");
    }

    static public String prettifyDataCategoryString(String dataGroup) {
        if (dataGroup.split("_").length > 1) {
            return prettifyCamelCaseString(dataGroup.split("_")[0]);
        } else {
            return prettifyCamelCaseString(dataGroup);
        }
    }

    static public String prettifyDataTypeString(String dataGroup) {
        if (dataGroup.split("_").length > 1) {
            return prettifyCamelCaseString(dataGroup.split("_")[1]);
        } else {
            return prettifyCamelCaseString(dataGroup);
        }
    }

    public static boolean checkUsageSource(Set<String> dataUsage, String target) {
        for (String usage : dataUsage) {
            if (target.equals(usage.split("_")[2])) {
                return true;
            }
        }
        return false;
    }

    public static void presentDataWithSource(@NotNull PresentationData presentation, Set<String> dataUsage, String data) {
        boolean usageByApp = checkUsageSource(dataUsage, "byApp");
        boolean usageByLibrary = checkUsageSource(dataUsage, "byLibrary");
        String source;
        if (usageByApp && usageByLibrary) {
            source = "by app and library";
        } else if (usageByApp) {
            source = "by app";
        } else {
            source = "by library";
        }
        presentation.setPresentableText(String.format("%s (%s)", data, source));
    }

    public static void labelErrorText(ArrayList<HighlightedRegion> myHighlightedRegions, String name, String todo,
                                      String all, boolean hasError) {
        TextAttributes redText = new TextAttributes();
        redText.setForegroundColor(JBColor.RED);

        myHighlightedRegions.add(new HighlightedRegion(
                0,
                name.length(),
                new TextAttributes()));
        myHighlightedRegions.add(new HighlightedRegion(
                name.length(),
                name.length() + todo.length(),
                hasError ? redText : new TextAttributes()));
        myHighlightedRegions.add(new HighlightedRegion(
                name.length() + todo.length(),
                name.length() + todo.length() + all.length(),
                new TextAttributes()));
    }

    public static void labelLinkText(ArrayList<HighlightedRegion> myHighlightedRegions, String name, String todo) {
        TextAttributes linkText = new TextAttributes();
        linkText.setForegroundColor(JBColor.blue);
        linkText.setEffectType(EffectType.LINE_UNDERSCORE);
        linkText.setEffectColor(JBColor.blue);

        myHighlightedRegions.add(new HighlightedRegion(
                0,
                name.length(),
                new TextAttributes()));
        myHighlightedRegions.add(new HighlightedRegion(
                name.length(),
                name.length() + todo.length(),
                linkText));
    }

    public static String getKeywordRegex(PersonalDataGroup myDataGroup) {
        String[] rawKeywordList = dataGroupKeywordMap.get(myDataGroup);
        String[] rawPermissionList = dataGroupPermissionMap.get(myDataGroup);
        ArrayList<String> keywords = new ArrayList<>();
        keywords.addAll(Arrays.stream(rawPermissionList).map(String::toLowerCase).collect(Collectors.toList()));
        keywords.addAll(Arrays.stream(rawKeywordList)
                .map(s -> {
                    Set<String> prefixes = new HashSet<>();
                    for (PersonalDataGroup dataGroup : PersonalDataGroup.values()) {
                        String groupString = "DataType." + dataGroup.toString();
                        if (groupString.toString().toLowerCase().contains(s.toLowerCase())) {
                            int lastIndex = 0;
                            while (lastIndex != -1) {
                                lastIndex = groupString.toLowerCase().indexOf(s.toLowerCase(), lastIndex);
                                if (lastIndex != -1) {
                                    prefixes.add(groupString.substring(0, lastIndex));
                                    lastIndex += s.length();
                                }
                            }
                        }
                    }
                    if (prefixes.isEmpty()) {
                        return s;
                    } else {
                        return String.format("(?<!%s)%s", String.join("|", prefixes), s);
                    }
                })
                .map(s -> s.replace(" ", " *"))
                .collect(Collectors.toList()));
        return String.join("|", keywords);
    }

    @NotNull
    public static FindModel getFindModel(String keywordPattern) {
        FindModel findModel = new FindModel();
        findModel.setCaseSensitive(false);
        findModel.setRegularExpressions(true);
        findModel.setFileFilter("*.java");
        findModel.setStringToFind(keywordPattern);
        return findModel;
    }

    public static void updateLibraryMaps(Project myProject,
                                         HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap) {
        libraryDataMap.clear();

        ThirdPartyDependencyInstance[] dependencyInstances =
                PrivacyPracticesHolder.getInstance(myProject).getThirdPartyDependencyInstances();
        for (ThirdPartyDependencyInstance dependencyInstance : dependencyInstances) {
            if (dependencyInstance.getDependencyInfo() == null) {
                continue;
            }
            if (!libraryDataMap.containsKey(dependencyInstance.getDependencyInfo().libName)) {
                libraryDataMap.put(dependencyInstance.getDependencyInfo().libName, new ArrayList<>());
            }
            Collections.addAll(libraryDataMap.get(dependencyInstance.getDependencyInfo().libName),
                    dependencyInstance.getDependencyInfo().getDataPractices(false, true));
        }
    }

    @NotNull
    public static XmlComment createXmlComment(Project project, String s) throws IncorrectOperationException {
        final XmlTag element = XmlElementFactory.getInstance(project).createTagFromText("<foo><!-- " + s + " --></foo>", XMLLanguage.INSTANCE);
        final XmlComment newComment = PsiTreeUtil.getChildOfType(element, XmlComment.class);
        assert newComment != null;
        return newComment;
    }

    public static XmlFile getLibraryPrivacyConfigFile(Project myProject) {
        PsiManager manager = PsiManager.getInstance(myProject);
        PsiDirectory baseDirectory = manager.findDirectory(myProject.getBaseDir());
        if (baseDirectory == null) {
            return null;
        }
        PsiDirectory xmlDirectory = baseDirectory.findSubdirectory("safety_section");
        if (xmlDirectory == null) {
            return null;
        }
        return (XmlFile) Objects.requireNonNull(xmlDirectory).findFile(
                "library_safety_section_config.xml");
    }

    public static boolean checkLibraryConfigFileCompletenessAndUpdate(Project myProject,
                                                                      HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap,
                                                                      boolean update) {
        XmlFile libraryPrivacyConfigFile = getLibraryPrivacyConfigFile(myProject);
        if (libraryPrivacyConfigFile != null) {
            XmlElementFactory xmlElementFactory = XmlElementFactory.getInstance(myProject);
            var ref = new Object() {
                XmlTag rootTag = libraryPrivacyConfigFile.getRootTag();
            };
            if (ref.rootTag == null || ref.rootTag.isEmpty()) {
                if (ref.rootTag == null) {
                    ref.rootTag = xmlElementFactory.createTagFromText(generateConfigFile(libraryDataMap),
                            XMLLanguage.INSTANCE);
                    WriteCommandAction.runWriteCommandAction(myProject, () -> {
                        libraryPrivacyConfigFile.add(ref.rootTag);
                    });
                } else {
                    WriteCommandAction.runWriteCommandAction(myProject, () -> {
                        ref.rootTag.replace(xmlElementFactory.createTagFromText(generateConfigFile(libraryDataMap)));
                    });
                }
                return false;
            } else {
                XmlTag[] libraryTags = Objects.requireNonNull(ref.rootTag).getSubTags();
                ArrayList<String> xmlLibraryNames = new ArrayList<>();
                for (XmlTag libraryTag : libraryTags) {
                    String libraryName = libraryTag.getAttributeValue("name");
                    xmlLibraryNames.add(libraryName);
                }
                boolean hasError = false;
                for (String libraryName : libraryDataMap.keySet()) {
                    if (!libraryDataMap.get(libraryName).isEmpty() && !xmlLibraryNames.contains(libraryName)) {
                        hasError = true;
                        if (update) {
                            XmlComment newComment = createXmlComment(myProject, comment);
                            XmlTag newLibraryTag = xmlElementFactory.createTagFromText(
                                    CoconutUIUtil.generateLibraryElement(libraryName, libraryDataMap));
                            WriteCommandAction.runWriteCommandAction(myProject, () -> {
                                ref.rootTag.add(newComment);
                                XmlComment[] xmlComments = PsiTreeUtil.findChildrenOfType(ref.rootTag, XmlComment.class).toArray(XmlComment[]::new);
                                ref.rootTag.addAfter(newLibraryTag, xmlComments[xmlComments.length - 1]);
                            });
                        }
                    }
                }
                runConfigFileInspection(libraryPrivacyConfigFile, myProject);
                return !hasError;
            }
        } else {
            generateLibraryConfig(myProject, libraryDataMap);
            return false;
        }
    }

    public static void generateLibraryConfig(Project myProject,
                                             HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap) {
        PsiManager manager = PsiManager.getInstance(myProject);
        PsiDirectory baseDirectory = manager.findDirectory(myProject.getBaseDir());
        // check if subdirectory exists
        if (baseDirectory != null) {
            final PsiDirectory[] subDirectory = {baseDirectory.findSubdirectory("safety_section")};
            if (subDirectory[0] == null) {
                ApplicationManager.getApplication().invokeLater(() ->
                    ApplicationManager.getApplication().runWriteAction(()->{
                        subDirectory[0] = baseDirectory.createSubdirectory("safety_section");
                        XmlFile newLibraryConfigFile = (XmlFile) PsiFileFactory.getInstance(myProject).createFileFromText(
                                "library_safety_section_config.xml",
                                XMLLanguage.INSTANCE,
                                generateConfigFile(libraryDataMap));
                        PsiFile existingFile = subDirectory[0].findFile("library_safety_section_config.xml");
                        if (existingFile == null) {
                            subDirectory[0].add(newLibraryConfigFile);
                        }
                    }));
            } else {
                XmlFile newLibraryConfigFile = (XmlFile) PsiFileFactory.getInstance(myProject).createFileFromText(
                        "library_safety_section_config.xml",
                        XMLLanguage.INSTANCE,
                        generateConfigFile(libraryDataMap));
                PsiFile existingFile = subDirectory[0].findFile("library_safety_section_config.xml");
                if (existingFile == null) {
                    subDirectory[0].add(newLibraryConfigFile);
                }
            }
        }
    }

    public static String generateLibraryElement(String libraryName,
                                                HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap) {
        ArrayList<String> dataElementList = new ArrayList<>();
        for (ThirdPartySafetySectionInfo safetySectionInfo : libraryDataMap.get(libraryName)) {
            SafetySectionDataElement safetySectionDataElement = safetySectionInfo.safetySectionDataElement;
            for (Map.Entry<String[], Set<String[]>> dataEntry: safetySectionDataElement.dataPractices.entrySet()) {
                String dataTypeAttribute =
                        String.valueOf(DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING.get(dataEntry.getKey()));
                String actionAttribute;
                if (dataEntry.getValue().contains(safetySectionCollected) &&
                        !dataEntry.getValue().contains(safetySectionShared)) {
                    actionAttribute = "Collected";
                } else if (!dataEntry.getValue().contains(safetySectionCollected) &&
                        dataEntry.getValue().contains(safetySectionShared)) {
                    actionAttribute = "Shared";
                } else if (dataEntry.getValue().contains(safetySectionCollected) &&
                        dataEntry.getValue().contains(safetySectionShared)) {
                    actionAttribute = "Collected_And_Shared";
                } else {
                    assert false;
                    continue;
                }
                String ephemeralAttribute = dataEntry.getValue().contains(safetySectionEphemeral) ? "true" : "false";
                String requiredAttribute = dataEntry.getValue().contains(safetySectionRequired) ? "true" : "false";

                String appFunctionalityAttribute = dataEntry.getValue().contains(safetySectionCollectionAppFunctionality) ||
                        dataEntry.getValue().contains(safetySectionSharingAppFunctionality) ? "true" : "false";
                String analyticsAttribute = dataEntry.getValue().contains(safetySectionCollectionAnalytics) ||
                        dataEntry.getValue().contains(safetySectionSharingAnalytics) ? "true" : "false";
                String developerCommunicationsAttribute = dataEntry.getValue().contains(safetySectionCollectionDevCommunications) ||
                        dataEntry.getValue().contains(safetySectionSharingDevCommunications) ? "true" : "false";
                String advertisingOrMarketingAttribute = dataEntry.getValue().contains(safetySectionCollectionAdvertising) ||
                        dataEntry.getValue().contains(safetySectionSharingAdvertising) ? "true" : "false";
                String fraudPreventionAttribute = dataEntry.getValue().contains(safetySectionCollectionFraudPrevention) ||
                        dataEntry.getValue().contains(safetySectionSharingFraudPrevention) ? "true" : "false";
                String personalizationAttribute = dataEntry.getValue().contains(safetySectionCollectionPersonalization) ||
                        dataEntry.getValue().contains(safetySectionSharingPersonalization) ? "true" : "false";
                String accountManagementAttribute = dataEntry.getValue().contains(safetySectionCollectionAccountManagement) ||
                        dataEntry.getValue().contains(safetySectionSharingAccountManagement) ? "true" : "false";

                dataElementList.add(String.format(dataElementTemplate, dataTypeAttribute, actionAttribute,
                        ephemeralAttribute, requiredAttribute,
                        appFunctionalityAttribute, analyticsAttribute, developerCommunicationsAttribute,
                        advertisingOrMarketingAttribute, fraudPreventionAttribute, personalizationAttribute,
                        accountManagementAttribute, formattedNote(safetySectionInfo.note)));
            }
        }
        return String.format(libraryElementTemplate, libraryName, String.join("\n\n", dataElementList));
    }

    private static void runConfigFileInspection(PsiFile file, Project myProject) {
        InspectionProfileImpl profile =
                (InspectionProfileImpl) InspectionProjectProfileManager.getInstance(myProject).getInspectionProfile();
        InspectionManagerEx inspectionManager = (InspectionManagerEx) InspectionManager.getInstance(myProject);
        GlobalInspectionContext context = inspectionManager.createNewGlobalContext(false);
        InspectionToolWrapper wrapper = profile.getInspectionTool(new LibraryConfigXmlInspection().getShortName(), myProject);
        InspectionEngine.runInspectionOnFile(file, wrapper, context);
    }

    private static String generateConfigFile(HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap) {
        ArrayList<String> xmlList = new ArrayList<>();
        for (Map.Entry<String, ArrayList<ThirdPartySafetySectionInfo>> entry : libraryDataMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                xmlList.add(commentFormatted + CoconutUIUtil.generateLibraryElement(entry.getKey(), libraryDataMap));
            }
        }
        return String.format(libraryPrivacyTemplate, String.join("\n\n", xmlList));
    }

    private static String formattedNote(String note) {
        String [] noteLines = note.split("\n");
        return String.join("\n",
                Arrays.stream(noteLines).map(s -> "                " + s).toArray(String[]::new));
    }

}
