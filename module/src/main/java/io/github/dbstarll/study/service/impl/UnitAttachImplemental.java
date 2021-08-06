package io.github.dbstarll.study.service.impl;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.study.entity.StudyEntities;
import io.github.dbstarll.study.entity.join.UnitBase;
import io.github.dbstarll.study.service.StudyServices;
import io.github.dbstarll.study.service.attach.UnitAttach;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public final class UnitAttachImplemental<E extends StudyEntities & UnitBase, S extends StudyServices<E>>
        extends StudyImplementals<E, S> implements UnitAttach<E> {
  public UnitAttachImplemental(S service, Collection<E> collection) {
    super(service, collection);
  }

  @Override
  public Bson filterByUnitId(ObjectId unitId) {
    return eq(UnitBase.FIELD_NAME_UNIT_ID, unitId);
  }

  @Override
  public long countByUnitId(ObjectId unitId) {
    return service.count(filterByUnitId(unitId));
  }

  @Override
  public FindIterable<E> findByUnitId(ObjectId unitId) {
    return service.find(filterByUnitId(unitId));
  }
}
