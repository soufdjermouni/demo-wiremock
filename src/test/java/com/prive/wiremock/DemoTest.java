package com.prive.wiremock;

import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DemoTest {


    @Test
    void test (){

        stubFor(get(urlEqualTo("/body"))
                .willReturn(aResponse()
                        .withBody("Literal text to put in the body")));
    }
}
