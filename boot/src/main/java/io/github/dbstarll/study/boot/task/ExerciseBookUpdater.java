package io.github.dbstarll.study.boot.task;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.study.entity.Exercise;
import io.github.dbstarll.study.entity.ExerciseWord;
import io.github.dbstarll.study.entity.join.BookBase;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import static com.mongodb.client.model.Filters.eq;

//@Component
class ExerciseBookUpdater implements InitializingBean {
  @Autowired
  private Collection<Exercise> exerciseCollection;
  @Autowired
  private Collection<ExerciseWord> exerciseWordCollection;

  @Override
  public void afterPropertiesSet() throws Exception {
    clearBook(new ObjectId("5c1349fac0ad810254687ca1"));
  }

  private void clearBook(final ObjectId bookId) {
    final Bson filter = eq(BookBase.FIELD_NAME_BOOK_ID, bookId);
    System.err.println("delete exercise: " + exerciseCollection.deleteMany(filter).getDeletedCount());
    System.err.println("delete exerciseWord: " + exerciseWordCollection.deleteMany(filter).getDeletedCount());
  }
}
