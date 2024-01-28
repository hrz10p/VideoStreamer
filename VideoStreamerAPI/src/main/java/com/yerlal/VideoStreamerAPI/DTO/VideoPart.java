package com.yerlal.VideoStreamerAPI.DTO;

import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Data
public class VideoPart {
    private StreamingResponseBody responseBody;

    private long fileSize;

    private long rangeStart;

    private long rangeEnd;

    private String contentType;
}
