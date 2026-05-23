package com.example.proect_lab123.domain;

import com.example.proect_lab123.config.LocalDateConverter;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;


@jakarta.persistence.Entity
@Table(name = "actors")
@AttributeOverride(name = "id", column = @Column(name = "ida", columnDefinition = "INTEGER"))
public class Actor extends Entity<Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birthday", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = LocalDateConverter.class)
    private LocalDate birthday;

    @Column(name = "idm", columnDefinition = "INTEGER")
    private Long idm;


    public Actor() {
        super();
    }

    public Actor(String name, LocalDate birthday) {
        super();
        this.name = name;
        this.birthday = birthday;
        this.idm = null;
    }

    public Actor(String name, LocalDate birthday, Long idm) {
        super();
        this.name = name;
        this.birthday = birthday;
        this.idm = idm;
    }

    public Actor(Long id, String name, LocalDate birthday) {
        super(id);
        this.name = name;
        this.birthday = birthday;
        this.idm = null;
    }

    public Actor(Long id, String name, LocalDate birthday, Long idm) {
        super(id);
        this.name = name;
        this.birthday = birthday;
        this.idm = idm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Long getIdm() {
        return idm;
    }

    public void setIdm(Long idm) {
        this.idm = idm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actor)) return false;
        if (!super.equals(o)) return false;
        Actor actor = (Actor) o;
        return Objects.equals(name, actor.name) &&
                Objects.equals(birthday, actor.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, birthday);
    }

    @Override
    public String toString() {
        return "Actor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", idm=" + idm +
                '}';
    }
}
