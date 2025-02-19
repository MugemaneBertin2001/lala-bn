package lala.house.lala.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import lala.house.lala.entities.Property;
import lala.house.lala.entities.User;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByAvailableTrue();

    List<Property> findByHost(User host);
}