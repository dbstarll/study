package io.github.dbstarll.study.service.impl;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.AbstractImplemental;
import io.github.dbstarll.study.entity.StudyEntities;
import io.github.dbstarll.study.service.StudyServices;

public abstract class StudyImplementals<E extends StudyEntities, S extends StudyServices<E>>
        extends AbstractImplemental<E, S> {
  public StudyImplementals(S service, Collection<E> collection) {
    super(service, collection);
  }
}
