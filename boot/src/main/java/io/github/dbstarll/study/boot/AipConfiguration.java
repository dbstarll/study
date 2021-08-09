package io.github.dbstarll.study.boot;

import com.baidu.aip.speech.AipSpeech;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "study.aip", name = "enabled", matchIfMissing = true)
public class AipConfiguration {
    @Configuration
    @ConditionalOnClass({AipSpeech.class})
    @ConditionalOnProperty(prefix = "study.aip.baidu", name = "enabled", matchIfMissing = true)
    @EnableConfigurationProperties({BaiduAipProperties.class})
    static class BaiduAipConfiguration {
        @Bean
        @ConditionalOnMissingBean(AipSpeech.class)
        AipSpeech aipSpeech(BaiduAipProperties baiduAipProperties) {
            final AipSpeech aip = new AipSpeech(baiduAipProperties.getAppId(), baiduAipProperties.getAipKey(),
                    baiduAipProperties.getAipToken());
            aip.setConnectionTimeoutInMillis(2000);
            aip.setSocketTimeoutInMillis(2000);
            return aip;
        }
    }
}
