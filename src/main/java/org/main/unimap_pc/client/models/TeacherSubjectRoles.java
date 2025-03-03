package org.main.unimap_pc.client.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherSubjectRoles {
    private String subjectName;
    private List<String> roles;
    public TeacherSubjectRoles(JSONObject jsonBase) {
        try {
            subjectName = jsonBase.getString("subjectName");
        } catch (org.json.JSONException e) {
            subjectName = "";
        }
        try {
            roles = jsonBase.getJSONArray("roles").toList().stream()
                    .map(Object::toString)
                    .toList();
        } catch (org.json.JSONException e) {
            roles = new ArrayList<>();
        }
    }
        public String getFormattedRoles() {
            return String.join(", ", roles);
        }
}
