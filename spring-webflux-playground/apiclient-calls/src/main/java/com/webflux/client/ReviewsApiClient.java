package com.webflux.client;

import com.webflux.domain.Review;
import com.webflux.exception.ReviewsClientException;
import com.webflux.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class ReviewsApiClient {

    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public ReviewsApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<List<Review>> retrieveReviews(Long movieId){

        var url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand().toString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new ReviewsClientException(response)));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new ReviewsServerException(response)));
                }))
                .bodyToFlux(Review.class)
                .collectList();

    }




}
