package org.intellij.privacyHelper.codeInspection.utils;

import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

public class ThirdPartyDependencies {
    static ThirdPartyDependencyInfo [] thirdPartyLibList = null;

     static String getJsonStringFromRemote() {
        try {
            String url = "https://gist.github.com/i7mist/1fa774af300fe8639b4e615923b437f0/raw";
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            // check if response code is 200
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
     }

     static public ThirdPartyDependencyInfo[] getThirdPartyLibList(Project project) {
         if (thirdPartyLibList != null) {
             return thirdPartyLibList;
         }
        String json = getJsonStringFromRemote();
        if (json != null) {
            PropertiesComponent.getInstance().setValue("thirdPartyLibList", json);
            thirdPartyLibList = new Gson().fromJson(json, ThirdPartyDependencyInfo[].class);
        } else {
            if (PropertiesComponent.getInstance().getValue("thirdPartyLibList") != null) {
                thirdPartyLibList =
                        new Gson().fromJson(PropertiesComponent.getInstance().getValue("thirdPartyLibList"),
                                ThirdPartyDependencyInfo[].class);
            } else {
                CoconutUIUtil.pushNotification(project, "Unable to fetch library privacy info",
                        "Please check your network connection.", NotificationType.ERROR);
            }
        }
        return Objects.requireNonNullElseGet(thirdPartyLibList, () -> new ThirdPartyDependencyInfo[0]);
     }
}
