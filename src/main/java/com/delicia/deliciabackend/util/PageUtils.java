package com.delicia.deliciabackend.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Utilidad para crear PageRequest de forma segura (page >= 0, size >= 1).
 */
public final class PageUtils {
    private PageUtils() {}

    /**
     * Crea un PageRequest garantizando que page >= 0 y size >= 1.
     * Si page es null -> 0. Si size es null o <= 0 -> 20.
     */
    public static PageRequest of(Integer page, Integer size) {
        int p = (page == null) ? 0 : Math.max(0, page);
        int s = (size == null || size <= 0) ? 20 : Math.max(1, size);
        return PageRequest.of(p, s);
    }

    /**
     * Igual que of(page,size) pero permite pasar un Sort.
     */
    public static PageRequest of(Integer page, Integer size, Sort sort) {
        int p = (page == null) ? 0 : Math.max(0, page);
        int s = (size == null || size <= 0) ? 20 : Math.max(1, size);
        return PageRequest.of(p, s, sort);
    }
}