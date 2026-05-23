package com.example.proect_lab123.controller;
import com.example.proect_lab123.domain.Actor;
import com.example.proect_lab123.domain.Movie;
import com.example.proect_lab123.service.ActorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.stream.StreamSupport;

public class ActorDetailsController {
    private ActorService actorService;
    private ObservableList<Actor> model = FXCollections.observableArrayList();

    @FXML TableView<Actor> tableViewActors;
    @FXML TableColumn<Actor, String> columnName;
    @FXML TableColumn<Actor, String> columnBirthday;

    @FXML private Label labelMovieTitle;

    public void initData(Movie movie, ActorService service) {
        this.actorService = service;
        labelMovieTitle.setText("Cast of: " + movie.getTitle());

        var actors = StreamSupport.stream(actorService.findAll().spliterator(), false)
                .filter(a -> a.getIdm() != null && a.getIdm().equals(movie.getId()))
                .toList();
        model.setAll(actors);
    }

    @FXML
    public void initialize() {
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnBirthday.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        tableViewActors.setItems(model);
    }

    public void setService(ActorService service) { this.actorService = service; }

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        // Închide fereastra curentă
        ((javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).close();
    }
}