// view/MultiplayerSetupView.java
package view;

import controller.GameController;
import controller.Main;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import multiplayer.GameSettings;
import multiplayer.LanDiscoveryService;
import multiplayer.MpMessage;
import multiplayer.MpMessageType;
import multiplayer.MultiplayerSession;
import multiplayer.NetworkSession;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MultiplayerSetupView extends BorderPane {

    private final Main mainApp;

    // ✅ FIX Join flash styles
    private static final String JOIN_ENABLED_STYLE =
            "-fx-background-color: #2563EB;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 14;" +
            "-fx-padding: 0 18 0 18;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;";

    private static final String JOIN_DISABLED_STYLE =
            "-fx-background-color: #1E293B;" +
            "-fx-text-fill: rgba(255,255,255,0.55);" +
            "-fx-background-radius: 14;" +
            "-fx-padding: 0 18 0 18;" +
            "-fx-opacity: 0.75;";

    // UI
    private final Button backBtn = new Button("Menu");
    private final TextField nameField = new TextField();

    private final RadioButton easyBtn = new RadioButton("Easy");
    private final RadioButton mediumBtn = new RadioButton("Medium");
    private final RadioButton hardBtn = new RadioButton("Hard");

    private final Button hostBtn = new Button("Host");
    private final Button joinBtn = new Button("Join Selected");
    private final Button refreshBtn = new Button("Refresh");

    private final Label statusLabel = new Label("");
    private final ListView<LanDiscoveryService.DiscoveredHost> hostsList = new ListView<>();

    // Networking
    private final LanDiscoveryService discovery = new LanDiscoveryService();
    private final Map<String, LanDiscoveryService.DiscoveredHost> discoveredMap = new ConcurrentHashMap<>();

    private final BooleanProperty hosting = new SimpleBooleanProperty(false);
    private final BooleanProperty joined  = new SimpleBooleanProperty(false);

    private volatile ServerSocket serverSocket;

    private enum MpState { IDLE, HOSTING, JOINING, IN_GAME }
    private MpState state = MpState.IDLE;

    private NetworkSession activeSession = null;
    private MultiplayerSession mpSession = null;

    public MultiplayerSetupView(Main mainApp) {
        this.mainApp = mainApp;
        buildUI();
        startDiscoveryListener();

        hosting.set(false);
        joined.set(false);
        status("Ready");
    }

    private void buildUI() {
        setStyle("-fx-background-color: #0f172a;");

        // ===== TOP BAR =====
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(18, 28, 8, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);

        styleMenuButton(backBtn);
        backBtn.setOnAction(e -> {
            cleanupNetworking();
            Main.showMainMenu(Main.getPrimaryStage());
        });

        Label title = new Label("Multiplayer (LAN)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, spacer, title);
        setTop(topBar);

        // ===== CENTER CONTENT =====
        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(18, 30, 20, 30));

        Label subtitle = new Label("Host picks difficulty • Each player enters their name • Same Wi-Fi required");
        subtitle.setTextFill(Color.web("#9CA3AF"));
        subtitle.setFont(Font.font("Arial", 13));

        // Name card
        VBox nameCard = card();
        Label nameLbl = new Label("Your Name");
        nameLbl.setTextFill(Color.web("#E5E7EB"));
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        nameField.setPromptText("Enter your name...");
        nameField.setFont(Font.font("Arial", 14));
        nameField.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-text-fill: #E5E7EB;
            -fx-prompt-text-fill: rgba(148,163,184,0.8);
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: rgba(148,163,184,0.18);
            -fx-padding: 11 14;
        """);
        nameCard.getChildren().addAll(nameLbl, nameField);

        // Host card
        VBox hostCard = card();
        Label hostLbl = new Label("Host a Game (you choose difficulty)");
        hostLbl.setTextFill(Color.web("#BFDBFE"));
        hostLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ToggleGroup diffGroup = new ToggleGroup();
        easyBtn.setToggleGroup(diffGroup);
        mediumBtn.setToggleGroup(diffGroup);
        hardBtn.setToggleGroup(diffGroup);
        easyBtn.setSelected(true);

        styleRadio(easyBtn, "#22C55E");
        styleRadio(mediumBtn, "#3B82F6");
        styleRadio(hardBtn, "#EF4444");

        HBox diffs = new HBox(18, easyBtn, mediumBtn, hardBtn);
        diffs.setAlignment(Pos.CENTER_LEFT);

        stylePrimary(hostBtn, "#22C55E");

        // Host disabled when joined or empty name
        hostBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> safeName().isEmpty() || joined.get(),
                nameField.textProperty(),
                joined
        ));
        hostBtn.setOnAction(e -> startHosting());

        hostCard.getChildren().addAll(hostLbl, diffs, hostBtn);

        // Join card
        VBox joinCard = card();
        Label joinLbl = new Label("Join a Game");
        joinLbl.setTextFill(Color.web("#BFDBFE"));
        joinLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        hostsList.setPlaceholder(new Label("No hosts found yet. Click Refresh."));
        hostsList.setStyle("""
            -fx-background-color: #020617;
            -fx-control-inner-background: #020617;
            -fx-border-color: rgba(148,163,184,0.18);
            -fx-border-radius: 14;
            -fx-background-radius: 14;
            -fx-text-fill: #E5E7EB;
        """);

        hostsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LanDiscoveryService.DiscoveredHost item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.hostName() + "  •  " + item.difficulty() + "  •  " + item.ip());
                    setTextFill(Color.web("#E5E7EB"));
                }
            }
        });

        styleSecondary(refreshBtn);
     // ✅ Stable Join styles (no flash)
        joinBtn.setPrefHeight(40);
        joinBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // initial (safe)
        joinBtn.setStyle(JOIN_DISABLED_STYLE);

        // when disabled/enabled changes
        joinBtn.disabledProperty().addListener((obs, wasDisabled, isDisabled) -> {
            joinBtn.setStyle(isDisabled ? JOIN_DISABLED_STYLE : JOIN_ENABLED_STYLE);
        });

        // when selection changes (extra safety)
        hostsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean disabled = joinBtn.isDisabled();
            joinBtn.setStyle(disabled ? JOIN_DISABLED_STYLE : JOIN_ENABLED_STYLE);
        });

        // also re-apply on press/release (prevents skin "flash")
        joinBtn.setOnMousePressed(e -> {
            boolean disabled = joinBtn.isDisabled();
            joinBtn.setStyle(disabled ? JOIN_DISABLED_STYLE : JOIN_ENABLED_STYLE);
        });
        joinBtn.setOnMouseReleased(e -> {
            boolean disabled = joinBtn.isDisabled();
            joinBtn.setStyle(disabled ? JOIN_DISABLED_STYLE : JOIN_ENABLED_STYLE);
        });


        refreshBtn.setOnAction(e -> refreshHostList());

        // Join disabled when hosting OR empty name OR no selection
        joinBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> hosting.get() || safeName().isEmpty() || hostsList.getSelectionModel().getSelectedItem() == null,
                hosting,
                nameField.textProperty(),
                hostsList.getSelectionModel().selectedItemProperty()
        ));
        joinBtn.setOnAction(e -> joinSelectedHost());

        // ✅ Force stable style (prevents "flash")
        joinBtn.setStyle(JOIN_DISABLED_STYLE);

        joinBtn.disabledProperty().addListener((obs, wasDisabled, isDisabled) -> {
            joinBtn.setStyle(isDisabled ? JOIN_DISABLED_STYLE : JOIN_ENABLED_STYLE);
        });

        // Also re-apply style when selection changes (extra safety)
        hostsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean disabled = joinBtn.isDisabled();
            joinBtn.setStyle(disabled ? JOIN_DISABLED_STYLE : JOIN_ENABLED_STYLE);
        });

        HBox joinButtons = new HBox(10, refreshBtn, joinBtn);
        joinButtons.setAlignment(Pos.CENTER_LEFT);

        joinCard.getChildren().addAll(joinLbl, hostsList, joinButtons);

        // Busy binding
        BooleanProperty busy = new SimpleBooleanProperty();
        busy.bind(hosting.or(joined));

        easyBtn.disableProperty().bind(busy);
        mediumBtn.disableProperty().bind(busy);
        hardBtn.disableProperty().bind(busy);
        refreshBtn.disableProperty().bind(busy);
        hostsList.disableProperty().bind(busy);
        nameField.disableProperty().bind(busy);

        statusLabel.setTextFill(Color.web("#93C5FD"));
        statusLabel.setFont(Font.font("Arial", 13));

        root.getChildren().addAll(subtitle, nameCard, hostCard, joinCard, statusLabel);
        setCenter(root);

        refreshHostList();
    }

    private void startDiscoveryListener() {
        discovery.startListening(host -> {
            String key = host.ip() + ":" + host.port();
            discoveredMap.put(key, host);
            Platform.runLater(this::refreshHostList);
        });
    }

    private void refreshHostList() {
        List<LanDiscoveryService.DiscoveredHost> list = new ArrayList<>(discoveredMap.values());
        list.sort(Comparator.comparing(LanDiscoveryService.DiscoveredHost::hostName)
                .thenComparing(LanDiscoveryService.DiscoveredHost::ip));
        hostsList.getItems().setAll(list);
    }

    private void setState(MpState newState) {
        state = newState;
        if (state == MpState.IN_GAME) status("Starting game...");
    }

    private void stopHostingIfRunning() {
        discovery.stop();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (Exception ignored) {}
        serverSocket = null;
    }

    private void cleanupAllNetworking() {
        discovery.stop();
        stopHostingIfRunning();
    }

    private void resetToIdle() {
        cleanupAllNetworking();
        hosting.set(false);
        joined.set(false);
        setState(MpState.IDLE);
        status("Ready");
    }

    private void startHosting() {
        if (hosting.get() || joined.get()) return;

        hosting.set(true);
        joined.set(false);
        setState(MpState.HOSTING);

        stopHostingIfRunning();
        status("Hosting... waiting for a player to join");

        String hostName = safeName();
        String difficulty = easyBtn.isSelected() ? "Easy" : mediumBtn.isSelected() ? "Medium" : "Hard";

        Thread t = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(0)) {
                serverSocket = ss;
                int port = ss.getLocalPort();

                discovery.startHostingBroadcast(hostName, difficulty, port);

                Socket client = ss.accept();
                activeSession = new NetworkSession(client);

                activeSession.startReceiverLoop(msg -> {

                    // ✅ join handshake
                    if (msg.type == MpMessageType.HELLO_JOIN) {
                        String joinName = (String) msg.payload;

                        // ✅ generate ONE seed on host and send it
                        long seed = System.currentTimeMillis();
                        GameSettings settings = new GameSettings(hostName, joinName, difficulty, seed);

                        try {
                            activeSession.send(new MpMessage(MpMessageType.GAME_SETTINGS, settings));
                        } catch (Exception ignored) {}

                        Platform.runLater(() -> {
                            cleanupAllNetworking();
                            setState(MpState.IN_GAME);

                            GameController gc = mainApp.startGameFromSetupReturnController(
                                    settings.hostName, settings.joinName, settings.difficulty, settings.seed
                            );

                            mpSession = new MultiplayerSession(activeSession);
                            mpSession.setRole(true); // ✅ HOST
                            gc.attachMultiplayer(mpSession);
                        });

                        return;
                    }

                    // ✅ After game starts: forward actions/messages
                    if (mpSession != null) {
                        mpSession.handleIncoming(msg);
                    }

                }, err -> Platform.runLater(() -> {
                    status("Connection lost");
                    resetToIdle();
                }));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    status("Host failed");
                    resetToIdle();
                });
            }
        }, "mp-host");
        t.setDaemon(true);
        t.start();
    }

    private void joinSelectedHost() {
        if (hosting.get() || joined.get()) return;

        LanDiscoveryService.DiscoveredHost host = hostsList.getSelectionModel().getSelectedItem();
        if (host == null) return;

        joined.set(true);
        hosting.set(false);
        setState(MpState.JOINING);

        discovery.stop();
        status("Joining " + host.hostName() + "...");

        Thread t = new Thread(() -> {
            try {
                Socket socket = new Socket(host.ip(), host.port());
                activeSession = new NetworkSession(socket);

                activeSession.send(new MpMessage(MpMessageType.HELLO_JOIN, safeName()));

                activeSession.startReceiverLoop(msg -> {

                    // ✅ settings handshake
                    if (msg.type == MpMessageType.GAME_SETTINGS) {
                        GameSettings settings = (GameSettings) msg.payload;

                        Platform.runLater(() -> {
                            cleanupAllNetworking();
                            setState(MpState.IN_GAME);

                            GameController gc = mainApp.startGameFromSetupReturnController(
                                    settings.hostName, settings.joinName, settings.difficulty, settings.seed
                            );

                            mpSession = new MultiplayerSession(activeSession);
                            mpSession.setRole(false); // ✅ CLIENT
                            gc.attachMultiplayer(mpSession);
                        });

                        return;
                    }

                    // ✅ After game starts: forward actions/messages
                    if (mpSession != null) {
                        mpSession.handleIncoming(msg);
                    }

                }, err -> Platform.runLater(() -> {
                    status("Connection lost");
                    resetToIdle();
                }));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    status("Join failed");
                    resetToIdle();
                });
            }
        }, "mp-join");
        t.setDaemon(true);
        t.start();
    }

    private void cleanupNetworking() {
        discovery.stop();
        stopHostingIfRunning();
        resetToIdle();
    }

    private VBox card() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setMaxWidth(720);
        box.setBackground(new Background(new BackgroundFill(
                Color.web("rgba(255,255,255,0.06)"),
                new CornerRadii(18),
                Insets.EMPTY
        )));
        box.setBorder(new Border(new BorderStroke(
                Color.web("rgba(37,99,235,0.20)"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(18),
                new BorderWidths(1.1)
        )));
        box.setEffect(new DropShadow(16, Color.rgb(0,0,0,0.35)));
        return box;
    }

    private String safeName() {
        String s = nameField.getText();
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";
        if (s.length() > 12) s = s.substring(0, 12);
        return s;
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }

    private void styleMenuButton(Button btn) {
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        String normalStyle = """
            -fx-background-color: #1e293b;
            -fx-text-fill: #e5e7eb;
            -fx-background-radius: 999;
            -fx-padding: 7 18;
            -fx-cursor: hand;
        """;

        String hoverStyle = """
            -fx-background-color: #334155;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 999;
            -fx-padding: 7 18;
            -fx-cursor: hand;
        """;

        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
    }

    private void styleRadio(RadioButton rb, String markColor) {
        rb.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rb.setTextFill(Color.web("#E5E7EB"));
        rb.setStyle("-fx-cursor: hand; -fx-mark-color: " + markColor + ";");
    }

    private void stylePrimary(Button btn, String color) {
        btn.setPrefHeight(40);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 0 18 0 18;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleSecondary(Button btn) {
        btn.setPrefHeight(40);
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle(
                "-fx-background-color: rgba(15,23,42,0.90);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(34,197,94,0.35);" +
                "-fx-border-color: rgba(34,197,94,0.65);" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1.2;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: rgba(15,23,42,0.90);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
        ));
    }
}
