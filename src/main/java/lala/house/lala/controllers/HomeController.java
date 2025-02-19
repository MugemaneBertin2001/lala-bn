package lala.house.lala.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Object> home() {
        return ResponseEntity.ok(
                Map.of(
                        "message", "Welcome to LaLa Rental API",
                        "status", "success"));
    }

    @GetMapping("/error")
    public ResponseEntity<Object> handleError() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "message", "Page not found. Please check the URL.",
                        "status", "error"));
    }
}