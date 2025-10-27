package com.YoutubeTools.Controller;

import com.YoutubeTools.Model.SearchVideo;
import com.YoutubeTools.Service.YoutubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/youtube")
public class YoutubeTagsController {

    @Autowired
    private YoutubeService youtubeService;

    @Value("${youtube.api.key}")
    private String apiKey;

    //   helper method declared OUTSIDE other methods
    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @PostMapping("/search")
    public String videoTags(@RequestParam("videoTitle") String videoTitle, Model model) {

        if (!isApiKeyConfigured()) {
            model.addAttribute("error", "API key is not configured");
            return "home";
        }

        if (videoTitle == null || videoTitle.isEmpty()) {
            model.addAttribute("error", "Video title is required");
            return "home";
        }

        try {
            SearchVideo result = youtubeService.searchVideos(videoTitle);
            model.addAttribute("primaryVideo", result.getPrimaryVideo());
            model.addAttribute("relatedVideos", result.getRelatedVideos());
            return "home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "home";
        }
    }
}
