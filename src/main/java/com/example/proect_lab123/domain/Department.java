package com.example.proect_lab123.domain; // Ajustează pachetul conform proiectului tău

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "departamente")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "nume", nullable = false)
    private String name;

    // Relație One-To-Many: Un departament are mai multe proiecte
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    public Department() {}

    public Department(String name) {
        this.name = name;
    }

    // Getter și Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    @Override
    public String toString() {
        return "Department{id=" + id + ", name='" + name + "'}";
    }
}