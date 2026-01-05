package com.musicwise.service;

import com.musicwise.dto.MixResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MixService {

    private static final double DEFAULT_MUSIC_VOLUME = 0.2;
    private final RestTemplate restTemplate = new RestTemplate();

    public MixResponse mix(MultipartFile ttsFile, MultipartFile musicFile, String musicUrl, Double musicVolume, Double targetDuration) throws IOException {
        boolean hasMusicUpload = musicFile != null && !musicFile.isEmpty();
        if (ttsFile == null || ttsFile.isEmpty() || (!hasMusicUpload && !StringUtils.hasText(musicUrl))) {
            throw new IllegalArgumentException("tts or music missing");
        }
        double volume = musicVolume != null && musicVolume > 0 ? musicVolume : DEFAULT_MUSIC_VOLUME;
        File ttsTmp = null;
        File musicTmp = null;
        File outTmp = null;
        try {
            ttsTmp = writeTempFile("tts_", ".wav", ttsFile.getBytes());
            if (hasMusicUpload) {
                String suffix = resolveSuffix(Objects.requireNonNull(musicFile.getOriginalFilename()));
                musicTmp = writeTempFile("music_", suffix, musicFile.getBytes());
            } else {
                musicTmp = downloadToTemp("music_", ".mp3", musicUrl);
            }
            outTmp = Files.createTempFile("mixed_", ".mp3").toFile();
            runFfmpeg(ttsTmp, musicTmp, outTmp, volume, targetDuration);
            byte[] mixedBytes = Files.readAllBytes(outTmp.toPath());
            String base64 = Base64.getEncoder().encodeToString(mixedBytes);
            return new MixResponse(base64, "audio/mpeg");
        } finally {
            deleteQuietly(ttsTmp);
            deleteQuietly(musicTmp);
            deleteQuietly(outTmp);
        }
    }

    private void runFfmpeg(File tts, File music, File out, double volume, Double targetDuration) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(tts.getAbsolutePath());
        command.add("-i");
        command.add(music.getAbsolutePath());

        StringBuilder filter = new StringBuilder();
        filter.append("[1:a]volume=").append(volume);
        filter.append(",aloop=loop=-1:size=0");
        if (targetDuration != null && targetDuration > 0) {
            filter.append(",atrim=start=0:d=").append(targetDuration);
        }
        filter.append(",asetpts=PTS-STARTPTS");
        filter.append(";[0:a][1:a]amix=inputs=2:duration=first:dropout_transition=2");

        command.add("-filter_complex");
        command.add(filter.toString());
        command.add("-ar");
        command.add("44100");
        command.add("-b:a");
        command.add("192k");
        command.add(out.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            StreamUtils.copy(process.getInputStream(), baos);
        }
        try {
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("ffmpeg exit code " + exit);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffmpeg interrupted", e);
        }
    }

    private File downloadToTemp(String prefix, String suffix, String url) throws IOException {
        byte[] data = restTemplate.getForObject(URI.create(url), byte[].class);
        if (data == null || data.length == 0) {
            throw new IOException("empty music content");
        }
        return writeTempFile(prefix, suffix, data);
    }

    private File writeTempFile(String prefix, String suffix, byte[] data) throws IOException {
        File file = Files.createTempFile(prefix, suffix).toFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
        return file;
    }

    private String resolveSuffix(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return ".mp3";
        }
        String suffix = filename.substring(filename.lastIndexOf('.'));
        if (suffix.length() > 6) {
            return ".mp3";
        }
        return suffix;
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException ignored) {
                // ignore
            }
        }
    }
}
