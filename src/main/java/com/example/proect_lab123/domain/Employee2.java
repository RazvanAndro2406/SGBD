package com.example.proect_lab123.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "employees2")
public class Employee2 {

    @Id
    private Long id; // Aici nu punem @GeneratedValue ca sa ii putem seta noi id-ul manual (ex: 1, 2, 3) în demo

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "department_id", nullable = false)
    private Integer departmentId;

    @Column(name = "salary", nullable = false)
    private BigDecimal salary;

    public Employee2() {
    }

    public Employee2(Long id, String name, Integer departmentId, BigDecimal salary) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.salary = salary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
}