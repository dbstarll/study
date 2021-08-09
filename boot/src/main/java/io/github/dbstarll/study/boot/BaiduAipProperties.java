package io.github.dbstarll.study.boot;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.apache.commons.lang3.Validate.notNull;

@ConfigurationProperties(prefix = "study.aip.baidu")
public class BaiduAipProperties implements InitializingBean {
    private boolean enabled = true;
    private String appId;
    private String aipKey;
    private String aipToken;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAipKey() {
        return aipKey;
    }

    public void setAipKey(String aipKey) {
        this.aipKey = aipKey;
    }

    public String getAipToken() {
        return aipToken;
    }

    public void setAipToken(String aipToken) {
        this.aipToken = aipToken;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(appId, "study.baidu.aip.appId is null");
        notNull(aipKey, "study.baidu.aip.aipKey is null");
        notNull(aipToken, "study.baidu.aip.aipToken is null");
    }
}
