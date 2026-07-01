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
        // 테스트용 도서 객체를 생성하고 데이터를 넣는다
        Book book = Book.builder().id(1L).title("클린 코드").isbn("i-1").totalCopies(3).availableCopies(2).build();
        // findById가 호출되면 1L에 저장된 테스트용 도서 데이터를 꺼낸다
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        // lendRepository.save가 어떤 객체로 호출되든 넘겨받은 lend 객체(첫 번째 인자)를 그대로 리턴하도록 설정
        when(lendRepository.save(any(Lend.class))).thenAnswer(inv -> inv.getArgument(0));
        // 1번 도서를 "kim"이 대출하도록 lend() 메서드 실행
        Lend lend = libraryService.lend(1L, "kim");

        assertEquals(1, book.getAvailableCopies()); // 책의 재고가 1이 맞는지 확인
        assertNotNull(lend.getDueAt()); // dueAt(반납기한)이 null이면 안된다
        assertEquals("kim", lend.getMember()); // 책을 대출한 사람이 "kim"이 맞는지 확인
    }

    @Test
    @DisplayName("대출 경계: 재고 0 이면 대출할 수 없다 (D1 경계값)")
    void lend_재고0_예외() {
        // 테스트용 도서 객체를 생성하고 데이터를 넣는다
        Book book = Book.builder().id(1L).availableCopies(0).build();
        // findById가 호출되면 1L에 저장된 테스트용 도서 데이터를 꺼낸다
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        // 재고가 0인 도서를 대출하려고 하면 BizException이 발생해야 함
        assertThrows(BizException.class, () -> libraryService.lend(1L, "kim"));
    }

    @Test
    @DisplayName("대출 예외: 존재하지 않는 도서면 BizException")
    void lend_미존재도서_예외() {
        fail("TODO: 테스트를 작성하세요");
    }

    @Test
    @DisplayName("반납 예외: 두 번 반납해도 재고는 한 번만 늘고 두 번째는 BizException")
    void returnBook_중복반납_차단() {
        fail("TODO: 테스트를 작성하세요");
    }

    @Test
    @DisplayName("연체 경계: 반납기한 당일은 연체가 아니고 그 이후가 연체다")
    void isOverdue_경계() {
        fail("TODO: 테스트를 작성하세요");
    }
}
