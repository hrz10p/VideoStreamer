package com.yerlal.VideoStreamerAPI.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VideoDTO {
    private MultipartFile video;
    private MultipartFile preview;
    private String description;
}
