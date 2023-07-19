package com.prive.wiremock.config;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "wiremock-config")
@Data
@Profile("integration")
public class WireMockConfig {
    //This is because if we have to call multiple external APIs, then we can just append the configuration below the
    // current one and it will work.
    private List<WireMockProxy> proxies;
}
