package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Repository.BookRepository;
import com.example.demo.Domain.Common.Repository.LendRepository;
import com.example.demo.Exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LibraryServiceImpl implements LibraryService {

    public static final int LOAN_DAYS = 14;

    private final BookRepository bookRepository;
    private final LendRepository lendRepository;

    public LibraryServiceImpl(BookRepository bookRepository, LendRepository lendRepository) {
        this.bookRepository = bookRepository;
        this.lendRepository = lendRepository;
    }

    @Override
    @Transactional
    public Lend lend(Long bookId, String member) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BizException("존재하지 않는 도서입니다. id=" + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new BizException("대출 가능한 재고가 없습니다.");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        LocalDateTime now = LocalDateTime.now();
        Lend lend = Lend.builder()
                .bookId(bookId)
                .member(member)
                .lentAt(now)
                .dueAt(now.plusDays(LOAN_DAYS))
                .build();
        return lendRepository.save(lend);
    }

    @Override
    @Transactional
    public Lend returnBook(Long lendId) {
        Lend lend = lendRepository.findById(lendId)
                .orElseThrow(() -> new BizException("존재하지 않는 대출 기록입니다. id=" + lendId));

        if (lend.getReturnedAt() != null) {
            throw new BizException("이미 반납된 대출입니다.");
        }

        lend.setReturnedAt(LocalDateTime.now());
        lendRepository.save(lend);

        Book book = bookRepository.findById(lend.getBookId())
                .orElseThrow(() -> new BizException("존재하지 않는 도서입니다. id=" + lend.getBookId()));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return lend;
    }

    @Override
    public boolean isOverdue(Lend lend, LocalDateTime asOf) {
        if (lend.getReturnedAt() != null) {
            return false;
        }
        return asOf.isAfter(lend.getDueAt());
    }

    @Override
    @Transactional(readOnly = true)
    public int availableCopies(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BizException("존재하지 않는 도서입니다. id=" + bookId))
                .getAvailableCopies();
    }
}
