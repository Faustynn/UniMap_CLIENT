package org.main.unimap_pc.client.models;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.main.unimap_pc.client.services.FilterService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
    public Subject(JSONObject jsonBase) {
        try {
            code = jsonBase.getString("code");
        } catch (org.json.JSONException e) {
            code = "";
        }
        try {
            name = jsonBase.getString("name");
        } catch (org.json.JSONException e) {
            name = "";
        }
        try {
            type = jsonBase.getString("type");
        } catch (org.json.JSONException e) {
            type = "";
        }
        try {
            credits = jsonBase.getInt("credits");
        } catch (org.json.JSONException e) {
            credits = -1;
        }
        try {
            studyType = jsonBase.getString("studyType");
        } catch (org.json.JSONException e) {
            studyType = "";
        }
        try {
            semester = jsonBase.getString("semester");
        } catch (org.json.JSONException e) {
            semester = "";
        }
        try {
            completionType = jsonBase.getString("completionType");
        } catch (org.json.JSONException e) {
            completionType = "";
        }
        try {
            studentCount = jsonBase.getInt("studentCount");
        } catch (org.json.JSONException e) {
            studentCount = -1;
        }
        try {
            assesmentMethods = jsonBase.getString("assesmentMethods");
        } catch (org.json.JSONException e) {
            assesmentMethods = "";
        }
        try {
            learningOutcomes = jsonBase.getString("learningOutcomes");
        } catch (org.json.JSONException e) {
            learningOutcomes = "";
        }
        try {
            courseContents = jsonBase.getString("courseContents");
        } catch (org.json.JSONException e) {
            courseContents = "";
        }
        try {
            aScore = jsonBase.getString("aScore");
        } catch (org.json.JSONException e) {
            aScore = "";
        }
        try {
            bScore = jsonBase.getString("bscore");
        } catch (org.json.JSONException e) {
            bScore = "";
        }
        try {
            cScore = jsonBase.getString("cscore");
        } catch (org.json.JSONException e) {
            cScore = "";
        }
        try {
            dScore = jsonBase.getString("dscore");
        } catch (org.json.JSONException e) {
            dScore = "";
        }
        try {
            eScore = jsonBase.getString("escore");
        } catch (org.json.JSONException e) {
            eScore = "";
        }
        try {
            fxScore = jsonBase.getString("ascore");
        } catch (org.json.JSONException e) {
            fxScore = "";
        }
        try {
            languages = jsonBase.getJSONArray("languages").toList().stream()
                    .map(Object::toString)
                    .toList();
        } catch (org.json.JSONException e) {
            languages = new ArrayList<String>();
        }
        try {
            garant = FilterService.filterTeachers(new FilterService.teacherSearchForm("",name,true)).getFirst().getName();
        } catch (NoSuchElementException e) {
            garant = "";
        }
    }
}
