package com.example.proect_lab123.service;

import com.example.proect_lab123.domain.Movie;
import com.example.proect_lab123.repository.IMovieRepository;
import com.example.proect_lab123.repository.MovieRepository;
import com.example.proect_lab123.util.event.EntityChangeEvent;
import com.example.proect_lab123.util.event.EntityChangeEventType;
import com.example.proect_lab123.util.observer.Observable;
import com.example.proect_lab123.util.observer.Observer;
import com.example.proect_lab123.util.paging.Page;
import com.example.proect_lab123.util.paging.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieService implements IMovieService<Movie> {

    private IMovieRepository movieRepository;
    private List<Observer<EntityChangeEvent<Movie>>> observers = new ArrayList<>();

    public MovieService(IMovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Optional<Movie> findOne(Long id) {
        return movieRepository.findOne(id);
    }

    public Iterable<Movie> findAll() {
        return movieRepository.findAll();
    }

    public Optional<Movie> save(Movie movie) {
        // În repo-ul tău: empty = succes
        Optional<Movie> result = movieRepository.save(movie);
        if (result.isEmpty()) {
            notifyObservers(new EntityChangeEvent<>(EntityChangeEventType.ADD, movie));
        }
        return result;
    }

    public Optional<Movie> update(Movie movie) {
        Optional<Movie> oldMovie = movieRepository.findOne(movie.getId());
        Optional<Movie> result = movieRepository.update(movie);

        if (result.isEmpty() && oldMovie.isPresent()) {
            notifyObservers(new EntityChangeEvent<>(EntityChangeEventType.UPDATE, movie, oldMovie.get()));
        }
        return result;
    }

    public Optional<Movie> delete(Long id) {
        Optional<Movie> deletedMovie = movieRepository.delete(id);
        deletedMovie.ifPresent(m ->
                notifyObservers(new EntityChangeEvent<>(EntityChangeEventType.DELETE, null, m)));
        return deletedMovie;
    }

    public Page<Movie> findAllOnPage(Pageable pageable) {
        return movieRepository.findAllOnPage(pageable);
    }

    @Override
    public void addObserver(Observer<EntityChangeEvent<Movie>> o) { observers.add(o); }

    @Override
    public void removeObserver(Observer<EntityChangeEvent<Movie>> o) { observers.remove(o); }

    @Override
    public void notifyObservers(EntityChangeEvent<Movie> e) {
        observers.forEach(o -> o.update(e));
    }
}