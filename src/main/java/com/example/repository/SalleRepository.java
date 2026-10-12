package com.example.repository;

import com.example.model.Salle;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Accès aux données des salles : recherches JPQL et Criteria, avec pagination.
 */
public interface SalleRepository {

    /**
     * Salles qu'aucune réservation ne chevauche sur le créneau donné (requête JPQL).
     *
     * @param debut début du créneau recherché
     * @param fin   fin du créneau recherché
     * @return les salles disponibles, triées par nom
     */
    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    /**
     * Recherche multi-critères (API Criteria). Un critère {@code null} est ignoré.
     *
     * @param nom         partie du nom (insensible à la casse), ou {@code null}
     * @param capaciteMin capacité minimale, ou {@code null}
     * @param debut       début du créneau à laisser libre, ou {@code null}
     * @param fin         fin du créneau à laisser libre, ou {@code null}
     * @param page        numéro de page (commence à 1)
     * @param size        nombre d'éléments par page
     * @return la page demandée, triée par nom
     */
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    /**
     * Nombre total de salles qui respectent les mêmes critères que {@link #searchRooms}.
     */
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);
}