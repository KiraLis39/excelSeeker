package ru.seeker.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import ru.seeker.config.ApplicationProperties;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Service
@ApplicationScope
@RequiredArgsConstructor
public class AuthService {
    private final Map<String, ZonedDateTime> authHosts = new HashMap<>(32);
    private final ApplicationProperties props;

    public boolean checkAuth(String login, String password, HttpServletRequest request) throws AuthenticationException {
        if (password.equals(props.getCorrectWebPassword())) {
            log.info("Входящий логин c {}", request.getRemoteAddr());
            putAuthUser(request.getRemoteHost());
            return isAuthUser(request);
        } else {
            throw new AuthenticationException("Неверный логин или пароль");
        }
    }

    public void putAuthUser(String host) {
        authHosts.put(host, ZonedDateTime.now());
    }

    public boolean isAuthUser(HttpServletRequest request) {
        String host = request.getRemoteHost();
        log.info("Проверка наличия пользователя {} в списке авторизованных...", host);
        boolean hasFound = authHosts.containsKey(host);
        if (!hasFound) {
            log.info("Пользователь {} не найден в списке авторизованных.", host);
        }

        // если 'тогда + 1 час' уже прошли, относительно 'сейчас':
        if (authHosts.get(host).plusHours(1).isBefore(ZonedDateTime.now())) {
            hasFound = false;
            removeAuthUser(host);
            log.info("Session timeout for ({}).", request.getRemoteAddr());
        }

        if (isAdmin(request)) {
            hasFound = true;
            log.info("Но он оказался Админом ({}), так что путь открыт...", request.getRemoteAddr());
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
