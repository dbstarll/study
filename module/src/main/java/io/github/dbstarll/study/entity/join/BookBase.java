package io.github.dbstarll.study.entity.join;

import io.github.dbstarll.dubai.model.entity.JoinBase;
import org.bson.types.ObjectId;

public interface BookBase extends JoinBase {
  String FIELD_NAME_BOOK_ID = "bookId";

  ObjectId getBookId();

  void setBookId(ObjectId bookId);
}
