package io.github.dbstarll.study.boot.task;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.entity.Principal.Mode;
import io.github.dbstarll.study.entity.Subscribe;
import io.github.dbstarll.study.entity.Subscribe.SubscribeType;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.service.PrincipalService;
import io.github.dbstarll.study.service.SubscribeService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
class PrincipalUpdater implements InitializingBean {
  @Autowired
  private PrincipalService principalService;

  @Autowired
  private SubscribeService subscribeService;

  @Override
  public void afterPropertiesSet() throws Exception {
    updateEntity();
  }

  private void updateEntity() {
    int count = 0;
    int match = 0;
    int update = 0;
    for (Principal principal : principalService.find(Filters.and(Filters.ne("mode", Mode.GUEST),
            Filters.exists(Sourceable.FIELD_NAME_SOURCES + '.' + Module.ENGLISH, false)))) {
      count++;
      final Subscribe subscribe = subscribeService
              .findOne(Filters.and(subscribeService.filterByPrincipalId(principal.getId()),
                      Filters.eq("type", SubscribeType.entity), Filters.eq("module", Module.ENGLISH)));
      if (subscribe != null) {
        match++;
        Map<String, ObjectId> sources = principal.getSources();
        if (sources == null) {
          principal.setSources(sources = new HashMap<>());
        }
        sources.put(subscribe.getModule().name(), subscribe.getEntityId());
        if (null != principalService.save(principal, (Validate) null)) {
          update++;
        }
      }
    }
    System.err.println("PrincipalUpdater.updateEntity: count=" + count + ", match=" + match + ", update=" + update);
  }
}
