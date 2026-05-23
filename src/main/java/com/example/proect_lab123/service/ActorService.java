package com.example.proect_lab123.service;

import com.example.proect_lab123.domain.Actor;
import com.example.proect_lab123.repository.ActorRepository;
import com.example.proect_lab123.repository.IActorRepository;
import com.example.proect_lab123.util.event.EntityChangeEvent;
import com.example.proect_lab123.util.event.EntityChangeEventType;
import com.example.proect_lab123.util.observer.Observable;
import com.example.proect_lab123.util.observer.Observer;
import com.example.proect_lab123.util.paging.Page;
import com.example.proect_lab123.util.paging.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActorService implements IActorService<Actor> {

    private IActorRepository actorRepository;
    private List<Observer<EntityChangeEvent<Actor>>> observers = new ArrayList<>();

    public ActorService(IActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public Optional<Actor> findOne(Long id) {
        return actorRepository.findOne(id);
    }

    public Iterable<Actor> findAll() {
        return actorRepository.findAll();
    }

    public Optional<Actor> save(Actor actor) {
        Optional<Actor> result = actorRepository.save(actor);
        if (result.isEmpty()) {
            notifyObservers(new EntityChangeEvent<>(EntityChangeEventType.ADD, actor));
        }
        return result;
    }

    public Optional<Actor> update(Actor actor) {
        Optional<Actor> oldActor = actorRepository.findOne(actor.getId());
        Optional<Actor> result = actorRepository.update(actor);

        if (result.isEmpty() && oldActor.isPresent()) {
            notifyObservers(new EntityChangeEvent<>(EntityChangeEventType.UPDATE, actor, oldActor.get()));
        }
        return result;
    }

    public Optional<Actor> delete(Long id) {
        Optional<Actor> deletedActor = actorRepository.delete(id);
        deletedActor.ifPresent(a ->
                notifyObservers(new EntityChangeEvent<>(EntityChangeEventType.DELETE, null, a)));
        return deletedActor;
    }

    /**
     * Metodă specifică pentru a muta un actor la alt film
     */
    public void assignActorToMovie(Long actorId, Long movieId) {
        actorRepository.findOne(actorId).ifPresent(actor -> {
            Actor oldActorState = new Actor(actor.getId(), actor.getName(), actor.getBirthday(), actor.getIdm());
            actor.setIdm(movieId);
            if (actorRepository.update(actor).isEmpty()) {
                notifyObservers(new EntityChangeEvent<>(EntityChangeEventType.UPDATE, actor, oldActorState));
            }
        });
    }

    public Page<Actor> findAllOnPage(Pageable pageable) {
        return actorRepository.findAllOnPage(pageable);
    }

    @Override
    public void addObserver(Observer<EntityChangeEvent<Actor>> o) { observers.add(o); }

    @Override
    public void removeObserver(Observer<EntityChangeEvent<Actor>> o) { observers.remove(o); }

    @Override
    public void notifyObservers(EntityChangeEvent<Actor> e) {
        observers.forEach(o -> o.update(e));
    }
}