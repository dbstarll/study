package io.github.dbstarll.study.boot.model;

import io.github.dbstarll.study.utils.PageQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SummaryWithTotal<E> implements Serializable {
    private static final long serialVersionUID = -2233435060462034790L;

    private final long total;
    private final List<E> summaries;
    private final int summarySize;

    private PageQuery query;

    private SummaryWithTotal(long total, List<E> summaries) {
        this.total = total;
        this.summaries = summaries;
        this.summarySize = summaries == null ? 0 : summaries.size();
    }

    public long getTotal() {
        return total;
    }

    public List<E> getSummaries() {
        return summaries;
    }

    public int getSummarySize() {
        return summarySize;
    }

    public PageQuery getQuery() {
        return query;
    }

    public SummaryWithTotal<E> query(PageQuery query) {
        this.query = query;
        return this;
    }

    public static <E> SummaryWithTotal<E> warp(long total, List<E> summaries) {
        return new SummaryWithTotal<>(total, summaries);
    }

    /**
     * 包装SummaryWithTotal.
     *
     * @param total    total
     * @param iterable iterable of summaries
     * @return SummaryWithTotal
     */
    public static <E> SummaryWithTotal<E> warp(long total, Iterable<E> iterable) {
        final List<E> summaries = new ArrayList<>();
        for (E e : iterable) {
            summaries.add(e);
        }
        return warp(total, summaries);
    }
}
