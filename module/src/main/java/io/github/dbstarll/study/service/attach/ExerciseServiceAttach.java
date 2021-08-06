package io.github.dbstarll.study.service.attach;

import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.study.entity.Exercise;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.service.impl.ExerciseServiceImplemental;
import org.bson.types.ObjectId;

import java.util.Map.Entry;

@Implementation(ExerciseServiceImplemental.class)
public interface ExerciseServiceAttach extends StudyAttachs {
  Iterable<Entry<String, Integer>> countErrors(ObjectId bookId, ObjectId wordId, Exercise.ExerciseKey exerciseKey,
                                               Word.ExchangeKey exchangeKey);
}
