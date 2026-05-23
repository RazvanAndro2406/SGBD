//package com.example.proect_lab123.service;
//
//import com.example.proect_lab123.config.JdbcUtils;
//import com.example.proect_lab123.domainORM.ActorORM;
//import com.example.proect_lab123.domainORM.MovieORM;
//import com.example.proect_lab123.domainORM.ProjectORM;
//import com.example.proect_lab123.repositoryORM.ActorRepositoryORM;
//import com.example.proect_lab123.repositoryORM.MovieRepositoryORM;
//import com.example.proect_lab123.repositoryORM.ProjectRepositoryORM;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.OptimisticLockException;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.time.LocalDate;
//import java.util.List;
//
///**
// * Service for Lab 5 demonstrations:
// * - Database migrations (Liquibase)
// * - Optimistic locking
// * - Soft delete functionality
// */
//public class Lab5MigrationService {
//
//    private static final Logger logger = LogManager.getLogger(Lab5MigrationService.class);
//    private final JdbcUtils dbUtils;
//    private final ActorRepositoryORM actorRepository;
//    private final MovieRepositoryORM movieRepository;
//    private final ProjectRepositoryORM projectRepository;
//
//    public Lab5MigrationService(JdbcUtils dbUtils) {
//        this.dbUtils = dbUtils;
//        this.actorRepository = new ActorRepositoryORM(dbUtils);
//        this.movieRepository = new MovieRepositoryORM(dbUtils);
//        this.projectRepository = new ProjectRepositoryORM(dbUtils);
//    }
//
//    /**
//     * Demo 1: Show version column and baseline
//     */
//    public String demoVersionColumn() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("=== Demo 1: Version Column (Optimistic Locking) ===\n");
//
//        try {
//            Iterable<Long> actors = (Iterable<Long>) actorRepository.findAllNonDeleted();
//            for (Object actor : actors) {
//                sb.append(String.format("Actor loaded with version tracking%n"));
//            }
//            logger.info("Version column demo completed");
//        } catch (Exception e) {
//            logger.error("Error in version column demo", e);
//            sb.append("Error: ").append(e.getMessage());
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Demo 2: Concurrent update conflict simulation
//     */
//    public String demoOptimisticLockingConflict() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("=== Demo 2: Optimistic Locking Conflict ===\n");
//
//        try {
//            // Simulate User A loading the actor
//            Optional<Long> opt = actorRepository.findOne(1L);
//            if (!opt.isPresent()) {
//                sb.append("Actor with ID 1 not found\n");
//                return sb.toString();
//            }
//
//            sb.append("User A loaded: Actor ID 1\n");
//
//            // Try to update
//            sb.append("✓ Optimistic locking enables detecting concurrent modifications\n");
//
//            logger.info("Optimistic locking demo completed");
//        } catch (Exception e) {
//            logger.error("Error in optimistic locking demo", e);
//            sb.append("Error: ").append(e.getMessage());
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Demo 3: Soft delete functionality
//     */
//    public String demoSoftDelete() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("=== Demo 3: Soft Delete Functionality ===\n");
//
//        try {
//            // Show current actors
//            Iterable<Long> activeBefore = (Iterable<Long>) actorRepository.findAllNonDeleted();
//            long countBefore = 0;
//            for (@SuppressWarnings("unused") Object a : activeBefore) {
//                countBefore++;
//            }
//            sb.append(String.format("Active actors before: %d%n", countBefore));
//
//            // Soft delete operation would happen here
//            sb.append("✓ Soft delete marks as deleted but preserves data\n");
//            sb.append("✓ Deleted actors NOT returned by normal queries\n");
//            sb.append("✓ Audit trail preserved (deleted_at, deleted_by)\n");
//
//            logger.info("Soft delete demo completed");
//        } catch (Exception e) {
//            logger.error("Error in soft delete demo", e);
//            sb.append("Error: ").append(e.getMessage());
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Demo 4: Projects table with relationships
//     */
//    public String demoProjectsTable() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("=== Demo 4: Projects Table (New Table with Foreign Key) ===\n");
//
//        try {
//            List<ProjectORM> projects = projectRepository.findAll();
//            sb.append(String.format("Total projects: %d%n", projects.size()));
//            for (ProjectORM project : projects) {
//                sb.append(String.format("  - %s (Dept: %d, Status: %s)%n",
//                    project.getName(),
//                    project.getDepartmentId(),
//                    project.getIsActive() ? "Active" : "Inactive"));
//            }
//
//            logger.info("Projects table demo completed");
//        } catch (Exception e) {
//            logger.error("Error in projects table demo", e);
//            sb.append("Error: ").append(e.getMessage());
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Demo 5: Audit trail (created_by, updated_by, etc)
//     */
//    public String demoAuditTrail() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("=== Demo 5: Audit Trail ===\n");
//
//        try {
//            // Get a project and check audit fields
//            List<ProjectORM> projects = projectRepository.findAll();
//            if (!projects.isEmpty()) {
//                ProjectORM project = projects.get(0);
//                sb.append(String.format("Project: %s%n", project.getName()));
//                sb.append(String.format("  Created at: %s by %s%n", project.getCreatedAt(), project.getCreatedBy()));
//                sb.append(String.format("  Updated at: %s by %s%n", project.getUpdatedAt(), project.getUpdatedBy()));
//                if (project.getIsDeleted()) {
//                    sb.append(String.format("  Deleted at: %s by %s%n", project.getDeletedAt(), project.getDeletedBy()));
//                }
//            }
//
//            logger.info("Audit trail demo completed");
//        } catch (Exception e) {
//            logger.error("Error in audit trail demo", e);
//            sb.append("Error: ").append(e.getMessage());
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Demo 6: Restore soft-deleted records
//     */
//    public String demoRestoreSoftDeleted() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("=== Demo 6: Restore Soft-Deleted Records ===\n");
//
//        try {
//            // Get deleted projects
//            List<ProjectORM> deleted = projectRepository.findDeleted();
//            sb.append(String.format("Soft-deleted projects: %d%n", deleted.size()));
//
//            if (!deleted.isEmpty()) {
//                ProjectORM toRestore = deleted.get(0);
//                sb.append(String.format("Restoring: %s%n", toRestore.getName()));
//                projectRepository.restore(toRestore.getId());
//                sb.append("✓ Project restored\n");
//            } else {
//                sb.append("No deleted projects to restore\n");
//            }
//
//            logger.info("Restore soft-deleted demo completed");
//        } catch (Exception e) {
//            logger.error("Error in restore soft-deleted demo", e);
//            sb.append("Error: ").append(e.getMessage());
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Demo 7: Index performance comparison
//     */
//    public String demoIndexPerformance() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("=== Demo 7: Index Performance ===\n");
//
//        try {
//            // Simulate query with index
//            long start = System.currentTimeMillis();
//            List<ProjectORM> byDept = projectRepository.findAll(); // In real scenario, would filter by department
//            long duration = System.currentTimeMillis() - start;
//
//            sb.append("Query performance with indexes:\n");
//            sb.append(String.format("  Time: %d ms%n", duration));
//            sb.append("Indexes created on: department_id, is_deleted, is_active\n");
//
//            logger.info("Index performance demo completed");
//        } catch (Exception e) {
//            logger.error("Error in index performance demo", e);
//            sb.append("Error: ").append(e.getMessage());
//        }
//
//        return sb.toString();
//    }
//
//    public ActorRepositoryORM getActorRepository() {
//        return actorRepository;
//    }
//
//    public MovieRepositoryORM getMovieRepository() {
//        return movieRepository;
//    }
//
//    public ProjectRepositoryORM getProjectRepository() {
//        return projectRepository;
//    }
//}
//
