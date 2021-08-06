package io.github.dbstarll.study.entity.ext;

import io.github.dbstarll.dubai.model.entity.Base;
import io.github.dbstarll.study.entity.Word;

import java.util.List;

public final class Part implements Base {
  private static final long serialVersionUID = -1381475250841967402L;

  private List<Word.PartKey> key;
  private List<String> means;

  public Part() {
  }

  public Part(List<Word.PartKey> key, List<String> means) {
    this.key = key;
    this.means = means;
  }

  public List<Word.PartKey> getKey() {
    return key;
  }

  public void setKey(List<Word.PartKey> key) {
    this.key = key;
  }

  public List<String> getMeans() {
    return means;
  }

  public void setMeans(List<String> means) {
    this.means = means;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((means == null) ? 0 : means.hashCode());
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
    Part other = (Part) obj;
    if (key == null) {
      if (other.key != null) {
        return false;
      }
    } else if (!key.equals(other.key)) {
      return false;
    }
    if (means == null) {
      if (other.means != null) {
        return false;
      }
    } else if (!means.equals(other.means)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Part [key=" + key + ", means=" + means + "]";
  }
}
