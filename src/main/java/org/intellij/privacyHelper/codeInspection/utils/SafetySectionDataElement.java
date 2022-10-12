package org.intellij.privacyHelper.codeInspection.utils;

import com.google.gson.annotations.JsonAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class SafetySectionDataElement {
    @JsonAdapter(SafetySectionMapAdapter.class)
    public Map<String[], Set<String[]>> dataPractices;
    public boolean encryptedInTransit;
    public boolean userRequestDelete;

    public SafetySectionDataElement() {
        this.dataPractices = new HashMap<>();
        this.encryptedInTransit = true; // all encrypted
        this.userRequestDelete = false; // any can be deleted
    }

    public SafetySectionDataElement(boolean encryptedInTransit, boolean userRequestDelete,
                                    Map<String[], Set<String[]>> dataPractices) {
        this.dataPractices = dataPractices;
        this.encryptedInTransit = encryptedInTransit;
        this.userRequestDelete = userRequestDelete;
    }

    public void combine(SafetySectionDataElement otherElement) {
        if (otherElement == null) {
            return;
        }
        this.encryptedInTransit = this.encryptedInTransit && otherElement.encryptedInTransit;
        this.userRequestDelete = this.userRequestDelete || otherElement.userRequestDelete;
        for (Map.Entry<String[], Set<String[]>> dataPractice : otherElement.dataPractices.entrySet()) {
            String[] dataType = dataPractice.getKey();
            Set<String[]> practiceItems = dataPractice.getValue();
            if (!dataPractices.containsKey(dataType)) {
                dataPractices.put(dataType, practiceItems);
            } else {
                if (dataPractices.get(dataType).contains(safetySectionEphemeral) &&
                        !practiceItems.contains(safetySectionEphemeral)) {
                    dataPractices.get(dataType).remove(safetySectionEphemeral);
                }
                dataPractices.get(dataType).addAll(practiceItems);
            }
        }
        for (Map.Entry<String[], Set<String[]>> dataPractice : this.dataPractices.entrySet()) {
            if (dataPractice.getValue().contains(safetySectionRequired) &&
                    dataPractice.getValue().contains(safetySectionOptional)) {
                dataPractice.getValue().remove(safetySectionOptional);
            }
        }
    }

    public String getIsCollectingDataString() {
        return dataPractices.keySet().isEmpty() ? "FALSE" : "TRUE";
    }

    public String getEncryptedInTransitString() {
        return dataPractices.keySet().isEmpty() ? "" : (encryptedInTransit ? "TRUE" : "FALSE");
    }

    public String getUserRequestDeleteString() {
        return dataPractices.keySet().isEmpty() ? "" : (userRequestDelete ? "TRUE" : "FALSE");
    }

    public String getAnswerForFamilyPolicyString() {
        // FIXME:
        return "";
    }

    public String getAnswerForSecurityReviewString() {
        // FIXME:
        return "FALSE";
    }

}
