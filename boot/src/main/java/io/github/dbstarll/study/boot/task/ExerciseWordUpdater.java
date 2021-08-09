package io.github.dbstarll.study.boot.task;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.Exercise;
import io.github.dbstarll.study.entity.Exercise.ExerciseKey;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.ext.Exchange;
import io.github.dbstarll.study.entity.ext.MasterPercent;
import io.github.dbstarll.study.service.ExerciseService;
import io.github.dbstarll.study.service.ExerciseWordService;
import io.github.dbstarll.study.service.WordService;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
class ExerciseWordUpdater implements InitializingBean {
  @Autowired
  private WordService wordService;
  @Autowired
  private ExerciseService exerciseService;
  @Autowired
  private ExerciseWordService exerciseWordService;

  @Override
  public void afterPropertiesSet() throws Exception {
    updateBingoAndNext();
    // updateExchange();
  }

  private void updateBingoAndNext() {
    final List<Bson> filters = new LinkedList<>();
    for (ExerciseKey key : ExerciseKey.values()) {
      filters.add(Filters.and(Filters.exists("masterPercents." + key + ".total", true),
              Filters.exists("masterPercents." + key + ".bingo", false)));
    }
    int count = 0;
    int update = 0;
    for (ExerciseWord word : exerciseWordService.find(Filters.or(filters))) {
      count++;
      for (Entry<String, MasterPercent> entry : word.getMasterPercents().entrySet()) {
        updateMasterPercent(word, ExerciseKey.valueOf(entry.getKey()), entry.getValue());
      }
      if (null != exerciseWordService.save(word, (Validate) null)) {
        update++;
      }
    }
    System.err.println("ExerciseWordUpdater.updateBingoAndNext: update: " + update + "/" + count);
  }

  private void updateMasterPercent(final ExerciseWord word, final ExerciseKey key, final MasterPercent percent) {
    updateBingo(word, key, percent);
    percent.setNext(StudyUtils.getNextExerciseTime(percent));
  }

  private void updateBingo(final ExerciseWord word, final ExerciseKey key, final MasterPercent percent) {
    if (percent.getCorrect() == 0) {
      percent.setBingo(0);
    } else if (percent.getCorrect() == percent.getTotal()) {
      percent.setBingo(percent.getCorrect());
    } else {
      int bingo = 0;
      for (Exercise exercise : exerciseService
              .find(Filters.and(exerciseService.filterByBookId(word.getBookId()),
                      exerciseService.filterByWordId(word.getWordId()), Filters.eq("exerciseKey", key)))
              .sort(Sorts.descending("dateCreated"))) {
        if (exercise.isCorrect()) {
          bingo++;
        } else {
          break;
        }
      }
      percent.setBingo(bingo);
    }
  }

  private void updateExchange() {
    final List<Bson> filters = new LinkedList<>();
    filters.add(Filters.and(Filters.exists("exchanges")));

    int count = 0;
    int update = 0;
    for (ExerciseWord exerciseWord : exerciseWordService.find(Filters.or(filters))) {
      exerciseWord.setExchanges(exchanges(exerciseWord.getWordId()));
      count++;
      if (null != exerciseWordService.save(exerciseWord, (Validate) null)) {
        update++;
      }
    }
    System.err.println("ExerciseWordUpdater.updateExchange: update: " + update + "/" + count);
  }

  private Map<String, Exchange> exchanges(final ObjectId wordId) {
    final Word word = wordService.findById(wordId);
    if (word != null && word.getExchanges() != null && !word.getExchanges().isEmpty()) {
      final Map<String, Exchange> exchanges = new HashMap<>();
      for (Exchange exchange : word.getExchanges()) {
        if (exchange.getWord().indexOf(' ') < 0) {
          exchanges.put(exchange.getKey().name(), new Exchange(null, exchange.getWord(), exchange.getClassify()));
        }
      }
      return exchanges;
    }
    return null;
  }
}
