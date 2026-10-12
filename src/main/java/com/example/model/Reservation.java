package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Réservation d'une {@link Salle} sur un créneau {@code [debut, fin[}.
 *
 * <p>Côté <b>propriétaire</b> de la relation : la clé étrangère {@code salle_id}
 * est dans la table {@code reservations}. Chargée en {@code LAZY} pour ne pas
 * relire la salle à chaque réservation.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    /** Clé primaire, générée par la base (auto-incrément). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Salle réservée, obligatoire. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    /** Début du créneau (inclus). */
    @Column(nullable = false)
    private LocalDateTime debut;

    /** Fin du créneau (exclue) : une réservation 9h-11h n'empêche pas une autre à 11h. */
    @Column(nullable = false)
    private LocalDateTime fin;

    /** Constructeur vide exigé par JPA. */
    public Reservation() {}

    /**
     * @param debut début du créneau
     * @param fin   fin du créneau (doit être après {@code debut})
     */
    public Reservation(LocalDateTime debut, LocalDateTime fin) {
        this.debut = debut;
        this.fin = fin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public LocalDateTime getDebut() { return debut; }
    public void setDebut(LocalDateTime debut) { this.debut = debut; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }

    @Override
    public String toString() {
        return "Reservation [id=" + id + ", debut=" + debut + ", fin=" + fin + "]";
    }
}