package io.github.dbstarll.study.boot;

import io.github.dbstarll.utils.spring.security.ExtendWebAuthenticationDetailsSource;
import io.github.dbstarll.utils.spring.security.PreAuthenticatedAuthentication;
import io.github.dbstarll.utils.spring.security.PreAuthenticatedAuthenticationServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    private static final RequestMatcher skipAuthRequestMatcher = skipAuthRequestMatcher();

    private static final RequestMatcher skipAuthRequestMatcher() {
        final RequestMatcher staticResources;
        {
            final RequestMatcher audio = new AntPathRequestMatcher("/audio/**", HttpMethod.GET.name());
            final RequestMatcher css = new AntPathRequestMatcher("/css/**", HttpMethod.GET.name());
            final RequestMatcher images = new AntPathRequestMatcher("/images/**", HttpMethod.GET.name());
            final RequestMatcher js = new AntPathRequestMatcher("/js/**", HttpMethod.GET.name());
            final RequestMatcher recorder = new AntPathRequestMatcher("/recorder/**", HttpMethod.GET.name());
            final RequestMatcher webjars = new AntPathRequestMatcher("/webjars/**", HttpMethod.GET.name());
            staticResources = new OrRequestMatcher(audio, css, images, js, recorder, webjars);
        }

        final RequestMatcher deprecated;
        {
            final RequestMatcher voice = new AntPathRequestMatcher("/voice/**", HttpMethod.GET.name());
            final RequestMatcher word = new AntPathRequestMatcher("/word");
            final RequestMatcher wordSub = new AntPathRequestMatcher("/word/**");
            final RequestMatcher exercise = new AntPathRequestMatcher("/exercise/**");
            final RequestMatcher exerciseWord = new AntPathRequestMatcher("/exercise-word/**");
            deprecated = new OrRequestMatcher(voice, word, wordSub, exercise, exerciseWord);
        }

        final RequestMatcher check = new AntPathRequestMatcher("/check/**");
        final RequestMatcher error = new AntPathRequestMatcher("/error");

        return new OrRequestMatcher(staticResources, deprecated, check, error);
    }

    @Bean
    @SuppressWarnings("unchecked")
    @ConditionalOnMissingBean(SessionRegistry.class)
    SessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry(new RedisIndexedSessionRepository(
                sessionRedisOperations));
    }

    @Autowired
    RedisOperations<Object, Object> sessionRedisOperations;

    @Bean
    @ConditionalOnMissingBean(ShallowEtagHeaderFilter.class)
    ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Bean
    @ConditionalOnMissingBean(CacheControl.class)
    CacheControl cacheControl() {
        return CacheControl.maxAge(365, TimeUnit.DAYS);
    }

    @Bean
    @ConditionalOnMissingBean(ExtendWebAuthenticationDetailsSource.class)
    ExtendWebAuthenticationDetailsSource extendWebAuthenticationDetailsSource() {
        return new ExtendWebAuthenticationDetailsSource();
    }

    @Bean
    @ConditionalOnMissingBean(PreAuthenticatedAuthenticationServiceManager.class)
    PreAuthenticatedAuthenticationServiceManager preAuthenticatedAuthenticationServiceManager(
            List<PreAuthenticatedAuthentication<?, ?>> authentications) {
        return new PreAuthenticatedAuthenticationServiceManager(authentications);
    }

    @Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnMissingBean(PreAuthenticatedAuthenticationProvider.class)
    PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        final PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setThrowExceptionWhenTokenRejected(true);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationTrustResolver.class)
    AuthenticationTrustResolver trustResolver() {
        return new AuthenticationTrustResolverImpl();
    }

    @Bean
    @ConditionalOnMissingBean(PermissionEvaluator.class)
    PermissionEvaluator permissionEvaluator() {
        return new DenyAllPermissionEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder passwordEncoder() {
        return new DelegatingPasswordEncoder("bcrypt",
                Collections.singletonMap("bcrypt", (PasswordEncoder) new BCryptPasswordEncoder()));
    }

    @Configuration
    class AjaxOnlyWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        private final List<PreAuthenticatedAuthentication<?, ?>> authentications;

        public AjaxOnlyWebSecurityConfigurerAdapter(List<PreAuthenticatedAuthentication<?, ?>> authentications) {
            this.authentications = authentications;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            final UnAuthorizedStrategy unAuthorizedStrategy = new UnAuthorizedStrategy();

            addFilter(http).csrf().disable() // 禁用Cross-Site_Request_Forgery(CSRF)保护

                    .authorizeRequests()// 按URL鉴权
                    .requestMatchers(skipAuthRequestMatcher).permitAll() // 资源文件不检查鉴权
                    .requestMatchers(new AntPathRequestMatcher("/weixin/**")).fullyAuthenticated() // 完全认证
                    .requestMatchers(new AntPathRequestMatcher("/english/**")).fullyAuthenticated() // 完全认证
                    .anyRequest().authenticated()

                    .and().exceptionHandling().authenticationEntryPoint(unAuthorizedStrategy) // 鉴权异常处理

                    .and().sessionManagement() // Session管理配置项
                    .sessionAuthenticationFailureHandler(unAuthorizedStrategy) // 在Session认证失败时使用
                    .maximumSessions(1) // 限制只有一个有效登录
                    .expiredSessionStrategy(unAuthorizedStrategy) // 在Session过期时使用
                    .sessionRegistry(sessionRegistry());
        }

        private HttpSecurity addFilter(HttpSecurity http) {
            final Set<Class<? extends Filter>> filterClasses = new HashSet<>();
            Class<? extends Filter> afterFilter = AbstractPreAuthenticatedProcessingFilter.class;
            for (PreAuthenticatedAuthentication<?, ?> authentication : authentications) {
                final Filter filter = authentication.filter();
                final Class<? extends Filter> filterClass = filter.getClass();
                if (filterClasses.add(filterClass)) {
                    http.addFilterAfter(filter, afterFilter);
                    afterFilter = filterClass;
                }
            }
            return http;
        }
    }

    static class UnAuthorizedStrategy
            implements SessionInformationExpiredStrategy, AuthenticationFailureHandler, AuthenticationEntryPoint {
        @Override
        public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
            sendUnauthorized(event.getResponse(), "Session Expired");
        }

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationException exception) throws IOException, ServletException {
            if (UsernameNotFoundException.class.isInstance(exception)) {
                sendNotFound(response, "Authentication Failed: " + exception.getMessage());
            } else {
                sendUnauthorized(response, "Authentication Failed: " + exception.getMessage());
            }
        }

        private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        }

        private void sendNotFound(HttpServletResponse response, String message) throws IOException {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException, ServletException {
            if (!response.isCommitted()) {
                final AuthenticationException failed = (AuthenticationException) request
                        .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                if (InsufficientAuthenticationException.class.isInstance(authException)) {
                    onAuthenticationFailure(request, response, failed == null ? authException : failed);
                } else {
                    if (authException != null) {
                        LOGGER.warn("AuthException Unhandled: ", authException);
                    }
                    if (failed != null) {
                        LOGGER.warn("Last AuthException Unhandled: ", failed);
                    }
                }
            }
        }
    }
}
