package com.example.demo;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.MessageContentResponse;
import lombok.Value;

public class ComFunct {
    
    public static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path).build()
                .toUriString();
    }
    
    // not use
    
    private void exec(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            controler.addLogInfo("result: {} =>  {}", Arrays.toString(args), i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            controler.addLogInfo("Interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
    
    private DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
        controler.addLogInfo("Got content-type: {}", responseBody);

        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(responseBody.getStream(), outputStream);
            controler.addLogInfo("Saved {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
        Path tempFile = OrderApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(
                tempFile,
                ComFunct.createUri("/downloaded/" + tempFile.getFileName()));
    }
    
    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
    }
    
    private final OrderController controler;
    
    public ComFunct(OrderController _controler) {
        controler = _controler;
    }
    
}
