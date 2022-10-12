package org.intellij.privacyHelper.codeInspection.utils;

import java.util.ArrayList;

public class ThirdPartyDependencyInfo {
    public String libName;
    public String[] gradlePublicDependencyPatterns;
    public String[] gradleManualDependencyPatterns;
    public String[] importPackagePatterns;
    public ThirdPartySafetySectionInfo [] thirdPartySafetySectionInfos;


    ThirdPartyDependencyInfo(String libName, String[] gradlePublicDependencyPatterns,
                             String[] gradleManualDependencyPatterns, String[] importPackagePatterns,
                             ThirdPartySafetySectionInfo ... thirdPartySafetySectionInfos) {
        this.libName = libName;
        this.gradlePublicDependencyPatterns = gradlePublicDependencyPatterns; // ".*" + dependencyString.replaceAll("\\.", "\\\\.") + ".*";
        this.gradleManualDependencyPatterns = gradleManualDependencyPatterns;
        this.importPackagePatterns = importPackagePatterns;
        this.thirdPartySafetySectionInfos = thirdPartySafetySectionInfos;
    }

    public SafetySectionDataElement getSynthesizedDefaultDataPractices() {
        SafetySectionDataElement safetySectionDataElement = new SafetySectionDataElement();
        for (ThirdPartySafetySectionInfo thirdPartySafetySectionInfo : thirdPartySafetySectionInfos) {
            if (thirdPartySafetySectionInfo.isDefaultCollection) {
                safetySectionDataElement.combine(thirdPartySafetySectionInfo.safetySectionDataElement);
            }
        }
        return safetySectionDataElement;
    }

    public ThirdPartySafetySectionInfo[] getDataPractices(boolean getDefaultCollection, boolean getCustomCollection) {
        ArrayList<ThirdPartySafetySectionInfo> dataPractices = new ArrayList<>();
        for (ThirdPartySafetySectionInfo thirdPartySafetySectionInfo : thirdPartySafetySectionInfos) {
            if (thirdPartySafetySectionInfo.isDefaultCollection && getDefaultCollection ||
                    (!thirdPartySafetySectionInfo.isDefaultCollection && getCustomCollection)) {
                dataPractices.add(thirdPartySafetySectionInfo);
            }
        }
        return dataPractices.toArray(new ThirdPartySafetySectionInfo[0]);
    }

}
