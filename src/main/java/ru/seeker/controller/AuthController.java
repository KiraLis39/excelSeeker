package ru.seeker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.seeker.config.ApplicationProperties;
import ru.seeker.service.AuthService;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Методы контроля доступа", description = "Доступ и права")
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private final ApplicationProperties props;
    private final AuthService authService;

    @Operation(description = "Проверка пароля", hidden = true)
    @GetMapping()
    public ResponseEntity<String> auth(@RequestParam("password") String pass, HttpServletRequest request) {
        if (pass.equals(props.getCorrectWebPassword())) {
            log.info("Входящий логин c {}", request.getRemoteAddr());
            authService.putAuthUser(request.getRemoteHost());
            return ResponseEntity.accepted().build();
        } else {
            log.info("Безуспешный логин c {}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(description = "Проверить, был ли логин по IP", hidden = true)
    @GetMapping("/has_open_session")
    public ResponseEntity<String> hasOpenSession(HttpServletRequest request) {
        if (authService.isAuthUser(request.getRemoteHost())) {
            log.info("Успешная проверка доступа для {}", request.getRemoteAddr());
            return ResponseEntity.ok().build();
        } else {
            log.info("Запрещен доступ для {}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(description = "Завершение сессии", hidden = true)
    @GetMapping("/logout")
    public ResponseEntity<String> deauth(HttpServletRequest request) {
        authService.removeAuthUser(request.getRemoteHost());
        return ResponseEntity.ok().build();
    }
}
