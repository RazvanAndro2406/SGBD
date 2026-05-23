package com.example.proect_lab123.service;

import com.example.proect_lab123.util.event.EntityChangeEvent;
import com.example.proect_lab123.util.observer.Observable;
import com.example.proect_lab123.util.paging.Page;
import com.example.proect_lab123.util.paging.Pageable;

import java.util.Optional;

public interface IActorService<T> extends Observable<EntityChangeEvent<T>> {
    Optional<T> findOne(Long id);

    Iterable<T> findAll();

    Optional<T> save(T actor);

    Optional<T> update(T actor);

    Optional<T> delete(Long id);

    void assignActorToMovie(Long actorId, Long movieId);

    Page<T> findAllOnPage(Pageable pageable);
}

