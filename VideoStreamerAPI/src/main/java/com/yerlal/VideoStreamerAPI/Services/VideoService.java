package com.yerlal.VideoStreamerAPI.Services;

import com.yerlal.VideoStreamerAPI.DTO.VideoDTO;
import com.yerlal.VideoStreamerAPI.DTO.VideoPart;
import com.yerlal.VideoStreamerAPI.DTO.VideoRepr;
import com.yerlal.VideoStreamerAPI.Repositories.VideoDataRepository;
import com.yerlal.VideoStreamerAPI.models.VideoData;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

@Service
@Slf4j
public class VideoService {

    private final String dir = "VideoStreamerAPI/videos";

    private final String prevdir = "VideoStreamerAPI/previews";

    private final VideoDataRepository videoDataRepository;

    @Autowired
    public VideoService(VideoDataRepository videoDataRepository) {
        this.videoDataRepository = videoDataRepository;
    }

    @Cacheable(value = "all_video_reprs", key = "'all'")
    public List<VideoRepr> getAllReprs() {
        List<VideoData> videoDataList = videoDataRepository.findAll();
        log.info("{}",videoDataList);
        return videoDataList.stream().map(this::getVideoRepr).toList();
    }

    public ByteArrayResource getPreview(String id) throws IOException {
        VideoData data = videoDataRepository.findById(UUID.fromString(id)).orElseThrow();
        Path path = Path.of(data.getPreviewPath());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    private VideoRepr getVideoRepr(VideoData videoData) {
        VideoRepr videoRepr = new VideoRepr();
        videoRepr.setId(videoData.getId().toString());
        videoRepr.setName(videoData.getVideoName());
        videoRepr.setContentType(videoData.getVideoType());
        videoRepr.setDescription(videoData.getDescription());
        videoRepr.setPreviewPath(videoData.getPreviewPath());
        return videoRepr;
    }

    @Cacheable(value = "video_reprs", key = "#id")
    public VideoRepr getByID(String id) {
        Optional<VideoData> data = videoDataRepository.findById(UUID.fromString(id));
        if(data.isEmpty()) {
            return null;
        }
        VideoData videoData = data.get();
        return getVideoRepr(videoData);
    }

    @Transactional
    @CacheEvict(value = "all_video_reprs", allEntries = true)
    public void save(VideoDTO dto) throws IOException {
        VideoData data = new VideoData();

        data.setDescription(dto.getDescription());
        data.setVideoSize(dto.getVideo().getSize());
        data.setVideoName(dto.getVideo().getOriginalFilename());
        data.setVideoType(dto.getVideo().getContentType());

        VideoData saved = videoDataRepository.save(data);

        String uniqueFileName = saved.getId().toString() + dto.getVideo().getOriginalFilename();


        Path directory = Path.of(dir);

        Path filePath = directory.resolve(uniqueFileName);

        saved.setVideoPath(filePath.toString());

        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        Files.copy(dto.getVideo().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


        if(dto.getPreview() == null) {
            Path previewPath = Path.of(prevdir).resolve( "default.png");
            saved.setPreviewPath(previewPath.toString());
            return;
        }

        Path previewPath = Path.of(prevdir).resolve(saved.getId().toString() + dto.getPreview().getOriginalFilename());

        Files.copy(dto.getPreview().getInputStream(), previewPath, StandardCopyOption.REPLACE_EXISTING);

        saved.setPreviewPath(previewPath.toString());

        videoDataRepository.save(saved);
    }

    public byte[] streamVideo(String id) throws IOException {
        VideoData data = videoDataRepository.findById(UUID.fromString(id)).orElseThrow();
        Path path = Path.of(data.getVideoPath());
        return Files.readAllBytes(path);
    }

    public VideoPart getVideoPart(String id, HttpRange range) {


        VideoData data = videoDataRepository.findById(UUID.fromString(id)).orElse(null);
        assert data != null;
        if(data == null) {
            return null;
        }


        Path filePath = Path.of(data.getVideoPath());


        if (!Files.exists(filePath)) {
            log.error("File {} not found", filePath);
            return null;
        }


        try {
            long fileSize = Files.size(filePath);
            long chunkSize = fileSize / 100;
            if (range == null) {

                VideoPart part = new VideoPart();

                part.setFileSize(fileSize);
                part.setRangeStart(0);
                part.setRangeEnd(fileSize);
                part.setContentType(data.getVideoType());
                part.setResponseBody(out -> Files.newInputStream(filePath).transferTo(out));
                return part;
            }

            long rangeStart = range.getRangeStart(0);
            long rangeEnd = rangeStart + chunkSize; // range.getRangeEnd(fileSize);
            if (rangeEnd >= fileSize) {
                rangeEnd = fileSize - 1;
            }
            long finalRangeEnd = rangeEnd;

            VideoPart part = new VideoPart();

            part.setFileSize(fileSize);
            part.setRangeStart(rangeStart);
            part.setRangeEnd(rangeEnd);
            part.setContentType(data.getVideoType());

            InputStream inputStream = Files.newInputStream(filePath);
            inputStream.skip(rangeStart);
            byte[] bytes = inputStream.readNBytes((int) ((finalRangeEnd - rangeStart) + 1));

            part.setResponseBody(out -> out.write(bytes));

            return part;

        } catch (IOException e) {
            log.error("Error while reading file {} ", filePath);
            e.printStackTrace();
        }
        return null;
    }
}
