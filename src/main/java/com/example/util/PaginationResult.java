package com.example.util;

import java.util.List;

/**
 * Une page de résultats, avec les informations utiles à la navigation.
 *
 * @param <T> type des éléments de la page
 */
public class PaginationResult<T> {

    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    /**
     * @param items       éléments de la page courante
     * @param currentPage numéro de la page (commence à 1)
     * @param pageSize    nombre d'éléments par page
     * @param totalItems  nombre total d'éléments, toutes pages confondues
     */
    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() { return items; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public long getTotalItems() { return totalItems; }

    /** @return vrai s'il existe une page après celle-ci */
    public boolean hasNext() { return currentPage < totalPages; }

    /** @return vrai s'il existe une page avant celle-ci */
    public boolean hasPrevious() { return currentPage > 1; }
}