package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.LendRequest;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
public class BookController {

    private final LibraryService libraryService;

    public BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/lend")
    public ResponseEntity<Map<String, Object>> lend(@RequestBody @Valid LendRequest req) {
        Lend lend = libraryService.lend(req.getBookId(), req.getMember());
        Map<String, Object> body = new HashMap<>();
        body.put("lendId", lend.getId());
        body.put("dueAt", lend.getDueAt());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/return/{lendId}")
    public ResponseEntity<Map<String, Object>> returnBook(@PathVariable("lendId") Long lendId) {
        Lend lend = libraryService.returnBook(lendId);
        Map<String, Object> body = new HashMap<>();
        body.put("lendId", lend.getId());
        body.put("returnedAt", lend.getReturnedAt());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/books/{id}/available")
    public ResponseEntity<Map<String, Object>> available(@PathVariable("id") Long id) {
        Map<String, Object> body = new HashMap<>();
        body.put("availableCopies", libraryService.availableCopies(id));
        return ResponseEntity.ok(body);
    }
}
