package com.yerlal.VideoStreamerAPI.Controllers;

import com.yerlal.VideoStreamerAPI.DTO.VideoRepr;
import com.yerlal.VideoStreamerAPI.Services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ControllerPages {

    private final VideoService videoService;
    @Autowired
    public ControllerPages(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/videos")
    public String getVideos(Model model) {

        model.addAttribute("videos", videoService.getAllReprs());
        return "index";
    }

    @GetMapping("/upload")
    public String uploadVideo() {
        return "upload";
    }

    @GetMapping("/watch/{id}")
    public String watchVideo(@PathVariable String id, Model model) {
        VideoRepr videoRepr = videoService.getByID(id);

        model.addAttribute("video", videoRepr);
        return "videoPage";
    }
}
