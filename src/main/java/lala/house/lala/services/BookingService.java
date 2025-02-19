package lala.house.lala.services;

import lala.house.lala.entities.Booking;
import lala.house.lala.entities.Property;
import lala.house.lala.entities.User;
import lala.house.lala.enums.BookingStatus;
import lala.house.lala.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByRenter(User renter) {
        return bookingRepository.findByRenter(renter);
    }

    public List<Booking> getBookingsByProperty(Property property) {
        return bookingRepository.findByProperty(property);
    }

    public Booking createBooking(Booking booking) {
        validateBookingDates(booking);
        validatePropertyAvailability(booking);

        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        return bookingRepository.findById(bookingId)
                .map(existingBooking -> {
                    existingBooking.setStatus(newStatus);
                    existingBooking.setUpdatedAt(LocalDateTime.now());
                    return bookingRepository.save(existingBooking);
                })
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
    }

    public Booking updateBooking(Long bookingId, Booking bookingDetails) {
        return bookingRepository.findById(bookingId)
                .map(existingBooking -> {
                    existingBooking.setCheckInDate(bookingDetails.getCheckInDate());
                    existingBooking.setCheckOutDate(bookingDetails.getCheckOutDate());
                    existingBooking.setUpdatedAt(LocalDateTime.now());

                    validateBookingDates(existingBooking);
                    validatePropertyAvailability(existingBooking);

                    return bookingRepository.save(existingBooking);
                })
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public boolean isPropertyAvailable(Property property, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                property, checkIn, checkOut);
        return overlappingBookings.isEmpty();
    }

    private void validateBookingDates(Booking booking) {
        LocalDate now = LocalDate.now();

        if (booking.getCheckInDate().isBefore(now)) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        if (booking.getCheckOutDate().isBefore(booking.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (booking.getCheckInDate().equals(booking.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-in and check-out cannot be on the same day");
        }
    }

    private void validatePropertyAvailability(Booking booking) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                booking.getProperty(),
                booking.getCheckInDate(),
                booking.getCheckOutDate());

        if (booking.getId() != null) {
            overlappingBookings = overlappingBookings.stream()
                    .filter(b -> !b.getId().equals(booking.getId()))
                    .toList();
        }

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("Property is not available for the selected dates");
        }
    }

    public boolean hasOverlappingBookings(Property property, LocalDate checkIn, LocalDate checkOut) {
        return !bookingRepository.findOverlappingBookings(property, checkIn, checkOut).isEmpty();
    }
}