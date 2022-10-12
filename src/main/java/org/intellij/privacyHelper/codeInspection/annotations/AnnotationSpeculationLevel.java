package org.intellij.privacyHelper.codeInspection.annotations;

public enum AnnotationSpeculationLevel {
    AT_LEAST_ONE_REQUIRED, // requires at least one annotation that match annotations in this list or not personal data annotation
    ANY_POSSIBLE // suggests annotations that match annotations in this list (or none matches)
}
