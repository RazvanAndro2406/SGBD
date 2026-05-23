package com.example.proect_lab123.repository;

import com.example.proect_lab123.config.JdbcUtils;
import com.example.proect_lab123.domain.Movie;
import com.example.proect_lab123.util.paging.Page;
import com.example.proect_lab123.util.paging.Pageable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class MovieRepository implements IMovieRepository {
    private static final Logger logger = LogManager.getLogger(MovieRepository.class);
    private JdbcUtils dbUtils;

    public MovieRepository(Properties props) {
        this.dbUtils = new JdbcUtils(props);
        logger.info("MovieRepository initialized with properties: {}", props);
    }

    @Override
    public Optional<Movie> findOne(Long id) {
        String sql = "SELECT * FROM movies WHERE idm = ?";
        logger.debug("Finding movie id={}", id);
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Movie movie = new Movie(
                        rs.getLong("idm"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getFloat("duration")
                );
                logger.debug("Movie found id={}", id);
                return Optional.of(movie);
            }
            logger.debug("Movie not found id={}", id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Failed to find movie id={}", id, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Movie> findAll() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies";
        logger.debug("Executing findAll for movies");
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                movies.add(new Movie(
                        rs.getLong("idm"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getFloat("duration")
                ));
            }
            logger.debug("findAll returned {} movies", movies.size());
            return movies;
        } catch (SQLException e) {
            logger.error("Failed to fetch all movies", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Movie> save(Movie entity) {
        String sql = "INSERT INTO movies (idm, title, genre, duration) VALUES (?, ?, ?, ?)";
        logger.debug("Saving movie id={} title={}", entity.getId(), entity.getTitle());
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getTitle());
            statement.setString(3, entity.getGenre());
            statement.setFloat(4, entity.getDuration());

            int affectedRows = statement.executeUpdate();
            logger.info("Saved movie id={} affectedRows={}", entity.getId(), affectedRows);
            return affectedRows > 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            logger.error("Failed to save movie id={}", entity.getId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Movie> delete(Long id) {
        Optional<Movie> movie = findOne(id);
        if (movie.isEmpty()) return Optional.empty();

        String sql = "DELETE FROM movies WHERE idm = ?";
        logger.debug("Deleting movie id={}", id);
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            logger.info("Deleted movie id={} affectedRows={}", id, affectedRows);
            return movie;
        } catch (SQLException e) {
            logger.error("Failed to delete movie id={}", id, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Movie> update(Movie entity) {
        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ? WHERE idm = ?";
        logger.debug("Updating movie id={}", entity.getId());
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getGenre());
            statement.setFloat(3, entity.getDuration());
            statement.setLong(4, entity.getId());

            int affectedRows = statement.executeUpdate();
            logger.info("Updated movie id={} affectedRows={}", entity.getId(), affectedRows);
            return affectedRows > 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            logger.error("Failed to update movie id={}", entity.getId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Movie> findAllOnPage(Pageable pageable) {
        return null;
    }
}