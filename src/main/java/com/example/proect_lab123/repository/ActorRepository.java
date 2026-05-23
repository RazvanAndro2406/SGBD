package com.example.proect_lab123.repository;

import com.example.proect_lab123.config.JdbcUtils;
import com.example.proect_lab123.domain.Actor;
import com.example.proect_lab123.util.paging.Page;
import com.example.proect_lab123.util.paging.Pageable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class ActorRepository implements IActorRepository {
    private static final Logger logger = LogManager.getLogger(ActorRepository.class);

    private JdbcUtils dbUtils;

    public ActorRepository(Properties props) {
        this.dbUtils = new JdbcUtils(props);
        logger.info("ActorRepository initialized with properties: {}", props);

    }

    @Override
    public Page<Actor> findAllOnPage(Pageable pageable) {
//        List<Actor> actors = new ArrayList<>();
//        int offset = pageable.getPageNumber() * pageable.getPageSize();
//        String sql = "SELECT * FROM actors LIMIT ? OFFSET ?";
//
//        try (Connection connection = DriverManager.getConnection(url);
//             PreparedStatement statement = connection.prepareStatement(sql)) {
//
//            statement.setInt(1, pageable.getPageSize());
//            statement.setInt(2, offset);
//
//            try (ResultSet resultSet = statement.executeQuery()) {
//                while (resultSet.next()) {
//
//                    Long ida = resultSet.getLong("ida");
//                    String name = resultSet.getString("name");
//                    LocalDate birthday = LocalDate.parse(resultSet.getString("birthday"));
//                    Long idm = resultSet.getLong("idm");
//
//                    Actor actor = new Actor(ida,name, birthday, idm);
//                    actor.setId(ida);
//                    actors.add(actor);
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return new PageImplementation<>(actors, countTotalActors());
        return null;
    }

    @Override
    public Iterable<Actor> findAll() {
        List<Actor> actors = new ArrayList<>();
        String sql = "SELECT * FROM actors";
        logger.debug("Executing findAll for actors");

        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("ida");
                String name = resultSet.getString("name");
                LocalDate birthday = LocalDate.parse(resultSet.getString("birthday"));
                Long idm = resultSet.getLong("idm");

                Actor actor = new Actor(id,name, birthday, idm);
                actor.setId(id);
                actors.add(actor);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch all actors", e);
            throw new RuntimeException("Error fetching all actors", e);
        }
        logger.debug("findAll returned {} actors", actors.size());
        return actors;
    }

    @Override
    public Optional<Actor> update(Actor entity) throws RuntimeException {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        String updateSQL = "UPDATE actors SET name = ?, birthday = ?, idm = ? WHERE ida = ?";

        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSQL)) {

            statement.setString(1, entity.getName());
            statement.setString(2, entity.getBirthday().toString());

            if (entity.getIdm() != null) {
                statement.setLong(3, entity.getIdm());
            } else {
                statement.setNull(3, Types.INTEGER);
            }

            statement.setLong(4, entity.getId());

            int response = statement.executeUpdate();
            logger.info("Updated actor id={} affectedRows={}", entity.getId(), response);

            return response > 0 ? Optional.empty() : Optional.of(entity);

        } catch (SQLException e) {
            logger.error("Failed to update actor id={}", entity.getId(), e);
            throw new RuntimeException("Error updating actor: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Actor> save(Actor entity) throws RuntimeException {
        String insertSQL = "INSERT INTO actors (name, birthday, idm) VALUES (?, ?, ?)";
        logger.debug("Saving actor name={} idm={}", entity.getName(), entity.getIdm());
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {

            statement.setString(1, entity.getName());
            statement.setString(2, entity.getBirthday().toString());


            if (entity.getIdm() != null) {
                statement.setLong(3, entity.getIdm());
            } else {
                statement.setNull(3, Types.INTEGER);
            }

            int response = statement.executeUpdate();
            logger.info("Saved actor name={} affectedRows={}", entity.getName(), response);
            return response > 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            logger.error("Failed to save actor name={}", entity.getName(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Actor> delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        String deleteSQL = "delete from actors where ida = ?";
        logger.debug("Deleting actor id={}", id);
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteSQL)) {
            statement.setLong(1, id);
            Optional<Actor> foundActor = findOne(id);
            int response = 0;
            if (foundActor.isPresent()) {
                response = statement.executeUpdate();
            }
            logger.info("Delete actor id={} affectedRows={}", id, response);
            return response == 0 ? Optional.empty() : foundActor;
        } catch (SQLException e) {
            logger.error("Failed to delete actor id={}", id, e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public Optional<Actor> findOne(Long id) throws RuntimeException {
        logger.debug("Finding actor id={}", id);
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM actors WHERE ida = ?")) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                LocalDate birthday = LocalDate.parse(resultSet.getString("birthday"));

                long idmValue = resultSet.getLong("idm");
                Long idm = resultSet.wasNull() ? null : idmValue;

                Actor actor = new Actor(id, name, birthday, idm);
                logger.debug("Actor found id={}", id);
                return Optional.of(actor);
            }
            logger.debug("Actor not found id={}", id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Failed to find actor id={}", id, e);
            throw new RuntimeException(e);
        }
    }
}
