package com.example.proect_lab123.domain;

import jakarta.persistence.*;

@jakarta.persistence.Entity
@Table(name = "proiecte")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "titlu", nullable = false)
    private String name;

    @Column(name = "descriere")
    private String description;

    // Relație Many-to-One: Mai multe proiecte aparțin unui singur departament
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departament_id", nullable = false)
    private Department department;

    public Project() {}

    public Project(String name, String description, Department department) {
        this.name = name;
        this.description = description;
        this.department = department;
    }

    // Getter și Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name='" + name + "', description='" + description + "'}";
    }
}