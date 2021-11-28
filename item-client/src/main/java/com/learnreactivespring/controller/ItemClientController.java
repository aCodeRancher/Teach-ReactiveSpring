package com.learnreactivespring.controller;

import com.learnreactivespring.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;


@RestController
@Slf4j
public class ItemClientController {

    @Value("${restClient.baseUrl}")
     private String baseUrl;

    private WebClient webClient = WebClient.builder().build();


    @GetMapping("/client/retrieve")
    public Mono<List<Item>> getAllItemsUsingRetrieve() {

       return webClient.get().uri(baseUrl+"/v1/items")
               .retrieve()
               .bodyToFlux(Item.class)
               .collectList();
    }

    @GetMapping("/client/exchange")
    public Flux<Item> getAllItemsUsingExchange(){

        return webClient.get().uri(baseUrl+ "/v1/items")
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
                .log("Items in Client Project exchange : ");
    }

     @GetMapping("/client/retrieve/singleItem/{id}")
    public Mono<Item> getOneItemUsingRetrieve(@PathVariable("id") String id){

        return webClient.get().uri(baseUrl+"/v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Items in Client Project retrieve single Item : ");
    }

  @GetMapping("/client/exchange/singleItem")
    public Mono<Item> getOneItemUsingExchange( ){

        String id = "ABC";

        return webClient.get().uri(baseUrl+"/v1/items/{id}",id)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
                .log("Items in Client Project retrieve single Item : ");
    }

    @PostMapping("/client/createItem")
    public Mono<Item> createItem(@RequestBody Item item){

        Mono<Item> itemMono = Mono.just(item);
       return webClient.post().uri(baseUrl+"/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(itemMono, Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Created item is : ");

    }

     @PostMapping("/client/exchange/createItem")
    public Mono<Item> createItemExchange(@RequestBody Item item){

        Mono<Item> itemMono = Mono.just(item);
        return webClient.post().uri(baseUrl+"/v1/items").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(itemMono, Item.class))
                .exchange().flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
                .log("Created item is : ");

    }

    @DeleteMapping("/client/deleteItem/{id}")
    public Mono<Void> deleteItem(@PathVariable String id){

        return webClient.delete().uri(baseUrl +"/v1/items/{id}",id)
                .retrieve()
                .bodyToMono(Void.class)
                .log("Deleted Item is");
    }

   @GetMapping("/client/retrieve/error")
    public Flux<Item> errorRetrieve(){

        return webClient.get()
                .uri(baseUrl +"/v1/items/runtimeException")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {

                    Mono<String> errorMono = clientResponse.bodyToMono(String.class);
                    return errorMono.flatMap((errorMessage) -> {
                        log.error("The error Message is : " + errorMessage);
                        throw  new RuntimeException(errorMessage);
                    });
                })
               .bodyToFlux(Item.class);
    }


    @GetMapping("/client/exchange/error")
    public Flux<Item> errorExchange(){

        return webClient.get()
                .uri(baseUrl+"/v1/items/runtimeException")
                .exchange()
                .flatMapMany((clientResponse -> {

                    if(clientResponse.statusCode().is5xxServerError()){

                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error Message in errorExchange : " + errorMessage);
                                    throw  new RuntimeException(errorMessage);
                                });
                    }else{
                        return clientResponse.bodyToFlux(Item.class);
                    }

                }));

    }

    @PutMapping("/client/updateItem/{id}")
    public Mono<Item> updateItem(@PathVariable String id,
                                 @RequestBody Item item){

        Mono<Item> itemBody = Mono.just(item);

        return webClient.put().uri(baseUrl+"/v1/items/{id}",id)
                .body(itemBody, Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Updated Item is : ");
    }
}
