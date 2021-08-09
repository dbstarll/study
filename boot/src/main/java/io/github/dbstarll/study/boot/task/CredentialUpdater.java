package io.github.dbstarll.study.boot.task;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.user.entity.Credential;
import io.github.dbstarll.dubai.user.entity.enums.SourceType;
import io.github.dbstarll.dubai.user.entity.ext.CredentialDetails;
import io.github.dbstarll.dubai.user.entity.ext.MiniProgramCredentials;
import io.github.dbstarll.dubai.user.service.CredentialService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class CredentialUpdater implements InitializingBean {
    @Autowired
    private Collection<Credential> credentialCollection;
    @Autowired
    private CredentialService credentialService;

    @Override
    public void afterPropertiesSet() throws Exception {
        updateDefunctableCredential();
        updateDisabled();
        updateAppId();
    }

    private void updateDefunctableCredential() {
        final UpdateResult result = credentialCollection.original().updateMany(
                Filters.exists(Defunctable.FIELD_NAME_DEFUNCT, false), Updates.set(Defunctable.FIELD_NAME_DEFUNCT, false));
        System.err.println("CredentialUpdater:updateDefunctableCredential - update: " + result.getModifiedCount() + "/"
                + result.getMatchedCount());
    }

    private void updateDisabled() {
        final UpdateResult result = credentialCollection.original().updateMany(Filters.exists("disabled", false),
                Updates.set("disabled", false));
        System.err.println(
                "CredentialUpdater:updateDisabled - update: " + result.getModifiedCount() + "/" + result.getMatchedCount());
    }

    private void updateAppId() {
        final UpdateResult result = credentialCollection.updateMany(
                Filters.and(credentialService.filterBySource(SourceType.MiniProgram),
                        Filters.exists(CredentialDetails.FIELD_CREDENTIALS + '.' + MiniProgramCredentials.FIELD_APP_ID, false)),
                Updates.set(CredentialDetails.FIELD_CREDENTIALS + '.' + MiniProgramCredentials.FIELD_APP_ID,
                        "wx826147db82c2e9f8"));
        System.err.println("CredentialUpdater.updateAppId: matched=" + result.getMatchedCount() + ", modified="
                + result.getModifiedCount());
    }
}
