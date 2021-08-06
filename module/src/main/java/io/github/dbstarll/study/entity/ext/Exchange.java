package io.github.dbstarll.study.entity.ext;

import io.github.dbstarll.dubai.model.entity.Base;
import io.github.dbstarll.study.entity.Word;

public final class Exchange implements Base {
  private static final long serialVersionUID = -7677978794602431270L;

  private Word.ExchangeKey key;
  private String word;
  private String classify;

  public Exchange() {
  }

  /**
   * 构造Exchange.
   *
   * @param key      key
   * @param word     word
   * @param classify classify
   */
  public Exchange(Word.ExchangeKey key, String word, String classify) {
    this.key = key;
    this.word = word;
    this.classify = classify;
  }

  public Word.ExchangeKey getKey() {
    return key;
  }

  public void setKey(Word.ExchangeKey key) {
    this.key = key;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public String getClassify() {
    return classify;
  }

  public void setClassify(String classify) {
    this.classify = classify;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((classify == null) ? 0 : classify.hashCode());
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((word == null) ? 0 : word.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Exchange other = (Exchange) obj;
    if (classify == null) {
      if (other.classify != null) {
        return false;
      }
    } else if (!classify.equals(other.classify)) {
      return false;
    }
    if (key != other.key) {
      return false;
    }
    if (word == null) {
      if (other.word != null) {
        return false;
      }
    } else if (!word.equals(other.word)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Exchange [key=" + key + ", word=" + word + ", classify=" + classify + "]";
  }
}
