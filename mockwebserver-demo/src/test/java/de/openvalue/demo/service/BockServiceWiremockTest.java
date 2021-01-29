package de.openvalue.demo.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class BockServiceWiremockTest {
    static WireMockServer wireMockServer;
    WebClient.Builder clientBuilder;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @BeforeEach
    void beforeEach() {
        clientBuilder = WebClient.builder();
        wireMockServer.resetAll();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    void findBooksByAuthor() {
        var service = new BookService("http://localhost:" + wireMockServer.port(), clientBuilder);
        var books = service.findBooksByAuthor("King", "Stephen");
        wireMockServer.givenThat(get(urlEqualTo("/books/King/Stephen"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"title\":\"It\"},{\"title\":\"The eyes of the dragon!\"}]")));
        StepVerifier.create(books)
                .expectNext(new Book("It"), new Book("The eyes of the dragon!"))
                .verifyComplete();

    }
}
