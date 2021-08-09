package io.github.dbstarll.study.boot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.user.entity.Credential;
import io.github.dbstarll.dubai.user.service.CredentialService;
import io.github.dbstarll.study.boot.api.WeixinApi;
import io.github.dbstarll.study.boot.security.MiniProgramAuthentication.Credentials;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.entity.Principal.Mode;
import io.github.dbstarll.study.service.PrincipalService;
import io.github.dbstarll.study.service.SubscribeService;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import io.github.dbstarll.utils.net.api.ApiException;
import io.github.dbstarll.utils.spring.security.AutowiredPreAuthenticatedAuthentication;
import io.github.dbstarll.utils.spring.security.PreAuthenticatedAuthenticationFilter;
import io.github.dbstarll.utils.spring.security.PreAuthenticatedAuthenticationService;
import io.github.dbstarll.utils.spring.security.PreAuthenticatedAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * 微信小程序认证.
 *
 * @author dbstar
 */
@Component
public final class MiniProgramAuthentication extends AutowiredPreAuthenticatedAuthentication<String, Credentials> {
  static final String MODE_PREFIX = "MODE_";
  static final String MODULE_PREFIX = "MODULE_";
  static final String PAGE_PREFIX = "PAGE_";

  private @Value("${study.weixin.mini.admins}")
  String admins;

  @Override
  protected PreAuthenticatedAuthenticationFilter<String, Credentials> originalFilter() {
    return new Filter(new AntPathRequestMatcher("/weixin/mp/login", HttpMethod.POST.name()));
  }

  @Override
  protected PreAuthenticatedAuthenticationService<String, Credentials> originalService() {
    return new Service(Arrays.asList(StringUtils.split(admins, ',')));
  }

  public static class Credentials implements Serializable {
    private static final long serialVersionUID = 5276337743075679321L;

    public final String appId;
    public final String code;

    public Credentials(String appId, String code) {
      this.appId = appId;
      this.code = code;
    }
  }

  static class Filter extends PreAuthenticatedAuthenticationFilter<String, Credentials> {
    private static final String HEADER_VARIABLE_PRINCIPAL = "userName";
    private static final String HEADER_VARIABLE_APP_ID = "appId";
    private static final String HEADER_VARIABLE_CREDENTIALS = "code";

    private Filter(RequestMatcher requestMatcher) {
      super(requestMatcher, true);
    }

    @Override
    protected String getPreAuthenticatedPrincipal(HttpServletRequest request) {
      final String userName = request.getHeader(HEADER_VARIABLE_PRINCIPAL);
      if (StringUtils.isBlank(userName)) {
        return null;
      } else {
        final String encoding = request.getCharacterEncoding();
        try {
          return URLDecoder.decode(userName, StringUtils.isBlank(encoding) ? "UTF-8" : encoding);
        } catch (UnsupportedEncodingException e) {
          return userName;
        }
      }
    }

    @Override
    protected Credentials getPreAuthenticatedCredentials(HttpServletRequest request) {
      final String appId = request.getHeader(HEADER_VARIABLE_APP_ID);
      final String code = request.getHeader(HEADER_VARIABLE_CREDENTIALS);
      if (StringUtils.isBlank(appId) || StringUtils.isBlank(code)) {
        return null;
      } else {
        return new Credentials(appId, code);
      }
    }
  }

  static class Service implements PreAuthenticatedAuthenticationService<String, Credentials> {
    private final Collection<String> admins;

    @Autowired
    private WeixinApi api;
    @Autowired
    private PrincipalService principalService;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private SubscribeService subscribeService;
    @Autowired
    private ObjectMapper mapper;

    private Service(Collection<String> admins) {
      this.admins = new HashSet<>(admins);
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken<String, Credentials> token)
            throws UsernameNotFoundException {
      final Map<String, String> session;
      try {
        session = mapper.convertValue(api.session(token.getCredentials().appId, token.getCredentials().code),
                mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
      } catch (IOException | ApiException e) {
        throw new BadCredentialsException("微信小程序登录凭证校验失败：" + e.getMessage(), e);
      }

      final String openid = session.get("openid");
      if (StringUtils.isBlank(openid)) {
        throw new BadCredentialsException("微信小程序登录凭证校验失败：无效的openid");
      }

      final Entry<Credential, Principal> entry = loadCredential(token.getCredentials().appId, openid);
      return new User<String, Credentials>(token, session, entry.getKey(), entry.getValue())
              .subscribe(subscribeService.findByPrincipalId(entry.getValue().getId()));
    }

    private Entry<Credential, Principal> loadCredential(final String appId, final String openid) {
      final Credential credential = credentialService
              .findOne(io.github.dbstarll.dubai.user.entity.ext.MiniProgramCredentials.distinctFilter(appId, openid));
      if (credential != null) {
        return EntryWrapper.wrap(credential, principalService.findById(credential.getPrincipalId()));
      } else {
        final Principal newPrincipal = EntityFactory.newInstance(Principal.class);
        newPrincipal.setMode(admins.contains(openid) ? Mode.ADMIN : Mode.GUEST);
        principalService.save(newPrincipal, (Validate) null);

        final Credential newCredential = io.github.dbstarll.dubai.user.entity.ext.Credentials.miniProgram(appId, openid);
        newCredential.setPrincipalId(newPrincipal.getId());
        credentialService.save(newCredential, (Validate) null);

        return EntryWrapper.wrap(newCredential, newPrincipal);
      }
    }
  }
}
