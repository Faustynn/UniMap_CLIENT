package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.main.unimap_pc.client.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherSubjectRoles {
    private String subjectName;
    private List<String> roles = new ArrayList<>();

    public TeacherSubjectRoles(JSONObject jsonBase) {
        this.subjectName = parseSubjectName(jsonBase);
        this.roles = parseRoles(jsonBase);
    }

    private String parseSubjectName(JSONObject json) {
        try {
            return json.getString("subjectName");
        } catch (Exception e) {
            Logger.error("Error parsing 'subjectName' in TeacherSubjectRoles: " + e.getMessage());
            return "";
        }
    }

    private List<String> parseRoles(JSONObject json) {
        try {
            if (json.has("roles") && json.get("roles") instanceof JSONArray rolesArray) {
                return rolesArray.toList().stream()
                        .map(Object::toString)
                        .map(role -> role.replaceAll("[{}\"']", "").trim())
                        .filter(role -> !role.isEmpty())
                        .collect(Collectors.toList());
            } else {
                return parseRolesString(json.optString("roles", ""));
            }
        } catch (Exception e) {
            Logger.error("Error parsing 'roles' in TeacherSubjectRoles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> parseRolesString(String rolesString) {
        if (rolesString == null || rolesString.isBlank()) return Collections.emptyList();
        return List.of(rolesString.replaceAll("[{}\"']", "").split(","))
                .stream()
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
    }

    public String getFormattedRoles() {
        return roles == null ? "" : String.join(", ", roles);
    }
}
