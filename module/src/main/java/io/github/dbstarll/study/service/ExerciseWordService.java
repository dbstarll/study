package io.github.dbstarll.study.service;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.service.attach.BookAttach;
import io.github.dbstarll.study.service.attach.ExerciseWordServiceAttach;
import io.github.dbstarll.study.service.attach.WordAttach;

@EntityService
public interface ExerciseWordService
        extends StudyServices<ExerciseWord>, BookAttach<ExerciseWord>, WordAttach<ExerciseWord>, ExerciseWordServiceAttach {

}
