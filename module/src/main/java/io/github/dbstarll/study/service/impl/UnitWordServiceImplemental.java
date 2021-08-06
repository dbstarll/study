package io.github.dbstarll.study.service.impl;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Variable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation.Position;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.entity.Unit;
import io.github.dbstarll.study.entity.UnitWord;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.entity.join.UnitBase;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.UnitService;
import io.github.dbstarll.study.service.UnitWordService;
import io.github.dbstarll.study.service.WordService;
import io.github.dbstarll.study.service.attach.UnitWordServiceAttach;
import io.github.dbstarll.study.utils.PageQuery;
import io.github.dbstarll.utils.lang.wrapper.IterableWrapper;
import io.github.dbstarll.utils.lang.wrapper.IteratorWrapper;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.Validate.notNull;

public final class UnitWordServiceImplemental extends StudyImplementals<UnitWord, UnitWordService>
        implements UnitWordServiceAttach {
  private static final List<Variable<String>> LET_WORD_ID = new LinkedList<>();

  static {
    LET_WORD_ID.add(new Variable<>(WordBase.FIELD_NAME_WORD_ID, "$" + WordBase.FIELD_NAME_WORD_ID));
  }

  private static final Bson MATCH_WORD_ID = Filters
          .expr(Filters.eq("$eq", Arrays.asList("$" + WordBase.FIELD_NAME_WORD_ID, "$$" + WordBase.FIELD_NAME_WORD_ID)));
  private static final Bson PROJECTION_UNIT_WORD = Aggregates.project(Projections.fields(Projections.excludeId(),
          Projections.exclude("dateCreated", "lastModified", BookBase.FIELD_NAME_BOOK_ID)));

  private static final Bson PROJECTION_EXERCISE_WORD = Aggregates
          .project(Projections.fields(Projections.excludeId(), Projections.exclude("lastModified",
                  BookBase.FIELD_NAME_BOOK_ID, Namable.FIELD_NAME_NAME, WordBase.FIELD_NAME_WORD_ID)));// masterPercents

  private WordService wordService;
  private UnitService unitService;

  public UnitWordServiceImplemental(UnitWordService service, Collection<UnitWord> collection) {
    super(service, collection);
  }

  public void setWordService(WordService wordService) {
    this.wordService = wordService;
  }

  public void setUnitService(UnitService unitService) {
    this.unitService = unitService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    notNull(wordService, "wordService is null");
    notNull(unitService, "unitService is null");
  }

  @Override
  public Iterable<UnitWordWithExercise> findWithExercise(final Bson filter, final ObjectId exerciseBookId,
                                                         final PageQuery query) {
    final List<Bson> pipeline = new LinkedList<>();
    if (filter != null) {
      pipeline.add(Aggregates.match(filter));
    }
    query.apply(pipeline);
    pipeline.add(PROJECTION_UNIT_WORD);
    pipeline.add(exerciseLookup(exerciseBookId));
    pipeline.add(wordLookup());
    return IterableWrapper.wrap(new IteratorWrapper<UnitWordWithExercise, UnitWordWithExercise>(
            getCollection().aggregate(pipeline, UnitWordWithExercise.class).iterator()) {
      @Override
      protected UnitWordWithExercise next(UnitWordWithExercise entity) {
        final List<ExerciseWord> exercises = entity.getExercises();
        entity.setExercise(exercises == null || exercises.isEmpty() ? null : exercises.get(0));
        entity.setExercises(null);

        final List<Word> words = entity.getWords();
        if (entity.getExercise() == null) {
          entity.setWord(words == null || words.isEmpty() ? null : words.get(0));
        }
        entity.setWords(null);
        return entity;
      }
    });
  }

  private Bson exerciseLookup(final ObjectId exerciseBookId) {
    final Bson matchBookId = Filters.eq(BookBase.FIELD_NAME_BOOK_ID, exerciseBookId);
    final Bson match = Aggregates.match(Filters.and(matchBookId, MATCH_WORD_ID));
    return Aggregates.lookup("exercise_word", LET_WORD_ID, Arrays.asList(match, PROJECTION_EXERCISE_WORD), "exercises");
  }

  private Bson wordLookup() {
    return Aggregates.lookup("word", WordBase.FIELD_NAME_WORD_ID, Entity.FIELD_NAME_ID, "words");
  }

  @Override
  public UnitWord save(UnitWord entity, ObjectId newEntityId, Validate validate) {
    return validateAndSave(entity, newEntityId, validate, new DuplicateValidation());
  }

  private class DuplicateValidation extends AbstractEntityValidation {
    @Override
    public void validate(UnitWord entity, UnitWord original, Validate validate) {
      if (original == null && !validate.hasErrors()) {
        final Bson filter = Filters.and(service.filterByUnitId(entity.getUnitId()),
                service.filterByWordId(entity.getWordId()));
        if (service.count(filter) > 0) {
          validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "重复的单词");
        }
      }
    }
  }

  /**
   * wordBaseValidation.
   *
   * @return wordBaseValidation
   */
  @GeneralValidation(value = WordBase.class, position = Position.FIRST)
  public Validation<UnitWord> wordBaseValidation() {
    return new AbstractEntityValidation() {
      @Override
      public void validate(UnitWord entity, UnitWord original, Validate validate) {
        if (entity.getWordId() == null) {
          validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "单词未设置");
        } else if (original == null) {
          final Word word = wordService.findById(entity.getWordId());
          if (word == null) {
            validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "未知单词");
          } else if (word.getWordId() != null && word.getExchanges() == null) {
            validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "这是一个派生词，请添加原型词");
          } else {
            entity.setName(word.getName());
          }
        } else if (!entity.getWordId().equals(original.getWordId())
                || !StringUtils.equals(entity.getName(), original.getName())) {
          validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "单词设置后不得修改");
        }
      }
    };
  }

  /**
   * unitBaseValidation.
   *
   * @return unitBaseValidation
   */
  @GeneralValidation(value = UnitBase.class, position = Position.FIRST)
  public Validation<UnitWord> unitBaseValidation() {
    return new AbstractEntityValidation() {
      @Override
      public void validate(UnitWord entity, UnitWord original, Validate validate) {
        if (entity.getUnitId() == null) {
          validate.addFieldError(UnitBase.FIELD_NAME_UNIT_ID, "单元未设置");
        } else if (original == null) {
          final Unit unit = unitService.findById(entity.getUnitId());
          if (unit == null) {
            validate.addFieldError(UnitBase.FIELD_NAME_UNIT_ID, "未知单元");
          } else {
            entity.setBookId(unit.getBookId());
          }
        } else if (!entity.getUnitId().equals(original.getUnitId())
                || !original.getBookId().equals(entity.getBookId())) {
          validate.addFieldError(UnitBase.FIELD_NAME_UNIT_ID, "单元设置后不得修改");
        }
      }
    };
  }
}
