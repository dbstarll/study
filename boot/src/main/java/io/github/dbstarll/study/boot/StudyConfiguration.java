package io.github.dbstarll.study.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dbstarll.study.boot.api.WeixinApi;
import io.github.dbstarll.study.utils.DictionaryApi;
import io.github.dbstarll.utils.http.client.HttpClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class StudyConfiguration {
    @Bean
    @ConditionalOnMissingBean(DictionaryApi.class)
    DictionaryApi dictionaryApi(@Value("${study.dictionary.api.key}") String key, ObjectMapper objectMapper) {
        return new DictionaryApi(key, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(HttpClient.class)
    HttpClient httpClient() {
        return new HttpClientFactory().build();
    }

    @Bean
    @ConditionalOnMissingBean(WeixinApi.class)
    WeixinApi weixinApi(HttpClient httpClient, ObjectMapper objectMapper,
                        @Value("${study.weixin.mini.secrets}") String secrets) {
        return new WeixinApi(httpClient, objectMapper, parseSecrets(secrets));
    }

    private Map<String, String> parseSecrets(String str) {
        final Map<String, String> secrets = new HashMap<>();
        for (String sp : StringUtils.split(str, ',')) {
            final int index = sp.indexOf('=');
            if (index > 0) {
                final String appId = sp.substring(0, index);
                final String secret = sp.substring(index + 1);
                if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(secret)) {
                    secrets.put(appId, secret);
                }
            }
        }
        return secrets.isEmpty() ? null : secrets;
    }
}
