package io.github.dbstarll.study.service;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.study.entity.Unit;
import io.github.dbstarll.study.service.attach.BookAttach;

@EntityService
public interface UnitService extends StudyServices<Unit>, BookAttach<Unit> {

}
