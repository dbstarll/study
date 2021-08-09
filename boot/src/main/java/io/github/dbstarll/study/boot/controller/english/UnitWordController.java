package io.github.dbstarll.study.boot.controller.english;

import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.boot.model.SummaryWithTotal;
import io.github.dbstarll.study.boot.security.StudySecurity;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.Book;
import io.github.dbstarll.study.entity.Unit;
import io.github.dbstarll.study.entity.UnitWord;
import io.github.dbstarll.study.service.BookService;
import io.github.dbstarll.study.service.UnitService;
import io.github.dbstarll.study.service.UnitWordService;
import io.github.dbstarll.study.service.attach.UnitWordServiceAttach.UnitWordWithExercise;
import io.github.dbstarll.study.utils.CountBlock;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/english/unit-word", produces = MediaType.APPLICATION_JSON_VALUE)
class UnitWordController {
  @Autowired
  private UnitWordService unitWordService;
  @Autowired
  private UnitService unitService;
  @Autowired
  private BookService bookService;
  @Autowired
  private StudySecurity security;

  @GetMapping("/{unitId}/unit")
  SummaryWithTotal<UnitWord> index(@PathVariable("unitId") final ObjectId unitId, PageQuery query) {
    final Bson filter = unitWordService.filterByUnitId(unitId);
    return SummaryWithTotal.warp(unitWordService.count(filter), query.apply(unitWordService.find(filter))).query(query);
  }

  @GetMapping("/{bookId}/exercise")
  SummaryWithTotal<UnitWordWithExercise> indexByExerciseBook(@PathVariable("bookId") final ObjectId bookId,
                                                             final PageQuery query) {
    final ObjectId exerciseBookId = StudyUtils.getUserBookId(security);
    final Bson filter = unitWordService.filterByBookId(bookId);
    return SummaryWithTotal
            .warp(unitWordService.count(filter), unitWordService.findWithExercise(filter, exerciseBookId, query))
            .query(query);
  }

  @PutMapping("/{unitId}/{wordId}")
  @PreAuthorize("hasAuthority('MODE_ADMIN')")
  UnitWord create(@PathVariable("unitId") final ObjectId unitId, @PathVariable("wordId") final ObjectId wordId) {
    final UnitWord word = EntityFactory.newInstance(UnitWord.class);
    word.setUnitId(unitId);
    word.setWordId(wordId);
    if (null != unitWordService.save(word, (Validate) null)) {
      final Unit unit = unitService.findById(word.getUnitId());
      if (unit != null) {
        unit.setWordCount((int) unitWordService.countByUnitId(word.getUnitId()));
      }
      unitService.save(unit, (Validate) null);

      final Book book = bookService.findById(word.getBookId());
      if (book != null) {
        final CountBlock<ObjectId> count = new CountBlock<>();
        unitWordService.distinctWordId(unitWordService.filterByBookId(word.getBookId())).forEach(count);
        book.setWordCount(count.getCount());
      }
      bookService.save(book, (Validate) null);
    }
    return word;
  }
}
