package org.main.unimap_pc.client.models;

import lombok.*;
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
            id = String.valueOf(jsonBase.getInt("id"));
        } catch (org.json.JSONException e) {
            id = "";
        }
        try {
            name = jsonBase.getString("name");
        } catch (org.json.JSONException e) {
            name = "";
        }
        try {
            email = jsonBase.getString("email");
        } catch (org.json.JSONException e) {
            email = "";
        }
        try {
            phone = jsonBase.getString("number");
        } catch (org.json.JSONException e) {
            phone = "";
        }
        try {
            office = jsonBase.getString("office");
            if (office.equals("null")) {
                office = "";
            }
        } catch (org.json.JSONException e) {
            office = "None";
        }
        try {
            subjects = jsonBase.getJSONArray("subject").toList().stream().map(o -> {
                JSONObject subjectJson = new JSONObject((java.util.Map<?, ?>) o);
                return new TeacherSubjectRoles(subjectJson);
            }).toList();
        } catch (org.json.JSONException e) {
            subjects = new ArrayList<TeacherSubjectRoles>();
        }
    }
}
