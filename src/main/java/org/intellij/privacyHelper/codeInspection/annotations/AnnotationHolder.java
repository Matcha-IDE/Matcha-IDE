package org.intellij.privacyHelper.codeInspection.annotations;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.*;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class AnnotationHolder {
    public Map<String, ArrayList<String>> plainValueFieldPairs = new HashMap<>();
    public Map<PsiElement, ArrayList<PsiElement>> psiElementFieldPairs = new HashMap<>();
    public CoconutAnnotationType mAnnotationType;

    public AnnotationHolder() {
    }
    public AnnotationHolder(CoconutAnnotationType annotationType, PsiAnnotation annotation) {
        mAnnotationType = annotationType;
        PsiNameValuePair[] annotationTypeValuePairs = annotation.getParameterList().getAttributes();
        for (PsiElement element : annotationTypeValuePairs) {
            PsiNameValuePair nameValuePair = (PsiNameValuePair) element;
            if (nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                continue;
            }
            PsiIdentifier nameIdentifier = nameValuePair.getNameIdentifier();
            String name = nameValuePair.getName();
            PsiElement value = nameValuePair.getValue();
            ArrayList<String> elementArrayList = new ArrayList<>();
            ArrayList<PsiElement> psiElementArrayList = new ArrayList<>();
            if (value instanceof PsiArrayInitializerMemberValue) {
                PsiAnnotationMemberValue [] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();
                for (PsiAnnotationMemberValue e : initializers) {
                    elementArrayList.add(e.getText());
                }
                psiElementArrayList.addAll(Arrays.asList(initializers));
            } else {
                elementArrayList.add(value.getText());
                psiElementArrayList.add(value);
            }
            plainValueFieldPairs.put(name, elementArrayList);
            psiElementFieldPairs.put(nameIdentifier, psiElementArrayList);
        }
    }
    public AnnotationHolder(CoconutAnnotationType annotationType) {
        mAnnotationType = annotationType;
        initAllFields();
    }
    public void initAllFields() {
        String typename = mAnnotationType.toString();
        assert CodeInspectionUtil.ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING.containsKey(typename) || NO_VALUE_ANNOTATION.contains(typename);
        if (ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING.containsKey(typename)) {
            Map<String, String> field_init_value_mapping = CodeInspectionUtil.ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING.get(typename);
            for (Map.Entry<String, String> field : field_init_value_mapping.entrySet()) {
                if (field.getValue() != null) {
                    add(field.getKey(), field.getValue());
                } else {
                    add(field.getKey(), new ArrayList<>());
                }
            }
        }
    }

    public void put(String key, ArrayList<String> values) {
        plainValueFieldPairs.put(key, values);
    }
    public void put(String key, String value) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(value);
        plainValueFieldPairs.put(key, arrayList);
    }
    public void add(String key, String value) {
        ArrayList<String> list;
        if (plainValueFieldPairs.containsKey(key)) {
            list = plainValueFieldPairs.get(key);
        } else {
            list = new ArrayList<>();
        }
        list.add(value);
        plainValueFieldPairs.put(key, list);
    }
    public void add(String key, ArrayList<String> values) {
        plainValueFieldPairs.put(key, values);
    }

    public String getAnnotationStringNoPackageName() {
        String annotationPackagePrefix = ANNOTATION_PKG + ".";
        return getAnnotationString("", annotationPackagePrefix);
    }
    public String getAnnotationString(String appPackageName) {
        String appPackagePrefix = appPackageName.isEmpty() ? "" : appPackageName + ".";
        String annotationPackagePrefix = ANNOTATION_PKG + ".";
        return getAnnotationString(appPackagePrefix, annotationPackagePrefix);
    }

    private String getAnnotationString(String appPackagePrefix, String annotationPackagePrefix) {
        String annotationContentString = "";
        if (!NO_VALUE_ANNOTATION.contains(mAnnotationType.toString())) {
            ArrayList<String> generatedAnnotationItems = new ArrayList<>();
            annotationContentString = "(\n";
            String[] keyList = CodeInspectionUtil.ANNOTATION_FIELD_ORDER.getOrDefault(mAnnotationType.toString(), null);
            if (keyList != null) {
                for (String key : keyList) {
                    String valueStr;
                    String pkgStr;
                    if (PREDEFINED_VALUE_FIELDS.contains(key)) {
                        pkgStr = annotationPackagePrefix;
                    } else {
                        pkgStr = "";
                    }
                    if (SINGLE_VALUE_FIELDS.contains(key)) {
                        // plainValueFieldPairs should only contain one element, which is the ID that uniquely identifies
                        // the personal data instance
                        if (!plainValueFieldPairs.containsKey(key) || plainValueFieldPairs.get(key).isEmpty()) {
                            valueStr = "";
                        } else {
                            if (plainValueFieldPairs.get(key).get(0).startsWith("R.")) {
                                valueStr = appPackagePrefix + plainValueFieldPairs.get(key).get(0);
                            } else {
                                valueStr = pkgStr + plainValueFieldPairs.get(key).get(0);
                            }
                        }
                    } else {
                        if (plainValueFieldPairs.containsKey(key)) {
                            valueStr = "{" + plainValueFieldPairs.get(key).stream()
                                    .map(s -> (s.startsWith("R.") ? appPackagePrefix : pkgStr) + s)
                                    .collect(Collectors.joining(",")) + "}";
                        } else {
                            valueStr = "{}";
                        }
                    }
                    generatedAnnotationItems.add(key + " = " + valueStr);
                }
            }
            annotationContentString += String.join(",\n", generatedAnnotationItems);
            annotationContentString += ")\n";
        }
        return annotationPackagePrefix + mAnnotationType.toString() + annotationContentString;
    }
    public boolean containsKey(String key) {
        return plainValueFieldPairs.containsKey(key);
    }

    public boolean containsPair(String key, String value) {
        if (!plainValueFieldPairs.containsKey(key)) {
            return false;
        }
        return plainValueFieldPairs.get(key).contains(value);
    }

    public String getInstanceFirstValue(String field_name) {
        if (plainValueFieldPairs.containsKey(field_name) &&
                !plainValueFieldPairs.get(field_name).isEmpty()) {
            return plainValueFieldPairs.get(field_name).get(0);
        } else {
            return "";
        }
    }

    public String getInstanceFirstValueNoPkgPrefix(String field_name, int parts) {
        if (plainValueFieldPairs.containsKey(field_name) &&
                !plainValueFieldPairs.get(field_name).isEmpty()) {
            String[] items = plainValueFieldPairs.get(field_name).get(0).split("\\.");
            return String.join(".", Arrays.copyOfRange(items, items.length - parts, items.length));
        } else {
            return "";
        }
    }

    public String[] getInstanceAllValues(String field_name) {
        if (plainValueFieldPairs.containsKey(field_name) &&
                !plainValueFieldPairs.get(field_name).isEmpty()) {
            return plainValueFieldPairs.get(field_name).toArray(String[]::new);
        } else {
            return new String[0];
        }
    }

    public String[] getInstanceAllValuesNoPkgPrefix(String field_name, int parts) {
        if (plainValueFieldPairs.containsKey(field_name) &&
                !plainValueFieldPairs.get(field_name).isEmpty()) {
            return Arrays.stream(plainValueFieldPairs.get(field_name).toArray(String[]::new)).map(s -> {
                String[] items = s.split("\\.");
                if (items.length <= parts) {
                    return s;
                } else {
                    return String.join(".", Arrays.copyOfRange(items, items.length - parts, items.length));
                }
            }).toArray(String[]::new);
        } else {
            return new String[0];
        }
    }

    public String getDataType() {
        String dataTypeString;
        if (mAnnotationType == CoconutAnnotationType.DataAccess) {
            dataTypeString = getInstanceFirstValue(fieldDataAccessDataType);
        } else {
            dataTypeString = "";
        }
        if (dataTypeString.contains(".")) {
            dataTypeString = dataTypeString.substring(dataTypeString.lastIndexOf(".")+1);
        }
        return dataTypeString;
    }

    public String [] getDataTypes() {
        if (mAnnotationType == CoconutAnnotationType.DataAccess) {
            return getInstanceAllValuesNoPkgPrefix(fieldDataAccessDataType, 1);
        } else {
            return new String[0];
        }
    }

    public PersonalDataGroup[] getTransmissionDataTypes(Project project) {
        String[] transmissionAccessIds = getInstanceAllValues(fieldDataTransmissionAccessIdList);
        return CodeInspectionUtil.getDataTypesByAccessIds(transmissionAccessIds, project);
    }

    public String getDataTypeReadableString() {
        return Arrays.stream(getDataTypes())
                .map(CoconutUIUtil::prettifyDataTypeString)
                .collect(Collectors.joining(", "));
    }

    public String getDataId(boolean hasQuote) {
        if (hasQuote) {
            return String.format("\"%s\"", fieldDataAccessId);
        } else {
            return getInstanceFirstValue(fieldDataAccessId);
        }
    }

    public String getAnnotationShortSummary(Project project, boolean basic) {
        if (basic) {
            if (mAnnotationType == CoconutAnnotationType.DataAccess) {
                return String.format("@%s(%s)", CoconutAnnotationType.DataAccess,
                        getDataTypeReadableString().toLowerCase());
            } else {
                return String.format("@%s", mAnnotationType);
            }
        } else {
            if (mAnnotationType == CoconutAnnotationType.DataTransmission) {
                String dataType = Arrays.stream(getTransmissionDataTypes(project))
                        .map(d -> CoconutUIUtil.prettifyDataTypeString(d.toString())).collect(Collectors.joining(", "));
                ArrayList<String> exemptStrings = new ArrayList<>();
                boolean isCollected = Arrays.stream(
                        getInstanceAllValues(fieldDataTransmissionCollectionAttributeList)).anyMatch(
                                s -> s.contains(CollectionAttribute.TransmittedOffDevice_True.toString()));
                boolean isShared = Arrays.stream(
                        getInstanceAllValues(fieldDataTransmissionSharingAttributeList)).anyMatch(
                        s -> s.contains(SharingAttribute.SharedWithThirdParty_True.toString()));
                boolean collectionExempt = Arrays.stream(
                        getInstanceAllValues(fieldDataTransmissionCollectionAttributeList)).anyMatch(
                        s -> {
                            for (String exemptValue : COLLECTION_EXEMPT_VALUES) {
                                if (s.contains(exemptValue)) {
                                    return true;
                                }
                            }
                            return false;
                        });
                boolean sharingExempt = Arrays.stream(
                        getInstanceAllValues(fieldDataTransmissionSharingAttributeList)).anyMatch(
                        s -> {
                            for (String exemptValue : SHARING_EXEMPT_VALUES) {
                                if (s.contains(exemptValue)) {
                                    return true;
                                }
                            }
                            return false;
                        });

                if (collectionExempt) {
                    exemptStrings.add("collection is exempt");
                }
                if (sharingExempt) {
                    exemptStrings.add("sharing is exempt");
                }

                return String.format("@%s(%s %s; %s)", CoconutAnnotationType.DataTransmission,
                        isShared ? (isCollected ? "collect and share: " : "share: ") : "collect: ",
                        dataType.toLowerCase(),
                        String.join("; ", exemptStrings));
            } else {
                return String.format("@%s(%s, Id: %s)", CoconutAnnotationType.DataAccess,
                        getDataTypeReadableString().toLowerCase(), getDataId(false));
            }
        }
    }

    public String getTransmissionAttributeSummary() {
        if (mAnnotationType == CoconutAnnotationType.DataTransmission) {
            ArrayList<String> allAttributes = new ArrayList<>();
            Collections.addAll(allAttributes, getInstanceAllValues(fieldDataTransmissionCollectionAttributeList));
            Collections.addAll(allAttributes, getInstanceAllValues(fieldDataTransmissionSharingAttributeList));
            String[] collectionPurposes = allAttributes.stream().filter(s -> s.startsWith("CollectionAttribute.For"))
                    .toArray(String[]::new);
            String[] sharingPurposes = allAttributes.stream().filter(s -> s.startsWith("SharingAttribute.For"))
                    .toArray(String[]::new);
            String[] otherAttributes = allAttributes.stream().filter(
                    s -> !Arrays.asList(collectionPurposes).contains(s) && !Arrays.asList(sharingPurposes).contains(s))
                    .toArray(String[]::new);
            String collectionPurposeString = collectionPurposes.length == 0 ? "none" : Arrays.stream(collectionPurposes).map(
                    s -> CoconutUIUtil.prettifyCamelCaseString(s.replace("CollectionAttribute.For", "")))
                    .collect(Collectors.joining(", "));
            String sharingPurposeString = sharingPurposes.length == 0 ? "none" : Arrays.stream(sharingPurposes).map(
                    s -> CoconutUIUtil.prettifyCamelCaseString(s.replace("SharingAttribute.For", "")))
                    .collect(Collectors.joining(", "));
            String attributeString = Arrays.stream(otherAttributes).map(
                    s -> {
                        String[] stringElements =
                                s.replaceAll("CollectionAttribute\\.|SharingAttribute\\.", "")
                                        .split("_");
                        if (stringElements.length <= 1) {
                            return CoconutUIUtil.prettifyCamelCaseString(stringElements[0]);
                        } else {
                            String state = stringElements[1];
                            String attribute = CoconutUIUtil.prettifyCamelCaseString(stringElements[0]);
                            if ("False".equals(state)) {
                                return String.format("not %s", attribute);
                            } else {
                                return attribute;
                            }
                        }
                    }).collect(Collectors.joining(", "));
            return String.format("%s; Collection Purposes: %s; Sharing Purposes: %s", attributeString,
                    collectionPurposeString, sharingPurposeString);
        } else {
            return "";
        }
    }

    public String[] getTransmissionAttributes(String fieldName) {
        if (mAnnotationType == CoconutAnnotationType.DataTransmission) {
            return getInstanceAllValues(fieldName);
        } else {
            return new String[0];
        }
    }

    public boolean hasTransmissionAttribute(String fieldName, String targetAttribute) {
        String[] collectionAttributes = getTransmissionAttributes(fieldName);
        for (String attribute : collectionAttributes) {
            if (attribute.contains(targetAttribute)) {
                return true;
            }
        }
        return false;
    }

    public void clear(String fieldNAme) {
        if (plainValueFieldPairs.containsKey(fieldNAme)) {
            plainValueFieldPairs.get(fieldNAme).clear();
        }
    }
}
