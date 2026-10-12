package com.example.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Salle qui peut être réservée sur des créneaux horaires.
 *
 * <p>Une salle est le <b>parent</b> de ses réservations ({@code cascade = ALL}) :
 * enregistrer ou supprimer la salle fait la même chose sur ses réservations.
 */
@Entity
@Table(name = "salles")
public class Salle {

    /** Clé primaire, générée par la base (auto-incrément). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom de la salle, obligatoire et unique. */
    @Column(nullable = false, unique = true)
    private String nom;

    /** Nombre de places, obligatoire. */
    @Column(nullable = false)
    private int capacite;

    /**
     * Réservations de la salle (côté inverse de {@link Reservation#getSalle()}).
     * Initialisée vide pour éviter un {@code NullPointerException} sur {@code add()}.
     */
    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    /** Constructeur vide exigé par JPA. */
    public Salle() {}

    /**
     * @param nom      nom unique de la salle
     * @param capacite nombre de places
     */
    public Salle(String nom, int capacite) {
        this.nom = nom;
        this.capacite = capacite;
    }

    /**
     * Ajoute une réservation en mettant à jour <b>les deux côtés</b> de la relation.
     *
     * @param reservation la réservation à rattacher à cette salle
     */
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setSalle(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public Set<Reservation> getReservations() { return reservations; }
    public void setReservations(Set<Reservation> reservations) { this.reservations = reservations; }

    @Override
    public String toString() {
        return "Salle [id=" + id + ", nom=" + nom + ", capacite=" + capacite + "]";
    }
}