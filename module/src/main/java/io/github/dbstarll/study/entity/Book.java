package io.github.dbstarll.study.entity;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.study.entity.enums.CefrLevel;
import io.github.dbstarll.study.entity.enums.SchoolLevel;
import io.github.dbstarll.study.entity.enums.Term;

@Table
public interface Book extends StudyEntities, Namable {
  CefrLevel getCefr();

  void setCefr(CefrLevel cefr);

  SchoolLevel getSchool();

  void setSchool(SchoolLevel school);

  int getGrade();

  void setGrade(int grade);

  Term getTerm();

  void setTerm(Term term);

  String getPrefix();

  void setPrefix(String prefix);

  int getUnitCount();

  void setUnitCount(int unitCount);

  int getWordCount();

  void setWordCount(int wordCount);
}
