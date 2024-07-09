package ru.seeker.controller.hidden;

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

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Базовые методы", description = "Общее")
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class BaseController {
    private final ApplicationProperties props;

    @Operation(description = "Заглушка корня сайта", hidden = true)
    @GetMapping()
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("!!! WELCOME !!!");
    }

    @Operation(description = "Заглушка корня сайта", hidden = true)
    @GetMapping("/auth")
    public ResponseEntity<String> auth(@RequestParam("password") String pass) {
        return pass.equals(props.getCorrectWebPassword())
                ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
