package org.teamapps.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.teamapps.media.media.MediaFileUtils;
import org.teamapps.media.pdf.PdfUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.Assert.*;

/** Real compatibility checks for the dependency upgrade, also runnable on the application's classpath. */
public class DependencyCompatibilityTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void existingPdfApiCountsPagesAndProducesReadableThumbnail() throws Exception {
        File pdf = temporary.newFile("document.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(144, 144));
            document.addPage(page);
            document.addPage(new PDPage());
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setNonStrokingColor(Color.BLUE);
                content.addRect(0, 0, 144, 144);
                content.fill();
            }
            document.save(pdf);
        }
        assertEquals(2, PdfUtils.getPdfPageCount(pdf));
        byte[] thumbnail = PdfUtils.createPdfThumbnail(pdf, 72);
        assertNotNull(thumbnail);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(thumbnail));
        assertEquals(144, image.getWidth());
        assertEquals(144, image.getHeight());
        assertTrue(new Color(image.getRGB(72, 72)).getBlue() > 200);
    }

    @Test public void existingImageApiPreservesAspectRatioAndCommonsIoRemainsCompatible() throws Exception {
        File image = temporary.newFile("image.png");
        ImageIO.write(new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB), "png", image);
        File thumbnail = MediaFileUtils.createFilePreviewThumbnail(image, image.getName());
        assertNotNull(thumbnail);
        try {
            BufferedImage result = ImageIO.read(thumbnail);
            assertEquals(200, result.getWidth());
            assertEquals(100, result.getHeight());
        } finally { Files.deleteIfExists(thumbnail.toPath()); }
        assertEquals("1 MB", MediaFileUtils.getFileSize(1024 * 1024));
    }

    @Test public void jerseyInjectionAndJsonProvidersWorkTogether() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            try (exchange) {
                byte[] body = exchange.getRequestBody().readAllBytes();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
        });
        server.start();
        Client client = null;
        try {
            client = ClientBuilder.newBuilder().register(JacksonFeature.class).register(MultiPartFeature.class).build();
            try (Response response = client.target("http://127.0.0.1:" + server.getAddress().getPort() + "/echo")
                    .request().post(Entity.json(Map.of("greeting", "Grüße", "count", 2)))) {
                assertEquals(200, response.getStatus());
                JsonNode result = response.readEntity(JsonNode.class);
                assertEquals("Grüße", result.path("greeting").asText());
                assertEquals(2, result.path("count").asInt());
            }
        } finally { if (client != null) client.close(); server.stop(0); }
    }

    @Test public void jerseyMultipartCanUploadAFileWithTheEffectiveActivationApi() throws Exception {
        File file = temporary.newFile("attachment.txt");
        Files.writeString(file.toPath(), "attachment-content");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/upload", exchange -> {
            try (exchange) {
                byte[] body = exchange.getRequestBody().readAllBytes();
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
        });
        server.start();
        Client client = null;
        try (FormDataMultiPart form = new FormDataMultiPart()) {
            client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            form.bodyPart(new FileDataBodyPart("upload", file));
            try (Response response = client.target("http://127.0.0.1:" + server.getAddress().getPort() + "/upload")
                    .request().post(Entity.entity(form, form.getMediaType()))) {
                assertEquals(200, response.getStatus());
                String body = response.readEntity(String.class);
                assertTrue(body.contains("attachment.txt"));
                assertTrue(body.contains("attachment-content"));
            }
        } finally { if (client != null) client.close(); server.stop(0); }
    }
}
