package ru.seeker.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.ApplicationScope;
import ru.seeker.config.ApplicationProperties;
import ru.seeker.config.Constant;
import ru.seeker.repository.PassRepository;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
@Service
@ApplicationScope
@RequiredArgsConstructor
public class AuthService {
    private final Map<String, ZonedDateTime> authHosts = new HashMap<>(32);
    private final ApplicationProperties props;
    private final PassRepository passRepository;
    private final String DEFAULT_USER_NAME = "Default";

    public boolean checkAuth(String login, String password, HttpServletRequest request) throws AuthenticationException {
        Optional<Integer> pOpt = passRepository.findPassHashByLogin(login != null ? login : DEFAULT_USER_NAME);
        if (pOpt.isPresent() && password.hashCode() == pOpt.get()) {
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

        boolean hasFound = false;
        if (!authHosts.isEmpty()) {
            log.info("Проверка наличия пользователя {} в списке авторизованных...", host);
            hasFound = authHosts.containsKey(host);
            if (!hasFound) {
                log.info("Пользователь {} не найден в списке авторизованных.", host);
            }

            // если 'тогда + SESSION_LIVE_MINUTES' уже прошли, относительно 'сейчас':
            if (authHosts.get(host).plusMinutes(Constant.SESSION_LIVE_MINUTES).isBefore(ZonedDateTime.now())) {
                hasFound = false;
                removeAuthUser(host);
                log.info("Session timeout for ({}).", request.getRemoteAddr());
            }
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

    @Transactional
    public boolean changePassword(
            String login,
            String oldPass,
            String newPass,
            HttpServletRequest request
    ) throws AuthenticationException {
        Optional<Integer> pOpt = passRepository.findPassHashByLogin(login != null ? login : DEFAULT_USER_NAME);
        if (pOpt.isPresent() && oldPass.hashCode() == pOpt.get()) {
            log.info("Пользователь {} ({}) запрашивает смену пароля...", login, request.getRemoteAddr());
            passRepository.updatePasswordByLogin(login != null ? login : DEFAULT_USER_NAME, newPass.hashCode());
            authHosts.clear();
            putAuthUser(request.getRemoteHost());
            return isAuthUser(request);
        } else {
            throw new AuthenticationException("Неверный логин или пароль");
        }
    }
}
