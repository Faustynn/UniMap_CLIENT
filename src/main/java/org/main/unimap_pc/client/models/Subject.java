package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.main.unimap_pc.client.services.FilterService;
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
public class Subject {
    private String code;
    private String name;
    private String type;
    private int credits;
    private String studyType;
    private String semester;
    private List<String> languages;
    private String completionType;
    private long studentCount;
    private String aScore;
    private String bScore;
    private String cScore;
    private String dScore;
    private String eScore;
    private String fxScore;
    private String assesmentMethods;
    private String learningOutcomes;
    private String courseContents;
    private String plannedActivities;
    private String evaluationMethods;
    private String garant;
    private String evaluation;
    private List<TeacherSubjectRoles> teachers_roles = new ArrayList<>();
    private List<Teacher> teachers = new ArrayList<>();

    public Subject(JSONObject jsonBase, JSONObject jsonTeachers) {
        code = getString(jsonBase, "code");
        name = getString(jsonBase, "name");
        type = getString(jsonBase, "type");
        credits = getInt(jsonBase, "credits", -1);
        studyType = getString(jsonBase, "studyType");
        semester = getString(jsonBase, "semester");
        completionType = getString(jsonBase, "completionType");
        studentCount = getInt(jsonBase, "studentCount", -1);
        assesmentMethods = getString(jsonBase, "assesmentMethods");
        learningOutcomes = getString(jsonBase, "learningOutcomes");
        courseContents = getString(jsonBase, "courseContents");
        aScore = getString(jsonBase, "ascore");
        bScore = getString(jsonBase, "bscore");
        cScore = getString(jsonBase, "cscore");
        dScore = getString(jsonBase, "dscore");
        eScore = getString(jsonBase, "escore");
        fxScore = getString(jsonBase, "fxscore");
        plannedActivities = getString(jsonBase, "plannedActivities");
        evaluationMethods = getString(jsonBase, "evaluationMethods");
        evaluation = getString(jsonBase, "evaluation");
        garant = FilterService.subSearchForGarant(code);
        languages = getStringList(jsonBase, "languages");
        parseTeachers(jsonTeachers);
    }

    private String getString(JSONObject json, String key) {
        try {
            return json.getString(key);
        } catch (Exception e) {
            Logger.error("Error parsing '" + key + "' in Subject: " + e.getMessage());
            return "";
        }
    }

    private int getInt(JSONObject json, String key, int defaultValue) {
        try {
            return json.getInt(key);
        } catch (Exception e) {
            Logger.error("Error parsing '" + key + "' in Subject: " + e.getMessage());
            return defaultValue;
        }
    }

    private List<String> getStringList(JSONObject json, String key) {
        try {
            return json.getJSONArray(key).toList().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            Logger.error("Error parsing '" + key + "' in Subject: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void parseTeachers(JSONObject jsonTeachers) {
        if (!jsonTeachers.has("teachers")) return;

        try {
            JSONArray teachersArray = jsonTeachers.getJSONArray("teachers");
            for (int i = 0; i < teachersArray.length(); i++) {
                JSONObject teacherJson = teachersArray.getJSONObject(i);
                JSONArray subjects = teacherJson.getJSONArray("subjects");

                for (int j = 0; j < subjects.length(); j++) {
                    JSONObject subject = subjects.getJSONObject(j);
                    String subjectName = subject.getString("subjectName");

                    if (subjectName.equals(code)) {
                        JSONObject specificSubjectRoles = new JSONObject();
                        specificSubjectRoles.put("subjectName", subjectName);
                        specificSubjectRoles.put("roles", subject.getJSONArray("roles"));

                        teachers_roles.add(new TeacherSubjectRoles(specificSubjectRoles));
                        teachers.add(new Teacher(teacherJson));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Error parsing JSON in teachers processing: " + e.getMessage());
        }
    }
}
