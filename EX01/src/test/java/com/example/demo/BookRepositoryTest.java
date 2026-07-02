package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // JPA 관련 컴포넌트(Entity, Repository)만 로드해 데이터 계층을 테스트하기 위한 어노테이션
class BookRepositoryTest {

    // === Repository 주입 ===
    @Autowired // 스프링 컨테이너에 등록된 Bean을 자동으로 필드에 주입하는 어노테이션
    private BookRepository bookRepository;  // BookRepository를 스프링 컨테이너에서 주입받는다.

    // === ISBN 조회 및 존재 여부 테스트 ===
    // 도서를 저장한 뒤 ISBN으로 도서 조회, 존재 여부를 확인한다.
    @Test // JUnit이 해당 메서드를 테스트 케이스로 인식하고 실행하도록 지정하는 어노테이션
    @DisplayName("ISBN 으로 도서를 조회하고 존재 여부를 확인한다") // 테스트 실행 결과 화면에 표시될 테스트 이름을 지정하는 어노테이션
    void findByIsbn_existsByIsbn() {

        // === 테스트용 도서 저장 ===
        // .builder(): 복잡한 객체를 단계별로 생성할 수 있도록 하는 생성 패턴
        bookRepository.save(Book.builder() // Book 생성 & DB에 저장
                .title("리팩터링") // 제목 설정
                .isbn("REPO-1") // ISBN 설정
                .totalCopies(2) // 총 권수 설정
                .availableCopies(2) // 대출 가능 권수 설정
                .build() // Book 객체 생성
        );

        // === ISBN 조회 검증 ===
        // findByIsbn(): ISBN 값으로 도서를 조회해 Optional<Book> 형태로 반환하는 메서드
        // isPresent(): Optional 객체 내부에 값이 존재하는지 여부를 확인하는 메서드
        // 저장되었는지 확인
        assertTrue( // 조건이 True인지 검증
                bookRepository.findByIsbn("REPO-1") // ISBN으로 도서 조회
                        .isPresent()             // 조회 결과가 존재하는지 확인
        );
        // 도서 조회
        assertEquals( // 두 값이 같은지 검증
                "리팩터링", // 기대 값 (책 제목)
                bookRepository.findByIsbn("REPO-1") // ISBN으로 도서 조회
                        .get()                  // Optional에서 Book 객체 꺼내기
                        .getTitle()             // Book의 책 제목 가져오기
        );

        // === ISBN 존재 여부 검증 ===
        // existsByIsbn(): 특정 ISBN을 가진 데이터가 존재하는지 여부를 boolean으로 반환하는 메서드
        // ISBN 존재 여부 확인
        assertTrue( // 조건이 True인지 검증
                bookRepository.existsByIsbn("REPO-1")); // ISBN이 존재하는지 확인
        // ISBN 미존재 여부 확인
        assertFalse( // 조건이 False인지 검증
                bookRepository.existsByIsbn("NONE-0")); // ISBN이 존재하지 않는지 확인
    }
    
}
