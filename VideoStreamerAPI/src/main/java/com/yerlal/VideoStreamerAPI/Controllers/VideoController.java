package com.yerlal.VideoStreamerAPI.Controllers;

import com.yerlal.VideoStreamerAPI.DTO.VideoDTO;
import com.yerlal.VideoStreamerAPI.DTO.VideoPart;
import com.yerlal.VideoStreamerAPI.Services.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/video")
@Slf4j
public class VideoController {

    private final VideoService videoService;
    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadVideo(VideoDTO dto) {
        log.info(dto.getDescription());
        try {
            videoService.save(dto);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<byte[]> getVideo(@PathVariable String id) {
        try {
            return ResponseEntity.ok(videoService.streamVideo(id));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/partial/{id}")
    public ResponseEntity<StreamingResponseBody> streamVideo(@RequestHeader(value = "Range", required = false) String httpRangeHeader,
                                                             @PathVariable("id") String id) {

        List<HttpRange> httpRangeList = HttpRange.parseRanges(httpRangeHeader);
        VideoPart videoPart = videoService.getVideoPart(id, !httpRangeList.isEmpty() ? httpRangeList.get(0) : null);
        if (videoPart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        long byteLength = videoPart.getRangeEnd() - videoPart.getRangeStart() + 1;
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(httpRangeList.size() > 0 ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                .header("Content-Type", videoPart.getContentType())
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", Long.toString(byteLength));

        if (httpRangeList.size() > 0) {
            builder.header("Content-Range",
                    "bytes " + videoPart.getRangeStart() +
                            "-" + videoPart.getRangeEnd() +
                            "/" + videoPart.getFileSize());
        }

        return builder.body(videoPart.getResponseBody());
    }


    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> previewVideo(@PathVariable("id") String id) {
        try {
            Resource imagePreview = videoService.getPreview(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imagePreview);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }



}
