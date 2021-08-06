package io.github.dbstarll.study.classify.exchange;

public class EstExchangeClassifier implements ExchangeClassifier {
  @Override
  public String classify(String word, String exchange) {
    if (exchange.equals(word + "est")) {
      return null;
    } else if (exchange.equals("most " + word)) {
      return null;
    } else if (word.endsWith("y") && exchange.equals(word.substring(0, word.length() - 1) + "iest")) {
      return "yest";
    } else if (word.endsWith("e") && exchange.equals(word + "st")) {
      return "eest";
    } else if (exchange.equals(word + word.substring(word.length() - 1, word.length()) + "est")) {
      return "2est";
    } else if (exchange.equals(word)) {
      return "same";
    } else {
      return "irregular";
    }
  }
}
