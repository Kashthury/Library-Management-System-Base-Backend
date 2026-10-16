package com.library.lms.service;

import com.library.lms.dto.request.FinePaymentRequest;
import com.library.lms.dto.request.FineRequest;
import com.library.lms.dto.request.StudentRequest;
import com.library.lms.dto.response.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse create(StudentRequest request);

    List<StudentResponse> getAll();

    StudentResponse getById(Long id);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);

    List<StudentResponse> getByGender(String gender);

    /** Calculates the fine for the given late days and STORES it on the student. */
    StudentResponse calculateFine(Long id, FineRequest request);

    /** Reduces the student's outstanding fine. */
    StudentResponse payFine(Long id, FinePaymentRequest request);
}
