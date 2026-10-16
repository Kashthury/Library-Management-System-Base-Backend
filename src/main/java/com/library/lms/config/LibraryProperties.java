package com.library.lms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Business rules bound from the "library.*" section of application.yml. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "library")
public class LibraryProperties {

    /** Maximum number of books a student may hold at the same time. */
    private int maxBooksPerStudent = 5;

    /** Loan period (days) used when the borrow request does not send a due date. */
    private int defaultLoanDays = 14;

    /** When true, students with an unpaid fine cannot borrow more books. */
    private boolean blockBorrowWithOutstandingFine = false;
}
