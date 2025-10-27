package com.YoutubeTools.Controller;

import com.YoutubeTools.Service.ThumbnailService;
import com.YoutubeTools.Service.YoutubeService;
import com.YoutubeTools.Model.VideoDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeService youtubeService;
    private final ThumbnailService thumbnailService;

    // Display form to enter video URL or ID
    @GetMapping("/youtube/video-details")
    public String showVideoForm() {
        return "video-details"; // Thymeleaf template
    }

    // Handle form submission and fetch video details
    @PostMapping("/youtube/video-details")
    public String fetchVideoDetails(@RequestParam("videoUrlOrId") String videoUrlOrId, Model model) {
        String videoId = extractVideoId(videoUrlOrId);

        if (videoId == null || videoId.isEmpty()) {
            model.addAttribute("error", "Invalid YouTube URL or ID");
            return "video-details";
        }

        VideoDetails details = youtubeService.getVideoDetails(videoId);

        if (details == null) {
            model.addAttribute("error", "Video not found or unable to fetch details");
            return "video-details";
        }

        model.addAttribute("videoDetails", details); // ✅ matches template
        return "video-details";
    }

    // Helper method to extract YouTube video ID from URL or ID directly
    private String extractVideoId(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        if (input.contains("youtube.com/watch?v=")) {
            return input.substring(input.indexOf("v=") + 2, Math.min(input.indexOf("v=") + 13, input.length()));
        } else if (input.contains("youtu.be/")) {
            return input.substring(input.lastIndexOf("/") + 1, Math.min(input.lastIndexOf("/") + 12, input.length()));
        } else {
            return input;
        }
    }
}
