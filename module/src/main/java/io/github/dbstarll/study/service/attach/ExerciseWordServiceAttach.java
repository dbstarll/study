package io.github.dbstarll.study.service.attach;

import com.mongodb.client.AggregateIterable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.ServiceSaver;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.service.impl.ExerciseWordServiceImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.regex.Pattern;

@Implementation(ExerciseWordServiceImplemental.class)
public interface ExerciseWordServiceAttach extends StudyAttachs, ServiceSaver<ExerciseWord> {
  AggregateIterable<ExerciseWord> sample(Bson filter, int num);

  Bson filterByInterfere(ObjectId bookId, ExerciseWord exerciseWord, Pattern pattern);
}
