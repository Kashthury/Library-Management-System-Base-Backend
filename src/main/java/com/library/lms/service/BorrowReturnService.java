package com.library.lms.service;

import com.library.lms.dto.request.BorrowRequest;
import com.library.lms.dto.request.ReturnRequest;
import com.library.lms.dto.response.BorrowRecordResponse;

import java.util.List;

public interface BorrowReturnService {

    BorrowRecordResponse borrow(BorrowRequest request);

    BorrowRecordResponse returnBooks(ReturnRequest request);

    List<BorrowRecordResponse> getAllRecords();

    List<BorrowRecordResponse> getRecordsByStudent(Long studentId);
}
