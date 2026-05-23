package com.example.proect_lab123.repositoryORM;

import com.example.proect_lab123.config.HibernateOrmConfig;
import com.example.proect_lab123.config.JdbcUtils;
import com.example.proect_lab123.domain.Actor;
import com.example.proect_lab123.repository.IActorRepository;
import com.example.proect_lab123.repository.PagingRepository;
import com.example.proect_lab123.util.paging.Page;
import com.example.proect_lab123.util.paging.Pageable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Hibernate-based repository implementation for Actor using ORM API.
 * Uses JdbcUtils pattern with Properties for configuration.
 */
public class ActorRepositoryORM implements IActorRepository {
    private static final Logger logger = LogManager.getLogger(ActorRepositoryORM.class);

    private final SessionFactory sessionFactory;

    public ActorRepositoryORM(Properties props) {
        this.sessionFactory = HibernateOrmConfig.createSessionFactory(props);
        logger.info("ActorRepositoryORM initialized with properties-based configuration");
    }

    public ActorRepositoryORM(JdbcUtils dbUtils) {
        this.sessionFactory = dbUtils.getEntityManagerFactory();
        logger.info("ActorRepositoryORM initialized with JdbcUtils");
    }

    @Override
    public Optional<Actor> findOne(Long id) {
        logger.debug("Finding actor by id={}", id);

        try (Session session = sessionFactory.openSession()) {
            Actor actor = session.get(Actor.class, id);
            if (actor != null) {
                logger.debug("Actor found: id={}", id);
                return Optional.of(actor);
            }
            logger.debug("Actor not found: id={}", id);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to find actor by id={}", id, e);
            throw new RuntimeException("Error finding actor", e);
        }
    }

    @Override
    public Iterable<Actor> findAll() {
        logger.debug("Fetching all actors");

        try (Session session = sessionFactory.openSession()) {
            Query<Actor> query = session.createQuery("FROM Actor", Actor.class);
            List<Actor> actors = query.getResultList();
            logger.debug("findAll returned {} actors", actors.size());
            return actors;
        } catch (Exception e) {
            logger.error("Failed to fetch all actors", e);
            throw new RuntimeException("Error fetching all actors", e);
        }
    }

    @Override
    public Optional<Actor> save(Actor entity) {
        logger.debug("Saving actor: name={}", entity.getName());

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.persist(entity);
                transaction.commit();
                logger.info("Actor saved successfully: id={}", entity.getId());
                return Optional.empty();
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to save actor: name={}", entity.getName(), e);
            throw new RuntimeException("Error saving actor", e);
        }
    }

    @Override
    public Optional<Actor> delete(Long id) {
        logger.debug("Deleting actor: id={}", id);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                Actor actor = session.get(Actor.class, id);
                if (actor != null) {
                    session.remove(actor);
                    transaction.commit();
                    logger.info("Actor deleted successfully: id={}", id);
                    return Optional.of(actor);
                }

                transaction.commit();
                logger.debug("Actor not found for deletion: id={}", id);
                return Optional.empty();

            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to delete actor: id={}", id, e);
            throw new RuntimeException("Error deleting actor", e);
        }
    }

    @Override
    public Optional<Actor> update(Actor entity) {
        logger.debug("Updating actor: id={}", entity.getId());

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.merge(entity);
                transaction.commit();
                logger.info("Actor updated successfully: id={}", entity.getId());
                return Optional.empty();

            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to update actor: id={}", entity.getId(), e);
            throw new RuntimeException("Error updating actor", e);
        }
    }

    @Override
    public Page<Actor> findAllOnPage(Pageable pageable) {
        logger.debug("Fetching actors on page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());

        try (Session session = sessionFactory.openSession()) {
            Query<Long> countQuery = session.createQuery("SELECT COUNT(a) FROM Actor a", Long.class);
            long totalCount = countQuery.uniqueResult();

            int offset = pageable.getPageNumber() * pageable.getPageSize();
            Query<Actor> query = session.createQuery("FROM Actor ORDER BY id", Actor.class);
            query.setFirstResult(offset);
            query.setMaxResults(pageable.getPageSize());
            List<Actor> actors = query.getResultList();

            logger.debug("Fetched {} actors for page {}", actors.size(), pageable.getPageNumber());
            return null;
        } catch (Exception e) {
            logger.error("Failed to fetch paginated actors", e);
            throw new RuntimeException("Error fetching paginated actors", e);
        }
    }

    /**
     * Batch insert actors for performance testing
     */
    public long batchInsert(List<Actor> actors) {
        logger.info("Starting batch insert of {} actors", actors.size());
        long startTime = System.nanoTime();

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                for (int i = 0; i < actors.size(); i++) {
                    session.persist(actors.get(i));
                    if (i % 20 == 0) {
                        session.flush();
                        session.clear();
                    }
                }
                transaction.commit();

                long duration = (System.nanoTime() - startTime) / 1_000_000;
                return duration;

            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            logger.error("Batch insert failed", e);
            throw new RuntimeException("Error in batch insert", e);
        }
    }

    /**
     * Find an actor by ID (helper method)
     */
    public Optional<Actor> findById(Long id) {
        return findOne(id);
    }

}