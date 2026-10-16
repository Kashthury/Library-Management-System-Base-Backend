# Library Management System - Backend

Spring Boot 3.5 · Spring Data JPA · PostgreSQL · MapStruct · Lombok · Maven · Java 21+

REST API for the React frontend (`Library-Management-System-Base`). Every JSON field name matches the
frontend's `src/types/index.ts`, so the existing `src/api/management/*` services work unchanged.

> **Note:** Spring Boot 3.5 reached end of life on 2026-06-30 (3.5.16 was the last release). It is used here
> because it is the most widely documented line; plan an upgrade to Boot 4.x for production.

## 1. Run it

```bash
docker compose up -d          # PostgreSQL 16, database "library_db"  (or use your own Postgres)
mvn clean package             # generate MapStruct mapper beans and compile the application
mvn spring-boot:run           # API on http://localhost:8080
mvn test                      # unit tests (no database needed)
```

Tables `students`, `books`, `borrow_records` (with foreign keys) are created/updated automatically on startup
(`spring.jpa.hibernate.ddl-auto=update`). Only the empty database must exist.
Override connection settings with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

Then start the frontend (`npm run dev`, port 5173 is already allowed by CORS).
`frontend-patch/api.ts` is optional but recommended: replace `src/lib/api.ts` with it so the UI shows the
backend's real error message instead of "API request failed: 409 Conflict".

## 2. Package layout

```
com.library.lms
  controller/        REST endpoints
  dto/request|response   Java records (validation lives on the request records)
  service/           interfaces
  service/impl/      business logic
  mapper/            MapStruct mappers
  entity/            Student, Book, BorrowRecord
  repository/        Spring Data JPA (incl. row-locking + summary queries)
  exception/         custom exceptions + @RestControllerAdvice
  config/            CORS + business-rule properties
  util/FineCalculator
```

## 3. Endpoints

| Method | Endpoint | Purpose | Success |
|---|---|---|---|
| POST | `/api/students` | Register student (fine starts at 0) | 201 |
| GET | `/api/students` | All students | 200 |
| GET | `/api/students/{id}` | One student | 200 / 404 |
| PUT | `/api/students/{id}` | Update name, age, gender, course | 200 |
| DELETE | `/api/students/{id}` | Delete student | 204 / 409 |
| GET | `/api/students/gender/{M\|F}` | Filter by gender (`[]` if none, 400 if invalid) | 200 |
| POST | `/api/students/{id}/fine` | Body `{"lateDays": n}` - calculate **and store** fine | 200 |
| POST | `/api/students/{id}/fine/pay` | Body `{"amount": n}` - settle a fine *(extra)* | 200 |
| POST | `/api/books` | Register book | 201 |
| GET | `/api/books` | All books | 200 |
| GET | `/api/books/{id}` | One book *(extra)* | 200 / 404 |
| PUT | `/api/books/{id}` | Edit book / restock *(extra)* | 200 |
| POST | `/api/borrow` | Borrow | 201 |
| GET | `/api/borrow` | All borrow records, newest first *(the frontend needs this)* | 200 |
| GET | `/api/borrow/student/{studentId}` | Records of one student *(extra)* | 200 |
| POST | `/api/return` | Return | 200 |
| GET | `/api/library/summary` | Library summary | 200 |

Dates are `yyyy-MM-dd`. Money is a number (`250.00`).

## 4. Logic that the assignment left out (decisions made for you)

| Topic | What the backend does |
|---|---|
| **Automatic fine** | On every return: late days = `returnDate - dueDate` (never negative) -> fine table -> stored on the borrow record **and added to the student's `fineAmount`**. A record keeps the fine of its worst lateness, and only the *increase* is added, so returning one record in several parts never charges the same days twice. |
| **Manual fine endpoint** | `POST /students/{id}/fine` **sets** `fineAmount` to the calculated value (as the assignment says "store"). Automatic fines **add up**. If you would rather have the manual endpoint add as well, change one line in `StudentServiceImpl.calculateFine`. |
| **Due date** | Frontend sends `borrowDate` + `dueDate`. If missing: borrowDate = today, dueDate = +14 days (`library.default-loan-days`). Due date before borrow date -> 400. |
| **Return date** | Optional, default today. Before the borrow date -> 400. |
| **Partial returns** | `numberOfBooks` on a record = books **still borrowed**; it is reduced on each return. `returnDate` is filled only when the last book of that record is back (so the UI keeps showing "Return" while books remain). `originalQuantity` keeps the borrowed amount. |
| **Same book borrowed twice** | Each borrow is its own record. A return is applied to the **oldest** records first; the quantity is validated against the total the student holds of that book. |
| **5-book limit** | Counted from the still-borrowed quantity of all the student's records (`library.max-books-per-student`). |
| **Stock & concurrency** | Student and book rows are locked (`PESSIMISTIC_WRITE`) during borrow/return, so two simultaneous requests cannot take the last copy or exceed the limit. Everything runs in one transaction. |
| **Delete student** | Blocked with 409 while books are still borrowed. Otherwise the student's returned-history records are deleted with the student. |
| **Unpaid fines** | Optional rule `library.block-borrow-with-outstanding-fine` (default `false`) blocks borrowing until the fine is paid via `/fine/pay`. |
| **Summary definitions** | `totalRegisteredBooks` = all copies owned (shelf + borrowed); `totalAvailableBooks` = copies on the shelf; `totalBorrowedBooks` = `totalBooksCurrentlyBorrowed` = copies out right now. Students with 0..5 books are counted live; students with no loans = total - students with loans. |
| **Gender** | Accepts `M`, `F`, `Male`, or `Female` (case-insensitive); student responses return `Male` / `Female`. |

## 5. Error format

```json
{
  "timestamp": "2026-10-15T10:21:04",
  "status": 409,
  "error": "Conflict",
  "message": "Borrow limit exceeded: student already has 4 book(s) and requested 2 more (4 + 2 = 6, maximum is 5)",
  "path": "/api/borrow",
  "fieldErrors": null
}
```

| Status | When |
|---|---|
| 400 | Validation failed (`fieldErrors` lists each field), malformed JSON/date, invalid gender, due date < borrow date |
| 404 | Student / book not found |
| 409 | Business rule: 5-book limit, not enough stock, return > borrowed, no active borrow, delete with books out, overpaying a fine |
| 500 | Unexpected (details only in the server log) |

## 6. Small things to know about the existing frontend

* The Borrow/Return table's **Quantity** column shows `numberOfBooks`, which is `0` for fully returned rows.
  To show the original amount for history rows use `record.originalQuantity ?? record.numberOfBooks`
  (add `originalQuantity?: number` to `BorrowRecord` in `src/types/index.ts`).
* Extra response fields (`studentName`, `bookTitle`, `status`, `originalQuantity`) are ignored by the current UI and are
  available if you want to simplify it later.
* There is no authentication; add Spring Security before exposing this beyond a local/demo setup.
