package io.github.dbstarll.study.boot.controller.english;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.boot.controller.EntityNotFoundException;
import io.github.dbstarll.study.boot.model.SummaryWithTotal;
import io.github.dbstarll.study.boot.security.StudySecurity;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.Book;
import io.github.dbstarll.study.entity.UnitWord;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.BookService;
import io.github.dbstarll.study.service.ExerciseWordService;
import io.github.dbstarll.study.service.UnitWordService;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/english/book", produces = MediaType.APPLICATION_JSON_VALUE)
class BookController {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private BookService bookService;
    @Autowired
    private UnitWordService unitWordService;
    @Autowired
    private ExerciseWordService exerciseWordService;
    @Autowired
    private StudySecurity security;

    @GetMapping
    SummaryWithTotal<Book> index(PageQuery query) {
        final Bson filter = null;
        return SummaryWithTotal.warp(bookService.count(filter), query.apply(bookService.find(filter))).query(query);
    }

    @GetMapping("/exercise")
    SummaryWithTotal<ObjectNode> indexByExerciseBook(PageQuery query) {
        final ObjectId exerciseBookId = StudyUtils.getUserBookId(security);

        final Bson filter = Filters.gt("wordCount", 0);
        final Bson projection = Projections.exclude("dateCreated", "lastModified");

        final List<ObjectNode> list = new LinkedList<>();
        for (Book book : query.apply(bookService.find(filter)).projection(projection)) {
            final ObjectNode node = mapper.convertValue(book, ObjectNode.class);

            final Set<ObjectId> words = new HashSet<>();
            for (UnitWord word : unitWordService.findByBookId(book.getId())) {
                words.add(word.getWordId());
            }

            node.put("match", exerciseWordService.count(Filters.and(exerciseWordService.filterByBookId(exerciseBookId),
                    Filters.in(WordBase.FIELD_NAME_WORD_ID, words))));

            list.add(node);
        }

        return SummaryWithTotal.warp(bookService.count(filter), list).query(query);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('MODE_ADMIN')")
    Book create(@RequestBody final ObjectNode body) throws Exception {
        final Book book = mapper.readerForUpdating(EntityFactory.newInstance(Book.class)).readValue(body);
        return bookService.save(book, (Validate) null);
    }

    @PostMapping("/{bookId}")
    @PreAuthorize("hasAuthority('MODE_ADMIN')")
    Book modify(@PathVariable("bookId") final ObjectId bookId, @RequestBody final ObjectNode body) throws Exception {
        final Book book = bookService.findById(bookId);
        if (book == null) {
            throw new EntityNotFoundException(Book.class, bookId);
        }
        mapper.readerForUpdating(book).readValue(body);
        return bookService.save(book, (Validate) null);
    }
}
