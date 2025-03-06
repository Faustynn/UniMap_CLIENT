package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.main.unimap_pc.client.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherSubjectRoles {
    private String subjectName;
    private List<String> roles;

    public TeacherSubjectRoles(JSONObject jsonBase) {
        System.out.println("TeacherSubjectRoles: " + jsonBase);
        try {
            subjectName = jsonBase.getString("subjectName");
        } catch (JSONException e) {
            subjectName = "";
        }

        try {
            JSONArray rolesArray = jsonBase.optJSONArray("roles");
            if (rolesArray != null) {
                roles = rolesArray.toList().stream()
                        .map(Object::toString)
                        .map(role -> role.replaceAll("[{}\"']", "").trim())
                        .collect(Collectors.toList());
            } else {
                String rolesString = jsonBase.optString("roles", "");
                roles = parseRolesString(rolesString);
            }
        } catch (Exception e) {
            roles = new ArrayList<>();
            Logger.error("Error parsing 'roles' in TeacherSubjectRoles: " + e.getMessage());
        }
    }

    private List<String> parseRolesString(String rolesString) {
        if (rolesString == null || rolesString.isEmpty()) {
            return new ArrayList<>();
        }

        return List.of(rolesString.replaceAll("[{}\"']", "")
                        .split(","))
                .stream()
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
    }

    public String getFormattedRoles() {
        return roles == null ? "" : String.join(", ", roles);
    }
}