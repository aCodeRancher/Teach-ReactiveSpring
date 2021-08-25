package com.learnreactivespring.controller;

import com.learnreactivespring.domain.Item;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import static org.junit.Assert.assertTrue;


@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
public class ItemClientControllerTest  {

    private WebClient webClient;
    private static final String BASEURL= "http://localhost:8080";

    @Before
     public void setUp(){
        webClient = WebClient.builder().baseUrl(BASEURL)
                   .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                   .build();
    }


    @Test
    public void retrieveAllItems(){
       Item item =  webClient.get().uri("/v1/items").accept(MediaType.APPLICATION_JSON)
               .retrieve()
               .bodyToFlux(Item.class).blockLast();

       assertTrue(item.getDescription().equals("Beats HeadPhones"));
     }

    @Test
    public void exchangeAllItems() {

         Item item = webClient.get().uri("/v1/items").exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class)).blockLast();
        assertTrue(item.getDescription()!=null);
    }

    @Test
    public void retrieveOneItem(){
        Item item =  webClient.get().uri("/v1/items/{id}", "ABC")
                              .retrieve().bodyToMono(Item.class).block();
        assertTrue(item.getDescription()!=null);

   }

    @Test
    public void exchangeOneItem(){
        Item item = webClient.get().uri("/v1/items/{id}", "ABC")
                 .exchange().flatMap(clientResponse -> clientResponse.bodyToMono(Item.class)).block();
         assertTrue(item.getDescription()!=null);
    }

    @Test
    public void createItem(){
        Item laptop = new Item(null, "Lenovo laptop", 990.99);
        Mono<Item> laptopMono = Mono.just(laptop);
       Item addedItem = webClient.post().uri("/v1/items/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(laptopMono, Item.class)
                .retrieve()
                .bodyToMono(Item.class).block();
        assertTrue(addedItem.getDescription().equals("Lenovo laptop"));
   }


    @Test
    public void updateItem(){
        Mono<Item> itemBody = Mono.just(new Item("ABC", "Beats HeadPhones", 139.99));
        Item updatedItem = webClient.put().uri("/v1/items/{id}", "ABC")
               .body(itemBody, Item.class)
               .retrieve()
               .bodyToMono(Item.class).block();
         assertTrue(updatedItem.getPrice()==139.99);

   }

   @Test
    public void deleteItem() {

       webClient.delete().uri("/v1/items/{id}","ABC")
               .retrieve()
               .bodyToMono(Void.class);

    }

}