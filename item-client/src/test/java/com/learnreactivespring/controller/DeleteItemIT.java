package com.learnreactivespring.controller;

import com.learnreactivespring.domain.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
public class DeleteItemIT {

    private WebClient webClient;
    private static final String BASEURL= "http://localhost:8080";

    @Before
    public void setUp(){
        webClient = WebClient.builder().baseUrl(BASEURL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .build();
    }

    @Test (expected= WebClientResponseException.class)
    public void deleteItem(){
        webClient.delete().uri("/v1/items/{id}", "ABC")
                .retrieve().bodyToMono(Item.class).block();
        webClient.get().uri("/v1/items/{id}", "ABC")
                .retrieve().bodyToMono(Item.class).block();
   }
}
