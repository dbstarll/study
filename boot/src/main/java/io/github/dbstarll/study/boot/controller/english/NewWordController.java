package io.github.dbstarll.study.boot.controller.english;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.study.boot.controller.EntityNotFoundException;
import io.github.dbstarll.study.boot.model.SummaryWithTotal;
import io.github.dbstarll.study.boot.security.StudySecurity;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.entity.join.UnitBase;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.WordService;
import io.github.dbstarll.study.service.impl.WordServiceImplemental.WordWithJoin;
import io.github.dbstarll.study.utils.DictionaryApi;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/english/word", produces = MediaType.APPLICATION_JSON_VALUE)
class NewWordController {
    @Autowired
    private WordService wordService;
    @Autowired
    private DictionaryApi dictionaryApi;
    @Autowired
    private CacheControl cacheControl;
    @Autowired
    private StudySecurity security;

    @GetMapping("/{word}")
    ResponseEntity<Word> show(@PathVariable final String word) {
        final Word wd = wordService.findOne(wordService.filterByWord(word, false, false));
        if (wd == null) {
            throw new EntityNotFoundException(Word.class, null, "query word failed: " + word);
        }
        return ResponseEntity.ok().cacheControl(cacheControl).body(wd);
    }

    @GetMapping("/{wordId}/id")
    ResponseEntity<Word> showById(@PathVariable final ObjectId wordId) {
        final Word wd = wordService.findById(wordId);
        if (wd == null) {
            throw new EntityNotFoundException(Word.class, wordId);
        }
        return ResponseEntity.ok().cacheControl(cacheControl).body(wd);
    }

    @GetMapping("/{word}/{matchExchange}")
    SummaryWithTotal<Word> query(@PathVariable("word") final String word,
                                 @PathVariable("matchExchange") final boolean matchExchange) {
        final Bson wordFilter = wordService.filterByWord(word + ".*", matchExchange, true);
        final Bson filter;
        if (matchExchange) {
            filter = wordFilter;
        } else {
            filter = Filters.and(wordFilter, Filters.eq("cri", true),
                    Filters.or(Filters.exists(WordBase.FIELD_NAME_WORD_ID, false), Filters.exists("exchanges")));
        }
        return SummaryWithTotal.warp(wordService.count(filter),
                wordService.find(filter).sort(Sorts.ascending(Namable.FIELD_NAME_NAME)).limit(10));
    }

    @GetMapping("/{word}/{unitId}/unit")
    SummaryWithTotal<WordWithJoin> queryJoinUnit(@PathVariable("word") final String word,
                                                 @PathVariable("unitId") final ObjectId unitId) {
        final Bson wordFilter = wordService.filterByWord(word + ".*", false, true);
        final Bson filter = Filters.and(wordFilter, Filters.eq("cri", true),
                Filters.or(Filters.exists(WordBase.FIELD_NAME_WORD_ID, false), Filters.exists("exchanges")));

        final PageQuery query = new PageQuery();
        query.setSort(Namable.FIELD_NAME_NAME);
        return SummaryWithTotal.warp(wordService.count(filter),
                wordService.findWithJoin(filter, "unit_word", UnitBase.FIELD_NAME_UNIT_ID, unitId, query));
    }

    @GetMapping("/{word}/exercise")
    SummaryWithTotal<WordWithJoin> queryJoinExercise(@PathVariable("word") final String word) {
        final Bson wordFilter = wordService.filterByWord(word + ".*", false, true);
        final Bson filter = Filters.and(wordFilter, Filters.eq("cri", true),
                Filters.or(Filters.exists(WordBase.FIELD_NAME_WORD_ID, false), Filters.exists("exchanges")));

        final PageQuery query = new PageQuery();
        query.setSort(Namable.FIELD_NAME_NAME);
        return SummaryWithTotal.warp(wordService.count(filter), wordService.findWithJoin(filter, "exercise_word",
                BookBase.FIELD_NAME_BOOK_ID, StudyUtils.getUserBookId(security), query));
    }

    @PutMapping("/{word}")
    Word create(@PathVariable final String word) {
        final Word wd;
        try {
            wd = dictionaryApi.query(word);
        } catch (Throwable ex) {
            throw new EntityNotFoundException(Word.class, null, "dictionaryApi.query failed: " + word, ex);
        }

        if (wd == null) {
            throw new EntityNotFoundException(Word.class, null, "dictionaryApi.query failed: " + word);
        } else {
            return StudyUtils.saveRecursion(wd, wordService, dictionaryApi);
        }
    }
}
