package com.github.zjor.web;

import com.github.zjor.services.users.UserService;
import com.google.inject.Inject;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.github.zjor.web.Rest2MqttController.error;

@Slf4j
public class AccessManagerImpl implements AccessManager {

    private final UserService userService;

    @Inject
    public AccessManagerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void manage(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<? extends RouteRole> routeRoles) throws Exception {
        if (routeRoles.contains(Role.ANYONE)) {
            handler.handle(ctx);
        } else if (routeRoles.contains(Role.AUTHENTICATED)) {
            try {
                var creds = ctx.basicAuthCredentials();
                log.info("auth({}; {})", creds.getUsername(), creds.getPassword());
                var userOpt = userService.findByTelegramId(Long.valueOf(creds.getUsername()));
                if (userOpt.isPresent()) {
                    ctx.attribute("user", userOpt.get());
                    handler.handle(ctx);
                } else {
                    error(ctx, HttpStatus.UNAUTHORIZED, "User was not found");
                }
            } catch (Exception e) {
                log.warn("Basic authentication failed: {}", e.getMessage(), e);
                error(ctx, HttpStatus.UNAUTHORIZED, "Basic authentication failed: " + e.getMessage());
            }
        } else {
            error(ctx, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

}
