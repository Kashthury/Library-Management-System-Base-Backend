package com.library.lms.service.impl;

import com.library.lms.dto.request.FinePaymentRequest;
import com.library.lms.dto.request.FineRequest;
import com.library.lms.dto.request.StudentRequest;
import com.library.lms.dto.response.StudentResponse;
import com.library.lms.entity.Student;
import com.library.lms.enums.Gender;
import com.library.lms.exception.BusinessRuleException;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.mapper.StudentMapper;
import com.library.lms.repository.BorrowRecordRepository;
import com.library.lms.repository.StudentRepository;
import com.library.lms.service.StudentService;
import com.library.lms.util.FineCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {
        Student student = studentMapper.toEntity(request);
        student.setFineAmount(BigDecimal.ZERO); // new students always start with no fine
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentMapper.toResponseList(studentRepository.findAll(Sort.by("studentId")));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return studentMapper.toResponse(findStudent(id));
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudent(id);
        studentMapper.updateEntity(request, student); // fine amount is not touched
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> notFound(id));

        long stillBorrowed = borrowRecordRepository.countCurrentlyBorrowedByStudent(id);
        if (stillBorrowed > 0) {
            throw new BusinessRuleException("Cannot delete student #" + id + ": " + stillBorrowed
                    + " book(s) are still borrowed. Return them first.");
        }

        // Only fully-returned history is left, remove it so the foreign key does not block the delete.
        borrowRecordRepository.deleteByStudentId(id);
        studentRepository.delete(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getByGender(String gender) {
        Gender parsed = Gender.parse(gender); // 400 for anything except M / F
        return studentMapper.toResponseList(studentRepository.findByGenderOrderByStudentIdAsc(parsed));
    }

    @Override
    @Transactional
    public StudentResponse calculateFine(Long id, FineRequest request) {
        Student student = studentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> notFound(id));
        student.setFineAmount(FineCalculator.calculate(request.lateDays()));
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentResponse payFine(Long id, FinePaymentRequest request) {
        Student student = studentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> notFound(id));

        BigDecimal outstanding = student.getFineAmount();
        if (request.amount().compareTo(outstanding) > 0) {
            throw new BusinessRuleException("Payment of " + request.amount()
                    + " is more than the outstanding fine of " + outstanding);
        }
        student.setFineAmount(outstanding.subtract(request.amount()));
        return studentMapper.toResponse(studentRepository.save(student));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Student Not Found with id " + id);
    }
}
