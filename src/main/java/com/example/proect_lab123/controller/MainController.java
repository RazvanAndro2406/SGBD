package com.example.proect_lab123.controller;

import com.example.proect_lab123.domain.Actor;
import com.example.proect_lab123.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MainController {
    @FXML private TableView<Actor> tableActors;
    @FXML private TableColumn<Actor, Long> colId;
    @FXML private TableColumn<Actor, String> colName;
    @FXML private TableColumn<Actor, LocalDate> colBirthday;
    @FXML private TableColumn<Actor, Long> colIdm;

    @FXML private TextField txtName;
    @FXML private DatePicker dpBirthday;
    @FXML private TextField txtIdm;

    private ActorService actorService;
    private MovieService movieService;
    private final ObservableList<Actor> actorModel = FXCollections.observableArrayList();

    public void setService(ActorService service,MovieService movieService) {
        this.actorService = service;
        this.movieService=movieService;
        loadData();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBirthday.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        colIdm.setCellValueFactory(new PropertyValueFactory<>("idm"));
        tableActors.setItems(actorModel);

        // Umplem câmpurile când selectăm un rând
        tableActors.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtName.setText(newSel.getName());
                dpBirthday.setValue(newSel.getBirthday());
                txtIdm.setText(String.valueOf(newSel.getIdm()));
            }
        });
    }

    private void loadData() {
        actorModel.setAll((List<Actor>) actorService.findAll());
    }

    @FXML
    void onAddActor() {
        try {
            Actor a = new Actor(txtName.getText(), dpBirthday.getValue(), Long.parseLong(txtIdm.getText()));
            actorService.save(a);
            loadData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    void onDeleteActor() {
        Actor selected = tableActors.getSelectionModel().getSelectedItem();
        if (selected != null) {
            actorService.delete(selected.getId());
            loadData();
        }
    }

    @FXML
    void onUpdateActor() {
        Actor selected = tableActors.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setName(txtName.getText());
            selected.setBirthday(dpBirthday.getValue());
            selected.setIdm(Long.parseLong(txtIdm.getText()));
            actorService.update(selected);
            loadData();
        }
    }

    @FXML
    void onOpenMovies() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proect_lab123/movies-view.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Movie Management");

        // Dacă MovieController are nevoie de MovieService, îl pasezi aici:
         MovieController mc = loader.getController();
         mc.setService(movieService,actorService);

        stage.show();
    }

    @FXML
    void onOpenTransactionLab() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proect_lab123/transaction-lab-view.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load(), 980, 640));
        stage.setTitle("PostgreSQL Transaction Lab");
        stage.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}