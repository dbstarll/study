package io.github.dbstarll.study.service.attach;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.study.entity.StudyEntities;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.impl.WordAttachImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(WordAttachImplemental.class)
public interface WordAttach<E extends StudyEntities & WordBase> extends StudyAttachs {
  Bson filterByWordId(ObjectId wordId);

  long countByWordId(ObjectId wordId);

  FindIterable<E> findByWordId(ObjectId wordId);

  DistinctIterable<ObjectId> distinctWordId(Bson filter);
}
