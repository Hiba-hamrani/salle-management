package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Démonstration : salles disponibles (JPQL), recherche multi-critères (Criteria) et pagination.
 */
public class App {

    private static final LocalDateTime LUNDI_9H = LocalDateTime.of(2024, 6, 1, 9, 0);
    private static final LocalDateTime LUNDI_11H = LocalDateTime.of(2024, 6, 1, 11, 0);

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();
        try {
            insererDonnees(em);

            // Injection manuelle : EntityManager -> Repository -> Service
            SalleService service = new SalleServiceImpl(new SalleRepositoryImpl(em));

            // ---------- 1. JPQL : salles disponibles ----------
            System.out.println("\n===== Salles disponibles le " + LUNDI_9H.toLocalDate()
                    + " de 9h à 11h =====");
            List<Salle> dispo = service.getAvailableRooms(LUNDI_9H, LUNDI_11H);
            dispo.forEach(s -> System.out.println("- " + s.getNom() + " (Capacité: " + s.getCapacite() + ")"));

            // ---------- 2. Criteria : recherche multi-critères ----------
            System.out.println("\n===== Salles contenant 'salle', capacité >= 20, libres de 9h à 11h =====");
            service.searchRooms("salle", 20, LUNDI_9H, LUNDI_11H, 1, 10)
                    .forEach(s -> System.out.println("- " + s.getNom() + " (Capacité: " + s.getCapacite() + ")"));

            // ---------- 3. Pagination ----------
            System.out.println("\n===== Pagination : toutes les salles, 3 par page =====");
            int page = 1;
            PaginationResult<Salle> resultat;
            do {
                resultat = service.searchRoomsPaginated(null, null, null, null, page, 3);
                System.out.println("Page " + resultat.getCurrentPage() + "/" + resultat.getTotalPages()
                        + " (" + resultat.getTotalItems() + " salles au total)");
                resultat.getItems().forEach(s -> System.out.println("   - " + s.getNom()));
                page++;
            } while (resultat.hasNext());

        } finally {
            em.close();
            emf.close();
        }
    }

    /** Insère 7 salles et 2 réservations sur le créneau de 9h à 11h. */
    private static void insererDonnees(EntityManager em) {
        em.getTransaction().begin();
        try {
            Salle a = new Salle("Salle A", 10);
            Salle b = new Salle("Salle B", 20);
            Salle c = new Salle("Salle C", 30);
            Salle d = new Salle("Salle D", 40);
            Salle e = new Salle("Salle E", 50);
            Salle amphi = new Salle("Amphi 1", 200);
            Salle labo = new Salle("Labo Info", 25);

            // Salle B : 9h-11h (occupée) ; Salle D : 10h-12h (chevauche aussi)
            b.addReservation(new Reservation(LUNDI_9H, LUNDI_11H));
            d.addReservation(new Reservation(LUNDI_9H.plusHours(1), LUNDI_11H.plusHours(1)));
            // Salle C : 11h-12h, commence pile à la fin du créneau -> ne chevauche pas
            c.addReservation(new Reservation(LUNDI_11H, LUNDI_11H.plusHours(1)));

            for (Salle s : new Salle[]{a, b, c, d, e, amphi, labo}) {
                em.persist(s); // les réservations suivent (cascade ALL)
            }
            em.getTransaction().commit();
            em.clear();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        }
    }
}