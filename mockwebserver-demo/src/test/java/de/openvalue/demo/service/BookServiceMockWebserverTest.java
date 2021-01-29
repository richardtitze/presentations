package de.openvalue.demo.service;


import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

public class BookServiceMockWebserverTest {
    private WebClient.Builder clientBuilder;
    private MockWebServer mockWebServer;
    private BookService service;

    @BeforeEach
    void beforeEach() throws Exception {
        // in the case of MockWebServer we have to create a new instance for each and every test
        // as there's no way to "reset" an existing instance
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        clientBuilder = WebClient.builder();
        service = new BookService("http://localhost:" + mockWebServer.getPort(), clientBuilder);
    }

    @AfterEach
    void afterEach() throws Exception {
        // do never forget to close the server (close is just a synonym for shutdown)
        mockWebServer.close();
    }

    @Test
    void findBooksByAuthorWithEnqueue() throws Exception {
        // the first version uses enqueue
        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"title\":\"It\"},{\"title\":\"The eyes of the dragon!\"}]")
                .addHeader("Content-Type", "application/json"));

        // call the service
        var books = service.findBooksByAuthor("King", "Stephen");

        // we expect 2 books
        StepVerifier.create(books)
                .expectNext(new Book("It"), new Book("The eyes of the dragon!"))
                .verifyComplete();

        // now we can make any assertions we want on the requests received by MockWebServer instance
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);

        // takeRequest will block - but we already know that we have at least one request ready to be asserted
        var request = mockWebServer.takeRequest();
        assertThat(request.getPath()).endsWith("/books/King/Stephen");

    }

    @Test
    void findBooksByAuthorWithDispatcher() {
        // the second version uses a dispatcher
        // a dispatcher can inspect the received request and return a MockResponse accordingly
        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {
                return switch (recordedRequest.getPath()) {
                    case "/books/King/Stephen" -> new MockResponse()
                            .setBody("[{\"title\":\"It\"},{\"title\":\"The eyes of the dragon!\"}]")
                            .addHeader("Content-Type", "application/json");
                    default -> new MockResponse().setResponseCode(400);
                };
            }
        });

        // call service and verify results as before
        var books = service.findBooksByAuthor("King", "Stephen");
        StepVerifier.create(books)
                .expectNext(new Book("It"), new Book("The eyes of the dragon!"))
                .verifyComplete();
    }
}
