package com.YoutubeTools.Service;

import com.YoutubeTools.Model.SearchVideo;
import com.YoutubeTools.Model.Video;
import com.YoutubeTools.Model.VideoDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final WebClient.Builder webClientBuilder;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.base.url}")
    private String baseUrl;

    @Value("${youtube.api.max.related.videos}")
    private int maxRelatedVideos;

    public SearchVideo searchVideos(String videoTitle) {
        List<String> videoIds = searchForVideoIds(videoTitle);
        if (videoIds.isEmpty()) {
            return SearchVideo.builder()
                    .primaryVideo(null)
                    .relatedVideos(Collections.emptyList())
                    .build();
        }

        String primaryVideoId = videoIds.get(0);
        List<String> relatedVideoIds = videoIds.subList(1, Math.min(videoIds.size(), maxRelatedVideos + 1));

        Video primaryVideo = getVideoById(primaryVideoId);
        List<Video> relatedVideos = new ArrayList<>();

        for (String id : relatedVideoIds) {
            Video video = getVideoById(id);
            if (video != null) {
                relatedVideos.add(video);
            }
        }

        return SearchVideo.builder()
                .primaryVideo(primaryVideo)
                .relatedVideos(relatedVideos)
                .build();
    }

    private List<String> searchForVideoIds(String videoTitle) {
        SearchApiResponse response = webClientBuilder.baseUrl(baseUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("part", "snippet")
                        .queryParam("q", videoTitle)
                        .queryParam("type", "video")
                        .queryParam("maxResults", maxRelatedVideos)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(SearchApiResponse.class)
                .block();

        if (response == null || response.items == null) {
            return Collections.emptyList();
        }

        List<String> videoIds = new ArrayList<>();
        for (SearchItem item : response.items) {
            if (item.id != null && item.id.videoId != null) {
                videoIds.add(item.id.videoId);
            }
        }
        return videoIds;
    }

    private Video getVideoById(String videoId) {
        VideoApiResponse response = webClientBuilder.baseUrl(baseUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("part", "snippet")
                        .queryParam("id", videoId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(VideoApiResponse.class)
                .block();

        if (response == null || response.items == null || response.items.isEmpty()) {
            return null;
        }

        Snippet snippet = response.items.get(0).snippet;

        return Video.builder()
                .id(videoId)
                .title(snippet.title)
                .channelTitle(snippet.channelTitle)
                .tags(snippet.tags == null ? Collections.emptyList() : snippet.tags)
                .build();
    }

    public VideoDetails getVideoDetails(String videoId) {
        // Fetch video from API
        VideoApiResponse response = webClientBuilder.baseUrl(baseUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("part", "snippet")
                        .queryParam("id", videoId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(VideoApiResponse.class)
                .block();

        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            return null; // Video not found
        }

        Snippet snippet = response.getItems().get(0).getSnippet();

        // Map API snippet to VideoDetails
        return VideoDetails.builder()
                .id(videoId)
                .Title(snippet.getTitle())
                .description(snippet.getDescription())
                .tags(snippet.getTags() == null ? Collections.emptyList() : snippet.getTags())
                .thumbnails(snippet.getThumbnails() != null ? snippet.getThumbnails().getBestThumbnailUrl() : "")
                .ChannelTitle(snippet.getChannelTitle())
                .publishedAt(snippet.getPublishedAt())
                .build();
    }



    // === Response Mapping Classes ===
    @Data
    static class SearchApiResponse {
        private List<SearchItem> items;
    }

    @Data
    static class SearchItem {
        private Id id;
        private Snippet snippet;
    }

    @Data
    static class Id {
        private String videoId;
    }

    @Data
    static class VideoApiResponse {
        private List<VideoItem> items;
    }

    @Data
    static class VideoItem {
        private Snippet snippet;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Snippet {
        private String title;
        private String description;
        private String channelTitle;
        private String publishedAt;
        private List<String> tags;
        private Thumbnails thumbnails;
    }

    @Data
    static class Thumbnails {
        private ThumbnailDetails maxres;
        private ThumbnailDetails high;
        private ThumbnailDetails medium;
        private ThumbnailDetails _default;

        String getBestThumbnailUrl() {
            if (maxres != null) return maxres.url;
            if (high != null) return high.url;
            if (medium != null) return medium.url;
            return _default != null ? _default.url : "";
        }
    }

    @Data
    static class ThumbnailDetails {
        private String url;
        private int width;
        private int height;
    }
}
