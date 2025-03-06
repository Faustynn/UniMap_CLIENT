package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.main.unimap_pc.client.utils.Logger;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Teacher {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String office;
    private List<TeacherSubjectRoles> subjects;

    public Teacher(JSONObject jsonBase) {
        try {
            id = jsonBase.has("id") ?
                    String.valueOf(jsonBase.get("id")) :
                    "";
        } catch (JSONException e) {
            Logger.error("Error parsing 'id' in Teacher: " + e.getMessage());
            id = "";
        }

        try {
            name = jsonBase.optString("name", "");
        } catch (JSONException e) {
            Logger.error("Error parsing 'name' in Teacher: " + e.getMessage());
            name = "";
        }

        try {
            email = jsonBase.has("email") && !jsonBase.isNull("email") ?
                    String.valueOf(jsonBase.get("email")) :
                    "";
        } catch (JSONException e) {
            Logger.error("Error parsing 'email' in Teacher: " + e.getMessage());
            email = "";
        }

        try {
            phone = jsonBase.has("phone") && !jsonBase.isNull("phone") ?
                    String.valueOf(jsonBase.get("phone")) :
                    "";
        } catch (JSONException e) {
            Logger.error("Error parsing 'phone' in Teacher: " + e.getMessage());
            phone = "";
        }

        try {
            office = jsonBase.has("office") && !jsonBase.isNull("office") ?
                    String.valueOf(jsonBase.get("office")) :
                    "";
            if ("null".equals(office)) {
                office = "";
            }
        } catch (JSONException e) {
            Logger.error("Error parsing 'office' in Teacher: " + e.getMessage());
            office = "";
        }
     //   System.out.println("Teacher: " + jsonBase);
        try {
            if (jsonBase.has("subjects") && !jsonBase.isNull("subjects")) {
              //  System.out.println("Subjects found for teacher " + name);
                JSONArray subjectsArray = jsonBase.getJSONArray("subjects");
                subjects = new ArrayList<>();
                for (int i = 0; i < subjectsArray.length(); i++) {
                    JSONObject subjectObj = subjectsArray.getJSONObject(i);
                    JSONObject subjectJson = new JSONObject();
                    subjectJson.put("subjectName", subjectObj.getString("subjectName"));

                    JSONArray rolesArray = subjectObj.getJSONArray("roles");
                    List<String> rolesList = new ArrayList<>();
                    for (int j = 0; j < rolesArray.length(); j++) {
                        rolesList.add(rolesArray.getString(j).replaceAll("[{}\"']", "").trim());
                    }
                    subjectJson.put("roles", new JSONArray(rolesList));

                    subjects.add(new TeacherSubjectRoles(subjectJson));
                }
            } else {
                System.out.println("No subjects found for teacher " + name);
                subjects.add(new TeacherSubjectRoles(new JSONObject().put("subjectName", "").put("roles", new JSONArray())));
            }
        } catch (JSONException e) {
            subjects = new ArrayList<>();
            Logger.error("Error parsing 'subjects' in Teacher: " + e.getMessage());
        }
    }
}