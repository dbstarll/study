package io.github.dbstarll.study.entity;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;

import java.util.Map;

@Table
public interface Principal extends StudyEntities, Sourceable {
  enum Mode {
    ADMIN, USER, GUEST
  }

  Mode getMode();

  void setMode(Mode mode);

  Map<String, Object> getUserInfo();

  void setUserInfo(Map<String, Object> userInfo);
}
