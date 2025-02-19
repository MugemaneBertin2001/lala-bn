package lala.house.lala.services;

import lala.house.lala.entities.Property;
import lala.house.lala.entities.User;
import lala.house.lala.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public Optional<Property> getPropertyById(Long id) {
        return propertyRepository.findById(id);
    }

    public List<Property> getAvailableProperties() {
        return propertyRepository.findByAvailableTrue();
    }

    public List<Property> getPropertiesByHost(User host) {
        return propertyRepository.findByHost(host);
    }

    public Property saveProperty(Property property) {
        return propertyRepository.save(property);
    }

    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return propertyRepository.existsById(id);
    }

    public Property updateProperty(Long id, Property propertyDetails) {
        return propertyRepository.findById(id)
                .map(existingProperty -> {
                    existingProperty.setTitle(propertyDetails.getTitle());
                    existingProperty.setDescription(propertyDetails.getDescription());
                    existingProperty.setPricePerNight(propertyDetails.getPricePerNight());
                    existingProperty.setLocation(propertyDetails.getLocation());
                    existingProperty.setAvailable(propertyDetails.isAvailable());
                    return propertyRepository.save(existingProperty);
                })
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
    }

    public long getPropertyCount() {
        return propertyRepository.count();
    }
}