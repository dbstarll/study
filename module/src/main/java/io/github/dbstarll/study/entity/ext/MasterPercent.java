package io.github.dbstarll.study.entity.ext;

import io.github.dbstarll.dubai.model.entity.Base;

import java.util.Date;

public final class MasterPercent implements Base {
  private static final long serialVersionUID = -5871261000987014190L;

  private float percent;
  private int total;
  private int correct;
  private int bingo;
  private Date last;
  private Date next;

  public float getPercent() {
    return percent;
  }

  public void setPercent(float percent) {
    this.percent = percent;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getCorrect() {
    return correct;
  }

  public void setCorrect(int correct) {
    this.correct = correct;
  }

  public int getBingo() {
    return bingo;
  }

  public void setBingo(int bingo) {
    this.bingo = bingo;
  }

  public Date getLast() {
    return last;
  }

  public void setLast(Date last) {
    this.last = last;
  }

  public Date getNext() {
    return next;
  }

  public void setNext(Date next) {
    this.next = next;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + bingo;
    result = prime * result + correct;
    result = prime * result + ((last == null) ? 0 : last.hashCode());
    result = prime * result + ((next == null) ? 0 : next.hashCode());
    result = prime * result + Float.floatToIntBits(percent);
    result = prime * result + total;
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
    MasterPercent other = (MasterPercent) obj;
    if (bingo != other.bingo) {
      return false;
    }
    if (correct != other.correct) {
      return false;
    }
    if (last == null) {
      if (other.last != null) {
        return false;
      }
    } else if (!last.equals(other.last)) {
      return false;
    }
    if (next == null) {
      if (other.next != null) {
        return false;
      }
    } else if (!next.equals(other.next)) {
      return false;
    }
    if (Float.floatToIntBits(percent) != Float.floatToIntBits(other.percent)) {
      return false;
    }
    if (total != other.total) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "MasterPercent [percent=" + percent + ", correct=" + bingo + "/" + correct + "/" + total + ", last=" + last
            + ", next=" + next + "]";
  }
}
