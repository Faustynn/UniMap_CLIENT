package org.main.unimap_pc.client.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.models.Teacher;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterService {

    public FilterService(){
        getSubjects();
        getTeachers();
    }

    private static String removeDiacritics(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", ""); // Remove diacritical marks
    }

    private JSONArray SubjectArray;
    private JSONArray TeacherArray;

    private void getSubjects() {
        Object subjects = CacheService.get("SUBJECTS");
        System.out.println("HAAAAAA"+subjects);

        // TODO: make here correct output
        if (subjects instanceof String) {
            try {
                SubjectArray = new JSONArray((String) subjects);
            } catch (JSONException e) {
                SubjectArray = new JSONArray();
            }
        } else if (subjects instanceof JSONArray) {
            SubjectArray = (JSONArray) subjects;
        } else {
            SubjectArray = new JSONArray();
        }
    }

    private void getTeachers() {
        Object teachers = CacheService.get("TEACHERS");
        System.out.println("HAAAAAA"+teachers);

        // TODO: make here correct output
        if (teachers instanceof String) {
            try {
                TeacherArray = new JSONArray((String) teachers);
            } catch (JSONException e) {
                TeacherArray = new JSONArray();
            }
        } else if (teachers instanceof JSONArray) {
            TeacherArray = (JSONArray) teachers;
        } else {
            TeacherArray = new JSONArray();
        }
    }


    // FilterService.java
    public List<Teacher> filterTeachers(teacherSearchForm searchForm) {
        getTeachers();

        List<Teacher> TeacherList = TeacherArray.toList().stream()
                .map(obj -> new JSONObject((Map<String, Object>) obj))
                .filter(searchForm.teachesSubject)
                .filter(searchForm.isGuarantor)
                .filter(searchForm.nameSearch)
                .map(Teacher::new)
                .collect(Collectors.toList());
        return TeacherList;
    }

    public List<Subject> filterSubjects(subjectSearchForm searchForm){
        getSubjects();

        List<Subject> SubjectList = SubjectArray.toList().stream()
                .map(obj -> new JSONObject((Map<String, Object>)  obj))
                .filter(searchForm.semesterPredicate)
                .filter(searchForm.studyTypePredicate)
                .filter(searchForm.subjectTypePredicate)
                .filter(searchForm.nameSearch)
                .map(Subject::new)
                .collect(Collectors.toList());
        return SubjectList;
    }

    // FilterService.java
    public static class teacherSearchForm {
        private final Predicate<JSONObject> nameSearch;
        private final Predicate<JSONObject> teachesSubject;
        private final Predicate<JSONObject> isGuarantor;

        public teacherSearchForm(String searchTerm, String targetSubjectCode) {
            teachesSubject = teacher -> {
                if (targetSubjectCode.isBlank()) {
                    return true;
                }
                JSONArray subjects = teacher.getJSONArray("subjects");
                for (int i = 0; i < subjects.length(); i++) {
                    JSONObject subject = subjects.getJSONObject(i);
                    if (subject.getString("subjectCode").equals(targetSubjectCode)) {
                        return true;
                    }
                }
                return false;
            };

            isGuarantor = teacher -> {
                JSONArray subjects = teacher.getJSONArray("subjects");
                for (int i = 0; i < subjects.length(); i++) {
                    JSONObject subject = subjects.getJSONObject(i);
                    if (subject.getString("subjectCode").equals(targetSubjectCode) && subject.getBoolean("isGuarantor")) {
                        return true;
                    }
                }
                return false;
            };

            nameSearch = teacher -> {
                String originalName = teacher.getString("name");
                String normalizedName = removeDiacritics(originalName).toLowerCase();
                String normalizedSearchTerm = removeDiacritics(searchTerm).toLowerCase();
                return normalizedName.contains(normalizedSearchTerm);
            };
        }
    }
    public static class subjectSearchForm{
        public enum subjectTypeEnum{POV,POV_VOL,VOL,NONE}
        public enum studyTypeEnum{BC,ING,NONE}
        public enum semesterEnum{ZS,LS,NONE}
        private final Predicate<JSONObject> nameSearch;
        private final Predicate<JSONObject> subjectTypePredicate;
        private final Predicate<JSONObject> studyTypePredicate;
        private final Predicate<JSONObject> semesterPredicate;
        public subjectSearchForm(String searchTerm, subjectTypeEnum subjectType, studyTypeEnum studyType,semesterEnum semester){
            nameSearch = subject -> {
                if(searchTerm.isBlank()){return true;}
                String originalName = subject.getString("name");
                String originalCode = subject.getString("code");
                String normalizedName = removeDiacritics(originalName).toLowerCase();
                String normalizedCode = removeDiacritics(originalCode).toLowerCase();
                String normalizedSearchTerm = removeDiacritics(searchTerm).toLowerCase();
                return normalizedName.contains(normalizedSearchTerm) || normalizedCode.contains(normalizedSearchTerm);
            };
            subjectTypePredicate = subject ->{
                String type = subject.getString("type");
                type = type.toLowerCase();
                type = removeDiacritics(type);
                return switch (subjectType) {
                    case POV -> type.equals("povinny");
                    case POV_VOL -> type.equals("povinne volitelny");
                    case VOL -> type.equals("volitelny");
                    case NONE -> true;
                };
            };
            studyTypePredicate = subject ->{
                String type = subject.getString("studyType");
                type = type.toLowerCase();
                type = removeDiacritics(type);
                return switch (studyType) {
                    case BC -> type.equals("bakalarsky");
                    case ING -> type.equals("inziniersky");
                    case NONE -> true;
                };
            };
            semesterPredicate = subject ->{
                String type = subject.getString("semester");
                type = type.toUpperCase();
                type = removeDiacritics(type);
                return switch (semester) {
                    case ZS -> type.equals("ZS");
                    case LS -> type.equals("LS");
                    case NONE -> true;
                };
            };
        }

    }

}
