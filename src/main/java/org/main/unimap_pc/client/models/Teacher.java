package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.main.unimap_pc.client.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<TeacherSubjectRoles> subjects = new ArrayList<>();

    public Teacher(JSONObject jsonBase) {
        id = getString(jsonBase, "id");
        name = getString(jsonBase, "name");
        email = getString(jsonBase, "email");
        phone = getString(jsonBase, "phone");
        office = sanitizeOffice(getString(jsonBase, "office"));
        subjects = parseSubjects(jsonBase);
    }

    private String getString(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) ? json.get(key).toString() : "";
        } catch (Exception e) {
            Logger.error("Error parsing '" + key + "' in Teacher: " + e.getMessage());
            return "";
        }
    }

    private String sanitizeOffice(String raw) {
        return "null".equalsIgnoreCase(raw) ? "" : raw;
    }

    private List<TeacherSubjectRoles> parseSubjects(JSONObject jsonBase) {
        if (!jsonBase.has("subjects") || jsonBase.isNull("subjects")) {
            Logger.info("No subjects found for teacher " + name);
            return Collections.singletonList(new TeacherSubjectRoles(new JSONObject().put("subjectName", "").put("roles", new JSONArray())));
        }

        List<TeacherSubjectRoles> rolesList = new ArrayList<>();
        try {
            JSONArray subjectsArray = jsonBase.getJSONArray("subjects");
            for (int i = 0; i < subjectsArray.length(); i++) {
                JSONObject subjectObj = subjectsArray.getJSONObject(i);
                JSONObject subjectJson = new JSONObject();
                subjectJson.put("subjectName", subjectObj.getString("subjectName"));

                JSONArray rolesArray = subjectObj.getJSONArray("roles");
                List<String> roles = rolesArray.toList().stream()
                        .map(Object::toString)
                        .map(r -> r.replaceAll("[{}\"']", "").trim())
                        .collect(Collectors.toList());
                subjectJson.put("roles", new JSONArray(roles));

                rolesList.add(new TeacherSubjectRoles(subjectJson));
            }
        } catch (Exception e) {
            Logger.error("Error parsing 'subjects' in Teacher: " + e.getMessage());
        }
        return rolesList;
    }
}
