package io.github.dbstarll.study.boot.utils;

import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.boot.security.StudySecurity;
import io.github.dbstarll.study.entity.Principal.Mode;
import io.github.dbstarll.study.entity.Subscribe;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.entity.ext.Exchange;
import io.github.dbstarll.study.entity.ext.MasterPercent;
import io.github.dbstarll.study.service.WordService;
import io.github.dbstarll.study.utils.DictionaryApi;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Date;
import java.util.Set;

public final class StudyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudyUtils.class);

    private static final int[] nextMinutes = new int[]{0, 1, 5, 30, 720, 1440, 1440 * 2, 1440 * 4, 1440 * 7, 1440 * 15,
            1440 * 30, 1440 * 60};

    private StudyUtils() {
    }

    /**
     * 获得当前登录用户的exerciseBookId.
     *
     * @param security StudySecurity
     * @return 当前登录用户的exerciseBookId
     */
    public static ObjectId getUserBookId(StudySecurity security) {
        if (security.hasMode(Mode.ADMIN)) {
            final ObjectId fockBookId = (ObjectId) RequestContextHolder.getRequestAttributes().getAttribute("fockBookId",
                    RequestAttributes.SCOPE_SESSION);
            if (fockBookId != null) {
                return fockBookId;
            }
        }
        final Subscribe subscribe = security.subscribe(Module.ENGLISH);
        return subscribe != null ? subscribe.getEntityId() : new ObjectId("5c1349fac0ad810254687ca1");
    }

    /**
     * fock当前登录用户的exerciseBookId.
     *
     * @param security StudySecurity
     * @param bookId   exerciseBookId
     */
    public static void fockUserBookId(StudySecurity security, ObjectId bookId) {
        if (security.hasMode(Mode.ADMIN)) {
            RequestContextHolder.getRequestAttributes().setAttribute("fockBookId", bookId, RequestAttributes.SCOPE_SESSION);
        }
    }

    /**
     * 验证Subscribe是否有效.
     *
     * @param subscribe subscribe
     * @return 是否有效
     */
    public static boolean verificationSubscribe(Subscribe subscribe) {
        if (subscribe == null) {
            return false;
        }
        final Date now = new Date();
        if (subscribe.getStart() != null && now.compareTo(subscribe.getStart()) < 0) {
            return false;
        }
        if (subscribe.getEnd() != null && now.compareTo(subscribe.getEnd()) > 0) {
            return false;
        }

        switch (subscribe.getType()) {
            case page:
                return subscribe.getPage() != null;
            case entity:
                return subscribe.getEntityId() != null;
            default:
                return false;
        }
    }

    /**
     * 获得下次练习时间.
     *
     * @param bingo bingo
     */
    public static int getNextExerciseMinutes(final int bingo) {
        return bingo < nextMinutes.length ? nextMinutes[bingo] : nextMinutes[nextMinutes.length - 1];
    }

    public static Date getNextExerciseTime(final MasterPercent percent) {
        final int seconds = getNextExerciseMinutes(percent.getBingo()) * 60 * percent.getCorrect() / percent.getTotal();
        return DateUtils.addSeconds(percent.getLast(), seconds);
    }

    /**
     * 保存word及其所有Exchange.
     *
     * @param word          word
     * @param wordService   wordService
     * @param dictionaryApi dictionaryApi
     * @return word
     */
    public static Word saveRecursion(final Word word, final WordService wordService, final DictionaryApi dictionaryApi) {
        final Word existWord = wordService.findOne(wordService.filterByWord(word.getName(), false, false));
        if (existWord != null) {
            return existWord;
        }

        if (word.isCri() || word.getWordId() != null || word.getExchanges() != null) {
            if (null != wordService.save(word, new ObjectId(), (Validate) null)) {
                LOGGER.info("add word: {} with id: {}", word.getName(), word.getId());
                final ObjectId wordId = word.getId();
                final Set<Exchange> exchanges = word.getExchanges();
                if (exchanges != null) {
                    for (Exchange exchange : exchanges) {
                        if (!StringUtils.containsWhitespace(exchange.getWord())) {
                            final Word wd;
                            try {
                                wd = dictionaryApi.query(exchange.getWord());
                            } catch (Throwable ex) {
                                LOGGER.error("query word failed: " + exchange.getWord(), ex);
                                continue;
                            }
                            if (wd != null && exchange.getWord().equals(wd.getName())) {
                                wd.setWordId(wordId);
                                saveRecursion(wd, wordService, dictionaryApi);
                            }
                        }
                    }
                }
            }
        } else {
            LOGGER.warn("skip word: {} with cri: {}, wordId: {}, exchanges: {}", word.getName(), word.isCri(),
                    word.getWordId(), word.getExchanges());
        }
        return word;
    }
}
