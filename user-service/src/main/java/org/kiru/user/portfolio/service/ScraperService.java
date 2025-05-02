package org.kiru.user.portfolio.service;

import org.kiru.user.portfolio.service.dto.InstagramProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScraperService {

    private final WebClient webClient;

    public ScraperService(WebClient instagramWebClient) {
        this.webClient = instagramWebClient;
    }

    public Mono<List<String>> fetchImageUrls(String username) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{username}/")
                        .queryParam("__a", "1")
                        .build(username))
                .retrieve()
                .bodyToMono(InstagramProfileResponse.class)
                .map(resp -> resp.getGraphql()
                        .getUser()
                        .getMedia()
                        .getEdges()
                        .stream()
                        .map(edge -> edge.getNode().getDisplayUrl())
                        .collect(Collectors.toList()));
    }
}