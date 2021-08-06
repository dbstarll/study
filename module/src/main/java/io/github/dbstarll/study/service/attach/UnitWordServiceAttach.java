package io.github.dbstarll.study.service.attach;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.ServiceSaver;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.entity.UnitWord;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.service.impl.UnitWordServiceImplemental;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;

@Implementation(UnitWordServiceImplemental.class)
public interface UnitWordServiceAttach extends StudyAttachs, ServiceSaver<UnitWord> {
  Iterable<UnitWordWithExercise> findWithExercise(Bson filter, ObjectId exerciseBookId, PageQuery query);

  @Table
  interface UnitWordWithExercise extends UnitWord {
    List<ExerciseWord> getExercises();

    void setExercises(List<ExerciseWord> exercises);

    ExerciseWord getExercise();

    void setExercise(ExerciseWord exercise);

    List<Word> getWords();

    void setWords(List<Word> words);

    Word getWord();

    void setWord(Word word);
  }
}
