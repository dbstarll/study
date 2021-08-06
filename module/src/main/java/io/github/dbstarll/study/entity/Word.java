package io.github.dbstarll.study.entity;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.study.entity.ext.Exchange;
import io.github.dbstarll.study.entity.ext.Part;
import io.github.dbstarll.study.entity.ext.Phonetic;
import io.github.dbstarll.study.entity.join.WordBase;

import java.util.Set;

@Table
public interface Word extends StudyEntities, Namable, WordBase {
  enum PhoneticKey {
    en, // 英式英语
    am // 美式英语
  }

  enum ExchangeKey {
    pl, // 复数
    third, // 第三人称单数
    past, // 过去式
    done, // 过去分词
    ing, // 现在分词
    er, // 比较级
    est // 最高级
  }

  enum PartKey {
    n, // 名词
    v, // 动词
    vt, // 及物动词
    vi, // 不及物动词
    adj, // 形容词
    adv, // 副词
    pron, // 代词
    num, // 数词
    art, // 冠词
    prep, // 介词
    interj, // 叹词
    conj, // 连词
    aux, // 助动词
    na, // 不确定
    abbr, // 略语
    det, // 定冠词
    _int, // 感叹词
    link_v, phr
  }

  boolean isCri();

  void setCri(boolean cri);

  Set<Phonetic> getPhonetics();

  void setPhonetics(Set<Phonetic> phonetics);

  Set<Exchange> getExchanges();

  void setExchanges(Set<Exchange> exchanges);

  Set<Part> getParts();

  void setParts(Set<Part> parts);
}
