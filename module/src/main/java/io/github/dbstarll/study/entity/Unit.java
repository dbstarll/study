package io.github.dbstarll.study.entity;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.study.entity.join.BookBase;

@Table
public interface Unit extends StudyEntities, BookBase {
  String getSn();

  void setSn(String sn);

  String getTitle();

  void setTitle(String title);

  int getWordCount();

  void setWordCount(int wordCount);
}
