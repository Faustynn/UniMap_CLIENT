package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.main.unimap_pc.client.services.FilterService;
import org.main.unimap_pc.client.utils.Logger;

import java.util.ArrayList;
import java.util.List;

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

    private List<TeacherSubjectRoles> teachers_roles;
    private List<Teacher> teachers;

    public Subject(JSONObject jsonBase,JSONObject jsonTeachers) {
        try {
            code = jsonBase.getString("code");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'code' in Subject: " + e.getMessage());
            code = "";
        }
        try {
            name = jsonBase.getString("name");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'name' in Subject: " + e.getMessage());
            name = "";
        }
        try {
            type = jsonBase.getString("type");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'type' in Subject: " + e.getMessage());
            type = "";
        }
        try {
            credits = jsonBase.getInt("credits");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'credits' in Subject: " + e.getMessage());
            credits = -1;
        }
        try {
            studyType = jsonBase.getString("studyType");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'studyType' in Subject: " + e.getMessage());
            studyType = "";
        }
        try {
            semester = jsonBase.getString("semester");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'semester' in Subject: " + e.getMessage());
            semester = "";
        }
        try {
            completionType = jsonBase.getString("completionType");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'completionType' in Subject: " + e.getMessage());
            completionType = "";
        }
        try {
            studentCount = jsonBase.getInt("studentCount");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'studentCount' in Subject: " + e.getMessage());
            studentCount = -1;
        }
        try {
            assesmentMethods = jsonBase.getString("assesmentMethods");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'assesmentMethods' in Subject: " + e.getMessage());
            assesmentMethods = "";
        }
        try {
            learningOutcomes = jsonBase.getString("learningOutcomes");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'learningOutcomes' in Subject: " + e.getMessage());
            learningOutcomes = "";
        }
        try {
            courseContents = jsonBase.getString("courseContents");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'courseContents' in Subject: " + e.getMessage());
            courseContents = "";
        }
        try {
            aScore = jsonBase.getString("ascore");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'ascore' in Subject: " + e.getMessage());
            aScore = "";
        }
        try {
            bScore = jsonBase.getString("bscore");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'bscore' in Subject: " + e.getMessage());
            bScore = "";
        }
        try {
            cScore = jsonBase.getString("cscore");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'cscore' in Subject: " + e.getMessage());
            cScore = "";
        }
        try {
            dScore = jsonBase.getString("dscore");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'dscore' in Subject: " + e.getMessage());
            dScore = "";
        }
        try {
            eScore = jsonBase.getString("escore");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'escore' in Subject: " + e.getMessage());
            eScore = "";
        }
        try {
            fxScore = jsonBase.getString("ascore");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'ascore' in Subject: " + e.getMessage());
            fxScore = "";
        }
        try {
            languages = jsonBase.getJSONArray("languages").toList().stream()
                    .map(Object::toString)
                    .toList();
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'languages' in Subject: " + e.getMessage());
            languages = new ArrayList<String>();
        }

        try {
            plannedActivities = jsonBase.getString("plannedActivities");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'plannedActivities' in Subject: " + e.getMessage());
            plannedActivities = "";
        }
        garant = FilterService.subSearchForGarant(code);

        try {
            evaluationMethods = jsonBase.getString("evaluationMethods");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'evaluationMethods' in Subject: " + e.getMessage());
            evaluationMethods = "";
        }

        try {
            evaluation = jsonBase.getString("evaluation");
        } catch (org.json.JSONException e) {
            Logger.error("Error parsing 'evaluation' in Subject: " + e.getMessage());
            evaluation = "";
        }

        teachers_roles = new ArrayList<>();
        teachers = new ArrayList<>();
        if (jsonTeachers.has("teachers")) {
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

                            TeacherSubjectRoles teacher = new TeacherSubjectRoles(specificSubjectRoles);
                            teachers_roles.add(teacher);
                            teachers.add(new Teacher(teacherJson));
                            break;
                        }
                    }
                }
            } catch (org.json.JSONException e) {
                Logger.error("Error parsing JSON in teachers processing: " + e.getMessage());
            }
        }

    }
}
