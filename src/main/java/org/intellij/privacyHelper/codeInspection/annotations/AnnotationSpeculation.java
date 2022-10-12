package org.intellij.privacyHelper.codeInspection.annotations;

import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;

import java.util.ArrayList;
import java.util.List;

public class AnnotationSpeculation {
    AnnotationHolder[] speculations;
    AnnotationSpeculationLevel speculationLevel;

    public AnnotationSpeculation(AnnotationHolder speculation, AnnotationSpeculationLevel speculationLevel) {
        this.speculations = new AnnotationHolder[] {speculation};
        this.speculationLevel = speculationLevel;
    }

    public AnnotationSpeculation(AnnotationHolder[] speculations, AnnotationSpeculationLevel speculationLevel) {
        this.speculations = speculations;
        this.speculationLevel = speculationLevel;
    }

    public String getAccessDataTypeDescription() {
        List<String> dataTypes = new ArrayList<>();
        for (AnnotationHolder annotationHolder : speculations) {
            if (annotationHolder.mAnnotationType == CoconutAnnotationType.DataAccess) {
                dataTypes.add(annotationHolder.getDataType());
            }
        }
        switch (speculationLevel) {
            case AT_LEAST_ONE_REQUIRED:
                return String.format("Require at least one of the data type(s): %s", String.join(", ", dataTypes));
            case ANY_POSSIBLE:
                return String.format("Suggest the data type(s): %s", String.join(", ", dataTypes));
        }
        assert false;
        return "";
    }

    public AnnotationHolder[] getSpeculations() {
        return speculations;
    }

    public AnnotationSpeculationLevel getSpeculationLevel() {
        return speculationLevel;
    }
}
