package io.github.dbstarll.study.classify.exchange;

public class PlExchangeClassifier implements ExchangeClassifier {
  @Override
  public String classify(String word, String exchange) {
    if (exchange.equals(word + "s")) {
      if (word.endsWith("y")) {
        return "ys";
      } else if (word.endsWith("o")) {
        return "os";
      } else {
        return null;
      }
    } else if (exchange.equals(word + "es")) {
      if (word.endsWith("s") || word.endsWith("sh") || word.endsWith("ch") || word.endsWith("x")) {
        return "ses";
      } else if (word.endsWith("o")) {
        return "oes";
      } else {
        return "irregular";
      }
    } else if (word.endsWith("y") && exchange.equals(word.substring(0, word.length() - 1) + "ies")) {
      return "ies";
    } else if (word.endsWith("f") && exchange.equals(word.substring(0, word.length() - 1) + "ves")) {
      return "ves";
    } else if (word.endsWith("fe") && exchange.equals(word.substring(0, word.length() - 2) + "ves")) {
      return "ves";
    } else if (exchange.equals(word)) {
      return "same";
    } else {
      return "irregular";
    }
  }
}
