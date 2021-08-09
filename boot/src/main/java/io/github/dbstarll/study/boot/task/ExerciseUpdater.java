package io.github.dbstarll.study.boot.task;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.*;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.study.entity.Exercise;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.ExerciseService;
import io.github.dbstarll.study.service.WordService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Component
class ExerciseUpdater implements InitializingBean {
  @Autowired
  private ExerciseService exerciseService;
  @Autowired
  private Collection<Exercise> exerciseCollection;
  @Autowired
  private WordService wordService;

  @Override
  public void afterPropertiesSet() throws Exception {
    updateNoName();
    updateBingoAndLast();
  }

  private void updateNoName() {
    for (Exercise exercise : exerciseService.find(Filters.exists(Namable.FIELD_NAME_NAME, false))) {
      final Word word = wordService.findById(exercise.getWordId());
      if (word != null) {
        exerciseCollection.updateById(exercise.getId(), Updates.set(Namable.FIELD_NAME_NAME, word.getName()));
      }
    }
  }

  private void updateBingoAndLast() {
    final Bson matchBingo = Aggregates.match(Filters.exists("bingo", false));
    final Bson idExpression = new BasicDBObject(BookBase.FIELD_NAME_BOOK_ID, "$" + BookBase.FIELD_NAME_BOOK_ID)
            .append(WordBase.FIELD_NAME_WORD_ID, "$" + WordBase.FIELD_NAME_WORD_ID).append("exerciseKey", "$exerciseKey");
    final Bson group = Aggregates.group(idExpression, Accumulators.sum("count", 1));

    int exerciseCount = 0;
    int update = 0;
    for (Document doc : exerciseCollection.aggregate(Arrays.asList(matchBingo, group), Document.class)) {
      exerciseCount++;
      final Document id = doc.get(Entity.FIELD_NAME_ID, Document.class);
      System.err.println(id);
      int bingo = 0;
      Date last = null;
      for (Exercise exercise : exerciseService.find(id).sort(Sorts.ascending(Entity.FIELD_NAME_ID))) {
        final List<Bson> updates = new LinkedList<>();
        updates.add(Updates.set("bingo", bingo));
        if (last != null) {
          updates.add(Updates.set("last", last));
        }
        exerciseCollection.updateById(exercise.getId(), Updates.combine(updates));
        update++;

        if (exercise.isCorrect()) {
          bingo++;
        } else {
          bingo = 0;
        }
        last = exercise.getDateCreated();
      }
    }
    System.err.println("ExerciseUpdater.updateBingoAndLast: exerciseCount: " + exerciseCount + ", update: " + update);
  }
}
