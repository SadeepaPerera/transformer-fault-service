package com.example.transformer_fault_service.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ImageDownloadService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public DownloadResult downloadImage(String imageUrl) throws IOException, InterruptedException {
        URI uri = toUri(imageUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "transformer-fault-service/1.0")
                .GET()
                .build();

        HttpResponse<byte[]> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new IOException("Failed to download image. HTTP status: " + status);
        }

        String contentType = response.headers()
                .firstValue("content-type")
                .orElse("application/octet-stream");

        byte[] bytes = response.body();
        if (bytes == null || bytes.length == 0) {
            throw new IOException("Downloaded image is empty.");
        }

        return new DownloadResult(bytes, contentType);
    }

    private URI toUri(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IOException("imageUrl is empty.");
        }
        try {
            URI uri = new URI(imageUrl);
            if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http")
                    && !uri.getScheme().equalsIgnoreCase("https"))) {
                throw new IOException("imageUrl must start with http:// or https://");
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new IOException("Invalid imageUrl: " + imageUrl, e);
        }
    }

    public record DownloadResult(byte[] bytes, String contentType) {}
}
