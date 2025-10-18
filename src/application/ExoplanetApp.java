package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ExoplanetApp extends Application {

    private ExoplanetManager manager = new ExoplanetManager();
    private TableView<Exoplanet> table = new TableView<>();
    private ObservableList<Exoplanet> exoplanetList = FXCollections.observableArrayList();
    private List<Circle> stars = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        manager.loadFromFile();
        exoplanetList.addAll(manager.getAllExoplanets());

        // --- Title ---
        Label title = new Label("🌌 EXOPLANET EXPLORER");
        title.setFont(Font.font("Arial", 32));
        title.setTextFill(Color.CYAN);
        title.setStyle("-fx-effect: dropshadow(three-pass-box, blue, 20, 0, 0, 0);");

        // --- Table Columns ---
        TableColumn<Exoplanet, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));

        TableColumn<Exoplanet, String> distCol = new TableColumn<>("Distance");
        distCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getDistance())));

        TableColumn<Exoplanet, String> radiusCol = new TableColumn<>("Radius");
        radiusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getRadius())));

        TableColumn<Exoplanet, String> tempCol = new TableColumn<>("Temperature");
        tempCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getTemperature())));

        TableColumn<Exoplanet, String> habCol = new TableColumn<>("Habitability");
        habCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getHabitabilityScore())));

        // --- Color-coded habitability ---
        habCol.setCellFactory(column -> new TableCell<Exoplanet, String>() {
            @Override
            protected void updateItem(String scoreStr, boolean empty) {
                super.updateItem(scoreStr, empty);
                if (empty || scoreStr == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(scoreStr);
                    double score = Double.parseDouble(scoreStr);
                    if(score < 0.4) setTextFill(Color.RED);
                    else if(score < 0.7) setTextFill(Color.YELLOW);
                    else setTextFill(Color.LIME);
                }
            }
        });

        table.getColumns().addAll(nameCol, distCol, radiusCol, tempCol, habCol);
        table.setItems(exoplanetList);
        table.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-control-inner-background: rgba(0,0,0,0.3); -fx-border-color: cyan; -fx-border-width: 1px; -fx-table-cell-border-color: transparent;");

        // --- Input Fields ---
        TextField nameField = new TextField(); nameField.setPromptText("Name");
        TextField distField = new TextField(); distField.setPromptText("Distance AU");
        TextField radiusField = new TextField(); radiusField.setPromptText("Radius R⊕");
        TextField tempField = new TextField(); tempField.setPromptText("Temperature °C");

        // --- Buttons ---
        Button addBtn = createNeonButton("Add");
        addBtn.setOnAction(e -> {
            try {
                Exoplanet exo = new Exoplanet(
                        nameField.getText(),
                        Double.parseDouble(distField.getText()),
                        Double.parseDouble(radiusField.getText()),
                        Double.parseDouble(tempField.getText())
                );
                exo.computeFinalHabitability(1.0, "G");
                manager.addExoplanet(exo);
                exoplanetList.add(exo);

                nameField.clear(); distField.clear(); radiusField.clear(); tempField.clear();
            } catch(Exception ex) { showAlert("Invalid input", "Please enter valid numbers"); }
        });

        TextField searchField = new TextField(); searchField.setPromptText("Search by name");
        Button searchBtn = createNeonButton("Search");
        searchBtn.setOnAction(e -> exoplanetList.setAll(manager.searchByName(searchField.getText())));

        Button showAllBtn = createNeonButton("Show All");
        showAllBtn.setOnAction(e -> { manager.loadFromFile(); exoplanetList.setAll(manager.getAllExoplanets()); });

        Button aboutBtn = createNeonButton("About EXO");
        aboutBtn.setOnAction(e -> showAbout());

        Button chartBtn = createNeonButton("View Habitability Chart");
        chartBtn.setOnAction(e -> showHabitabilityChart());

        HBox inputBox = new HBox(5, nameField, distField, radiusField, tempField, addBtn);
        HBox searchBox = new HBox(5, searchField, searchBtn, showAllBtn, aboutBtn, chartBtn);
        VBox uiBox = new VBox(15, title, table, inputBox, searchBox);
        uiBox.setPadding(new Insets(15));

        // --- Star Field Background ---
        Pane starPane = new Pane();
        for(int i=0; i<200; i++){
            Circle star = new Circle(Math.random()*900, Math.random()*600, Math.random()*2, Color.WHITE);
            stars.add(star);
            starPane.getChildren().add(star);
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), ev -> moveStars()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        StackPane root = new StackPane(starPane, uiBox);
        Scene scene = new Scene(root, 1000, 600, Color.BLACK);

        // --- Planet info popup on double click ---
        table.setRowFactory(tv -> {
            TableRow<Exoplanet> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())) {
                    Exoplanet exo = row.getItem();
                    showPlanetInfo(exo);
                }
            });
            return row;
        });

        primaryStage.setTitle("Exoplanet Explorer - EXO 🌌");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- Neon Button Helper ---
    private Button createNeonButton(String text){
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: linear-gradient(to right, #0f0c29, #302b63, #24243e); " +
                "-fx-text-fill: cyan; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, cyan, 10, 0, 0, 0);");
        return btn;
    }

    // --- Move Stars ---
    private void moveStars(){
        for(Circle star: stars){
            star.setCenterY(star.getCenterY() + 1);
            if(star.getCenterY() > 600) star.setCenterY(0);
        }
    }

    // --- Alert ---
    private void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- About EXO ---
    private void showAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About EXO - Exoplanet Explorer");
        alert.setHeaderText("About the App & Algorithms");

        String content = "EXO - Exoplanet Explorer\n\n" +
                "Created by: Aniket Bisht\n\n" +
                "Algorithms & Calculations Used:\n" +
                "1. Habitability Score Computation:\n" +
                "   - Model 1: Simple Habitable Zone (HZ) calculation\n" +
                "   - Model 2: Earth Similarity Index (ESI)\n" +
                "   - Model 3: Probability-based model depending on star type\n" +
                "   - Model 4: Combined model integrating all above\n\n" +
                "2. Exoplanet Data Management:\n" +
                "   - Loading from text files (planets.txt)\n" +
                "   - Adding, searching, and listing exoplanets\n\n" +
                "3. API Integration:\n" +
                "   - Fetch star luminosity (st_lum) and spectral type (st_spectype)\n" +
                "   - JSON parsing using Gson library\n\n" +
                "UI Enhancements:\n" +
                "   - JavaFX TableView for displaying exoplanet data\n" +
                "   - Dark space-themed background with animated stars\n" +
                "   - Neon/glowing buttons and glowing title\n\n" +
                "Interactive Features:\n" +
                "   - Double-click planets for detailed info popup\n" +
                "   - Color-coded habitability scores\n" +
                "   - Habitability bar chart visualization\n\n" +
                "This app demonstrates exoplanet habitability modeling and interactive visualization in JavaFX.";

        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    // --- Planet Info Popup ---
    private void showPlanetInfo(Exoplanet exo){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Planet Info: " + exo.getName());
        alert.setHeaderText("Details for " + exo.getName());

        String content = "Name: " + exo.getName() + "\n" +
                "Distance (AU): " + exo.getDistance() + "\n" +
                "Radius (R⊕): " + exo.getRadius() + "\n" +
                "Temperature (°C): " + exo.getTemperature() + "\n" +
                "Habitability Score: " + exo.getHabitabilityScore();

        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    // --- Habitability Chart ---
    private void showHabitabilityChart(){
        Stage chartStage = new Stage();
        chartStage.setTitle("Habitability Scores");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Planet");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Habitability Score");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Exoplanets");

        for(Exoplanet exo : exoplanetList){
            series.getData().add(new XYChart.Data<>(exo.getName(), exo.getHabitabilityScore()));
        }

        chart.getData().add(series);
        Scene scene = new Scene(chart, 800, 600);
        chartStage.setScene(scene);
        chartStage.show();
    }
}
