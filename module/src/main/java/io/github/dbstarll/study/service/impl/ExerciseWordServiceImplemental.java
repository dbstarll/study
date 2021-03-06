package io.github.dbstarll.study.service.impl;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation.Position;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import io.github.dbstarll.study.entity.ExerciseBook;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.ExerciseBookService;
import io.github.dbstarll.study.service.ExerciseWordService;
import io.github.dbstarll.study.service.WordService;
import io.github.dbstarll.study.service.attach.ExerciseWordServiceAttach;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.Validate.notNull;

public final class ExerciseWordServiceImplemental extends StudyImplementals<ExerciseWord, ExerciseWordService>
        implements ExerciseWordServiceAttach {
  private WordService wordService;
  private ExerciseBookService exerciseBookService;

  public ExerciseWordServiceImplemental(ExerciseWordService service, Collection<ExerciseWord> collection) {
    super(service, collection);
  }

  public void setWordService(WordService wordService) {
    this.wordService = wordService;
  }

  public void setExerciseBookService(ExerciseBookService exerciseBookService) {
    this.exerciseBookService = exerciseBookService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    notNull(wordService, "wordService is null");
    notNull(exerciseBookService, "exerciseBookService is null");
  }

  @Override
  public AggregateIterable<ExerciseWord> sample(final Bson filter, final int num) {
    final List<Bson> pipeline = new LinkedList<>();
    if (filter != null) {
      pipeline.add(Aggregates.match(filter));
    }
    pipeline.add(Aggregates.sample(num));
    return getCollection().aggregate(pipeline);
  }

  @Override
  public Bson filterByInterfere(final ObjectId bookId, final ExerciseWord exerciseWord, final Pattern pattern) {
    final List<Bson> filters = new ArrayList<>();
    filters.add(service.filterByBookId(notNull(bookId)));
    filters.add(Filters.nin(Entity.FIELD_NAME_ID, notNull(exerciseWord).getId()));
    if (pattern != null) {
      filters.add(Filters.regex(Namable.FIELD_NAME_NAME, pattern));
    }
    return Filters.and(filters);
  }

  @Override
  public ExerciseWord save(ExerciseWord entity, ObjectId newEntityId, Validate validate) {
    return validateAndSave(entity, newEntityId, validate, new BookBaseValidation(), new DuplicateValidation());
  }

  private class BookBaseValidation extends AbstractBaseEntityValidation<BookBase> {
    public BookBaseValidation() {
      super(BookBase.class);
    }

    @Override
    protected void validate(BookBase entity, BookBase original, Validate validate) {
      if (entity.getBookId() == null) {
        validate.addFieldError(BookBase.FIELD_NAME_BOOK_ID, "??????????????????");
      } else if (original == null) {
        final ExerciseBook book = exerciseBookService.findById(entity.getBookId());
        if (book == null) {
          validate.addFieldError(BookBase.FIELD_NAME_BOOK_ID, "???????????????");
        }
      } else if (!entity.getBookId().equals(original.getBookId())) {
        validate.addFieldError(BookBase.FIELD_NAME_BOOK_ID, "??????????????????????????????");
      }
    }
  }

  private class DuplicateValidation extends AbstractEntityValidation {
    @Override
    public void validate(ExerciseWord entity, ExerciseWord original, Validate validate) {
      if (original == null && !validate.hasErrors()) {
        final Bson filter = Filters.and(service.filterByBookId(entity.getBookId()),
                service.filterByWordId(entity.getWordId()));
        if (service.count(filter) > 0) {
          validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "???????????????");
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
  public Validation<ExerciseWord> wordBaseValidation() {
    return new AbstractEntityValidation() {
      @Override
      public void validate(ExerciseWord entity, ExerciseWord original, Validate validate) {
        if (entity.getWordId() == null) {
          validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "???????????????");
        } else if (original == null) {
          final Word word = wordService.findById(entity.getWordId());
          if (word == null) {
            validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "????????????");
          } else if (!word.isCri() || (word.getWordId() != null && word.getExchanges() == null)) {
            validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "??????????????????????????????????????????");
          } else {
            entity.setName(word.getName());
          }
        } else if (!entity.getWordId().equals(original.getWordId())
                || !StringUtils.equals(entity.getName(), original.getName())) {
          validate.addFieldError(WordBase.FIELD_NAME_WORD_ID, "???????????????????????????");
        }
      }
    };
  }
}
