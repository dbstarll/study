package io.github.dbstarll.study.entity;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.user.entity.join.PrincipalBase;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.entity.enums.Page;
import org.bson.types.ObjectId;

import java.util.Date;

@Table
public interface Subscribe extends StudyEntities, PrincipalBase {
  enum SubscribeType {
    page, entity
  }

  Module getModule();

  void setModule(Module module);

  SubscribeType getType();

  void setType(SubscribeType type);

  Page getPage();

  void setPage(Page page);

  ObjectId getEntityId();

  void setEntityId(ObjectId entityId);

  Date getStart();

  void setStart(Date start);

  Date getEnd();

  void setEnd(Date end);
}
