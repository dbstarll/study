package io.github.dbstarll.study.entity.join;

import io.github.dbstarll.dubai.model.entity.JoinBase;
import org.bson.types.ObjectId;

public interface WordBase extends JoinBase {
  String FIELD_NAME_WORD_ID = "wordId";

  ObjectId getWordId();

  void setWordId(ObjectId wordId);
}
