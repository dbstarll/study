package io.github.dbstarll.study.service;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.study.entity.UnitWord;
import io.github.dbstarll.study.service.attach.BookAttach;
import io.github.dbstarll.study.service.attach.UnitAttach;
import io.github.dbstarll.study.service.attach.UnitWordServiceAttach;
import io.github.dbstarll.study.service.attach.WordAttach;

@EntityService
public interface UnitWordService extends StudyServices<UnitWord>, BookAttach<UnitWord>, UnitAttach<UnitWord>,
        WordAttach<UnitWord>, UnitWordServiceAttach {

}
