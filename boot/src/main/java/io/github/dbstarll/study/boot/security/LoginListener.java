package io.github.dbstarll.study.boot.security;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
class LoginListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {
    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        System.err.println(event);
    }
}