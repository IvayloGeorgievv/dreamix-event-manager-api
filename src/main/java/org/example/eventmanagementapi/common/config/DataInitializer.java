package org.example.eventmanagementapi.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventmanagementapi.building.Building;
import org.example.eventmanagementapi.building.BuildingRepository;
import org.example.eventmanagementapi.event.Event;
import org.example.eventmanagementapi.event.EventCategory;
import org.example.eventmanagementapi.event.EventRepository;
import org.example.eventmanagementapi.performer.Performer;
import org.example.eventmanagementapi.performer.PerformerRepository;
import org.example.eventmanagementapi.user.Role;
import org.example.eventmanagementapi.user.User;
import org.example.eventmanagementapi.user.UserRepository;
import org.example.eventmanagementapi.venue.Venue;
import org.example.eventmanagementapi.venue.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final VenueRepository venueRepository;
    private final PerformerRepository performerRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(final String... args) {
        if (userRepository.count() == 0) {
            log.info("Empty database detected. Seeding initial dummy data...");

            // 1. Create Admin Account
            final User admin = new User(
                    "System",
                    "Admin",
                    "admin@example.com",
                    passwordEncoder.encode("Admin123!"),
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);

            // 2. Create Building
            final Building tearsAndLaughter = new Building("Tears and Laughter Theatre", "Sofia", "ul. Rakovski 127");
            buildingRepository.save(tearsAndLaughter);

            // 3. Create Venue
            final Venue mainStage = new Venue("Main Stage", 450, tearsAndLaughter);
            venueRepository.save(mainStage);

            // 4. Create Performer
            final Performer performer = new Performer("Petar Petrov");
            performerRepository.save(performer);

            // 5. Create Event
            final Event event = new Event(
                    mainStage,
                    "Don't Bet on the Brits!",
                    EventCategory.THEATRE,
                    "A hilarious contemporary comedy following three lifelong friends whose casual weekend bets spiral into an absurd series of misadventures.",
                    "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80",
                    new BigDecimal("15.00"),
                    LocalDateTime.now(ZoneOffset.UTC).plusDays(15)
            );
            event.addPerformer(performer);
            eventRepository.save(event);

            log.info("Database seeding completed successfully.");
        }
    }
}
