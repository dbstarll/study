package io.github.dbstarll.study.service;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.attach.SourceAttach;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.service.attach.PrincipalServiceAttach;

@EntityService
public interface PrincipalService extends StudyServices<Principal>, SourceAttach<Principal>, PrincipalServiceAttach {

}
