package io.github.dbstarll.study.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * 测试DictionaryApi
 */
public class DictionaryApiTest {
    private DictionaryApi api;

    @BeforeEach
    void setUp() {
        this.api = new DictionaryApi("7A39163AF82AEEC16FD0CC5F5BDFBE16", new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        this.api = null;
    }

    @Test
    void query() throws IOException {
        System.out.println(api.query("jump"));
    }

    @Test
    void query1() throws IOException {
        System.out.println(api.query("fly"));
    }

    @Test
    void query2() throws IOException {
        System.out.println(api.query("忆苦思甜"));
    }
}