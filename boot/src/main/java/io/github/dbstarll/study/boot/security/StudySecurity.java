package io.github.dbstarll.study.boot.security;

import io.github.dbstarll.dubai.user.entity.Credential;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.entity.Principal.Mode;
import io.github.dbstarll.study.entity.Subscribe;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.entity.enums.Page;
import org.springframework.security.core.Authentication;

public interface StudySecurity {
    Authentication getAuthentication();

    boolean hasMode(Mode mode);

    boolean hasModule(Module module);

    boolean hasAnyModule(Module... modules);

    boolean hasPage(Page page);

    boolean hasAnyPage(Page... pages);

    User<?, ?> getUser();

    Credential getCredential();

    Principal getPrincipal();

    Subscribe subscribe(Page page);

    Subscribe subscribe(Module module);
}
