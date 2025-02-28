package org.main.unimap_pc.client;

import org.main.unimap_pc.client.services.FilterService;

import java.nio.file.DirectoryStream;

public class testMain {
    public static void main(String[] args){
        FilterService filterService = new FilterService();

        System.out.println(filterService.filterTeachers(new FilterService.teacherSearchForm(
                "",
                "VAVA_B")
        ));

        System.out.println(filterService.filterSubjects(new FilterService.subjectSearchForm(
                "",
                FilterService.subjectSearchForm.subjectTypeEnum.NONE,
                FilterService.subjectSearchForm.studyTypeEnum.NONE,
                FilterService.subjectSearchForm.semesterEnum.NONE)
        ));
    }
}
