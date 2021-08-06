package io.github.dbstarll.study.utils;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.List;

import static org.apache.commons.lang3.Validate.notNull;

public class PageQuery implements Serializable {
  private static final long serialVersionUID = -4017886857226927363L;

  public static enum Order {
    asc, desc
  }

  private int offset;
  private int limit = 10;
  private String sort;
  private Order order = Order.asc;

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = notNull(order);
  }

  @Override
  public String toString() {
    return "PageQuery [offset=" + offset + ", limit=" + limit + ", sort=" + sort + ", order=" + order + "]";
  }

  /**
   * 根据Page内容来修正FindIterable.
   *
   * @param iterable FindIterable
   * @return FindIterable
   */
  public <T> FindIterable<T> apply(final FindIterable<T> iterable) {
    if (offset > 0) {
      iterable.skip(offset);
    }
    if (limit > 0) {
      iterable.limit(limit);
    }
    if (StringUtils.isNotBlank(sort)) {
      iterable.sort(Order.asc == order ? Sorts.ascending(sort) : Sorts.descending(sort));
    }
    return iterable;
  }

  /**
   * 根据Page内容来修正pipeline.
   *
   * @param pipeline pipeline
   * @return 修正后的pipeline
   */
  public List<Bson> apply(final List<Bson> pipeline) {
    if (StringUtils.isNotBlank(sort)) {
      pipeline.add(Aggregates.sort(Order.asc == order ? Sorts.ascending(sort) : Sorts.descending(sort)));
    }
    if (offset > 0) {
      pipeline.add(Aggregates.skip(offset));
    }
    if (limit > 0) {
      pipeline.add(Aggregates.limit(limit));
    }
    return pipeline;
  }
}
