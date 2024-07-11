package ru.seeker.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import ru.seeker.config.ApplicationProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Service
@ApplicationScope
@RequiredArgsConstructor
public class AuthService {
    private final Set<String> authHosts = new HashSet<>(32);
    private final ApplicationProperties props;

    public void checkAuth(String login, String password) throws AuthenticationException {
        if (!password.equals(props.getCorrectWebPassword())) {
            throw new AuthenticationException("Неверный логин или пароль");
        }
    }

    public void putAuthUser(String host) {
        authHosts.add(host);
    }

    public boolean isAuthUser(String host) {
        log.info("Проверка наличия пользователя {} в списке авторизованных...", host);
        boolean hasFound = authHosts.contains(host);
        if (!hasFound) {
            log.info("Пользователь {} не найден в списке авторизованных.", host);
        }
        return hasFound;
    }

    public void removeAuthUser(String host) {
        log.info("Завершение сессии для {}", host);
        authHosts.remove(host);
    }

    public boolean isAdmin(HttpServletRequest request) {
        return request.getRemoteAddr().equals(props.getAdminIp());
    }
}
