package com.mais.seguradora.config;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Proteção dos novos contratos de sessão, sem mudar o CSRF dos formulários legados. */
public final class SessionSecurity {
    public static final String USER_ATTRIBUTE = "usuarioLogado";
    private static final String TOKEN_ATTRIBUTE = "mobileSessionToken";
    private static final SecureRandom RANDOM = new SecureRandom();
    private SessionSecurity() { }

    public static String renewToken(HttpSession session) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(TOKEN_ATTRIBUTE, token);
        return token;
    }
    public static String token(HttpSession session) {
        Object value = session.getAttribute(TOKEN_ATTRIBUTE);
        return value instanceof String token ? token : renewToken(session);
    }
    public static boolean matches(HttpSession session, String supplied) {
        Object expected = session.getAttribute(TOKEN_ATTRIBUTE);
        return supplied != null && expected instanceof String token
                && MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }
}
