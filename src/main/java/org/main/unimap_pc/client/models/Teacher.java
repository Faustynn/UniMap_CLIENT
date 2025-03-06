package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            id = "";
        }

        try {
            name = jsonBase.optString("name", "");
        } catch (JSONException e) {
            name = "";
        }

        try {
            email = jsonBase.has("email") && !jsonBase.isNull("email") ?
                    String.valueOf(jsonBase.get("email")) :
                    "";
        } catch (JSONException e) {
            email = "";
        }

        try {
            phone = jsonBase.has("phone") && !jsonBase.isNull("phone") ?
                    String.valueOf(jsonBase.get("phone")) :
                    "";
        } catch (JSONException e) {
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
            e.printStackTrace();
        }
    }
}