package com.library.lms.repository;

import com.library.lms.entity.Student;
import com.library.lms.enums.Gender;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByGenderOrderByStudentIdAsc(Gender gender);

    long countByGender(Gender gender);

    /** Row lock so two concurrent borrow/return requests for one student cannot interleave. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Student s where s.studentId = :id")
    Optional<Student> findByIdForUpdate(@Param("id") Long id);
}
