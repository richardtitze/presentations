package de.openvalue.demo.service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

public class BookService {

    private final WebClient client;

    public BookService(String url, WebClient.Builder clientBuilder) {
        this.client = clientBuilder.baseUrl(url).build();
    }

    public Flux<Book> findBooksByAuthor(String lastName, String firstname) {
        return this.client.get()
                .uri("/books/{lastName}/{firstName}", Map.of("lastName", lastName, "firstName",firstname))
                .retrieve()
                .bodyToFlux(Book.class);
    }
}
