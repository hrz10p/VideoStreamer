package com.yerlal.VideoStreamerAPI.DTO;

import lombok.Data;

@Data
public class VideoRepr {
    public String name;
    public String id;
    public String contentType;
    public String previewPath;
    public String description;
}
