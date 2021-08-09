package io.github.dbstarll.study.boot.controller;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.study.boot.model.SummaryWithTotal;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.WordService;
import io.github.dbstarll.study.utils.DictionaryApi;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

@Controller
@RequestMapping(path = "/word", produces = MediaType.APPLICATION_JSON_VALUE)
class WordController {
    @Autowired
    private WordService wordService;

    @Autowired
    private DictionaryApi dictionaryApi;
    @Autowired
    private CacheControl cacheControl;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    String indexPage() {
        return "word";
    }

    @ResponseBody
    @GetMapping
    SummaryWithTotal<Word> index() {
        return SummaryWithTotal.warp(wordService.count(null), (List<Word>) null);
    }

    @ResponseBody
    @GetMapping("/{word}")
    ResponseEntity<Word> show(@PathVariable final String word) {
        final Word wd = wordService.findOne(wordService.filterByWord(word, false, false));
        if (wd == null) {
            throw new EntityNotFoundException(Word.class, null, "query word failed: " + word);
        }
        return ResponseEntity.ok().cacheControl(cacheControl).body(wd);
    }

    @ResponseBody
    @GetMapping("/{wordId}/id")
    ResponseEntity<Word> showById(@PathVariable final ObjectId wordId) {
        final Word wd = wordService.findById(wordId);
        if (wd == null) {
            throw new EntityNotFoundException(Word.class, wordId);
        }
        return ResponseEntity.ok().cacheControl(cacheControl).body(wd);
    }

    @ResponseBody
    @GetMapping("/{word}/{matchExchange}")
    SummaryWithTotal<Word> query(@PathVariable("word") final String word,
                                 @PathVariable("matchExchange") final boolean matchExchange) {
        final Bson wordFilter = wordService.filterByWord(word + ".*", matchExchange, true);
        final Bson filter;
        if (matchExchange) {
            filter = wordFilter;
        } else {
            filter = Filters.and(wordFilter, Filters.or(Filters.eq("cri", true), Filters.exists("exchanges"),
                    Filters.exists(WordBase.FIELD_NAME_WORD_ID, false)));
        }
        return SummaryWithTotal.warp(wordService.count(filter),
                wordService.find(filter).sort(Sorts.ascending(Namable.FIELD_NAME_NAME)).limit(10));
    }

    @ResponseBody
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

    @ResponseBody
    @PutMapping(consumes = "text/plain;charset=UTF-8")
    SummaryWithTotal<String> createBatch(@RequestBody final String body) throws IOException {
        final Set<String> words = new TreeSet<>();
        final StringTokenizer tokenizer = new StringTokenizer(body, "　 -—=(’/;,\t\n\r\f", false);
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (allAlpha(token) && containAlpha(token)) {
                words.add(token.toLowerCase());
            }
        }

        final int total = words.size();
        if (total > 0) {
            for (Word word : wordService.find(Filters.in(Namable.FIELD_NAME_NAME, words))) {
                words.remove(word.getName());
            }
        }

        return SummaryWithTotal.warp(total, words);
    }

    private static boolean allAlpha(String val) {
        for (char ch : val.toCharArray()) {
            if (ch >= 'a' && ch <= 'z') {
                continue;
            } else if (ch >= 'A' && ch <= 'Z') {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean containAlpha(String val) {
        for (char ch : val.toCharArray()) {
            if (ch >= 'a' && ch <= 'z') {
                return true;
            } else if (ch >= 'A' && ch <= 'Z') {
                return true;
            }
        }
        return false;
    }
}
