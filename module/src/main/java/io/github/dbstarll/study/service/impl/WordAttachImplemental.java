package io.github.dbstarll.study.service.impl;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.study.entity.StudyEntities;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.StudyServices;
import io.github.dbstarll.study.service.attach.WordAttach;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public final class WordAttachImplemental<E extends StudyEntities & WordBase, S extends StudyServices<E>>
        extends StudyImplementals<E, S> implements WordAttach<E> {
  public WordAttachImplemental(S service, Collection<E> collection) {
    super(service, collection);
  }

  @Override
  public Bson filterByWordId(ObjectId wordId) {
    return eq(WordBase.FIELD_NAME_WORD_ID, wordId);
  }

  @Override
  public long countByWordId(ObjectId wordId) {
    return service.count(filterByWordId(wordId));
  }

  @Override
  public FindIterable<E> findByWordId(ObjectId wordId) {
    return service.find(filterByWordId(wordId));
  }

  @Override
  public DistinctIterable<ObjectId> distinctWordId(Bson filter) {
    return getCollection().distinct(WordBase.FIELD_NAME_WORD_ID, filter, ObjectId.class);
  }
}
