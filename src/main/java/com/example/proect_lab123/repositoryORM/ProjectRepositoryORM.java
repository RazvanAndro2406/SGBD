//package com.example.proect_lab123.repositoryORM;
//
//import com.example.proect_lab123.config.JdbcUtils;
//import com.example.proect_lab123.domain.Project;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.OptimisticLockException;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.util.List;
//
//public class ProjectRepositoryORM {
//
//    private static final Logger logger = LogManager.getLogger(ProjectRepositoryORM.class);
//    private final JdbcUtils dbUtils;
//
//    public ProjectRepositoryORM(JdbcUtils dbUtils) {
//        this.dbUtils = dbUtils;
//    }
//
//    public Project save(Project project) {
//        try (EntityManager em = dbUtils.getEntityManagerFactory().createEntityManager()) {
//            em.getTransaction().begin();
//            try {
//                if (project.getId() == null) {
//                    em.persist(project);
//                    logger.info("Project created: {}", project.getName());
//                } else {
//                    project = em.merge(project);
//                    logger.info("Project updated: {}", project.getName());
//                }
//                em.getTransaction().commit();
//                return project;
//            } catch (OptimisticLockException e) {
//                em.getTransaction().rollback();
//                logger.error("Optimistic lock exception while saving project: {}", project.getId(), e);
//                throw e;
//            } catch (Exception e) {
//                em.getTransaction().rollback();
//                logger.error("Error saving project: {}", project.getId(), e);
//                throw e;
//            }
//        }
//    }
//
//    public Project findById(Long id) {
//        try (EntityManager em = dbUtils.getEntityManagerFactory().createEntityManager()) {
//            return em.find(Project.class, id);
//        } catch (Exception e) {
//            logger.error("Error finding project by ID: {}", id, e);
//            throw e;
//        }
//    }
//
//    public List<Project> findAll() {
//        try (EntityManager em = dbUtils.getEntityManagerFactory().createEntityManager()) {
//            return em.createQuery("SELECT p FROM Project p WHERE p.isDeleted = false", Project.class)
//                    .getResultList();
//        } catch (Exception e) {
//            logger.error("Error finding all projects", e);
//            throw e;
//        }
//    }
//
//    public void delete(Long id, String username) {
//        try (EntityManager em = dbUtils.getEntityManagerFactory().createEntityManager()) {
//            em.getTransaction().begin();
//            try {
//                Project project = em.find(Project.class, id);
//                if (project != null) {
//                    project.softDelete(username);
//                    em.merge(project);
//                    logger.info("Project soft deleted: {} by {}", id, username);
//                }
//                em.getTransaction().commit();
//            } catch (Exception e) {
//                em.getTransaction().rollback();
//                logger.error("Error deleting project: {}", id, e);
//                throw e;
//            }
//        }
//    }
//
//    public void hardDelete(Long id) {
//        try (EntityManager em = dbUtils.getEntityManagerFactory().createEntityManager()) {
//            em.getTransaction().begin();
//            try {
//                Project project = em.find(Project.class, id);
//                if (project != null) {
//                    em.remove(project);
//                    logger.info("Project hard deleted: {}", id);
//                }
//                em.getTransaction().commit();
//            } catch (Exception e) {
//                em.getTransaction().rollback();
//                logger.error("Error hard deleting project: {}", id, e);
//                throw e;
//            }
//        }
//    }
//
//    public void restore(Long id) {
//        try (EntityManager em = dbUtils.getEntityManagerFactory().createEntityManager()) {
//            em.getTransaction().begin();
//            try {
//                Project project = em.find(Project.class, id);
//                if (project != null) {
//                    project.restore();
//                    em.merge(project);
//                    logger.info("Project restored: {}", id);
//                }
//                em.getTransaction().commit();
//            } catch (Exception e) {
//                em.getTransaction().rollback();
//                logger.error("Error restoring project: {}", id, e);
//                throw e;
//            }
//        }
//    }
//
//    public List<Project> findDeleted() {
//        try (EntityManager em = dbUtils.getEntityManagerFactory().createEntityManager()) {
//            return em.createQuery("SELECT p FROM Project p WHERE p.isDeleted = true", Project.class)
//                    .getResultList();
//        } catch (Exception e) {
//            logger.error("Error finding deleted projects", e);
//            throw e;
//        }
//    }
//}
//
