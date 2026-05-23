//package com.example.proect_lab123.domain;
//
//import jakarta.persistence.*;
//import java.time.LocalDate;
//import java.util.Objects;
//
//@jakarta.persistence.Entity
//@Table(name = "projects")
//public class Project extends Entity<Long> {
//
//    @Column(name = "name", nullable = false)
//    private String name;
//
//    @Column(name = "description", columnDefinition = "TEXT")
//    private String description;
//
//    @Column(name = "start_date")
//    private LocalDate startDate;
//
//    @Column(name = "end_date")
//    private LocalDate endDate;
//
//    @Column(name = "department_id")
//    private Integer departmentId;
//
//    @Column(name = "is_active")
//    private Boolean isActive = true;
//
//
//    public Project() {
//        super();
//    }
//
//    public Project(String name, String description, LocalDate startDate, Integer departmentId) {
//        super();
//        this.name = name;
//        this.description = description;
//        this.startDate = startDate;
//        this.departmentId = departmentId;
//    }
//
//    public Project(Long id, String name, String description, LocalDate startDate, Integer departmentId) {
//        super(id);
//        this.name = name;
//        this.description = description;
//        this.startDate = startDate;
//        this.departmentId = departmentId;
//    }
//
//    // Getters and setters
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public LocalDate getStartDate() {
//        return startDate;
//    }
//
//    public void setStartDate(LocalDate startDate) {
//        this.startDate = startDate;
//    }
//
//    public LocalDate getEndDate() {
//        return endDate;
//    }
//
//    public void setEndDate(LocalDate endDate) {
//        this.endDate = endDate;
//    }
//
//    public Integer getDepartmentId() {
//        return departmentId;
//    }
//
//    public void setDepartmentId(Integer departmentId) {
//        this.departmentId = departmentId;
//    }
//
//    public Boolean getIsActive() {
//        return isActive;
//    }
//
//    public void setIsActive(Boolean active) {
//        isActive = active;
//    }
//
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Project)) return false;
//        if (!super.equals(o)) return false;
//        Project project = (Project) o;
//        return Objects.equals(name, project.name) &&
//                Objects.equals(startDate, project.startDate);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(super.hashCode(), name, startDate);
//    }
//
//    @Override
//    public String toString() {
//        return "Project{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", description='" + description + '\'' +
//                ", startDate=" + startDate +
//                ", endDate=" + endDate +
//                ", departmentId=" + departmentId +
//                ", isActive=" + isActive +
//                '}';
//    }
//}
//
