package com.springvjpa.demo.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DepartmentScraperClient {

    private static final Logger log = LoggerFactory.getLogger(DepartmentScraperClient.class);
    private static final Pattern ANCHOR_PATTERN = Pattern.compile("<a[^>]*href=\\\"([^\\\"]+)\\\"[^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final HttpClient httpClient;
    private final String departmentPageUrl;

    public DepartmentScraperClient(
            @Value("${app.department-source-url:https://mahitikanaja.karnataka.gov.in/Department}") String departmentPageUrl
    ) {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.departmentPageUrl = departmentPageUrl;
    }

    public List<ScrapedDepartment> fetchTargetDepartments() {
        try {
            String html = fetchHtml();
            List<ScrapedDepartment> departments = parseDepartments(html);
            if (!departments.isEmpty()) {
                return departments;
            }
            log.warn("No target departments were found in source page. Falling back to default targets.");
        } catch (Exception ex) {
            log.warn("Unable to scrape departments from {}. Falling back to defaults.", departmentPageUrl, ex);
        }
        return fallbackDepartments();
    }

    private String fetchHtml() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(departmentPageUrl))
                .GET()
                .header("User-Agent", "Mozilla/5.0 (Spring-JPA Department Scraper)")
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IOException("Received HTTP " + response.statusCode() + " from " + departmentPageUrl);
        }

        return response.body();
    }

    private List<ScrapedDepartment> parseDepartments(String html) {
        Map<String, ScrapedDepartment> results = new LinkedHashMap<>();
        Matcher matcher = ANCHOR_PATTERN.matcher(html);

        while (matcher.find()) {
            String href = matcher.group(1).trim();
            String text = stripTags(matcher.group(2)).trim();
            String normalizedText = text.toLowerCase(Locale.ROOT);

            if (normalizedText.contains("revenue") || normalizedText.contains("urban development")) {
                String absoluteUrl = resolveUrl(href);
                results.putIfAbsent(text, new ScrapedDepartment(text, absoluteUrl));
            }
        }

        if (results.isEmpty()) {
            captureFromPageBody(html, results);
        }

        return new ArrayList<>(results.values());
    }

    private void captureFromPageBody(String html, Map<String, ScrapedDepartment> results) {
        String pageText = stripTags(html).toLowerCase(Locale.ROOT);
        if (pageText.contains("department of revenue") || pageText.contains(" revenue ")) {
            results.putIfAbsent("Department of Revenue", new ScrapedDepartment("Department of Revenue", departmentPageUrl));
        }
        if (pageText.contains("department of urban development") || pageText.contains(" urban development ")) {
            results.putIfAbsent("Department of Urban Development", new ScrapedDepartment("Department of Urban Development", departmentPageUrl));
        }
    }

    private String stripTags(String input) {
        return input.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");
    }

    private String resolveUrl(String href) {
        try {
            URI hrefUri = new URI(href);
            if (hrefUri.isAbsolute()) {
                return hrefUri.toString();
            }
            return URI.create(departmentPageUrl).resolve(hrefUri).toString();
        } catch (URISyntaxException ex) {
            return departmentPageUrl;
        }
    }

    private List<ScrapedDepartment> fallbackDepartments() {
        return List.of(
                new ScrapedDepartment("Department of Revenue", departmentPageUrl),
                new ScrapedDepartment("Department of Urban Development", departmentPageUrl)
        );
    }

    public record ScrapedDepartment(String name, String sourceUrl) {
    }
}
