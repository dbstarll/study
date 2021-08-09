package io.github.dbstarll.study.boot;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@ConditionalOnClass({ObjectMapper.class, Jackson2ObjectMapperBuilder.class})
public class JacksonConfiguration {
    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false).serializationInclusion(Include.NON_EMPTY).build();
    }
}
