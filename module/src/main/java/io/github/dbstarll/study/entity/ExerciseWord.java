package io.github.dbstarll.study.entity;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.study.entity.ext.Exchange;
import io.github.dbstarll.study.entity.ext.MasterPercent;
import io.github.dbstarll.study.entity.join.BookBase;
import io.github.dbstarll.study.entity.join.WordBase;

import java.util.Map;

@Table
public interface ExerciseWord extends StudyEntities, BookBase, WordBase, Namable {
  Map<String, MasterPercent> getMasterPercents();

  void setMasterPercents(Map<String, MasterPercent> masterPercents);

  Map<String, Exchange> getExchanges();

  void setExchanges(Map<String, Exchange> exchanges);
}
