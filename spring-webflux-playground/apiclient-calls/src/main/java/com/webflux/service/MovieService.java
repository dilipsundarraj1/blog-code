package com.webflux.service;

import com.webflux.client.MoviesInfoApiClient;
import com.webflux.client.RevenueApiClient;
import com.webflux.client.ReviewsApiClient;
import com.webflux.domain.Movie;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class MovieService {

    private MoviesInfoApiClient moviesInfoApiClient;
    private ReviewsApiClient reviewsApiClient;
    private RevenueApiClient revenueApiClient;

    public MovieService(MoviesInfoApiClient moviesInfoApiClient, ReviewsApiClient reviewsApiClient, RevenueApiClient revenueApiClient) {
        this.moviesInfoApiClient = moviesInfoApiClient;
        this.reviewsApiClient = reviewsApiClient;
        this.revenueApiClient = revenueApiClient;
    }

    public Mono<Movie> getMovie(Long movieId){

        var movieInfoMono = moviesInfoApiClient
                .retrieveMovieInfo(movieId);

        var reviews = reviewsApiClient.retrieveReviews(movieId);

        var revenue  = revenueApiClient.retrieveRevenue(movieId);

        return Mono.zip(movieInfoMono, reviews, revenue)
                .map(objects -> new Movie(objects.getT1(),objects.getT2(), objects.getT3()));

    }

}
