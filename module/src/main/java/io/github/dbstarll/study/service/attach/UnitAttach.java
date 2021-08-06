package io.github.dbstarll.study.service.attach;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.study.entity.StudyEntities;
import io.github.dbstarll.study.entity.join.UnitBase;
import io.github.dbstarll.study.service.impl.UnitAttachImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(UnitAttachImplemental.class)
public interface UnitAttach<E extends StudyEntities & UnitBase> extends StudyAttachs {
  Bson filterByUnitId(ObjectId unitId);

  long countByUnitId(ObjectId unitId);

  FindIterable<E> findByUnitId(ObjectId unitId);
}
