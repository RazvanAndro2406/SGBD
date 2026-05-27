package com.example.proect_lab123.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@jakarta.persistence.Entity
@Table(name = "employees")
@SQLRestriction("is_deleted = false") //asta e versiunea mai noua a lui @Where(clause = "is_deleted = false")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    // Metodă helper pentru ștergere soft
    public void softDelete(String username) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = username;
    }

    // Metodă helper pentru restaurare
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    // Getters și Setters pentru noile câmpuri
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "phone")
    private String phone;

    public Employee(Long id, String firstName, String lastName, String email, Integer departmentId, Double salary, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.departmentId = departmentId;
        this.salary = salary;
        this.phone = phone;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // --- Constructori ---
    public Employee() {
    }

    public Employee(String firstName, String lastName, String email, Integer departmentId, Double salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.departmentId = departmentId;
        this.salary = salary;
    }

    // --- Getters și Setters ---





    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", departmentId=" + departmentId +
                ", salary=" + salary +
                '}';
    }
}