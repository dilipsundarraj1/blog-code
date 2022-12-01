package com.webflux.client;


import com.webflux.domain.Revenue;
import com.webflux.exception.RevenueClientException;
import com.webflux.exception.RevenueServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RevenueApiClient {

    private WebClient webClient;

    @Value("${restClient.revenueUrl}")
    private String revenueUrl;

    public RevenueApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Revenue> retrieveRevenue(Long movieId){

        var url = UriComponentsBuilder.fromHttpUrl(revenueUrl)
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
                            .flatMap(response -> Mono.error(new RevenueClientException(response)));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new RevenueServerException(response)));
                }))
                .bodyToMono(Revenue.class);

    }
}
