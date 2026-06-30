package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Repository.BookRepository;
import com.example.demo.Domain.Common.Repository.LendRepository;
import com.example.demo.Domain.Common.Service.LibraryServiceImpl;
import com.example.demo.Exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LendServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LendRepository lendRepository;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Test
    @DisplayName("대출 정상: 재고가 1 줄고 반납기한이 설정된다")
    void lend_정상() {
        Book book = Book.builder().id(1L).title("클린 코드").isbn("i-1").totalCopies(3).availableCopies(2).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(lendRepository.save(any(Lend.class))).thenAnswer(inv -> inv.getArgument(0));

        Lend lend = libraryService.lend(1L, "kim");

        assertEquals(1, book.getAvailableCopies());
        assertNotNull(lend.getDueAt());
        assertEquals("kim", lend.getMember());
    }

    @Test
    @DisplayName("대출 경계: 재고 0 이면 대출할 수 없다 (D1 경계값)")
    void lend_재고0_예외() {
        Book book = Book.builder().id(1L).availableCopies(0).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(BizException.class, () -> libraryService.lend(1L, "kim"));
    }

    @Test
    @DisplayName("대출 예외: 존재하지 않는 도서면 BizException (D2)")
    void lend_미존재도서_예외() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> libraryService.lend(99L, "kim"));
    }

    @Test
    @DisplayName("반납 예외: 두 번 반납해도 재고는 한 번만 늘고 두 번째는 BizException (D3 중복 반납)")
    void returnBook_중복반납_차단() {
        Book book = Book.builder().id(1L).availableCopies(1).build();
        Lend lend = Lend.builder().id(1L).bookId(1L).build();
        when(lendRepository.findById(1L)).thenReturn(Optional.of(lend));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(lendRepository.save(any(Lend.class))).thenAnswer(inv -> inv.getArgument(0));

        libraryService.returnBook(1L);
        assertThrows(BizException.class, () -> libraryService.returnBook(1L));
        assertEquals(2, book.getAvailableCopies());
    }

    @Test
    @DisplayName("연체 경계: 반납기한 당일은 연체가 아니고 그 이후가 연체다 (D4 경계값)")
    void isOverdue_경계() {
        LocalDateTime due = LocalDateTime.of(2026, 1, 15, 10, 0);
        Lend lend = Lend.builder().dueAt(due).build();

        assertFalse(libraryService.isOverdue(lend, due.minusDays(1)));
        assertFalse(libraryService.isOverdue(lend, due));
        assertTrue(libraryService.isOverdue(lend, due.plusSeconds(1)));

        Lend returned = Lend.builder().dueAt(due).returnedAt(due.minusDays(2)).build();
        assertFalse(libraryService.isOverdue(returned, due.plusDays(10)));
    }
}
