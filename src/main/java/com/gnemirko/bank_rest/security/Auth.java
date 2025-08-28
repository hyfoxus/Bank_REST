package com.gnemirko.bank_rest.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class Auth {
    private Auth() {}

    public static Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a instanceof JwtAuthenticationToken token) {
            var jwt = token.getToken();

            // 1) Кладёшь id пользователя в claim "uid" при выдаче токена
            Object uid = jwt.getClaim("uid");
            if (uid instanceof Number n) return n.longValue();
            if (uid instanceof String s) return Long.parseLong(s);

            // 2) Фоллбек: использовать sub
            String sub = jwt.getSubject();
            try { return Long.parseLong(sub); }
            catch (NumberFormatException e) {
                throw new IllegalStateException("JWT sub не является числовым id: " + sub);
            }
        }
        throw new IllegalStateException("Unsupported authentication: " + a);
    }
}