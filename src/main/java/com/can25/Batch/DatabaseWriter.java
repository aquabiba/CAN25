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
    public void write(Chunk<? extends Spectator> chunk) throws Exception {

        for (Spectator spectator : chunk) {

            // ----- 1. VÉRIFICATION ET MISE À JOUR DU SPECTATEUR -----
            // Vérifier si le spectateur existe déjà
            Spectator existingSpectator = em.find(Spectator.class, spectator.getSpectatorId());

            if (existingSpectator == null) {
                // Nouveau spectateur : on le persiste
                Spectator newSpectator = new Spectator();
                newSpectator.setSpectatorId(spectator.getSpectatorId());
                newSpectator.setAge(spectator.getAge());
                newSpectator.setNationality(spectator.getNationality());
                newSpectator.setTotalMatches(spectator.getTotalMatches());
                newSpectator.setCategory(spectator.getCategory());

                em.persist(newSpectator);
            } else {
                // Spectateur existant : on met à jour
                existingSpectator.setTotalMatches(spectator.getTotalMatches());
                existingSpectator.setCategory(spectator.getCategory());

                em.merge(existingSpectator);
            }

            // ----- 2. CRÉATION DE L'ENTRÉE AU MATCH -----
            MatchEntry entry = new MatchEntry();
            entry.setSpectatorId(spectator.getSpectatorId());
            entry.setMatchId(spectator.getMatchId());  // Depuis les champs @Transient
            entry.setEntryTime(LocalDateTime.parse(spectator.getEntryTime()));
            entry.setGate(spectator.getGate());
            entry.setTicketNumber(spectator.getTicketNumber());
            entry.setTicketType(spectator.getTicketType());
            entry.setSeatLocation(spectator.getSeatLocation());

            em.persist(entry);

            // ----- 3. MISE À JOUR DES STATISTIQUES -----
            // Vérifier si des stats existent pour ce spectateur
            SpectatorStatistics stats = em.createQuery(
                            "SELECT s FROM SpectatorStatistics s WHERE s.spectatorId = :spectatorId",
                            SpectatorStatistics.class)
                    .setParameter("spectatorId", spectator.getSpectatorId())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (stats == null) {
                // Créer de nouvelles statistiques
                stats = new SpectatorStatistics();
                stats.setSpectatorId(spectator.getSpectatorId());
                stats.setTotalMatches(spectator.getTotalMatches());
                stats.setBehaviorCategory(spectator.getCategory().name());

                em.persist(stats);
            } else {
                // Mettre à jour les statistiques existantes
                stats.setTotalMatches(spectator.getTotalMatches());
                stats.setBehaviorCategory(spectator.getCategory().name());

                em.merge(stats);
            }
        }
    }
}