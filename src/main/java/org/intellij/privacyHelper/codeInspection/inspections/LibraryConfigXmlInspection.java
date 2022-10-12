package org.intellij.privacyHelper.codeInspection.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlTag;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup;
import org.intellij.privacyHelper.codeInspection.utils.SafetySectionDataElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.DATA_TYPE_PRIVACY_LABEL_MAPPING;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class LibraryConfigXmlInspection extends XmlSuppressableInspectionTool {
    @Override
    public @NonNls @NotNull String getShortName() {
        return "LibraryConfigXmlInspection";
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new XmlElementVisitor() {
            @Override
            public void visitXmlTag(XmlTag tag) {
                if (!"library_safety_section_config.xml".equals(tag.getContainingFile().getName())) {
                    return;
                }
                if ("library-custom-usage".equals(tag.getName())) {
                    if (tag.getAttribute("verified") == null) {
                        holder.registerProblem(tag, "Please add the \"verified\" attribute. " +
                                        "The value should be set to \"true\" after you have verified " +
                                        "whether your library's custom data usage is correctly listed below",
                                ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);
                        return;
                    }
                    if (tag.getAttribute("name") == null) {
                        holder.registerProblem(tag, "Please provide a valid library name",
                                ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);
                        return;
                    }
                    String libraryName = tag.getAttributeValue("name");
                    if (PrivacyPracticesHolder.getInstance(tag.getProject()).getThirdPartyDependencyInstances(
                            Objects.requireNonNull(libraryName)).length == 0) {
                        holder.registerProblem(Objects.requireNonNull(tag.getAttribute("name")),
                                "This library name doesn't match any integrated library. " +
                                        "If you don't know how to fix it, you can remove the entire " +
                                        "<library-custom-usage> tag and let the system re-generate it.",
                                ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);
                        return;
                    }
                    boolean verified = "true".equals(tag.getAttributeValue("verified"));
                    // TODO: also add the two values as xml attributes
                    boolean encryptedInTransit = true;
                    boolean userRequestDelete = false;
                    HashMap<String[], Set<String[]>> dataPractices = new HashMap<>();
                    if (!verified) {
                        holder.registerProblem(Objects.requireNonNull(tag.getAttribute("verified")),
                                "Please verify whether your library's custom" +
                                        "data usage is correctly listed below and then change the value of " +
                                        "\"verified\" to \"true\"",
                                ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);

                    } else {
                        XmlTag[] dataTags = tag.getSubTags();
                        for (XmlTag dataTag : dataTags) {
                            String type = dataTag.getAttributeValue("type");
                            String action = dataTag.getAttributeValue("action");
                            String appFunctionality = dataTag.getAttributeValue("app-functionality");
                            String analytics = dataTag.getAttributeValue("analytics");
                            String developerCommunications = dataTag.getAttributeValue("developer-communications");
                            String advertisingOrMarketing = dataTag.getAttributeValue("advertising-or-marketing");
                            String fraudPrevention = dataTag.getAttributeValue("fraud-prevention-and-security-and-compliance");
                            String personalization = dataTag.getAttributeValue("personalization");
                            String accountManagement = dataTag.getAttributeValue("account-management");
                            String ephemeral = dataTag.getAttributeValue("ephemeral");
                            String required = dataTag.getAttributeValue("required");

                            if (type == null || action == null || appFunctionality == null || analytics == null ||
                                    developerCommunications == null || advertisingOrMarketing == null ||
                                    fraudPrevention == null || personalization == null || accountManagement == null ||
                                    ephemeral == null || required == null) {
                                continue;
                            }

                            PersonalDataGroup dataType;
                            try {
                                dataType = PersonalDataGroup.valueOf(type);
                            } catch (IllegalArgumentException ignored) {
                                dataType = null;
                            }

                            Set<String[]> dataUsage = new HashSet<>();

                            if ("Collected".equals(action) || "Collected_And_Shared".equals(action)) {
                                dataUsage.add(safetySectionCollected);
                            }

                            if ("Shared".equals(action) || "Collected_And_Shared".equals(action)) {
                                dataUsage.add(safetySectionShared);
                            }

                            if ("true".equals(ephemeral)) {
                                dataUsage.add(safetySectionEphemeral);
                            }

                            if ("true".equals(required)) {
                                dataUsage.add(safetySectionRequired);
                            } else {
                                dataUsage.add(safetySectionOptional);
                            }

                            if ("true".equals(appFunctionality)) {
                                if (dataUsage.contains(safetySectionCollected)) {
                                    dataUsage.add(safetySectionCollectionAppFunctionality);
                                }
                                if (dataUsage.contains(safetySectionShared)) {
                                    dataUsage.add(safetySectionSharingAppFunctionality);
                                }
                            }

                            if ("true".equals(analytics)) {
                                if (dataUsage.contains(safetySectionCollected)) {
                                    dataUsage.add(safetySectionCollectionAnalytics);
                                }
                                if (dataUsage.contains(safetySectionShared)) {
                                    dataUsage.add(safetySectionSharingAnalytics);
                                }
                            }

                            if ("true".equals(developerCommunications)) {
                                if (dataUsage.contains(safetySectionCollected)) {
                                    dataUsage.add(safetySectionCollectionDevCommunications);
                                }
                                if (dataUsage.contains(safetySectionShared)) {
                                    dataUsage.add(safetySectionSharingDevCommunications);
                                }
                            }

                            if ("true".equals(advertisingOrMarketing)) {
                                if (dataUsage.contains(safetySectionCollected)) {
                                    dataUsage.add(safetySectionCollectionAdvertising);
                                }
                                if (dataUsage.contains(safetySectionShared)) {
                                    dataUsage.add(safetySectionSharingAdvertising);
                                }
                            }

                            if ("true".equals(fraudPrevention)) {
                                if (dataUsage.contains(safetySectionCollected)) {
                                    dataUsage.add(safetySectionCollectionFraudPrevention);
                                }
                                if (dataUsage.contains(safetySectionShared)) {
                                    dataUsage.add(safetySectionSharingFraudPrevention);
                                }
                            }

                            if ("true".equals(personalization)) {
                                if (dataUsage.contains(safetySectionCollected)) {
                                    dataUsage.add(safetySectionCollectionFraudPrevention);
                                }
                                if (dataUsage.contains(safetySectionShared)) {
                                    dataUsage.add(safetySectionSharingFraudPrevention);
                                }
                            }

                            if ("true".equals(accountManagement)) {
                                if (dataUsage.contains(safetySectionCollected)) {
                                    dataUsage.add(safetySectionCollectionAccountManagement);
                                }
                                if (dataUsage.contains(safetySectionShared)) {
                                    dataUsage.add(safetySectionSharingAccountManagement);
                                }
                            }
                            dataPractices.put(DATA_TYPE_PRIVACY_LABEL_MAPPING.get(dataType), dataUsage);
                        }
                    }
                    PrivacyPracticesHolder.getInstance(tag.getProject()).addThirdPartyCustomDataInstance(tag,
                            libraryName, verified, new SafetySectionDataElement(
                                    encryptedInTransit, userRequestDelete, dataPractices));
                }
            }
        };
    }
}
