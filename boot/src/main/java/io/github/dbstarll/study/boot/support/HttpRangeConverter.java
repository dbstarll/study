package io.github.dbstarll.study.boot.support;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpRangeConverter implements Converter<String, List<HttpRange>> {
    @Override
    public List<HttpRange> convert(String ranges) {
        return HttpRange.parseRanges(ranges);
    }
}