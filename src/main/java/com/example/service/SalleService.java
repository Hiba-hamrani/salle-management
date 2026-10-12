package com.example.service;

import com.example.model.Salle;
import com.example.util.PaginationResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Règles métier sur les salles : validation des paramètres avant d'interroger le repository.
 */
public interface SalleService {

    /** Salles libres sur le créneau ; refuse un créneau incohérent. */
    List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    /** Une page de la recherche multi-critères. */
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    /** Nombre total de résultats de la recherche multi-critères. */
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);

    /** Recherche multi-critères renvoyée avec ses informations de pagination. */
    PaginationResult<Salle> searchRoomsPaginated(String nom, Integer capaciteMin,
                                                 LocalDateTime debut, LocalDateTime fin, int page, int size);
}