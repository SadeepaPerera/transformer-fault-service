package com.example.transformer_fault_service.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ImageDownloadService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public DownloadResult downloadImage(String imageUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new IOException("Failed to download image. HTTP status: " + status);
        }

        String contentType = response.headers()
                .firstValue("content-type")
                .orElse("unknown");

        byte[] bytes = response.body();

        return new DownloadResult(bytes, contentType);
    }

    public record DownloadResult(byte[] bytes, String contentType) {}
}