package com.example.proect_lab123;

import com.example.proect_lab123.config.Config;
import com.example.proect_lab123.controller.MainController; // Schimbat la MainController
import com.example.proect_lab123.repository.ActorRepository;
import com.example.proect_lab123.repository.MovieRepository;
import com.example.proect_lab123.repositoryORM.ActorRepositoryORM;
import com.example.proect_lab123.repositoryORM.MovieRepositoryORM;
import com.example.proect_lab123.service.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Properties;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Properties props = Config.getProperties();

//        MovieRepository movieRepository = new MovieRepository(props);
//        ActorRepository actorRepository = new ActorRepository(props);
//
//        MovieService movieService = new MovieService(movieRepository);
//        ActorService actorService = new ActorService(actorRepository);

        MovieRepositoryORM movieRepositoryORM = new MovieRepositoryORM(props);
        ActorRepositoryORM actorRepositoryORM = new ActorRepositoryORM(props);

        MovieService movieService = new MovieService(movieRepositoryORM);
        ActorService actorService = new ActorService(actorRepositoryORM);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        MainController mainController = fxmlLoader.getController();
        mainController.setService(actorService,movieService);

        stage.setTitle("Actor Management System - Main");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}