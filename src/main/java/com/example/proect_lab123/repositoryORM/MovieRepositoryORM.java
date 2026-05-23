package com.example.proect_lab123.repositoryORM;

import com.example.proect_lab123.config.HibernateOrmConfig;
import com.example.proect_lab123.config.JdbcUtils;
import com.example.proect_lab123.domain.Movie;
import com.example.proect_lab123.repository.IMovieRepository;
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
 * Hibernate-based repository implementation for Movie using ORM API.
 * Uses JdbcUtils pattern with Properties for configuration.
 */
public class MovieRepositoryORM implements IMovieRepository {
    private static final Logger logger = LogManager.getLogger(MovieRepositoryORM.class);

    private final SessionFactory sessionFactory;

    public MovieRepositoryORM(Properties props) {
        this.sessionFactory = HibernateOrmConfig.createSessionFactory(props);
        logger.info("MovieRepositoryORM initialized with properties-based configuration");
    }

    public MovieRepositoryORM(JdbcUtils dbUtils) {
        this.sessionFactory = dbUtils.getEntityManagerFactory();
        logger.info("MovieRepositoryORM initialized with JdbcUtils");
    }

    @Override
    public Optional<Movie> findOne(Long id) {
        logger.debug("Finding movie by id={}", id);

        try (Session session = sessionFactory.openSession()) {
            Movie movie = session.get(Movie.class, id);
            if (movie != null) {
                logger.debug("Movie found: id={}", id);
                return Optional.of(movie);
            }
            logger.debug("Movie not found: id={}", id);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to find movie by id={}", id, e);
            throw new RuntimeException("Error finding movie", e);
        }
    }

    @Override
    public Iterable<Movie> findAll() {
        logger.debug("Fetching all movies");

        try (Session session = sessionFactory.openSession()) {
            Query<Movie> query = session.createQuery("FROM Movie", Movie.class);
            List<Movie> movies = query.getResultList();
            logger.debug("findAll returned {} movies", movies.size());
            return movies;
        } catch (Exception e) {
            logger.error("Failed to fetch all movies", e);
            throw new RuntimeException("Error fetching all movies", e);
        }
    }

    @Override
    public Optional<Movie> save(Movie entity) {
        logger.debug("Saving movie: title={}, genre={}", entity.getTitle(), entity.getGenre());

        // 1. Open the session - this is the "Outer" try
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            // 2. Start a "Nested" try for the transaction work
            try {
                transaction = session.beginTransaction();

                session.persist(entity);

                transaction.commit();
                logger.info("Movie saved successfully: id={}", entity.getId());
                return Optional.empty();

            } catch (Exception e) {
                // 3. The session is still OPEN here, so rollback works!
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                // Rethrow so the outer catch can log it properly
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to save movie: title={}", entity.getTitle(), e);
            // We throw a RuntimeException to signify a critical failure
            throw new RuntimeException("Error saving movie", e);
        }
    }
    @Override
    public Optional<Movie> delete(Long id) {
        logger.debug("Deleting movie: id={}", id);

        // 1. Session opens and stays open for the entire block
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                // 2. Start the transaction inside the nested try
                transaction = session.beginTransaction();

                Movie movie = session.get(Movie.class, id);
                if (movie != null) {
                    session.remove(movie);
                    transaction.commit();
                    logger.info("Movie deleted successfully: id={}", id);
                    return Optional.of(movie);
                }

                // Commit even if not found to finish the transaction lifecycle
                transaction.commit();
                logger.debug("Movie not found for deletion: id={}", id);
                return Optional.empty();

            } catch (Exception e) {
                // 3. Rollback works because the session is still active here!
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw e; // Pass it to the outer catch
            }
        } catch (Exception e) {
            logger.error("Failed to delete movie: id={}", id, e);
            throw new RuntimeException("Error deleting movie", e);
        }
    }
    @Override
    public Optional<Movie> update(Movie entity) {
        logger.debug("Updating movie: id={}", entity.getId());

        // Outer try: Manages the Session lifecycle
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            // Inner try: Manages the Transaction lifecycle
            try {
                transaction = session.beginTransaction();

                // merge() is perfect for updating detached entities in Hibernate
                session.merge(entity);

                transaction.commit();
                logger.info("Movie updated successfully: id={}", entity.getId());
                return Optional.empty();

            } catch (Exception e) {
                // This rollback now works because the session is still open!
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw e; // Rethrow to let the outer catch handle logging
            }
        } catch (Exception e) {
            logger.error("Failed to update movie: id={}", entity.getId(), e);
            throw new RuntimeException("Error updating movie", e);
        }
    }
    @Override
    public Page<Movie> findAllOnPage(Pageable pageable) {
        logger.debug("Fetching movies on page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());

        try (Session session = sessionFactory.openSession()) {
            // Get total count
            Query<Long> countQuery = session.createQuery("SELECT COUNT(m) FROM Movie m", Long.class);
            long totalCount = countQuery.uniqueResult();

            // Get paginated results
            int offset = pageable.getPageNumber() * pageable.getPageSize();
            Query<Movie> query = session.createQuery("FROM Movie ORDER BY id", Movie.class);
            query.setFirstResult(offset);
            query.setMaxResults(pageable.getPageSize());
            List<Movie> movies = query.getResultList();

            logger.debug("Fetched {} movies for page {}", movies.size(), pageable.getPageNumber());
            return null;
        } catch (Exception e) {
            logger.error("Failed to fetch paginated movies", e);
            throw new RuntimeException("Error fetching paginated movies", e);
        }
    }

    /**
     * Batch insert movies for performance testing
     */
    public long batchInsert(List<Movie> movies) {
        logger.info("Starting batch insert of {} movies", movies.size());
        long startTime = System.nanoTime();

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            for (int i = 0; i < movies.size(); i++) {
                session.persist(movies.get(i));
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // Convert to ms
            logger.info("Batch insert completed in {}ms for {} movies", duration, movies.size());
            return duration;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Batch insert failed", e);
            throw new RuntimeException("Error in batch insert", e);
        }
    }

    /**
     * Find a movie by ID (helper method)
     */
    public Optional<Movie> findById(Long id) {
        return findOne(id);
    }

}
