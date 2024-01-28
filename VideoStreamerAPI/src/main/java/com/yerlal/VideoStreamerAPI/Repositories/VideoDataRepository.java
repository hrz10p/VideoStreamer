package com.yerlal.VideoStreamerAPI.Repositories;

import com.yerlal.VideoStreamerAPI.models.VideoData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoDataRepository extends JpaRepository<VideoData, UUID> {
}