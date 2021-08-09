package io.github.dbstarll.study.boot.controller.english;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.boot.controller.EntityNotFoundException;
import io.github.dbstarll.study.boot.model.SummaryWithTotal;
import io.github.dbstarll.study.boot.security.StudySecurity;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.Exercise;
import io.github.dbstarll.study.entity.Exercise.ExerciseKey;
import io.github.dbstarll.study.entity.ExerciseBook;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.Word.ExchangeKey;
import io.github.dbstarll.study.entity.ext.Exchange;
import io.github.dbstarll.study.entity.ext.MasterPercent;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.service.ExerciseBookService;
import io.github.dbstarll.study.service.ExerciseService;
import io.github.dbstarll.study.service.ExerciseWordService;
import io.github.dbstarll.study.service.WordService;
import io.github.dbstarll.study.utils.PageQuery;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Map.Entry;

@RestController
@RequestMapping(path = "/english/exercise-word", produces = MediaType.APPLICATION_JSON_VALUE)
class ExerciseWordController {
    @Autowired
    private WordService wordService;
    @Autowired
    private ExerciseWordService exerciseWordService;
    @Autowired
    private ExerciseService exerciseService;
    @Autowired
    private ExerciseBookService exerciseBookService;
    @Autowired
    private StudySecurity security;
    @Autowired
    private ObjectMapper mapper;

    @GetMapping
    SummaryWithTotal<ExerciseWord> index(PageQuery query) {
        final Bson filter = exerciseWordService.filterByBookId(StudyUtils.getUserBookId(security));
        final Bson projection = Projections.fields(Projections.excludeId(),
                Projections.exclude(BookBase.FIELD_NAME_BOOK_ID));
        return SummaryWithTotal
                .warp(exerciseWordService.count(filter), query.apply(exerciseWordService.find(filter).projection(projection)))
                .query(query);
    }

    @PutMapping("/{wordId}")
    ExerciseWord create(@PathVariable("wordId") final ObjectId wordId) {
        final ObjectId bookId = StudyUtils.getUserBookId(security);
        final ExerciseWord exerciseWord = newExerciseWord(bookId, wordId, null);
        if (null != exerciseWord) {
            updateBookWordCount(bookId);
        }
        return exerciseWord;
    }

    @PutMapping
    Object createBatch(@RequestBody final Set<ObjectId> wordIds) {
        final ObjectId bookId = StudyUtils.getUserBookId(security);
        final Set<ObjectId> success = new HashSet<>();
        final Map<ObjectId, Validate> error = new HashMap<>();

        int total = 0;
        for (ObjectId wordId : wordIds) {
            total++;
            final Validate validate = new DefaultValidate();
            if (null != newExerciseWord(bookId, wordId, validate)) {
                success.add(wordId);
            } else {
                error.put(wordId, validate);
            }
        }

        final Map<String, Object> result = new HashMap<>();
        if (!success.isEmpty()) {
            updateBookWordCount(bookId);
            result.put("success", success);
        }
        if (!error.isEmpty()) {
            result.put("error", error);
        }
        result.put("total", total);
        result.put("changed", !success.isEmpty());
        return result;
    }

    private ExerciseWord newExerciseWord(final ObjectId bookId, final ObjectId wordId, final Validate validate) {
        final ExerciseWord exerciseWord = EntityFactory.newInstance(ExerciseWord.class);
        exerciseWord.setBookId(bookId);
        exerciseWord.setWordId(wordId);
        exerciseWord.setExchanges(exchanges(wordId));
        return exerciseWordService.save(exerciseWord, validate);
    }

    private Map<String, Exchange> exchanges(final ObjectId wordId) {
        final Word word = wordService.findById(wordId);
        if (word != null && word.getExchanges() != null && !word.getExchanges().isEmpty()) {
            final Map<String, Exchange> exchanges = new HashMap<>();
            for (Exchange exchange : word.getExchanges()) {
                if (exchange.getWord().indexOf(' ') < 0) {
                    exchanges.put(exchange.getKey().name(), new Exchange(null, exchange.getWord(), exchange.getClassify()));
                }
            }
            return exchanges;
        }
        return null;
    }

    private void updateBookWordCount(final ObjectId bookId) {
        final ExerciseBook exerciseBook = exerciseBookService.findById(bookId);
        if (exerciseBook != null) {
            exerciseBook.setWordCount((int) exerciseWordService.countByBookId(bookId));
            exerciseBookService.save(exerciseBook, (Validate) null);
        }
    }

    @PutMapping("/{exerciseWordId}/{exerciseKey}/{level}/{spellWord}")
    Object exercise(@PathVariable("exerciseWordId") final ObjectId exerciseWordId,
                    @PathVariable("exerciseKey") final ExerciseKey exerciseKey, @PathVariable("level") final int level,
                    @PathVariable("spellWord") final String spellWord) {
        return exercise(exerciseWordId, exerciseKey, null, level, spellWord, null);
    }

    @PutMapping("/{exerciseWordId}/{exerciseKey}/{exchangeKey}/{level}/{spellWord}/{nextExchangeKey}")
    Object exercise(@PathVariable("exerciseWordId") final ObjectId exerciseWordId,
                    @PathVariable("exerciseKey") final ExerciseKey exerciseKey,
                    @PathVariable("exchangeKey") final ExchangeKey exchangeKey, @PathVariable("level") final int level,
                    @PathVariable("spellWord") final String spellWord,
                    @PathVariable("nextExchangeKey") final ExchangeKey nextExchangeKey) {
        final ExerciseWord exerciseWord = exerciseWordService.findById(exerciseWordId);
        if (exerciseWord == null) {
            throw new EntityNotFoundException(ExerciseWord.class, exerciseWordId);
        }

        final MasterPercent masterPercent = getMasterPercent(exerciseWord, exerciseKey, exchangeKey);

        final String correctWord = getCorrectWord(exerciseWord, exchangeKey);
        final boolean correct = correctWord.equals(spellWord);
        final Exercise exercise = EntityFactory.newInstance(Exercise.class);
        exercise.setBookId(exerciseWord.getBookId());
        exercise.setWordId(exerciseWord.getWordId());
        exercise.setExerciseKey(exerciseKey);
        exercise.setExchangeKey(exchangeKey);
        exercise.setLevel(level);
        exercise.setName(correctWord);
        exercise.setCorrect(correct);
        exercise.setBingo(masterPercent.getBingo());
        exercise.setLast(masterPercent.getLast());
        if (!correct) {
            exercise.setDescription(spellWord);
        }
        exerciseService.save(exercise, (Validate) null);

        masterPercent.setTotal(masterPercent.getTotal() + 1);
        if (correct) {
            masterPercent.setCorrect(masterPercent.getCorrect() + 1);
            masterPercent.setBingo(masterPercent.getBingo() + 1);
        } else {
            masterPercent.setBingo(0);
        }
        masterPercent.setLast(exercise.getDateCreated());
        masterPercent.setNext(StudyUtils.getNextExerciseTime(masterPercent));
        masterPercent.setPercent(nextPercent(masterPercent.getPercent(), correct, exerciseKey));
        exerciseWordService.save(exerciseWord, (Validate) null);

        final Map<String, Object> results = exercise(exerciseWord.getBookId(), exerciseKey, nextExchangeKey);
        if (!correct) {
            final Collection<Error> errors = new LinkedList<>();
            for (Entry<String, Integer> entry : exerciseService.countErrors(exerciseWord.getBookId(),
                    exerciseWord.getWordId(), exerciseKey, exchangeKey)) {
                errors.add(new Error(entry.getKey(), entry.getValue()));
            }
            results.put("errors", errors);
        }
        return results;
    }

    @GetMapping("/{exerciseKey}/exercise")
    Object exercise(@PathVariable("exerciseKey") final ExerciseKey exerciseKey) {
        return exercise(StudyUtils.getUserBookId(security), exerciseKey, null);
    }

    @GetMapping("/{exerciseKey}/{exchangeKey}/exercise")
    Object exercise(@PathVariable("exerciseKey") final ExerciseKey exerciseKey,
                    @PathVariable("exchangeKey") final ExchangeKey exchangeKey) {
        return exercise(StudyUtils.getUserBookId(security), exerciseKey, exchangeKey);
    }

    private Map<String, Object> exercise(final ObjectId bookId, final ExerciseKey exerciseKey,
                                         final ExchangeKey exchangeKey) {
        final Date now = new Date();
        final String key = exerciseKey.name() + (exchangeKey != null ? "_" + exchangeKey.name() : "");
        final String fieldNext = "masterPercents." + key + ".next";
        final String fieldLast = "masterPercents." + key + ".last";
        final Bson filterByBookId = exerciseWordService.filterByBookId(bookId);
        final List<Bson> filterReview = filters(filterByBookId, Filters.exists(fieldNext, true),
                Filters.lt(fieldNext, now));
        final List<Bson> filterNew = filters(filterByBookId, Filters.exists(fieldNext, false));
        final List<Bson> filterLast = filters(filterByBookId, Filters.exists(fieldNext, true),
                Filters.lt(fieldLast, DateUtils.addMinutes(now, -10)));
        if (exchangeKey != null) {
            final Bson filterExchange = Filters.exists("exchanges." + exchangeKey.name() + ".classify");
            filterReview.add(filterExchange);
            filterNew.add(filterExchange);
            filterLast.add(filterExchange);
        }
        final Entry<Bson, Bson> entryReview = EntryWrapper.wrap(Filters.and(filterReview), Sorts.descending(fieldNext));
        final Entry<Bson, Bson> entryNew = EntryWrapper.wrap(Filters.and(filterNew), Sorts.ascending(Entity.FIELD_NAME_ID));
        final Entry<Bson, Bson> entryLast = EntryWrapper.wrap(Filters.and(filterLast), Sorts.ascending(fieldNext));

        final ExerciseWord exerciseWord = exerciseWord(entryReview, entryNew, entryLast);
        if (exerciseWord == null) {
            throw new EntityNotFoundException(ExerciseWord.class, null, "No more Exercise lately");
        }
        final Word word = wordService.findById(exerciseWord.getWordId());
        if (word == null) {
            throw new EntityNotFoundException(Word.class, exerciseWord.getWordId());
        }

        final Map<String, Object> results = new HashMap<>();
        results.put("exerciseWord", mapper.convertValue(exerciseWord, ObjectNode.class)
                .remove(Arrays.asList("bookId", "dateCreated", "lastModified", "wordId")));
        results.put("word",
                mapper.convertValue(word, ObjectNode.class).remove(Arrays.asList("dateCreated", "lastModified")));
        results.put("today", countToday(bookId, exerciseKey, exchangeKey, DateUtils.truncate(new Date(), Calendar.DATE)));
        results.put("all", countToday(bookId, exerciseKey, exchangeKey, null));
        if (ExerciseKey.listen == exerciseKey) {
            results.put("interferes", getInterfereWords(bookId, exerciseWord, 3, false));
        }

        results.put("review", exerciseWordService.count(entryReview.getKey()));
        results.put("new", exerciseWordService.count(entryNew.getKey()));
        results.put("guest", "5c1349fac0ad810254687ca1".equals(bookId.toHexString()));

        return results;
    }

    private List<Bson> filters(Bson... filters) {
        return new LinkedList<>(Arrays.asList(filters));
    }

    @SafeVarargs
    private final ExerciseWord exerciseWord(final Entry<Bson, Bson>... filters) {
        for (Entry<Bson, Bson> filter : filters) {
            final ExerciseWord word = exerciseWordService.find(filter.getKey()).sort(filter.getValue()).limit(1).first();
            if (word != null) {
                return word;
            }
        }
        return null;
    }

    private final String getCorrectWord(final ExerciseWord exerciseWord, final ExchangeKey exchangeKey) {
        if (exchangeKey != null) {
            final Word word = wordService.findById(exerciseWord.getWordId());
            for (Exchange exchange : word.getExchanges()) {
                if (exchange.getKey() == exchangeKey) {
                    return exchange.getWord();
                }
            }
        }
        return exerciseWord.getName();
    }

    private List<Word> getInterfereWords(final ObjectId bookId, final ExerciseWord exerciseWord, int num,
                                         boolean similar) {
        // final String name = exerciseWord.getName();
        // final char first = name.charAt(0);
        // final char last = name.charAt(name.length() - 1);
        // final Pattern pattern = Pattern.compile("^" + first + ".*" + "$", Pattern.CASE_INSENSITIVE);

        final Bson filter = exerciseWordService.filterByInterfere(bookId, exerciseWord, null);

        final Set<ObjectId> wordIds = new HashSet<>();
        for (ExerciseWord similarWord : exerciseWordService.sample(filter, num)) {
            wordIds.add(similarWord.getWordId());
        }

        final List<Word> words = new LinkedList<>();
        for (Word word : wordService.find(Filters.in(Entity.FIELD_NAME_ID, wordIds))) {
            words.add(word);
        }
        return words;
    }

    private Map<String, Long> countToday(final ObjectId bookId, final ExerciseKey exerciseKey,
                                         final ExchangeKey exchangeKey, final Date last) {
        final Map<String, Long> result = new HashMap<>();

        final List<Bson> filters = new ArrayList<>();
        filters.add(exerciseService.filterByBookId(bookId));
        filters.add(Filters.eq("exerciseKey", exerciseKey));
        if (exchangeKey != null) {
            filters.add(Filters.eq("exchangeKey", exchangeKey));
        }
        if (last != null) {
            filters.add(Filters.gte("dateCreated", last));
        }

        result.put("total", exerciseService.count(Filters.and(filters)));

        filters.add(Filters.eq("correct", true));
        result.put("correct", exerciseService.count(Filters.and(filters)));

        return result;
    }

    private float nextPercent(final float percent, final boolean correct, final ExerciseKey exerciseKey) {
        final int level = (int) percent;
        final float percentInLevel = percent - level;

        final float next;
        if (correct) {
            next = (1.0f + percentInLevel) / 2;
        } else if (level > 0) {
            next = 0f;
        } else {
            next = percentInLevel / 2;
        }

        if (next > 0.9 && level < exerciseKey.maxLevel()) {
            return level + 1.0f;
        } else if (level > 0 && percentInLevel == 0f && next == 0f) {
            return level - 1.0f;
        } else {
            return level + next;
        }
    }

    private MasterPercent getMasterPercent(final ExerciseWord exerciseWord, final ExerciseKey exerciseKey,
                                           final ExchangeKey exchangeKey) {
        Map<String, MasterPercent> percens = exerciseWord.getMasterPercents();
        if (percens == null) {
            exerciseWord.setMasterPercents(percens = new HashMap<>());
        }

        final String key = exerciseKey.name() + (exchangeKey != null ? "_" + exchangeKey.name() : "");
        MasterPercent masterPercent = percens.get(key);
        if (masterPercent == null) {
            percens.put(key, masterPercent = new MasterPercent());
        }

        return masterPercent;
    }

    public static class Error {
        private final String name;
        private final int value;

        public Error(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
