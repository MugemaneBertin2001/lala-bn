package lala.house.lala.controllers;

import lala.house.lala.entities.Property;
import lala.house.lala.entities.User;
import lala.house.lala.services.AuthService;
import lala.house.lala.services.JwtService;
import lala.house.lala.services.PropertyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(PropertyController.class);
    private final JwtService jwtService;
    private final AuthService authService; // Add this field

    @Autowired
    public PropertyController(
            PropertyService propertyService,
            JwtService jwtService,
            AuthService authService) { // Add UserService as a dependency
        this.propertyService = propertyService;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    // Public endpoints
    @GetMapping("/public/all")
    public ResponseEntity<List<Property>> getAllPublicProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<Property> getPublicPropertyById(@PathVariable Long id) {
        return propertyService.getPropertyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/public/available")
    public ResponseEntity<List<Property>> getPublicAvailableProperties() {
        return ResponseEntity.ok(propertyService.getAvailableProperties());
    }

    // Protected endpoints
    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable Long id) {
        return propertyService.getPropertyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public ResponseEntity<List<Property>> getAvailableProperties() {
        return ResponseEntity.ok(propertyService.getAvailableProperties());
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<Property>> getPropertiesByHost(@PathVariable Long hostId) {
        // Fetch the user by hostId
        Optional<User> host = authService.getUserById(hostId);

        if (host.isEmpty()) {
            return ResponseEntity.notFound().build(); // Return 404 if the user does not exist
        }

        // Fetch properties by the host
        List<Property> properties = propertyService.getPropertiesByHost(host.get());
        return ResponseEntity.ok(properties);
    }

    @PostMapping
    public ResponseEntity<Property> createProperty(
            @RequestBody Property property,
            @RequestParam("hostId") Long hostId) {

        // Validate the hostId
        if (hostId == null) {
            return ResponseEntity.badRequest().body(null); // Return 400 if hostId is missing
        }

        // Fetch the user by hostId
        Optional<User> host = authService.getUserById(hostId);
        if (host.isEmpty()) {
            return ResponseEntity.notFound().build(); // Return 404 if the user does not exist
        }

        // Set the host of the property
        property.setHost(host.get());

        // Save the property
        try {
            Property savedProperty = propertyService.saveProperty(property);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProperty);
        } catch (Exception e) {
            // Log the error and return a 500 Internal Server Error
            logger.error("Error creating property: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @RequestBody Property propertyDetails) {
        try {
            Property updatedProperty = propertyService.updateProperty(id, propertyDetails);
            return ResponseEntity.ok(updatedProperty);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        if (!propertyService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getPropertyCount() {
        return ResponseEntity.ok(propertyService.getPropertyCount());
    }
}