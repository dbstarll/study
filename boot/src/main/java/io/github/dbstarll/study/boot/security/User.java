package io.github.dbstarll.study.boot.security;

import io.github.dbstarll.dubai.user.entity.Credential;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.entity.Subscribe;
import io.github.dbstarll.study.entity.Subscribe.SubscribeType;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.entity.enums.Page;
import io.github.dbstarll.utils.spring.security.PreAuthenticatedAuthenticationToken;
import io.github.dbstarll.utils.spring.security.PreAuthenticatedAuthenticationUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

public class User<P, C> extends PreAuthenticatedAuthenticationUserDetails<P, C> {
    private static final long serialVersionUID = 601656229496797567L;

    private final Map<String, String> session;
    final Credential credential;
    final Principal principal;
    private final Collection<SimpleGrantedAuthority> authorities;
    private final Map<String, Subscribe> subscribes;

    User(final PreAuthenticatedAuthenticationToken<P, C> token, Map<String, String> session, Credential credential,
         Principal principal) {
        super(token);
        this.session = session;
        this.credential = credential;
        this.principal = principal;
        this.authorities = new HashSet<>(buildAuthorities(principal));
        this.subscribes = new HashMap<>();
    }

    private static Collection<SimpleGrantedAuthority> buildAuthorities(Principal principal) {
        return Collections
                .singleton(new SimpleGrantedAuthority(MiniProgramAuthentication.MODE_PREFIX + principal.getMode()));
    }

    public Map<String, String> getSession() {
        return session;
    }

    /**
     * 更新订阅信息.
     *
     * @param subscribes 新的订阅
     * @return User
     */
    public User<P, C> subscribe(Iterable<Subscribe> subscribes) {
        // 重置并根据subscribes来设置authorities
        this.authorities.clear();
        this.authorities.addAll(buildAuthorities(principal));

        this.subscribes.clear();
        for (Subscribe subscribe : subscribes) {
            subscribe(subscribe);
        }

        return this;
    }

    private void subscribe(Subscribe subscribe) {
        final String key = getSubscribeKey(subscribe);
        if (key != null) {
            this.subscribes.put(key, subscribe);
            this.authorities.add(new SimpleGrantedAuthority(MiniProgramAuthentication.MODULE_PREFIX + subscribe.getModule()));
            if (subscribe.getType() == SubscribeType.page) {
                this.authorities.add(new SimpleGrantedAuthority(MiniProgramAuthentication.PAGE_PREFIX + subscribe.getPage()));
            }
        }
    }

    Subscribe subscribe(Page page) {
        return subscribes.get(getSubscribeKey(page.module, SubscribeType.page, page));
    }

    Subscribe subscribe(Module module) {
        return subscribes.get(getSubscribeKey(module, SubscribeType.entity, null));
    }

    public Iterable<Subscribe> subscribes() {
        return subscribes.values();
    }

    private static String getSubscribeKey(Subscribe subscribe) {
        return getSubscribeKey(subscribe.getModule(), subscribe.getType(), subscribe.getPage());
    }

    private static String getSubscribeKey(Module module, SubscribeType type, Page page) {
        switch (type) {
            case page:
                return StringUtils.join(new Object[]{module, type, page}, ':');
            case entity:
                return StringUtils.join(new Object[]{module, type}, ':');
            default:
                return null;
        }
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + principal.getId().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User<?, ?> other = (User<?, ?>) obj;
        if (!principal.getId().equals(other.principal.getId())) {
            return false;
        }
        return true;
    }
}