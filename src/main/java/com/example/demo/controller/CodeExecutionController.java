package com.example.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
@CrossOrigin(origins = "https://goutham-siva-10.github.io")
@RestController
@RequestMapping("/api")
public class CodeExecutionController {

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson for JSON

    public CodeExecutionController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/key")
    public String testKey() {
        System.out.println("RapidAPI Key: " + rapidApiKey);
        return "Key loaded in backend";
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executeCode(@RequestBody CodeExecutionRequest request) {
        try {
            // Validate and set defaults with explicit checks
            String language = Optional.ofNullable(request.getLanguage())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .orElse("python").toLowerCase();
            String code = Optional.ofNullable(request.getCode())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .orElse("print('Hello from Judge0!')");
            String stdin = Optional.ofNullable(request.getStdin())
                    .map(String::trim)
                    .orElse("");

            // Additional validation
            if (code.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("output", "source_code cannot be blank"));
            }

            Integer languageId = getLanguageId(language);
            if (languageId == null) {
                return ResponseEntity.badRequest().body(Map.of("output", "Unsupported language: " + language));
            }

            // Prepare headers for Judge0 API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-RapidAPI-Key", rapidApiKey);
            headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");

            // Prepare and log request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("language_id", languageId);
            requestBody.put("source_code", code);
            requestBody.put("stdin", stdin);

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            System.out.println("Received from client: " + objectMapper.writeValueAsString(request));
            System.out.println("Sending to Judge0: " + requestBodyJson); // Log exact JSON

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Send request to Judge0 and log response
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("output", "Judge0 returned an empty response."));
            }

            // Extract output
            String stdout = Objects.toString(body.get("stdout"), "");
            String stderr = Objects.toString(body.get("stderr"), "");
            String compileOutput = Objects.toString(body.get("compile_output"), "");
            String message = Objects.toString(body.get("message"), "");

            StringBuilder output = new StringBuilder();
            if (!stdout.isEmpty()) output.append(stdout).append("\n");
            if (!stderr.isEmpty()) output.append("Error: ").append(stderr).append("\n");
            if (!compileOutput.isEmpty()) output.append("Compilation Output: ").append(compileOutput).append("\n");
            if (!message.isEmpty()) output.append("Message: ").append(message).append("\n");

            if (output.length() == 0) {
                output.append("No output returned.");
            } else {
                output.setLength(output.length() - 1); // Remove trailing newline
            }

            System.out.println("Judge0 Response: " + objectMapper.writeValueAsString(body)); // Log response
            return ResponseEntity.ok(Map.of("output", output.toString()));

        } catch (HttpClientErrorException e) {
            System.err.println("Judge0 HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            String errorMessage = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("output", "Error: " + (errorMessage.isEmpty() ? e.getMessage() : errorMessage)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("output", "Error executing code: " + e.getMessage()));
        }
    }

    private Integer getLanguageId(String language) {
        return switch (language) {
            case "python" -> 71;
            case "java" -> 62;
            case "cpp", "c++" -> 54;
            case "c" -> 50;
            case "javascript", "js" -> 63;
            default -> null;
        };
    }

    @Data
    static class CodeExecutionRequest {
        private String language;
        private String code;
        private String stdin;
    }
}