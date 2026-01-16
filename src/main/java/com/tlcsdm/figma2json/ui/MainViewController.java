package com.tlcsdm.figma2json.ui;

import com.tlcsdm.figma2json.api.Document;
import com.tlcsdm.figma2json.api.FigmaApiClient;
import com.tlcsdm.figma2json.api.FigmaFile;
import com.tlcsdm.figma2json.api.Node;
import com.tlcsdm.figma2json.converter.ConverterFactory;
import com.tlcsdm.figma2json.converter.FigmaConverter;
import com.tlcsdm.figma2json.generator.GeneratorFactory;
import com.tlcsdm.figma2json.generator.ProjectGenerator;
import com.tlcsdm.figma2json.util.SettingsManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Main view controller for the Figma to JSON application.
 */
public class MainViewController implements Initializable {

    @FXML
    private TextField figmaUrlField;

    @FXML
    private Button loadButton;

    @FXML
    private TreeView<Node> pagesTreeView;

    @FXML
    private TreeView<Node> layersTreeView;

    @FXML
    private ComboBox<String> formatComboBox;

    @FXML
    private ComboBox<String> generatorComboBox;

    @FXML
    private TextField outputPathField;

    @FXML
    private Button browseButton;

    @FXML
    private Button exportJsonButton;

    @FXML
    private Button generateProjectButton;

    @FXML
    private TextArea logArea;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label statusLabel;

    private final SettingsManager settingsManager = new SettingsManager();
    private final FigmaApiClient figmaClient = new FigmaApiClient();
    private FigmaFile currentFile;
    private ResourceBundle bundle;
    private PreferencesHelper preferencesHelper;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bundle = resources;

        // Load saved settings
        loadSettings();

        // Initialize combo boxes
        initializeComboBoxes();

        // Set up tree view selection listeners
        setupTreeViewListeners();

        // Set up button actions
        setupButtonActions();

        // Initially disable export/generate buttons
        exportJsonButton.setDisable(true);
        generateProjectButton.setDisable(true);

        log(bundle.getString("log.ready"));
    }

    /**
     * Sets the preferences helper.
     *
     * @param preferencesHelper the preferences helper
     */
    public void setPreferencesHelper(PreferencesHelper preferencesHelper) {
        this.preferencesHelper = preferencesHelper;
    }

    private void loadSettings() {
        figmaUrlField.setText(settingsManager.getLastFigmaUrl());
        outputPathField.setText(settingsManager.getOutputPath());
    }

    private void initializeComboBoxes() {
        // Format combo box
        List<String> formats = new ArrayList<>(ConverterFactory.getAvailableFormats());
        formatComboBox.setItems(FXCollections.observableArrayList(formats.stream()
                .map(String::toUpperCase).toList()));
        formatComboBox.setValue(settingsManager.getOutputFormat().toUpperCase());

        // Generator combo box
        List<String> generators = new ArrayList<>(GeneratorFactory.getAvailableGenerators());
        generatorComboBox.setItems(FXCollections.observableArrayList(generators.stream()
                .map(this::capitalize).toList()));
        generatorComboBox.setValue(capitalize(settingsManager.getGenerator().toLowerCase()));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void setupTreeViewListeners() {
        pagesTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && newValue.getValue() != null) {
                        populateLayersTree(newValue.getValue());
                    }
                });

        layersTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean hasSelection = newValue != null && newValue.getValue() != null;
                    exportJsonButton.setDisable(!hasSelection);
                    generateProjectButton.setDisable(!hasSelection);
                });
    }

    private void setupButtonActions() {
        loadButton.setOnAction(e -> loadFigmaFile());
        browseButton.setOnAction(e -> browseOutputDirectory());
        exportJsonButton.setOnAction(e -> exportToJson());
        generateProjectButton.setOnAction(e -> generateProject());

        // Save settings when fields change
        figmaUrlField.textProperty().addListener((obs, oldVal, newVal) ->
                settingsManager.setLastFigmaUrl(newVal));
        outputPathField.textProperty().addListener((obs, oldVal, newVal) ->
                settingsManager.setOutputPath(newVal));
        formatComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settingsManager.setOutputFormat(newVal.toLowerCase());
            }
        });
        generatorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settingsManager.setGenerator(newVal);
            }
        });
    }

    @FXML
    private void loadFigmaFile() {
        // Get access token from preferences
        String token = preferencesHelper != null ? preferencesHelper.getAccessToken() : settingsManager.getAccessToken();
        String url = figmaUrlField.getText();

        if (token == null || token.isBlank()) {
            showError(bundle.getString("error.noToken"));
            return;
        }

        String fileKey = FigmaApiClient.extractFileKey(url);
        if (fileKey == null) {
            showError(bundle.getString("error.invalidUrl"));
            return;
        }

        setLoading(true);
        log(bundle.getString("log.loading") + ": " + fileKey);

        // Configure the API client with token and API URL
        figmaClient.setAccessToken(token);
        String apiUrl = preferencesHelper != null ? preferencesHelper.getFigmaApiUrl() : settingsManager.getFigmaApiUrl();
        figmaClient.setBaseUrl(apiUrl);
        
        CompletableFuture<FigmaFile> future = figmaClient.getFile(fileKey);

        future.thenAccept(file -> Platform.runLater(() -> {
            currentFile = file;
            populatePagesTree(file);
            setLoading(false);
            log(bundle.getString("log.loaded") + ": " + file.getName());
            statusLabel.setText(bundle.getString("status.loaded") + ": " + file.getName());
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                setLoading(false);
                showError(bundle.getString("error.loadFailed") + ": " + ex.getMessage());
                log(bundle.getString("log.error") + ": " + ex.getMessage());
            });
            return null;
        });
    }

    private void populatePagesTree(FigmaFile file) {
        TreeItem<Node> root = new TreeItem<>();
        root.setExpanded(true);

        Document document = file.getDocument();
        if (document != null && document.getChildren() != null) {
            for (Node page : document.getChildren()) {
                TreeItem<Node> pageItem = new TreeItem<>(page);
                root.getChildren().add(pageItem);
            }
        }

        pagesTreeView.setRoot(root);
        pagesTreeView.setShowRoot(false);
    }

    private void populateLayersTree(Node page) {
        TreeItem<Node> root = createTreeItem(page);
        root.setExpanded(true);
        layersTreeView.setRoot(root);
        layersTreeView.setShowRoot(true);
    }

    private TreeItem<Node> createTreeItem(Node node) {
        TreeItem<Node> item = new TreeItem<>(node);
        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                item.getChildren().add(createTreeItem(child));
            }
        }
        return item;
    }

    @FXML
    private void browseOutputDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(bundle.getString("dialog.selectOutputDir"));

        String currentPath = outputPathField.getText();
        if (!currentPath.isBlank()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists()) {
                chooser.setInitialDirectory(currentDir);
            }
        }

        File selectedDir = chooser.showDialog(browseButton.getScene().getWindow());
        if (selectedDir != null) {
            outputPathField.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    private void exportToJson() {
        TreeItem<Node> selected = layersTreeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            showError(bundle.getString("error.noLayerSelected"));
            return;
        }

        Node node = selected.getValue();
        String formatValue = formatComboBox.getValue();
        if (formatValue == null || formatValue.isBlank()) {
            showError(bundle.getString("error.noConverter") + ": " + formatValue);
            return;
        }
        String format = formatValue.toLowerCase();
        FigmaConverter converter = ConverterFactory.getConverter(format);

        if (converter == null) {
            showError(bundle.getString("error.noConverter") + ": " + format);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("dialog.saveAs"));
        String nodeName = node.getName() != null ? node.getName() : node.getId();
        fileChooser.setInitialFileName(nodeName + "." + converter.getFileExtension());
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.toUpperCase() + " files",
                        "*." + converter.getFileExtension()));

        String outputPath = outputPathField.getText();
        if (!outputPath.isBlank()) {
            File outputDir = new File(outputPath);
            if (outputDir.exists()) {
                fileChooser.setInitialDirectory(outputDir);
            }
        }

        File file = fileChooser.showSaveDialog(exportJsonButton.getScene().getWindow());
        if (file != null) {
            try {
                String content = converter.convert(node);
                Files.writeString(file.toPath(), content);
                log(bundle.getString("log.exported") + ": " + file.getAbsolutePath());
                showSuccess(bundle.getString("success.exported"));
            } catch (IOException e) {
                showError(bundle.getString("error.exportFailed") + ": " + e.getMessage());
                log(bundle.getString("log.error") + ": " + e.getMessage());
            }
        }
    }

    @FXML
    private void generateProject() {
        TreeItem<Node> selected = layersTreeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            showError(bundle.getString("error.noLayerSelected"));
            return;
        }

        String outputPath = outputPathField.getText();
        if (outputPath.isBlank()) {
            showError(bundle.getString("error.noOutputPath"));
            return;
        }

        Node node = selected.getValue();
        String generatorValue = generatorComboBox.getValue();
        if (generatorValue == null || generatorValue.isBlank()) {
            showError(bundle.getString("error.noGenerator") + ": " + generatorValue);
            return;
        }
        String generatorName = generatorValue.toLowerCase();
        ProjectGenerator generator = GeneratorFactory.getGenerator(generatorName);

        if (generator == null) {
            showError(bundle.getString("error.noGenerator") + ": " + generatorName);
            return;
        }

        FigmaConverter converter = ConverterFactory.getDefaultConverter();
        String jsonData = converter.convert(node);

        if (!generator.canGenerate(jsonData)) {
            showError(bundle.getString("error.cannotGenerate"));
            return;
        }

        setLoading(true);
        log(bundle.getString("log.generating") + ": " + generator.getName());

        String projectName = node.getName() != null ? node.getName() : node.getId();
        CompletableFuture.runAsync(() -> {
            try {
                Path projectPath = Path.of(outputPath, projectName + "_project");
                generator.generate(jsonData, projectPath);
                Platform.runLater(() -> {
                    setLoading(false);
                    log(bundle.getString("log.generated") + ": " + projectPath);
                    showSuccess(bundle.getString("success.generated"));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError(bundle.getString("error.generateFailed") + ": " + e.getMessage());
                    log(bundle.getString("log.error") + ": " + e.getMessage());
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        loadButton.setDisable(loading);
        exportJsonButton.setDisable(loading);
        generateProjectButton.setDisable(loading);
    }

    private void log(String message) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("dialog.error"));
        alert.setHeaderText(null);
        
        // Use a TextArea for the content to make it copyable
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setPrefRowCount(4);
        textArea.setStyle("-fx-font-family: monospace;");
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setMinWidth(450);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Notifications.create()
                .title(bundle.getString("dialog.success"))
                .text(message)
                .showInformation();
    }
}
