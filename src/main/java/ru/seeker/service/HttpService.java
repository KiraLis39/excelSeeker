package ru.seeker.service;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Getter
@Service
public class HttpService {
    private RestTemplate restTemplate;

    public RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        }
        return restTemplate;
    }

    public HttpHeaders getDefaultHeaders() {
        return new HttpHeaders() {{
            setContentType(MediaType.APPLICATION_JSON);
            set("rqid", UUID.randomUUID().toString());
        }};
    }

    public MultiValueMap<String, String> getTokenHeaders(String access_token) {
        return new HttpHeaders() {{
            setContentType(MediaType.APPLICATION_JSON);
            setAccept(List.of(MediaType.APPLICATION_JSON));

            set("access_token", access_token);
        }};
    }

    public HttpHeaders getMultipartHeaders() {
        return new HttpHeaders() {{
            setContentType(MediaType.MULTIPART_FORM_DATA);
            set("rqid", UUID.randomUUID().toString());
        }};
    }
}
