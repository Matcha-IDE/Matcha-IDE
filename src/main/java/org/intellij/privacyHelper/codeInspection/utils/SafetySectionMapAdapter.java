package org.intellij.privacyHelper.codeInspection.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.*;

public class SafetySectionMapAdapter implements JsonSerializer<Map<String[], Set<String[]>>>,
        JsonDeserializer<Map<String[], Set<String[]>>> {
    @Override
    public Map<String[], Set<String[]>> deserialize(JsonElement jsonElement, Type type,
                                                    JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        Map<String, List<String>> reformattedMap = new HashMap<>();
        reformattedMap = jsonDeserializationContext.deserialize(jsonElement, reformattedMap.getClass());
        Map<String[], Set<String[]>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : reformattedMap.entrySet()) {
            String[] key = DATA_TYPE_PRIVACY_LABEL_MAPPING.get(PersonalDataGroup.valueOf(entry.getKey()));
            Set<String[]> attributes = new HashSet<>();
            for (String attribute : entry.getValue()) {
                attributes.add(SAFETY_SECTION_ATTRIBUTE_MAPPING.get(attribute));
            }
            result.put(key, attributes);
        }
        return result;
    }

    @Override
    public JsonElement serialize(Map<String[], Set<String[]>> setMap, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        // iterate through the map and serialize each entry
        Map<String, Set<String>> reformattedMap = new HashMap<>();
        for (Map.Entry<String[], Set<String[]>> entry : setMap.entrySet()) {
            String[] key = entry.getKey();
            PersonalDataGroup dataGroup = DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING.get(key);
            Set<String> attributes = new HashSet<>();
            for (String[] attribute : entry.getValue()) {
                attributes.add(SAFETY_SECTION_ATTRIBUTE_REVERSE_MAPPING.get(attribute));
            }
            reformattedMap.put(dataGroup.toString(), attributes);
        }
        return jsonSerializationContext.serialize(reformattedMap);
    }
}
