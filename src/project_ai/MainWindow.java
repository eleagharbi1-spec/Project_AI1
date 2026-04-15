package project_ai;

import java.util.Base64;
import java.util.List;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainWindow {

    // ── Couleurs ──────────────────────────────────────────────────────────────
    static final String BG_DARK  = "#0a0c14";
    static final String BG_PANEL = "#101320";
    static final String BG_CARD  = "#161a2a";
    static final String ACCENT   = "#52afff";
    static final String ACCENT2  = "#30d5a0";
    static final String C_EXIT_S = "#ff5a5a";
    static final String TXT_PRI  = "#e6ebff";
    static final String BORDER   = "#28324f";

    // ── État ──────────────────────────────────────────────────────────────────
    BuildingMap        map;
    BFSExplorer        bfs         = new BFSExplorer();
    Timeline           animTimeline;
    boolean            animRunning = false;
    int                animStep    = 0;
    boolean            editMode    = false;

    // ── Composants UI ─────────────────────────────────────────────────────────
    GridCanvas   canvas;// Le canvas de dessin
    Label        lblStatus;// Affiche les messages d'état
    VBox         statsContainer; // Conteneur pour les statistiques dynamiques des agents
    Button       btnRun, btnReset, btnEdit;// Boutons d'action
    Slider       sliderSpeed;// Contrôle de la vitesse d'animation
    ComboBox<String> comboScenario;//	Sélecteur de scénario
    ProgressBar  progressBar;// Bar de progression de l'animation


    // ─────────────────────────────────────────────────────────────────────────
    public MainWindow(Stage stage) {
        stage.setTitle("⬡ Évacuation d'un bâtiment");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        root.setTop(buildTopBar());
        root.setCenter(buildCenter());
        root.setRight(buildRightPanel());
        Scene scene = new Scene(root, 1150.0, 720.0);
        scene.getStylesheets().add(css());
        stage.setScene(scene);
        loadScenario(0);
        stage.show();
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    void setStatus(String msg) {
        Platform.runLater(() -> {
            if (lblStatus != null) lblStatus.setText("> " + msg);
        });
    }

    // ── Barre du haut ─────────────────────────────────────────────────────────
    HBox buildTopBar() {
        HBox bar = new HBox(20.0);
        bar.setStyle("-fx-background-color: " + BG_PANEL + ";"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 0 0 1 0;");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPrefHeight(60.0);
        bar.setPadding(new Insets(0, 20, 0, 20));

        Label logo = new Label("Évacuation d'un bâtiment");
        logo.setFont(Font.font("Monospaced", FontWeight.BOLD, 20.0));
        logo.setTextFill(Color.web(ACCENT));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        comboScenario = new ComboBox<>();
        comboScenario.getItems().addAll(SimulationManager.getScenarioNames());
        comboScenario.getSelectionModel().selectFirst();

        Button btnLoad = flatButton("Charger", ACCENT);
        btnLoad.setOnAction(e ->
                loadScenario(comboScenario.getSelectionModel().getSelectedIndex()));

        bar.getChildren().addAll(logo, spacer, comboScenario, btnLoad);
        return bar;
    }

    // ── Zone centrale (canvas) ────────────────────────────────────────────────
    ScrollPane buildCenter() {
        canvas = new GridCanvas();
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setStyle("-fx-background: " + BG_DARK + ";"
                + "-fx-background-color: " + BG_DARK + ";"
                + "-fx-border-color: transparent;");
        scroll.setPannable(true);
        return scroll;
    }

    // ── Panneau droit ─────────────────────────────────────────────────────────
    VBox buildRightPanel() {
        VBox panel = new VBox(0.0);
        panel.setStyle("-fx-background-color: " + BG_PANEL + ";"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 0 0 0 1;");
        panel.setPrefWidth(280.0);
        panel.setPadding(new Insets(20));

        // ── État du système
        panel.getChildren().add(sectionLabel(" ÉTAT DU SYSTÈME"));
        VBox statusCard = card();
        lblStatus = new Label("Prêt.");
        lblStatus.setTextFill(Color.web(ACCENT));
        lblStatus.setWrapText(true);
        lblStatus.setMinHeight(40.0);
        statusCard.getChildren().add(lblStatus);
        panel.getChildren().addAll(vSpacer(8), statusCard, vSpacer(20));

        // ── Statistiques par agent
        panel.getChildren().add(sectionLabel("STATISTIQUES PAR AGENT"));
        statsContainer = new VBox(8.0);
        panel.getChildren().addAll(vSpacer(8), statsContainer, vSpacer(10));

        // ── Barre de progression
        progressBar = new ProgressBar(0.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        panel.getChildren().add(progressBar);

        // ── Vitesse d'animation
        panel.getChildren().addAll(vSpacer(20), sectionLabel("VITESSE D'ANIMATION"));
        sliderSpeed = new Slider(10.0, 400.0, 80.0);
        panel.getChildren().add(sliderSpeed);

        // ── Édition grille
        panel.getChildren().addAll(vSpacer(20), sectionLabel("ÉDITION GRILLE"));
        HBox editBtns = new HBox(8.0);
        Button bWall = flatButton("Mur",   "#373c5a");
        Button bFree = flatButton("Libre", "#505882");
        bWall.setOnAction(e -> { canvas.paintMode = '#'; setStatus("Outil : MUR"); });
        bFree.setOnAction(e -> { canvas.paintMode = '.'; setStatus("Outil : LIBRE"); });
        HBox.setHgrow(bWall, Priority.ALWAYS);
        HBox.setHgrow(bFree, Priority.ALWAYS);
        editBtns.getChildren().addAll(bWall, bFree);

        btnEdit = flatButton("Activer l'édition", "#64469e");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        btnEdit.setOnAction(e -> toggleEdit());
        panel.getChildren().addAll(vSpacer(8), editBtns, vSpacer(8), btnEdit);

        // ── Actions
        panel.getChildren().addAll(vSpacer(30), sectionLabel("ACTIONS"));
        btnRun = flatButton(" Rechercher", ACCENT2);
        btnRun.setPrefHeight(45.0);
        btnRun.setMaxWidth(Double.MAX_VALUE);
        btnRun.setOnAction(e -> runBFS());

        btnReset = flatButton("Réinitialiser", C_EXIT_S);
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setOnAction(e -> resetView());
        panel.getChildren().addAll(vSpacer(10), btnRun, vSpacer(10), btnReset);

        // ── Légende
        panel.getChildren().addAll(vSpacer(20), sectionLabel("LÉGENDE DES COULEURS"), vSpacer(8));
        VBox legendCard = card();
        legendCard.setSpacing(6.0);
        legendCard.getChildren().addAll(
                buildLegendItem("Départ (S)",        Color.web(ACCENT2)),
                buildLegendItem("Sortie (E)",         Color.web(C_EXIT_S)),
                buildLegendItem("Mur",                Color.web("#141623")),
                buildLegendItem("Zone libre",         Color.web("#ebf0f8")),
                buildLegendItem("Exploration (BFS)",  Color.web(ACCENT)),
                buildLegendItem("Chemin optimal",     Color.web("#ffd23c"))
        );
        panel.getChildren().addAll(legendCard, vSpacer(20));
        return panel;
    }

    // ── Légende item ──────────────────────────────────────────────────────────
    private HBox buildLegendItem(String label, Color color) {
        HBox item = new HBox(10.0);
        item.setAlignment(Pos.CENTER_LEFT);
        Region colorBox = new Region();
        colorBox.setPrefSize(14.0, 14.0);
        String hex = String.format("#%02x%02x%02x",
                (int)(color.getRed()   * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue()  * 255));
        colorBox.setStyle("-fx-background-color: " + hex + ";"
                + "-fx-background-radius: 3;"
                + "-fx-border-color: rgba(255,255,255,0.2);"
                + "-fx-border-radius: 3;");
        Label lbl = new Label(label);
        lbl.setTextFill(Color.web("#a0a5b9"));
        lbl.setFont(Font.font("System", 12.0));
        item.getChildren().addAll(colorBox, lbl);
        return item;
    }

    // ── Toggle édition ────────────────────────────────────────────────────────
    void toggleEdit() {
        editMode = !editMode;
        canvas.editMode = editMode;
        if (editMode) {
            btnEdit.setText("Désactiver l'édition");
            btnEdit.setStyle("-fx-background-color: #c87820; -fx-text-fill: white; -fx-font-weight: bold;");
            setStatus("MODE ÉDITION ACTIF : Cliquez/Glissez sur la grille.");
        } else {
            btnEdit.setText("Activer l'édition");
            btnEdit.setStyle("-fx-background-color: #1a1f33; -fx-text-fill: #64469e; -fx-font-weight: bold;");
            setStatus("Édition désactivée.");
        }
    }

    // ── Lancer le BFS ─────────────────────────────────────────────────────────
    void runBFS() {
        if (animRunning) return;

        canvas.reset();
        canvas.paint();
        statsContainer.getChildren().clear();

        boolean found = bfs.explore(map);

        if (!found) {
            setStatus("ÉCHEC : Aucun chemin vers la sortie !");
            return;
        }

        // ── Cartes de statistiques par agent ──────────────────────────────────
        for (int i = 0; i < bfs.allVisitedOrders.size(); i++) {

            int    visited  = bfs.allVisitedOrders.get(i).size();
            int    maxDepth = bfs.allMaxDepths.get(i);                  // ← NEW
            String dist     = i < bfs.allPathOrders.size()
                    ? "" + (bfs.allPathOrders.get(i).size() - 1)
                    : "Inaccessible";

            VBox uCard = card();

            Label title = new Label("Fuyard #" + (i + 1));
            title.setTextFill(Color.web(ACCENT2));
            title.setFont(Font.font("System", FontWeight.BOLD, 12.0));

            Label lVisited = new Label("Noeuds explorés  : " + visited);
            lVisited.setTextFill(Color.WHITE);

            Label lDepth = new Label("Profondeur max   : " + maxDepth);  // ← NEW
            lDepth.setTextFill(Color.web(ACCENT));

            Label lDist = new Label("Distance finale  : " + dist + " pas");
            lDist.setTextFill(Color.web("#ffd23c"));

            uCard.getChildren().addAll(title, lVisited, lDepth, lDist);
            statsContainer.getChildren().add(uCard);

            // ── Log console (équivalent des print() Python) ──────────────────
            System.out.println("=== Fuyard #" + (i + 1) + " ===");
            for (String log : bfs.allLogs.get(i))   // ← NEW
                System.out.println(log);
            System.out.println("Noeuds explorés : " + visited);
            System.out.println("----------------");
        }

        setStatus("BFS terminé. Lancement de l'animation...");
        animRunning = true;
        btnRun.setDisable(true);

        // ── Calcul des étapes d'animation ─────────────────────────────────────
        int computedMaxV = 0;
        for (List<int[]> l : bfs.allVisitedOrders)
            computedMaxV = Math.max(computedMaxV, l.size());
        final int fMaxV = computedMaxV;

        int computedMaxP = 0;
        for (List<int[]> l : bfs.allPathOrders)
            computedMaxP = Math.max(computedMaxP, l.size());
        final int maxP = computedMaxP;

        final int totalSteps = fMaxV + maxP;

        // ── Timeline d'animation ──────────────────────────────────────────────
        animTimeline = new Timeline(new KeyFrame(
                Duration.millis(sliderSpeed.getValue()),
                e -> {
                    if (animStep < fMaxV) {
                        // Phase 1 : exploration BFS
                        for (List<int[]> list : bfs.allVisitedOrders) {
                            if (animStep < list.size()) {
                                int row = list.get(animStep)[0];
                                int col = list.get(animStep)[1];
                                canvas.visitedSet.add(row + "," + col);
                            }
                        }
                    } else {
                        // Phase 2 : chemin optimal
                        int pIdx = animStep - fMaxV;
                        for (List<int[]> list : bfs.allPathOrders) {
                            if (pIdx < list.size()) {
                                int row = list.get(pIdx)[0];
                                int col = list.get(pIdx)[1];
                                canvas.pathSet.add(row + "," + col);
                            }
                        }
                    }

                    canvas.paint();
                    ++animStep;
                    progressBar.setProgress((double) animStep / totalSteps);

                    if (animStep >= totalSteps) {
                        animTimeline.stop();
                        animRunning = false;
                        btnRun.setDisable(false);
                        setStatus("Simulation terminée.");
                    }
                }
        ));
        animTimeline.setCycleCount(totalSteps + 1);
        animStep = 0;
        animTimeline.play();
    }

    // ── Charger un scénario ───────────────────────────────────────────────────
    void loadScenario(int idx) {
        stopAnim();
        map = new BuildingMap(SimulationManager.getScenario(idx));
        canvas.setMap(map);
        canvas.reset();
        canvas.paint();
        if (statsContainer != null) statsContainer.getChildren().clear();
        setStatus("Scénario chargé : " + SimulationManager.getScenarioName(idx));
    }

    // ── Réinitialiser la vue ──────────────────────────────────────────────────
    void resetView() {
        stopAnim();
        canvas.reset();
        canvas.paint();
        if (statsContainer != null) statsContainer.getChildren().clear();
        progressBar.setProgress(0.0);
        setStatus("Grille réinitialisée.");
    }

    // ── Arrêter l'animation ───────────────────────────────────────────────────
    void stopAnim() {
        if (animTimeline != null) animTimeline.stop();
        animRunning = false;
        animStep    = 0;
        if (btnRun != null) btnRun.setDisable(false);
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
    VBox card() {
        VBox c = new VBox(5.0);
        c.setStyle("-fx-background-color: " + BG_CARD + ";"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 6; -fx-background-radius: 6;");
        c.setPadding(new Insets(10));
        return c;
    }

    Button flatButton(String txt, String fg) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color: #1a1f33;"
                + "-fx-text-fill: " + fg + ";"
                + "-fx-font-weight: bold;"
                + "-fx-border-color: " + fg + "44;"
                + "-fx-border-radius: 6;"
                + "-fx-cursor: hand;");
        return b;
    }

    Label sectionLabel(String txt) {
        Label l = new Label(txt);
        l.setFont(Font.font("Monospaced", FontWeight.BOLD, 11.0));
        l.setTextFill(Color.web("#4a557a"));
        return l;
    }

    Region vSpacer(double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }

    private String css() {
        return "data:text/css;base64,"
                + Base64.getEncoder().encodeToString((
                ".scroll-pane { -fx-background: #0a0c14; -fx-border-color: transparent; } "
                        + ".progress-bar > .bar { -fx-background-color: #52afff; }"
        ).getBytes());
    }
}