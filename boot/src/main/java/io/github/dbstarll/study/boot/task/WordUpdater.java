package io.github.dbstarll.study.boot.task;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import io.github.dbstarll.study.classify.exchange.ExchangeUtils;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.Word.ExchangeKey;
import io.github.dbstarll.study.entity.ext.Exchange;
import io.github.dbstarll.study.entity.join.WordBase;
import io.github.dbstarll.study.service.WordService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//@Component
class WordUpdater implements InitializingBean {
  @Autowired
  private WordService wordService;

  @Override
  public void afterPropertiesSet() throws Exception {
    // updateWordId();
    // checkWordId();
    // updateExchange(ExchangeKey.est);
  }

  private void updateWordId() {
    int count = 0;
    int missWord = 0;
    int missId = 0;
    for (Word word : wordService.find(Filters.exists("exchanges"))) {
      count++;
      for (Exchange exchange : word.getExchanges()) {
        if (!word.getName().equals(exchange.getWord()) && exchange.getWord().indexOf(' ') == -1) {
          final Word exchangeWord = wordService.findOne(Filters.eq(Namable.FIELD_NAME_NAME, exchange.getWord()));
          if (exchangeWord == null) {
            missWord++;
          } else if (exchangeWord.getWordId() == null) {
            missId++;
            exchangeWord.setWordId(word.getId());
            try {
              wordService.save(exchangeWord, (Validate) null);
              System.out.println(word.getName() + "\tmissId: " + exchange + "\t" + exchangeWord);
            } catch (ValidateException e) {
              System.err
                      .println(word.getName() + "\tmissId: " + exchange + "\t" + exchangeWord + "\t" + e.getMessage());
            }
          }
        }
      }
    }
    System.err.println("WordUpdater.updateWordId: count=" + count + ", missWord=" + missWord + ", missId=" + missId);
  }

  private void checkWordId() {
    int count = 0;
    int missWord = 0;
    int missExchange = 0;
    for (Word exchangeWord : wordService.find(Filters.exists(WordBase.FIELD_NAME_WORD_ID))) {
      count++;
      final Word word = wordService.findById(exchangeWord.getWordId());
      if (word != null) {
        boolean match = false;
        if (word.getExchanges() != null) {
          for (Exchange exchange : word.getExchanges()) {
            if (exchangeWord.getName().equals(exchange.getWord())) {
              match = true;
              break;
            }
          }
        }
        if (!match) {
          System.err.println(exchangeWord.getName() + "\t" + word.getExchanges());
          wordService.deleteById(exchangeWord.getId());
          missExchange++;
        }
      } else {
        missWord++;
      }
    }
    System.err.println("WordUpdater.checkWordId: count=" + count + ", miss=" + missWord + "/" + missExchange);
  }

  private void updateExchange(final ExchangeKey exchangeKey) throws IOException {
    int count = 0;
    int update = 0;
    final File file = new File("/Users/dbstar/Downloads/" + exchangeKey + ".txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      for (Word word : wordService.find(Filters.eq("exchanges.key", exchangeKey))) {
        count++;

        final Exchange exchange = findExchange(word, exchangeKey);
        final String classify = ExchangeUtils.classify(exchangeKey, word.getName(), exchange.getWord());
        writer.write(classify + "\t" + word.getName() + "\t" + exchange.getWord());
        writer.newLine();
        if (classify != null) {
          exchange.setClassify(classify);
          if (null != wordService.save(word, (Validate) null)) {
            update++;
          }
        }
      }
    }
    System.err.println("WordUpdater.updateExchange[" + exchangeKey + "]: update: " + update + "/" + count);
  }

  private Exchange findExchange(Word word, ExchangeKey exchangeKey) {
    for (Exchange exchange : word.getExchanges()) {
      if (exchange.getKey() == exchangeKey) {
        return exchange;
      }
    }
    return null;
  }
}
