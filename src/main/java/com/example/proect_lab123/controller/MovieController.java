package com.example.proect_lab123.controller;

import com.example.proect_lab123.domain.Movie;
import com.example.proect_lab123.service.*;
import com.example.proect_lab123.util.event.EntityChangeEvent;
import com.example.proect_lab123.util.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.stream.StreamSupport;

public class MovieController implements Observer<EntityChangeEvent<Movie>> {
    private MovieService movieService;
    private ActorService actorService;
    private ObservableList<Movie> model = FXCollections.observableArrayList();

    @FXML TableView<Movie> tableViewMovies;
    @FXML TableColumn<Movie, String> columnTitle;
    @FXML TableColumn<Movie, String> columnGenre;
    @FXML TableColumn<Movie, Float> columnDuration;

    public void setService(MovieService movieService,ActorService actorService) {
        this.movieService = movieService;
        this.actorService=actorService;
        movieService.addObserver(this);
        initModel();
    }

    @FXML
    public void initialize() {
        columnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        columnGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        columnDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        tableViewMovies.setItems(model);

        tableViewMovies.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showActorDetails(newSelection);
            }
        });
    }

    private void initModel() {
        Iterable<Movie> movies = movieService.findAll();
        model.setAll(StreamSupport.stream(movies.spliterator(), false).toList());
    }
    private void showActorDetails(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proect_lab123/actors-view.fxml"));

            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Actors in: " + movie.getTitle());
            stage.setScene(scene);

            ActorDetailsController controller = loader.getController();
            controller.initData(movie, actorService);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Path error: Resource not found!");
        }
    }

    @Override
    public void update(EntityChangeEvent<Movie> event) {
        initModel();
    }
}