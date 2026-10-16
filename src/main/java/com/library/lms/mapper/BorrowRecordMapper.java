package com.library.lms.mapper;

import com.library.lms.dto.response.BorrowRecordResponse;
import com.library.lms.entity.BorrowRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BorrowRecordMapper {

    @Mapping(source = "student.studentId", target = "studentId")
    @Mapping(source = "student.studentName", target = "studentName")
    @Mapping(source = "book.bookId", target = "bookId")
    @Mapping(source = "book.bookTitle", target = "bookTitle")
    @Mapping(target = "overdueDays", expression = "java(effectiveOverdueDays(borrowRecord))")
    @Mapping(target = "status", expression = "java(statusOf(borrowRecord))")
    BorrowRecordResponse toResponse(BorrowRecord borrowRecord);

    List<BorrowRecordResponse> toResponseList(List<BorrowRecord> borrowRecords);

    /** Active records show how late they are TODAY; returned records show the stored value. */
    default Integer effectiveOverdueDays(BorrowRecord borrowRecord) {
        int stored = borrowRecord.getOverdueDays() == null ? 0 : borrowRecord.getOverdueDays();
        if (!borrowRecord.isActive()) {
            return borrowRecord.getOverdueDays();
        }
        long live = Math.max(0, ChronoUnit.DAYS.between(borrowRecord.getDueDate(), LocalDate.now()));
        return (int) Math.max(live, stored);
    }

    default String statusOf(BorrowRecord borrowRecord) {
        return borrowRecord.isActive() ? "ACTIVE" : "RETURNED";
    }
}
