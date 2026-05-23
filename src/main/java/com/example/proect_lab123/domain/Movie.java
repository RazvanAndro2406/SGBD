package com.example.proect_lab123.domain;

import jakarta.persistence.*;

import java.util.Objects;

@jakarta.persistence.Entity
@Table(name = "movies")
@AttributeOverride(name = "id", column = @Column(name = "idm", columnDefinition = "INTEGER"))
public class Movie extends Entity<Long> {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "genre")
    private String genre;

    @Column(name = "duration")
    private Float duration;


    public Movie() {
        super();
    }

    public Movie(String title, String genre, Float duration) {
        super();
        this.title = title;
        this.genre = genre;
        this.duration = duration;
    }

    public Movie(Long id, String title, String genre, Float duration) {
        super(id);
        this.title = title;
        this.genre = genre;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie)) return false;
        if (!super.equals(o)) return false;
        Movie movie = (Movie) o;
        return Objects.equals(title, movie.title) &&
                Objects.equals(genre, movie.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, genre);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", duration=" + duration +
                '}';
    }
}
