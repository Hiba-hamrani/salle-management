package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests des requêtes JPQL, Criteria et de la pagination.
 * Une seule EntityManagerFactory, un EntityManager neuf par test, base vidée après chaque test.
 */
public class SalleServiceTest {

    private static final LocalDateTime H9 = LocalDateTime.of(2024, 6, 1, 9, 0);
    private static final LocalDateTime H10 = LocalDateTime.of(2024, 6, 1, 10, 0);
    private static final LocalDateTime H11 = LocalDateTime.of(2024, 6, 1, 11, 0);
    private static final LocalDateTime H12 = LocalDateTime.of(2024, 6, 1, 12, 0);

    private static EntityManagerFactory emf;
    private EntityManager em;
    private SalleService service;

    @BeforeClass
    public static void creerUsine() {
        emf = Persistence.createEntityManagerFactory("SallePU");
    }

    @AfterClass
    public static void fermerUsine() {
        if (emf != null) emf.close();
    }

    @Before
    public void setUp() {
        em = emf.createEntityManager();
        service = new SalleServiceImpl(new SalleRepositoryImpl(em));
        insererDonnees();
    }

    @After
    public void tearDown() {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Reservation").executeUpdate();
        em.createQuery("DELETE FROM Salle").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    /**
     * Salle A (10), B (20, réservée 9h-11h), C (30, réservée 11h-12h),
     * D (40, réservée 10h-12h), Amphi 1 (200).
     */
    private void insererDonnees() {
        em.getTransaction().begin();
        Salle a = new Salle("Salle A", 10);
        Salle b = new Salle("Salle B", 20);
        Salle c = new Salle("Salle C", 30);
        Salle d = new Salle("Salle D", 40);
        Salle amphi = new Salle("Amphi 1", 200);
        b.addReservation(new Reservation(H9, H11));
        c.addReservation(new Reservation(H11, H12));
        d.addReservation(new Reservation(H10, H12));
        for (Salle s : new Salle[]{a, b, c, d, amphi}) em.persist(s);
        em.getTransaction().commit();
        em.clear();
    }

    private static List<String> noms(List<Salle> salles) {
        return salles.stream().map(Salle::getNom).collect(Collectors.toList());
    }

    // ---------- JPQL : disponibilité ----------

    @Test
    public void disponibles_excluLesSallesQuiChevauchentLeCreneau() {
        // 9h-11h : B (9h-11h) et D (10h-12h) sont occupées
        assertEquals(List.of("Amphi 1", "Salle A", "Salle C"), noms(service.getAvailableRooms(H9, H11)));
    }

    @Test
    public void disponibles_unCreneauQuiCommencePileALaFinNestPasUnConflit() {
        // 11h-12h : B finit à 11h -> libre ; C (11h-12h) et D (10h-12h) sont occupées
        List<String> dispo = noms(service.getAvailableRooms(H11, H12));
        assertTrue(dispo.contains("Salle B"));
        assertFalse(dispo.contains("Salle C"));
        assertFalse(dispo.contains("Salle D"));
    }

    @Test
    public void disponibles_creneauInclusDansUneReservation() {
        // 10h-10h30 est à l'intérieur de B (9h-11h) et de D (10h-12h)
        List<String> dispo = noms(service.getAvailableRooms(H10, H10.plusMinutes(30)));
        assertFalse(dispo.contains("Salle B"));
        assertFalse(dispo.contains("Salle D"));
    }

    @Test
    public void disponibles_toutesLesSallesSiAucuneReservationCeJourLa() {
        assertEquals(5, service.getAvailableRooms(H9.plusDays(1), H11.plusDays(1)).size());
    }

    // ---------- Criteria : multi-critères ----------

    @Test
    public void recherche_parNomInsensibleALaCasse() {
        assertEquals(List.of("Amphi 1"), noms(service.searchRooms("AMPHI", null, null, null, 1, 10)));
    }

    @Test
    public void recherche_parCapaciteMinimale() {
        assertEquals(List.of("Amphi 1", "Salle C", "Salle D"),
                noms(service.searchRooms(null, 30, null, null, 1, 10)));
    }

    @Test
    public void recherche_combineNomCapaciteEtDisponibilite() {
        // "salle", >= 20 places, libre de 9h à 11h : B et D occupées -> seule C reste
        assertEquals(List.of("Salle C"), noms(service.searchRooms("salle", 20, H9, H11, 1, 10)));
    }

    @Test
    public void recherche_sansCritereRenvoieTout() {
        assertEquals(5, service.searchRooms(null, null, null, null, 1, 10).size());
        assertEquals(5, service.countRooms(null, null, null, null));
    }

    // ---------- Pagination ----------

    @Test
    public void pagination_decoupeLesResultatsEnPages() {
        PaginationResult<Salle> page1 = service.searchRoomsPaginated(null, null, null, null, 1, 2);
        PaginationResult<Salle> page3 = service.searchRoomsPaginated(null, null, null, null, 3, 2);

        assertEquals(List.of("Amphi 1", "Salle A"), noms(page1.getItems()));
        assertEquals(5, page1.getTotalItems());
        assertEquals(3, page1.getTotalPages());
        assertTrue(page1.hasNext());
        assertFalse(page1.hasPrevious());

        assertEquals("La dernière page n'est pas pleine", List.of("Salle D"), noms(page3.getItems()));
        assertFalse(page3.hasNext());
    }

    @Test
    public void pagination_pageAuDelaDeLaFinEstVide() {
        assertTrue(service.searchRooms(null, null, null, null, 10, 2).isEmpty());
    }

    @Test
    public void comptage_appliqueLesMemesCriteresQueLaRecherche() {
        assertEquals(1, service.countRooms("salle", 20, H9, H11));
    }

    // ---------- Validation (cas d'erreur) ----------

    @Test(expected = IllegalArgumentException.class)
    public void erreur_finAvantDebut() {
        service.getAvailableRooms(H11, H9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void erreur_creneauDeDureeNulle() {
        service.getAvailableRooms(H9, H9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void erreur_creneauIncomplet() {
        service.searchRooms(null, null, H9, null, 1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void erreur_numeroDePageInvalide() {
        service.searchRooms(null, null, null, null, 0, 10);
    }

    @Test
    public void erreur_nomEnDouble_leveUneExceptionEtRollback() {
        boolean exceptionLevee = false;
        try {
            em.getTransaction().begin();
            em.persist(new Salle("Salle A", 15)); // nom unique déjà utilisé
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            exceptionLevee = true;
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        }
        assertTrue(exceptionLevee);

        em.close();
        em = emf.createEntityManager();
        service = new SalleServiceImpl(new SalleRepositoryImpl(em));
        assertEquals("La base ne doit pas avoir changé", 5, service.countRooms(null, null, null, null));
    }
}