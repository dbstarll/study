package io.github.dbstarll.study.service.impl;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.study.entity.StudyEntities;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.service.StudyServices;
import io.github.dbstarll.study.service.attach.BookAttach;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public final class BookAttachImplemental<E extends StudyEntities & BookBase, S extends StudyServices<E>>
        extends StudyImplementals<E, S> implements BookAttach<E> {
  public BookAttachImplemental(S service, Collection<E> collection) {
    super(service, collection);
  }

  @Override
  public Bson filterByBookId(ObjectId bookId) {
    return eq(BookBase.FIELD_NAME_BOOK_ID, bookId);
  }

  @Override
  public long countByBookId(ObjectId bookId) {
    return service.count(filterByBookId(bookId));
  }

  @Override
  public FindIterable<E> findByBookId(ObjectId bookId) {
    return service.find(filterByBookId(bookId));
  }
}
