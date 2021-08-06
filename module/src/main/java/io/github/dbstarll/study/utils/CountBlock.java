package io.github.dbstarll.study.utils;

import com.mongodb.Block;

import java.util.concurrent.atomic.AtomicInteger;

public class CountBlock<T> implements Block<T> {
  private final AtomicInteger count = new AtomicInteger();

  @Override
  public void apply(T t) {
    count.incrementAndGet();
  }

  public int getCount() {
    return count.get();
  }
}
