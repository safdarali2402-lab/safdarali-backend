package com.YoutubeTools.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetails {
    private String id;
    private String Title;
    private String description;
    private List<String> tags;
    private String thumbnails;
    private String ChannelTitle;
    private String publishedAt;
}
