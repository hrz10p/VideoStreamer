package com.yerlal.VideoStreamerAPI.models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "video_data")
@Data
public class VideoData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String videoName;
    private String videoPath;
    private String videoType;
    private String description;
    private String previewPath;
    private Long videoSize;

    @Override
    public String toString() {
        return "{" +
            " videoName='" + getVideoName() + "'" +
            ", videoPath='" + getVideoPath() + "'" +
            ", videoType='" + getVideoType() + "'" +
            ", videoSize='" + getVideoSize() + "'" +
            "}";
    }
}
