package io.github.dbstarll.study.boot.security;

import io.github.dbstarll.dubai.user.entity.Credential;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.entity.Principal.Mode;
import io.github.dbstarll.study.entity.Subscribe;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.entity.enums.Page;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component("studySecurity")
class DefaultStudySecurity implements StudySecurity {
    private final SecurityExpressionOperations operations;

    DefaultStudySecurity() {
        this.operations = new SecurityExpressionRoot(SecurityContextHolder.getContext().getAuthentication()) {
        };
    }

    @Override
    public Authentication getAuthentication() {
        return operations.getAuthentication();
    }

    @Override
    public boolean hasMode(Mode mode) {
        return operations.hasAuthority(MiniProgramAuthentication.MODE_PREFIX + mode.name());
    }

    @Override
    public boolean hasModule(Module module) {
        return operations.hasAuthority(MiniProgramAuthentication.MODULE_PREFIX + module.name());
    }

    @Override
    public boolean hasAnyModule(Module... modules) {
        final String[] strModules = new String[modules.length];
        for (int i = 0; i < modules.length; i++) {
            strModules[i] = MiniProgramAuthentication.MODULE_PREFIX + modules[i].name();
        }
        return operations.hasAnyAuthority(strModules);
    }

    @Override
    public boolean hasPage(Page page) {
        return operations.hasAuthority(MiniProgramAuthentication.PAGE_PREFIX + page.name());
    }

    @Override
    public boolean hasAnyPage(Page... pages) {
        final String[] strPages = new String[pages.length];
        for (int i = 0; i < pages.length; i++) {
            strPages[i] = MiniProgramAuthentication.PAGE_PREFIX + pages[i].name();
        }
        return operations.hasAnyAuthority(strPages);
    }

    @Override
    public User<?, ?> getUser() {
        return (User<?, ?>) getAuthentication().getPrincipal();
    }

    @Override
    public Credential getCredential() {
        return getUser().credential;
    }

    @Override
    public Principal getPrincipal() {
        return getUser().principal;
    }

    @Override
    public Subscribe subscribe(Page page) {
        return getUser().subscribe(page);
    }

    @Override
    public Subscribe subscribe(Module module) {
        return getUser().subscribe(module);
    }
}