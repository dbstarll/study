package io.github.dbstarll.study.service;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.service.attach.WordAttach;
import io.github.dbstarll.study.service.attach.WordServiceAttach;

@EntityService
public interface WordService extends StudyServices<Word>, WordAttach<Word>, WordServiceAttach {

}
