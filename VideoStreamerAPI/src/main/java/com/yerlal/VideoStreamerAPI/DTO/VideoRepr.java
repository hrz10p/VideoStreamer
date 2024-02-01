package com.yerlal.VideoStreamerAPI.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class VideoRepr implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public String id;
    public String contentType;
    public String previewPath;
    public String description;
}
