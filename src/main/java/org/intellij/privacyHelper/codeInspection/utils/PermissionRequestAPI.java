package org.intellij.privacyHelper.codeInspection.utils;

public class PermissionRequestAPI extends SensitiveAPI {
    public int[] permissionPositionList;
    PermissionRequestAPI(String displayName, String fullAPINamePattern, String returnValueTypeCanonicalTextPattern, String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction, int[] permissionPositionList) {
        super(displayName, fullAPINamePattern, returnValueTypeCanonicalTextPattern, parameterTypeCanonicalTextRestriction, parameterValueTextRestriction, false, false);
        this.permissionPositionList = permissionPositionList;
    }
}
