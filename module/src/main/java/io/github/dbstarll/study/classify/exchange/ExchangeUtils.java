package io.github.dbstarll.study.classify.exchange;

import io.github.dbstarll.study.entity.Word;

import java.util.HashMap;
import java.util.Map;

public final class ExchangeUtils {
  private static final Map<Word.ExchangeKey, ExchangeClassifier> classifiers = new HashMap<>();

  static {
    classifiers.put(Word.ExchangeKey.pl, new PlExchangeClassifier());
    classifiers.put(Word.ExchangeKey.ing, new IngExchangeClassifier());
    classifiers.put(Word.ExchangeKey.done, new DoneExchangeClassifier());
    classifiers.put(Word.ExchangeKey.third, classifiers.get(Word.ExchangeKey.pl));
    classifiers.put(Word.ExchangeKey.past, classifiers.get(Word.ExchangeKey.done));
    classifiers.put(Word.ExchangeKey.er, new ErExchangeClassifier());
    classifiers.put(Word.ExchangeKey.est, new EstExchangeClassifier());
  }

  private ExchangeUtils() {
  }

  /**
   * 获得词态变化的分类.
   *
   * @param key      ExchangeKey
   * @param word     word
   * @param exchange exchange
   * @return 词态变化的分类
   */
  public static String classify(Word.ExchangeKey key, String word, String exchange) {
    final ExchangeClassifier classifier = classifiers.get(key);
    if (classifier != null) {
      return classifier.classify(word, exchange);
    }
    return null;
  }
}
