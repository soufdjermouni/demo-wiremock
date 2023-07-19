package com.prive.wiremock.service;

import com.prive.wiremock.dto.UniversityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@Slf4j
public class TestService {

    private final RestTemplate restTemplate;

    private final String universityBaseURL;

    public TestService(RestTemplate restTemplate, @Value("${universitiesBaseURL}") String universityBaseURL) {
        this.restTemplate = restTemplate;
        this.universityBaseURL = universityBaseURL;
    }

    public String hello() {
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(universityBaseURL + "/hello")
                .encode()
                .toUriString();
        ResponseEntity<String> rateResponse = null;
        try {
            rateResponse =
                    restTemplate.exchange(urlTemplate, HttpMethod.GET, null, String.class);
        } catch (HttpServerErrorException e) {
            if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                //return "{\"error\":\"Internal Server Error\"}";
                System.out.println(e.getResponseBodyAsString());
                return e.getResponseBodyAsString();
            } else {
                return e.getResponseBodyAsString();
            }
        } catch (HttpClientErrorException e) {
            System.out.println(e.getResponseBodyAsString());
            return e.getResponseBodyAsString();
        }

        return rateResponse.getBody();
    }

    public String greeting() {
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(universityBaseURL + "/greeting")
                .encode()
                .toUriString();
        ResponseEntity<String> rateResponse =
                restTemplate.exchange(urlTemplate,
                        HttpMethod.GET, null, String.class);
        return rateResponse.getBody();
    }
}
