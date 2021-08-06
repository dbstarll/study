package io.github.dbstarll.study.entity;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.info.Namable;

@Table
public interface ExerciseBook extends StudyEntities, Namable {
  int getWordCount();

  void setWordCount(int wordCount);
}
