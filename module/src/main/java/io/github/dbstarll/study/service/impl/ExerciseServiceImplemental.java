package io.github.dbstarll.study.service.impl;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Describable;
import io.github.dbstarll.study.entity.Exercise;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.service.ExerciseService;
import io.github.dbstarll.study.service.attach.ExerciseServiceAttach;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import io.github.dbstarll.utils.lang.wrapper.IterableWrapper;
import io.github.dbstarll.utils.lang.wrapper.IteratorWrapper;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Map.Entry;

public final class ExerciseServiceImplemental extends StudyImplementals<Exercise, ExerciseService>
        implements ExerciseServiceAttach {
  public ExerciseServiceImplemental(ExerciseService service, Collection<Exercise> collection) {
    super(service, collection);
  }

  @Override
  public Iterable<Entry<String, Integer>> countErrors(ObjectId bookId, ObjectId wordId, Exercise.ExerciseKey exerciseKey,
                                                      Word.ExchangeKey exchangeKey) {
    final Bson match = Aggregates.match(Filters.and(service.filterByBookId(bookId), service.filterByWordId(wordId),
            Filters.eq("exerciseKey", exerciseKey), Filters.eq("correct", false),
            exchangeKey == null ? Filters.exists("exchangeKey", false) : Filters.eq("exchangeKey", exchangeKey)));
    final Bson group = Aggregates.group("$" + Describable.FIELD_NAME_DESCRIPTION, Accumulators.sum("count", 1));
    final Bson sort = Aggregates.sort(Sorts.descending("count"));

    return IterableWrapper.wrap(new IteratorWrapper<Document, Entry<String, Integer>>(
            getCollection().aggregate(Arrays.asList(match, group, sort), Document.class).iterator()) {
      @Override
      protected Entry<String, Integer> next(Document entity) {
        return EntryWrapper.wrap(entity.getString(Entity.FIELD_NAME_ID), entity.getInteger("count"));
      }
    });
  }
}
