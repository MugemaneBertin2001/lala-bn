package lala.house.lala.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import lala.house.lala.entities.Booking;
import lala.house.lala.entities.User;
import lala.house.lala.entities.Property;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRenter(User renter);

    List<Booking> findByProperty(Property property);

    @Query("SELECT b FROM Booking b WHERE b.property = :property " +
            "AND b.status = 'CONFIRMED' " +
            "AND ((b.checkInDate BETWEEN :checkIn AND :checkOut) " +
            "OR (b.checkOutDate BETWEEN :checkIn AND :checkOut))")
    List<Booking> findOverlappingBookings(
            @Param("property") Property property,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}