package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.util.PaginationResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation de {@link SalleService}.
 *
 * <p>Sans conteneur EJB/CDI, le repository est injecté <b>par le constructeur</b>
 * (pas de {@code @Stateless} ni de {@code @Inject}).
 */
public class SalleServiceImpl implements SalleService {

    private final SalleRepository salleRepository;

    /** @param salleRepository repository utilisé pour les requêtes */
    public SalleServiceImpl(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        verifierCreneau(debut, fin, true);
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        verifierPagination(page, size);
        verifierCreneau(debut, fin, false);
        return salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        verifierCreneau(debut, fin, false);
        return salleRepository.countRooms(nom, capaciteMin, debut, fin);
    }

    @Override
    public PaginationResult<Salle> searchRoomsPaginated(String nom, Integer capaciteMin,
                                                        LocalDateTime debut, LocalDateTime fin, int page, int size) {
        List<Salle> items = searchRooms(nom, capaciteMin, debut, fin, page, size);
        long total = countRooms(nom, capaciteMin, debut, fin);
        return new PaginationResult<>(items, page, size, total);
    }

    // ---------- Règles métier ----------

    /**
     * @param obligatoire si vrai, les deux dates doivent être fournies ; sinon le créneau est facultatif
     * @throws IllegalArgumentException si le créneau est incomplet ou si la fin n'est pas après le début
     */
    private void verifierCreneau(LocalDateTime debut, LocalDateTime fin, boolean obligatoire) {
        if (debut == null && fin == null && !obligatoire) return;
        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Le créneau doit avoir une date de début ET une date de fin");
        }
        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        }
    }

    /** @throws IllegalArgumentException si la page ou la taille ne sont pas strictement positives */
    private void verifierPagination(int page, int size) {
        if (page < 1) throw new IllegalArgumentException("Le numéro de page commence à 1");
        if (size < 1) throw new IllegalArgumentException("La taille de page doit être au moins 1");
    }
}