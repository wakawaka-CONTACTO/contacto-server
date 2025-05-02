package org.kiru.user.portfolio.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class InstagramProfileResponse {
    @JsonProperty("graphql")
    private Graphql graphql;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    public static class Graphql {
        private User user;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        public static class User {
            @JsonProperty("edge_owner_to_timeline_media")
            private EdgeOwnerToTimelineMedia media;

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            public static class EdgeOwnerToTimelineMedia {
                private List<Edge> edges;

                @JsonIgnoreProperties(ignoreUnknown = true)
                @Getter
                public static class Edge {
                    private Node node;

                    @JsonIgnoreProperties(ignoreUnknown = true)
                    @Getter
                    public static class Node {
                        @JsonProperty("display_url")
                        private String displayUrl;
                    }
                }
            }
        }
    }
}
