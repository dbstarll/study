package io.github.dbstarll.study.service.attach;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.study.entity.StudyEntities;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.service.impl.BookAttachImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(BookAttachImplemental.class)
public interface BookAttach<E extends StudyEntities & BookBase> extends StudyAttachs {
  Bson filterByBookId(ObjectId bookId);

  long countByBookId(ObjectId bookId);

  FindIterable<E> findByBookId(ObjectId bookId);
}
