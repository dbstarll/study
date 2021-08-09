package io.github.dbstarll.study.boot.controller.english;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.dbstarll.study.boot.controller.EntityNotFoundException;
import io.github.dbstarll.study.boot.security.StudySecurity;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.ExerciseBook;
import io.github.dbstarll.study.service.ExerciseBookService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping(path = "/english/exercise-book", produces = MediaType.APPLICATION_JSON_VALUE)
class ExerciseBookController {
    @Autowired
    private ExerciseBookService exerciseBookService;
    @Autowired
    private StudySecurity security;
    @Autowired
    private ObjectMapper mapper;

    @GetMapping
    ObjectNode show() {
        final ObjectId bookId = StudyUtils.getUserBookId(security);
        final ExerciseBook book = exerciseBookService.findById(bookId);
        if (book == null) {
            throw new EntityNotFoundException(ExerciseBook.class, bookId);
        }

        final ObjectNode node = mapper.convertValue(book, ObjectNode.class);
        node.remove(Arrays.asList("id", "dateCreated", "lastModified"));
        return node;
    }

    @GetMapping("/fock/{bookId}")
    @PreAuthorize("hasAuthority('MODE_ADMIN')")
    ObjectNode fock(@PathVariable final ObjectId bookId) {
        final ExerciseBook book = exerciseBookService.findById(bookId);
        if (book == null) {
            throw new EntityNotFoundException(ExerciseBook.class, bookId);
        }
        StudyUtils.fockUserBookId(security, bookId);
        final ObjectNode node = mapper.convertValue(book, ObjectNode.class);
        node.remove(Arrays.asList("id", "dateCreated", "lastModified"));
        return node;
    }
}
