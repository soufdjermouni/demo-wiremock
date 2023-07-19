package com.prive.wiremock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.prive.wiremock.config.WireMockConfig;
import com.prive.wiremock.config.WireMockProxy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "integration")
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
@AutoConfigureMockMvc
class SpringBootWiremockApplicationBasicTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WireMockConfig wireMockConfig;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    private final List<WireMockServer> servers = new ArrayList<>();


    Function<WireMockProxy, WireMockServer> getMockServer =
            (WireMockProxy proxy) ->
                    new WireMockServer(
                            WireMockConfiguration.options()
                                    .port(proxy.getPort())
                                    .notifier(new ConsoleNotifier(true)));

    @BeforeEach
    void startRecording() {
        List<WireMockProxy> proxies = wireMockConfig.getProxies();
        if (!CollectionUtils.isEmpty(proxies)) {
            for (WireMockProxy proxy : proxies) {
                WireMockServer wireMockServer = getMockServer.apply(proxy);
                wireMockServer.start();
                if (proxy.isRecording()) {
                    wireMockServer.startRecording(config(proxy.getUrl(), true));
                }
                servers.add(wireMockServer);
            }
        }
    }

    @AfterEach
    void stopRecording() {
        if (!CollectionUtils.isEmpty(servers)) {
            for (WireMockServer server : servers) {
                if (server.getRecordingStatus().getStatus().equals(RecordingStatus.Recording)) {
                    server.stopRecording();
                }
                server.stop();
            }
        }
    }

    @Test
    void TestGreeting () throws Exception {
        String actualResponse = mockMvc.perform(org.springframework.test.web.servlet
                        .request.MockMvcRequestBuilders.get("/api/greeting")
                        .contentType("text/plain"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("Hello, World!",actualResponse);
    }

    @Test
    void helloTestApiSuccessAvecJsonMapping () throws Exception {
        String actualResponse = mockMvc.perform(org.springframework.test.web.servlet
                        .request.MockMvcRequestBuilders.get("/api/hello")
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("{\"message\":\"Hello, World!\"}",actualResponse);
    }

    @Test
    void helloTestApiSuccessAvecStubFor () throws Exception {

        // Définir le mappage pour l'API externe "hello" pour le scénario de succès
        JsonNode responseBody = objectMapper.readTree("{\"message\":\"Hello, World!\"}");
        configureFor("localhost", servers.get(0).port());
        stubFor(get(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                       .withJsonBody(responseBody)));

        String actualResponse = mockMvc.perform(org.springframework.test.web.servlet
                        .request.MockMvcRequestBuilders.get("/api/hello")
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("{\"message\":\"Hello, World!\"}",actualResponse);

        String actualResponse2 = mockMvc.perform(org.springframework.test.web.servlet
                        .request.MockMvcRequestBuilders.get("/api/hello/1")
                        .contentType("application/json"))
                .andExpect(status().is(404)).andReturn().getResponse().getContentAsString();
    }

    @Test
    void testHelloApiError () throws Exception {

        configureFor("localhost", servers.get(0).port());

        // Définir le mappage pour l'API externe "hello" pour le scénario de succès
        JsonNode responseBody = objectMapper.readTree("{\"error\":\"Internal Server Error\"}");

        stubFor(get(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(responseBody)));

        String actualResponse = mockMvc.perform(org.springframework.test.web.servlet
                        .request.MockMvcRequestBuilders.get("/api/hello")
                        .contentType("application/json"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"error\":\"Internal Server Error\"}",actualResponse);
    }


    @Test
    void testHelloApiErrorAvecMatchedStubId () throws Exception {
        configureFor("localhost", servers.get(0).port());
        StringValuePattern uuid =  matching("deefadbf-cac2-45f2-87bd-7f539972bf6b");
        stubFor(WireMock.get(urlEqualTo("/hello"))
                .withHeader("Matched-Stub-Id", uuid)
        );

        String actualResponse = mockMvc.perform(org.springframework.test.web.servlet
                        .request.MockMvcRequestBuilders.get("/api/hello")
                        .contentType("application/json"))
                .andReturn().getResponse().getContentAsString();

       assertEquals("{\"error\":\"Not Found Matched-Stub-Id\"}",actualResponse);

    }

    private RecordSpec config(String recordingURL, boolean recordingEnabled) {
        return WireMock.recordSpec()
                .forTarget(recordingURL)
                .onlyRequestsMatching(RequestPatternBuilder.allRequests())
                .captureHeader("Accept")
                .makeStubsPersistent(recordingEnabled)
                .ignoreRepeatRequests()
                .matchRequestBodyWithEqualToJson(true, true)
                .build();
    }
}