package io.github.dbstarll.study.utils;

import com.mongodb.Block;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CountBlock<T> implements Block<T>, Consumer<T> {
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public void accept(T t) {
        count.incrementAndGet();
    }

    @Override
    public void apply(T t) {
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }
}
