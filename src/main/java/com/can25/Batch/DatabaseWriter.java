package com.can25.Batch;

import com.can25.Entity.MatchEntry;
import com.can25.Entity.Spectator;
import com.can25.Entity.SpectatorStatistics;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DatabaseWriter implements ItemWriter<Spectator> {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void write(Chunk<? extends Spectator> chunk) {

        for (Spectator spectator : chunk) {

            //  Spectator
            Spectator persisted = em.find(Spectator.class, spectator.getSpectatorId());
            if (persisted == null) {
                em.persist(spectator);
                persisted = spectator;
            }

            //  Match Entry (ALWAYS NEW)
            MatchEntry entry = MatchEntry.builder()
                    .spectatorId(persisted.getSpectatorId())
                    .matchId(spectator.getMatchId())
                    .entryTime(parseTime(spectator.getEntryTime()))
                    .gate(spectator.getGate())
                    .ticketNumber(spectator.getTicketNumber())
                    .ticketType(spectator.getTicketType())
                    .seatLocation(spectator.getSeatLocation())
                    .build();

            em.persist(entry);

            // 3️⃣ Statistics
            SpectatorStatistics stats = em.createQuery(
                            "SELECT s FROM SpectatorStatistics s WHERE s.spectatorId = :sp",
                            SpectatorStatistics.class)
                    .setParameter("sp", persisted)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (stats == null) {
                stats = SpectatorStatistics.builder()
                        .spectatorId(persisted)
                        .totalMatches(persisted.getTotalMatches())
                        .behaviorCategory(persisted.getCategory().name())
                        .build();
                em.persist(stats);
            } else {
                stats.setTotalMatches(persisted.getTotalMatches());
                stats.setBehaviorCategory(persisted.getCategory().name());
            }
        }
    }

    private LocalDateTime parseTime(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
