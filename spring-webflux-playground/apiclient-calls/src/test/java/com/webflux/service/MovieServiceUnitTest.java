package com.webflux.service;

import com.webflux.client.MoviesInfoApiClient;
import com.webflux.client.RevenueApiClient;
import com.webflux.client.ReviewsApiClient;
import com.webflux.domain.MovieInfo;
import com.webflux.domain.Revenue;
import com.webflux.domain.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieServiceUnitTest {

    @Mock
    MoviesInfoApiClient moviesInfoApiClient;

    @Mock
    RevenueApiClient revenueApiClient;

    @Mock
    ReviewsApiClient reviewsApiClient;

    @InjectMocks
    MovieService movieService;

    @Test
    void retrieveMovie(){

        var movieId = 1L;
        when(moviesInfoApiClient.retrieveMovieInfo(movieId))
                .thenReturn(Mono.just(new MovieInfo(movieId, "Avengers", 2005, List.of("chris evans", "robert downmy jr"), LocalDate.parse("2012-11-09"))));

        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        when(reviewsApiClient.retrieveReviews(movieId))
                .thenReturn(Mono.just((reviewList)));

        when(revenueApiClient.retrieveRevenue(movieId))
                .thenReturn(Mono.just(new Revenue(movieId, BigDecimal.valueOf(1000000000), BigDecimal.valueOf(2000000000))));


        var movie = movieService.getMovie(movieId);

        StepVerifier
                .create(movie)
                .assertNext(movie1 ->{
                 var reviews =        movie1.getReviewList();
                 assert reviews.size() == 3;

                })
                .verifyComplete();

    }
}
