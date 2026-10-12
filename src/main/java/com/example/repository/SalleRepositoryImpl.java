package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JPA de {@link SalleRepository}.
 *
 * <p>Sans serveur EJB, il n'y a ni {@code @Stateless} ni {@code @PersistenceContext} :
 * l'{@code EntityManager} est fourni par le constructeur (injection manuelle).
 */
public class SalleRepositoryImpl implements SalleRepository {

    private final EntityManager em;

    /** @param em EntityManager utilisé pour toutes les requêtes */
    public SalleRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        // Deux créneaux se chevauchent si l'un commence avant que l'autre finisse, et inversement
        String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                "SELECT r.salle.id FROM Reservation r WHERE :debut < r.fin AND :fin > r.debut" +
                ") ORDER BY s.nom";
        return em.createQuery(jpql, Salle.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);

        cq.select(salle)
                .where(construireCriteres(cb, cq, salle, nom, capaciteMin, debut, fin))
                .orderBy(cb.asc(salle.get("nom"))); // un ordre stable est indispensable pour paginer

        return em.createQuery(cq)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Salle> salle = cq.from(Salle.class);

        // Les MÊMES critères que searchRooms, sinon le nombre de pages serait faux
        cq.select(cb.count(salle))
                .where(construireCriteres(cb, cq, salle, nom, capaciteMin, debut, fin));

        return em.createQuery(cq).getSingleResult();
    }

    /**
     * Construit les conditions à partir des critères renseignés.
     * Partagé par {@link #searchRooms} et {@link #countRooms} pour qu'ils restent cohérents.
     */
    private Predicate[] construireCriteres(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<Salle> salle,
                                           String nom, Integer capaciteMin,
                                           LocalDateTime debut, LocalDateTime fin) {
        List<Predicate> predicates = new ArrayList<>();

        if (nom != null && !nom.isEmpty()) {
            predicates.add(cb.like(cb.lower(salle.get("nom")), "%" + nom.toLowerCase() + "%"));
        }

        if (capaciteMin != null) {
            predicates.add(cb.ge(salle.get("capacite"), capaciteMin));
        }

        if (debut != null && fin != null) {
            Subquery<Long> sub = cq.subquery(Long.class);
            Root<Reservation> r = sub.from(Reservation.class);
            sub.select(r.get("salle").get("id"))
                    .where(cb.lessThan(r.get("debut"), fin),
                            cb.greaterThan(r.get("fin"), debut));
            predicates.add(cb.not(salle.get("id").in(sub)));
        }

        return predicates.toArray(new Predicate[0]);
    }
}