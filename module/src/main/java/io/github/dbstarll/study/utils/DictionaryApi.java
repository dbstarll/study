package io.github.dbstarll.study.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.study.classify.exchange.ExchangeUtils;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.ext.Exchange;
import io.github.dbstarll.study.entity.ext.Part;
import io.github.dbstarll.study.entity.ext.Phonetic;
import io.github.dbstarll.utils.lang.StandardCharsets;
import io.github.dbstarll.utils.lang.wrapper.IterableWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class DictionaryApi {
  private static final String BASE_URL = "http://dict-co.iciba.com/api/dictionary.php?type=json";

  private final String queryUrl;
  private final ObjectMapper objectMapper;

  public DictionaryApi(String key, ObjectMapper objectMapper) {
    this.queryUrl = BASE_URL + "&key=" + notBlank(key);
    this.objectMapper = notNull(objectMapper);
  }

  /**
   * 查询网络词典并组装成Word对象.
   *
   * @param word 待查的单词
   * @return 单词对应的Word对象，如果没有匹配的释义，则返回null
   * @throws IOException 网络异常
   */
  public Word query(String word) throws IOException {
    if (StringUtils.containsWhitespace(notBlank(word))) {
      throw new IllegalArgumentException("word contains whitespace: " + word);
    }
    final URL url = new URL(queryUrl + "&w=" + word.toLowerCase());
    final String json = IOUtils.toString(url, StandardCharsets.UTF_8);
    return parseWord(objectMapper.readTree(json));
  }

  private static Word parseWord(final JsonNode node) throws IOException {
    final JsonNode cri = node.get("is_CRI");
    if (cri != null) {
      final Word word = EntityFactory.newInstance(Word.class);
      word.setCri(cri.asInt() == 1);
      word.setName(node.get("word_name").asText().trim());
      word.setExchanges(parseExchanges(word.getName(), node.get("exchange")));
      final JsonNode symbols = node.get("symbols");
      if (symbols != null && symbols.isArray() && symbols.size() > 0) {
        final JsonNode symbol = symbols.get(0);
        word.setPhonetics(parsePhonetics(symbol));
        word.setParts(parseParts(symbol.get("parts")));
      }
      return word;
    }
    return null;
  }

  private static Set<Exchange> parseExchanges(final String word, final JsonNode node) {
    final Set<Exchange> exchanges = new HashSet<>();
    if (node != null && !node.isNull()) {
      for (Map.Entry<String, JsonNode> entry : IterableWrapper.wrap(node.fields())) {
        final String key = entry.getKey();
        final JsonNode exchangeNode = entry.getValue();
        if (key.startsWith("word_") && exchangeNode.isArray() && exchangeNode.size() > 0) {
          final Word.ExchangeKey exchangeKey;
          try {
            exchangeKey = Word.ExchangeKey.valueOf(key.substring(5));
          } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown ExchangeKey:" + key, ex);
          }

          final String exchangeValue = exchangeNode.get(0).asText().trim();
          exchanges
                  .add(new Exchange(exchangeKey, exchangeValue, ExchangeUtils.classify(exchangeKey, word, exchangeValue)));
        }
      }
    }
    return exchanges.isEmpty() ? null : exchanges;
  }

  private static Set<Phonetic> parsePhonetics(final JsonNode node) throws IOException {
    final Set<Phonetic> phonetics = new HashSet<>();
    if (node != null && !node.isNull()) {
      for (Word.PhoneticKey key : Word.PhoneticKey.values()) {
        final JsonNode symbolNode = node.get("ph_" + key.name());
        if (symbolNode != null && !symbolNode.isNull()) {
          final String symbol = symbolNode.asText().trim();
          if (StringUtils.isNotBlank(symbol)) {
            final Phonetic phonetic = new Phonetic(key, symbol);
            final JsonNode urlNode = node.get("ph_" + key.name() + "_mp3");
            if (urlNode != null && !urlNode.isNull()) {
              final String url = urlNode.asText();
              if (StringUtils.isNotBlank(url)) {
                phonetic.mp3(IOUtils.toByteArray(new URL(url)));
              }
            }
            phonetics.add(phonetic);
          }
        }
      }
    }
    return phonetics.isEmpty() ? null : phonetics;
  }

  private static Set<Part> parseParts(final JsonNode node) {
    final Set<Part> parts = new HashSet<>();
    if (node != null && node.isArray()) {
      for (JsonNode partNode : node) {
        final String key = partNode.get("part").asText();
        final JsonNode meansNode = partNode.get("means");
        if (key.endsWith(".") && meansNode != null && meansNode.isArray() && meansNode.size() > 0) {
          final List<Word.PartKey> partKey = parsePartKey(key);
          final List<String> means = new ArrayList<>(meansNode.size());
          for (JsonNode mean : meansNode) {
            means.add(mean.asText().trim());
          }
          parts.add(new Part(partKey, means));
        }
      }
    }
    return parts.isEmpty() ? null : parts;
  }

  private static List<Word.PartKey> parsePartKey(final String key) {
    final List<Word.PartKey> keys = new ArrayList<>();
    for (String k : StringUtils.split(key.replaceAll("\\.| ", "").replace('-', '_'), '&')) {
      try {
        keys.add(Word.PartKey.valueOf(k));
      } catch (IllegalArgumentException ex) {
        try {
          keys.add(Word.PartKey.valueOf('_' + k));
        } catch (IllegalArgumentException ex2) {
          throw new IllegalArgumentException("Unknown PartKey:" + key, ex);
        }
      }
    }
    return keys;
  }
}
