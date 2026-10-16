package com.library.lms.mapper;

import com.library.lms.dto.request.StudentRequest;
import com.library.lms.dto.response.StudentResponse;
import com.library.lms.entity.Student;
import com.library.lms.enums.Gender;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "fineAmount", ignore = true)
    Student toEntity(StudentRequest request);

    /** Student ID and fine are never changed through the update endpoint. */
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "fineAmount", ignore = true)
    void updateEntity(StudentRequest request, @MappingTarget Student student);

    @Mapping(target = "gender", expression = "java(student.getGender().getLabel())")
    StudentResponse toResponse(Student student);

    List<StudentResponse> toResponseList(List<Student> students);

    default Gender toGender(String code) {
        return code == null ? null : Gender.parse(code);
    }
}
