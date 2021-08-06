package io.github.dbstarll.study.entity.ext;

import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.join.VoiceBase;
import org.bson.types.ObjectId;

public final class Phonetic implements VoiceBase {
  private static final long serialVersionUID = 7083794416702339433L;

  private Word.PhoneticKey key;
  private String symbol;
  private ObjectId voiceId;
  private transient byte[] mp3 = null;

  public Phonetic() {
  }

  public Phonetic(Word.PhoneticKey key, String symbol) {
    this.key = key;
    this.symbol = symbol;
  }

  public Word.PhoneticKey getKey() {
    return key;
  }

  public void setKey(Word.PhoneticKey key) {
    this.key = key;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public ObjectId getVoiceId() {
    return voiceId;
  }

  @Override
  public void setVoiceId(ObjectId voiceId) {
    this.voiceId = voiceId;
  }

  public byte[] mp3() {
    return mp3;
  }

  public Phonetic mp3(byte[] mp3) {
    this.mp3 = mp3;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
    result = prime * result + ((voiceId == null) ? 0 : voiceId.hashCode());
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
    Phonetic other = (Phonetic) obj;
    if (key != other.key) {
      return false;
    }
    if (symbol == null) {
      if (other.symbol != null) {
        return false;
      }
    } else if (!symbol.equals(other.symbol)) {
      return false;
    }
    if (voiceId == null) {
      if (other.voiceId != null) {
        return false;
      }
    } else if (!voiceId.equals(other.voiceId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Phonetic [key=" + key + ", symbol=" + symbol + ", voiceId=" + voiceId
            + (mp3 == null ? "" : (", mp3=" + mp3.length)) + "]";
  }
}
