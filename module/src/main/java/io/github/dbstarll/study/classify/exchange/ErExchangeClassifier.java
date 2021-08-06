package io.github.dbstarll.study.classify.exchange;

public class ErExchangeClassifier implements ExchangeClassifier {
  @Override
  public String classify(String word, String exchange) {
    if (exchange.equals(word + "er")) {
      return null;
    } else if (exchange.equals("more " + word)) {
      return null;
    } else if (word.endsWith("y") && exchange.equals(word.substring(0, word.length() - 1) + "ier")) {
      return "yer";
    } else if (word.endsWith("e") && exchange.equals(word + "r")) {
      return "eer";
    } else if (exchange.equals(word + word.substring(word.length() - 1, word.length()) + "er")) {
      return "2er";
    } else if (exchange.equals(word)) {
      return "same";
    } else {
      return "irregular";
    }
  }
}
