package io.github.dbstarll.study.boot.controller.english;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Projections;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.boot.controller.EntityNotFoundException;
import io.github.dbstarll.study.boot.model.SummaryWithTotal;
import io.github.dbstarll.study.entity.Book;
import io.github.dbstarll.study.entity.Unit;
import io.github.dbstarll.study.service.BookService;
import io.github.dbstarll.study.service.UnitService;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/english/unit", produces = MediaType.APPLICATION_JSON_VALUE)
class UnitController {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UnitService unitService;
    @Autowired
    private BookService bookService;

    @GetMapping("/{bookId}/book")
    SummaryWithTotal<Unit> index(@PathVariable("bookId") final ObjectId bookId, PageQuery query) {
        final Bson filter = unitService.filterByBookId(bookId);
        final Bson projection = Projections.include("sn", "title", "wordCount");
        return SummaryWithTotal
                .warp(unitService.count(filter), query.apply(unitService.find(filter)).projection(projection)).query(query);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('MODE_ADMIN')")
    Unit create(@RequestBody final ObjectNode body) throws Exception {
        final Unit unit = mapper.readerForUpdating(EntityFactory.newInstance(Unit.class)).readValue(body);
        if (null != unitService.save(unit, (Validate) null)) {
            final Book book = bookService.findById(unit.getBookId());
            if (book != null) {
                book.setUnitCount((int) unitService.countByBookId(unit.getBookId()));
            }
            bookService.save(book, (Validate) null);
        }
        return unit;
    }

    @PostMapping("/{unitId}")
    @PreAuthorize("hasAuthority('MODE_ADMIN')")
    Unit modify(@PathVariable("unitId") final ObjectId unitId, @RequestBody final ObjectNode body) throws Exception {
        final Unit unit = unitService.findById(unitId);
        if (unit == null) {
            throw new EntityNotFoundException(Unit.class, unitId);
        }
        mapper.readerForUpdating(unit).readValue(body);
        return unitService.save(unit, (Validate) null);
    }
}
