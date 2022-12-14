package com.webflux.client;

import com.webflux.domain.MovieInfo;
import com.webflux.exception.MoviesInfoClientException;
import com.webflux.exception.MoviesInfoServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoApiClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(Long movieId) {

        var url = moviesInfoUrl.concat("/{id}");

        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException("There is no MovieInfo available for the passed in Id : " + movieId, clientResponse.statusCode().value()));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoClientException(response, clientResponse.statusCode().value())));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoServerException(response)));
                }))
                .bodyToMono(MovieInfo.class)
                .log();

    }


    public Mono<MovieInfo> retrieveMovieInfo_exchange(String movieId) {

        var url = moviesInfoUrl.concat("/{id}");

        return webClient.get()
                .uri(url, movieId)
                .exchangeToMono(clientResponse -> {

                    switch (clientResponse.statusCode()) {
                        case OK:
                            return clientResponse.bodyToMono(MovieInfo.class);
                        case NOT_FOUND:
                            return Mono.error(new MoviesInfoClientException("There is no MovieInfo available for the passed in Id : " + movieId, clientResponse.statusCode().value()));
                        default:
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(response -> Mono.error(new MoviesInfoServerException(response)));
                    }
                })
                .log();

    }


}

