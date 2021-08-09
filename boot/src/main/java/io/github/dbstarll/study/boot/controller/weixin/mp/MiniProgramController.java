package io.github.dbstarll.study.boot.controller.weixin.mp;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.user.entity.Authentication;
import io.github.dbstarll.dubai.user.entity.Credential;
import io.github.dbstarll.dubai.user.entity.join.PrincipalBase;
import io.github.dbstarll.dubai.user.service.AuthenticationService;
import io.github.dbstarll.study.boot.security.RequestUtils;
import io.github.dbstarll.study.boot.security.StudySecurity;
import io.github.dbstarll.study.boot.utils.StudyUtils;
import io.github.dbstarll.study.entity.ExerciseBook;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.entity.Principal.Mode;
import io.github.dbstarll.study.entity.Subscribe;
import io.github.dbstarll.study.entity.Subscribe.SubscribeType;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.entity.enums.Page;
import io.github.dbstarll.study.service.ExerciseBookService;
import io.github.dbstarll.study.service.PrincipalService;
import io.github.dbstarll.study.service.SubscribeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/weixin/mp")
class MiniProgramController {
    private static final JsonPointer jpDetails = JsonPointer.compile("/details");
    private static final JsonPointer jpCredentials = JsonPointer.compile("/credentials");
    private static final JsonPointer jpSession = JsonPointer.compile("/principal/session");
    private static final JsonPointer jpPlatform = JsonPointer.compile("/systemInfo/platform");

    private static final TypeFactory typeFactory = TypeFactory.defaultInstance();

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private StudySecurity security;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PrincipalService principalService;
    @Autowired
    private SubscribeService subscribeService;
    @Autowired
    private ExerciseBookService exerciseBookService;
    @Autowired
    private SessionRegistry sessionRegistry;

    @GetMapping("/heartbeat")
    Object heartbeat() {
        return new Date();
    }

    @PostMapping("/login")
    Object login(@RequestBody final ObjectNode body, final HttpServletRequest request) throws Exception {
        final ObjectNode authentication = mapper.convertValue(security.getAuthentication(), ObjectNode.class);
        final Map<String, Map<String, Object>> details = getLoginDetails(body, authentication);

        final Principal principal = updatePrincipal(security.getPrincipal(), details);
        if (!RequestUtils.isInternal(request) && !"devtools".equals(body.at(jpPlatform).asText())) {
            updateAuthentication(principal, details);
        }

        return modules();
    }

    private Map<String, Map<String, Object>> getLoginDetails(final ObjectNode body, final ObjectNode authentication) {
        body.set("details", authentication.at(jpDetails));
        body.set("credentials", authentication.at(jpCredentials));
        body.set("session", authentication.at(jpSession));
        return mapper.convertValue(body,
                typeFactory.constructMapType(HashMap.class, typeFactory.constructType(String.class),
                        typeFactory.constructMapType(HashMap.class, String.class, Object.class)));
    }

    private Principal updatePrincipal(final Principal principal, final Map<String, Map<String, Object>> details) {
        if (principal.getUserInfo() == null) {
            principal.setUserInfo(details.get("userInfo"));
            principalService.save(principal, (Validate) null);
        }
        return principal;
    }

    private void updateAuthentication(final Principal principal, final Map<String, Map<String, Object>> details) {
        final Credential credential = security.getCredential();
        final String appId = details.get("credentials").get("appId").toString();
        final String sessionKey = details.get("session").get("session_key").toString();
        final Bson filter = Filters.and(authenticationService.filterByPrincipalId(principal.getId()),
                Filters.eq("source", credential.getSource()), authenticationService.filterByCredentialId(credential.getId()),
                Filters.eq("details.credentials.appId", appId), Filters.eq("details.session.session_key", sessionKey));

        Authentication authentication = authenticationService.findOne(filter);
        if (authentication == null) {
            authentication = EntityFactory.newInstance(Authentication.class);
            authentication.setPrincipalId(principal.getId());
            authentication.setSource(credential.getSource());
            authentication.setCredentialId(credential.getId());
        }
        authentication.setDetails(details);
        authenticationService.save(authentication, (Validate) null);
    }

    @GetMapping("/module")
    Object modules() {
        final Map<Module, Map<Page, JsonNode>> modules = new TreeMap<>();

        for (Page page : Page.values()) {
            if (hasAuthority(page)) {
                final JsonNode subscribe = filterSubscribe(security.subscribe(page));
                final Map<Page, JsonNode> pages = modules.get(page.module);
                if (pages == null) {
                    modules.put(page.module, new TreeMap<>(Collections.singletonMap(page, subscribe)));
                } else {
                    pages.put(page, subscribe);
                }
            }
        }

        return modules;
    }

    private JsonNode filterSubscribe(Subscribe subscribe) {
        if (subscribe == null) {
            return mapper.getNodeFactory().booleanNode(false);
        } else {
            return mapper.convertValue(subscribe, ObjectNode.class)
                    .remove(Arrays.asList("id", PrincipalBase.FIELD_NAME_PRINCIPAL_ID, "type", "module", "page"));
        }
    }

    private boolean hasAuthority(Page page) {
        if (security.hasMode(Mode.ADMIN)) {
            return true;
        } else if (page.mode(Mode.GUEST) && security.hasMode(Mode.GUEST)) {
            return true;
        } else if (page.mode(Mode.USER) && security.hasMode(Mode.USER) && security.hasPage(page)) {
            return StudyUtils.verificationSubscribe(security.subscribe(page));
        } else {
            return false;
        }
    }

    @GetMapping("/subscribe")
    Object subscribes() {
        final Map<Module, Map<Page, JsonNode>> modules = new TreeMap<>();

        for (Page page : Page.values()) {
            if (page.mode(Mode.USER)) {
                final Subscribe subscribe = security.subscribe(page);
                if (subscribe == null || subscribe.getId() != null) {
                    final JsonNode json = filterSubscribe(subscribe);
                    final Map<Page, JsonNode> pages = modules.get(page.module);
                    if (pages == null) {
                        modules.put(page.module, new TreeMap<>(Collections.singletonMap(page, json)));
                    } else {
                        pages.put(page, json);
                    }
                }
            }
        }

        return modules;
    }

    @PostMapping("/subscribe")
    Object subscribe(@RequestBody final Page... pages) {
        final Principal principal = security.getPrincipal();

        boolean changed = false;
        try {
            final Set<Module> newModules = new HashSet<>();
            for (Page page : pages) {
                if (page.mode(Mode.USER)) {
                    newModules.add(page.module);
                    changed = extendPageSubscribe(principal, page) || changed;
                }
            }

            if (!newModules.isEmpty()) {
                changed = guestToUser(principal) || changed;
            }

            if (newModules.contains(Module.ENGLISH)) {
                changed = bindExerciseBook(principal) || changed;
            }
        } finally {
            if (changed) {
                security.getUser().subscribe(subscribeService.findByPrincipalId(principal.getId()));
                for (SessionInformation session : sessionRegistry.getAllSessions(security.getUser(), false)) {
                    session.expireNow();
                }
            }
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("changed", changed);
        result.put("subscribes", subscribes());
        return result;
    }

    private boolean extendPageSubscribe(Principal principal, Page page) {
        final Subscribe subscribe = security.subscribe(page);
        if (subscribe == null) {
            final Subscribe newSubscribe = EntityFactory.newInstance(Subscribe.class);
            newSubscribe.setPrincipalId(principal.getId());
            newSubscribe.setModule(page.module);
            newSubscribe.setType(SubscribeType.page);
            newSubscribe.setPage(page);
            newSubscribe
                    .setEnd(DateUtils.addSeconds(DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), 32), -1));
            return null != subscribeService.save(newSubscribe, (Validate) null);
        } else if (subscribe.getEnd() != null) {
            final Date now = new Date();
            final Date start = now.compareTo(subscribe.getEnd()) < 0 ? subscribe.getEnd() : now;
            subscribe.setEnd(DateUtils.addSeconds(DateUtils.addDays(DateUtils.truncate(start, Calendar.DATE), 32), -1));
            return null != subscribeService.save(subscribe, (Validate) null);
        } else {
            return false;
        }
    }

    private boolean guestToUser(final Principal principal) {
        if (principal.getMode() == Mode.GUEST) {
            principal.setMode(Mode.USER);
            return null != principalService.save(principal, (Validate) null);
        } else {
            return false;
        }
    }

    private boolean bindExerciseBook(final Principal principal) {
        if (security.subscribe(Module.ENGLISH) == null) {
            final ExerciseBook book = EntityFactory.newInstance(ExerciseBook.class);
            book.setName(StringUtils.substring(security.getAuthentication().getName(), 0, 12) + "的词汇本");
            if (null != exerciseBookService.save(book, (Validate) null)) {
                final Subscribe subscribe = EntityFactory.newInstance(Subscribe.class);
                subscribe.setPrincipalId(principal.getId());
                subscribe.setModule(Module.ENGLISH);
                subscribe.setType(SubscribeType.entity);
                subscribe.setEntityId(book.getId());
                if (null != subscribeService.save(subscribe, (Validate) null)) {
                    Map<String, ObjectId> sources = principal.getSources();
                    if (sources == null) {
                        principal.setSources(sources = new HashMap<>());
                    }
                    sources.put(subscribe.getModule().name(), subscribe.getEntityId());
                    return null != principalService.save(principal, (Validate) null);
                }
            }
        }
        return false;
    }
}
