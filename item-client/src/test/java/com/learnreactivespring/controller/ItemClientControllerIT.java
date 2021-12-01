package com.learnreactivespring.controller;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.learnreactivespring.domain.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestPropertySource(
        properties = {
                "restClient.baseUrl=http://localhost:8084"
   }
)

public class ItemClientControllerIT {

   @ConfigureWireMock
   Options options = wireMockConfig()
           .port(8084)
          .notifier(new ConsoleNotifier(true))
           .extensions(new ResponseTemplateTransformer(true));

    @InjectServer
    WireMockServer wireMockServer;

    @Autowired
    WebTestClient webTestClient;


    @AfterEach
    void cleanUp(){
        WireMock.reset();
    }

    @Test
    void retreiveAll() {

        stubFor(get(urlEqualTo("/v1/items"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("items.json")));

        webTestClient.get().uri("/client/retrieve").exchange()
                 .expectStatus().isOk()
                .expectBodyList(Item.class)
                 .consumeWith(list-> {
                      List<Item> items= list.getResponseBody();
                      assertTrue(items.size()==5);

                 });
        verify(exactly(1), getRequestedFor(urlEqualTo("/v1/items")));

    }

    @Test
    void exchangeAll() {

        stubFor(get(urlEqualTo("/v1/items"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("items.json")));

        webTestClient.get().uri("/client/exchange").exchange()
                .expectStatus().isOk()
                .expectBodyList(Item.class)
                .consumeWith(list-> {
                    List<Item> items= list.getResponseBody();
                    assertTrue(items.size()==5);

                });
        verify(exactly(1), getRequestedFor(urlEqualTo("/v1/items")));


    }

    @Test
    void retreiveOneItem() {

       stubFor(get(urlEqualTo("/v1/items/1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("item.json")));

       webTestClient.get().uri("/client/retrieve/singleItem/1").exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .consumeWith(item -> {
                     Item  itemFound= item.getResponseBody();
                    assertTrue(itemFound.getId().equals("1"));
              });
      verify(exactly(1), getRequestedFor(urlEqualTo("/v1/items/1")));
    }
    @Test
    void exchangeOneItem() {

        stubFor(get(urlEqualTo("/v1/items/ABC"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("itemABC.json")));

        webTestClient.get().uri("/client/exchange/singleItem").exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .consumeWith(item -> {
                    Item  itemFound= item.getResponseBody();
                    assertTrue(itemFound.getId().equals("ABC"));
                });
        verify(exactly(1), getRequestedFor(urlEqualTo("/v1/items/ABC")));
    }

    @Test
    void createOneItem() {
        Item itemToAdd = new Item("ABMac", "Mac Book Pro", 600.00);
        String itemJson = "{\n" +
                "  \"id\": \"ABMac\",\n" +
                "  \"description\": \"Mac Book Pro\",\n" +
                "  \"price\": 600.00\n" +
                "}";
        stubFor(post(urlEqualTo("/v1/items"))
                .withRequestBody(equalToJson(itemJson))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("addOneItem.json")));

        webTestClient.post().uri("/client/createItem")
                .bodyValue(itemToAdd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .consumeWith(item -> {
                    Item  itemFound= item.getResponseBody();
                    assertTrue(itemFound.getDescription().equals("Mac Book Pro"));
                    assertTrue(itemFound.getId().equals("ABMac"));
                });
        verify(exactly(1), postRequestedFor(urlEqualTo("/v1/items")));
    }

    @Test
    void createOneItem_responseTemplate() {
        Item itemToAdd = new Item(null, "iphone 14", 1000.00);

        stubFor(post(urlEqualTo("/v1/items"))
                .withRequestBody(matchingJsonPath("$.description", equalTo("iphone 14")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("item-template.json")));

        webTestClient.post().uri("/client/createItem")
                .bodyValue(itemToAdd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .consumeWith(item -> {
                    Item  itemFound= item.getResponseBody();
                    assertTrue(itemFound.getDescription().equals("iphone 14"));
                    assertTrue(itemFound.getId()!=null);
                    assertTrue(itemFound.getPrice()==1000.0);
                });
        verify(exactly(1), postRequestedFor(urlEqualTo("/v1/items")));
    }

    @Test
    void createOneItemUsingExchange() {
        Item itemToAdd = new Item("ABMac", "Mac Book Pro", 600.00);
        String itemJson = "{\n" +
                "  \"id\": \"ABMac\",\n" +
                "  \"description\": \"Mac Book Pro\",\n" +
                "  \"price\": 600.00\n" +
                "}";
        stubFor(post(urlEqualTo("/v1/items"))
                .withRequestBody(equalToJson(itemJson))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("addOneItem.json")));

        webTestClient.post().uri("/client/exchange/createItem")
                .bodyValue(itemToAdd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .consumeWith(item -> {
                    Item  itemFound= item.getResponseBody();
                    assertTrue(itemFound.getDescription().equals("Mac Book Pro"));
                    assertTrue(itemFound.getId().equals("ABMac"));
                });
        verify(exactly(1),postRequestedFor(urlEqualTo("/v1/items")));
    }

    @Test
    void deleteItem(){
        Item itemToAdd = new Item("ABMac", "Mac Book Pro", 600.00);

        /* stubFor(post(urlPathEqualTo("/v1/items"))
               .withRequestBody(matchingJsonPath("$.id", equalTo("ABMac")))
                .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("item.json")
         ));

         webTestClient.post().uri("/client/exchange/createItem")
                  .bodyValue(itemToAdd)
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(Item.class);
           */
         stubFor(delete(urlPathEqualTo("/v1/items/ABMac"))
                  .willReturn(WireMock.aResponse()
                   .withStatus(HttpStatus.OK.value())
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("Deleted Item is ABMac")
                  ));
        webTestClient.delete().uri("/client/deleteItem/ABMac")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
        verify(exactly(1),deleteRequestedFor(urlPathEqualTo("/v1/items/ABMac")));
    }

    @Test
    void errorRetrieve(){
        stubFor(get(urlEqualTo("/v1/items/runtimeException"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                         .withBodyFile("500-error.json")));
        webTestClient.get().uri("/client/retrieve/error")
                .exchange().expectStatus().is5xxServerError();
        verify(exactly(1),getRequestedFor(urlEqualTo("/v1/items/runtimeException")));
      }

    @Test
    void errorExchange(){
        stubFor(get(urlEqualTo("/v1/items/runtimeException"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBodyFile("500-error.json")));
        webTestClient.get().uri("/client/exchange/error")
                .exchange().expectStatus().is5xxServerError();
        verify(exactly(1),getRequestedFor(urlEqualTo("/v1/items/runtimeException")));
    }

    @Test
    void updateItem(){
         Item item = new Item("1", "Samsung TV", 500.0);
        stubFor(put(urlPathMatching("/v1/items/[0-9]+"))
                .withRequestBody(matchingJsonPath("$.price", equalTo("500.0")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("updateItem-template.json")));
        webTestClient.put().uri("/client/updateItem/1")
                .bodyValue(item)
                .exchange().expectStatus().isOk();
       verify(exactly(1),putRequestedFor(urlPathMatching("/v1/items/[0-9]+")));
    }
}

