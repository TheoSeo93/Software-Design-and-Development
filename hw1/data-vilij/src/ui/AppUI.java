package ui;

import actions.AppActions;
import algorithms.Algorithm;
import algorithm_list.RandomClassifier;
import algorithm_list.KMeansClusterer;
import algorithm_list.RandomClusterer;
import data.DataSet;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static settings.AppPropertyTypes.*;
import static ui.DataVisualizer.*;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;
    TSDProcessor tsdProcessor = new TSDProcessor();

    @SuppressWarnings("FieldCanBeLocal")
    private Path currentDataPath;
    private ComboBox comboBox;
    private ToggleButton toggleButton;
    private Button scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number> chart;          // the chart where data will be displayed
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;       // text area for new data input
    private boolean hasNewText;     // whether or not the text area has any new data since last display
    private Pane hBox;
    private Text textAreaTitle;
    private String scrnShotIconPath;
    private String currentText;
    private String displayIconPath;
    private String settingIconPath;
    private String configImgPath;
    private ImageView settings1;
    private ImageView settings2;
    private ImageView settings3;
    private boolean moreThanTen;
    private StringBuilder pendingText;
    private String textAreaLbl;
    private Algorithm[] randomClassifiers;
    private TextFlow dataDescription;
    private ImageView[] imageViews;
    private RadioButton[] radioButtons;
    private ImageView configImg;
    private String newCss;
    private KMeansClusterer[] kMeansClusterers;
    private int countClicked;
    private RandomClusterer[] randomClusterers;
    private static Algorithm.AlgorithmType algorithmType;
    private HBox displayBox;
    private Algorithm currentAlgorithm;
    private Label textWatchDisplay;
    private Object[] algorithms;

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {

        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        newCss = this.getClass().getResource(applicationTemplate.manager.getPropertyValue(CSS_ADDRESS.toString())).toExternalForm();
        PropertyManager manager = applicationTemplate.manager;
        kMeansClusterers = new KMeansClusterer[3];
        randomClusterers = new RandomClusterer[3];
        randomClassifiers = new RandomClassifier[3];
        algorithms = new Object[]{randomClassifiers,};
        hBox = new HBox();
        textAreaTitle = new Text(manager.getPropertyValue(TEXT_AREA_TITLE.toString()));
        textAreaTitle.setFont(new Font(17));
        textAreaTitle.getStyleClass().add(manager.getPropertyValue(LABELS.toString()));
        //Text FontSize to be corrected
        textAreaLbl = manager.getPropertyValue(TEXT_AREA.toString());
        textArea = new TextArea();
        dataDescription = new TextFlow();
        dataDescription.setPrefWidth(textArea.getWidth());
        toggleButton = new ToggleButton(manager.getPropertyValue(READ_ONLY.toString()));
        toggleButton.getStylesheets().setAll(newCss);
        toggleButton.getStyleClass().add(manager.getPropertyValue(OTHER_BUTTON.toString()));
        comboBox = new ComboBox();
        comboBox.getStylesheets().setAll(newCss);
        displayButton.getStylesheets().setAll(newCss);
        displayButton.getStyleClass().add(manager.getPropertyValue(OTHER_BUTTON.toString()));
        displayButton.setTooltip(new Tooltip(manager.getPropertyValue(DISPLAY_TOOLTIP.toString())));
        displayButton.setDisable(true);
        settings1 = new ImageView(new Image(getClass().getResourceAsStream(settingIconPath)));
        settings2 = new ImageView(new Image(getClass().getResourceAsStream(settingIconPath)));
        settings3 = new ImageView(new Image(getClass().getResourceAsStream(settingIconPath)));
        configImg = new ImageView(new Image(getClass().getResourceAsStream(configImgPath)));
        imageViews = new ImageView[]{settings1, settings2, settings3};
        displayBox = new HBox();

    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }


    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = manager.getPropertyValue(SEPARATOR.toString()) + String.join(manager.getPropertyValue(SEPARATOR.toString()),
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnShotIconPath = String.join(manager.getPropertyValue(SEPARATOR.toString()), iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        displayIconPath = String.join(manager.getPropertyValue(SEPARATOR.toString()), iconsPath, manager.getPropertyValue(DISPLAY_ICON.name()));
        settingIconPath = String.join(manager.getPropertyValue(SEPARATOR.toString()), iconsPath, manager.getPropertyValue(SETTINGS_ICON.name()));
        configImgPath = String.join(manager.getPropertyValue(SEPARATOR.toString()), iconsPath, manager.getPropertyValue(CONFIGIMG_ICON.name()));
        displayButton = new Button(manager.getPropertyValue(DISPLAY.toString()), new ImageView(new Image(getClass().getResourceAsStream(displayIconPath))));


    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        scrnshotButton = super.setToolbarButton(scrnShotIconPath, applicationTemplate.manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);

        super.toolBar = new ToolBar(newButton, saveButton, loadButton, printButton, scrnshotButton, exitButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        super.setToolbarHandlers(applicationTemplate);
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        super.newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        super.saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        super.loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        super.exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        super.printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.getStylesheets().setAll(cssPath);
        loadButton.getStylesheets().setAll(cssPath);
        exitButton.getStylesheets().setAll(cssPath);
        newButton.getStylesheets().setAll(cssPath);
        saveButton.getStylesheets().setAll(cssPath);
        printButton.getStylesheets().setAll(cssPath);

    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();

    }

    public TextArea getTextArea() {
        return textArea;
    }

    public RadioButton[] getRadioButtons() {
        return radioButtons;
    }

    public Algorithm.AlgorithmType getAlgorithmType() {
        return algorithmType;
    }

    public Algorithm[] getRandomClassifiers() {
        return randomClassifiers;
    }

    public Algorithm[] getRandomClusterers() {
        return randomClusterers;
    }

    public Algorithm[] getKmeansClusterers() {
        return kMeansClusterers;
    }

    @Override
    public void clear() {
        chart.getData().clear();
        tsdProcessor.update();
        textArea.clear();
    }

    private void layout() {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        primaryScene.getStylesheets().add(newCss);
        textWatchDisplay = new Label();
        chart = new LineChart<>(xAxis, yAxis);
        chart.getStylesheets().add(newCss);
        chart.setTitle(applicationTemplate.manager.getPropertyValue(DATA_VISUALIZATION.toString()));
        workspace = new VBox();
        workspace.getChildren().add(textAreaTitle);
        workspace.getChildren().add(textArea);
        workspace.getChildren().add(dataDescription);
        workspace.getChildren().add(toggleButton);
        workspace.getStylesheets().setAll(newCss);
        workspace.getStyleClass().add(PropertyManager.getManager().getPropertyValue(BACKGROUND.toString()));
        displayBox.getChildren().add(displayButton);
        displayBox.getChildren().add(textWatchDisplay);
        ((VBox) workspace).setSpacing(5);
        VBox.setMargin(textAreaTitle, new Insets(4, 0, 0, 200));
        hBox.getChildren().add(workspace);
        hBox.getChildren().add(chart);
        hBox.getStylesheets().setAll(newCss);
        hBox.getStyleClass().add(PropertyManager.getManager().getPropertyValue(BACKGROUND.toString()));
        super.appPane.getChildren().add(hBox);


    }

    private void setWorkspaceActions() {

        settings1.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            showConfigDialog(true, false, false);
        });
        settings1.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            primaryScene.setCursor(Cursor.HAND);
            settings1.setRotate(10);

        });
        settings1.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            primaryScene.setCursor(Cursor.DEFAULT);
            settings1.setRotate(-10);

        });
        settings2.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            showConfigDialog(false, true, false);
        });
        settings2.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            primaryScene.setCursor(Cursor.HAND);
            settings2.setRotate(10);

        });
        settings2.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            primaryScene.setCursor(Cursor.DEFAULT);
            settings2.setRotate(-10);

        });

        settings3.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            showConfigDialog(false, false, true);
        });
        settings3.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            primaryScene.setCursor(Cursor.HAND);
            settings3.setRotate(10);
        });
        settings3.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            primaryScene.setCursor(Cursor.DEFAULT);
            settings3.setRotate(-10);

        });
        displayButton.setOnAction(e -> {
            String chartData;
            if (pendingText != null)
                chartData = textArea.getText() + pendingText.toString();
            else
                chartData = textArea.getText();
            try {
                if (currentAlgorithm.tocontinue()) {
                    chart.getData().clear();
                    tsdProcessor.update();
                    if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
                        new AppData(applicationTemplate).loadData(chartData);
                    } else {
                        new AppData(applicationTemplate).loadData(chartData);
                    }
                    AppUI.super.saveButton.setDisable(false);
                    hasNewText = false;
                } else {
                    if (countClicked == 0) {
                        currentAlgorithm.setFinished(false);
                        comboBox.setDisable(true);
                        chart.getData().clear();
                        tsdProcessor.update();
                        if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER)
                            new AppData(applicationTemplate).loadData(chartData);
                        else
                            new AppData(applicationTemplate).loadData(chartData, true);
                        textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(CHART_DISPLAYED.toString()) + System.lineSeparator() + PropertyManager.getManager().getPropertyValue(PROMPT_PROCEED.toString()));
                        countClicked++;

                    } else {

                        if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
                            if (currentAlgorithm.getMaxIterations() / currentAlgorithm.getUpdateInterval() <= countClicked) {
                                new AppData(applicationTemplate).applyAlgorithm();
                                disableRadioButtons();
                                textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(ALGO_FINISHED.toString()) + System.lineSeparator() + PropertyManager.getManager().getPropertyValue(TOTAL_ITERATION.toString()) + (countClicked));
                                disableState(false);
                                displayButton.setDisable(true);
                                countClicked = 0;
                                currentAlgorithm.setFinished(true);
                            } else {
                                toggleButton.setDisable(true);
                                comboBox.setDisable(true);
                                new AppData(applicationTemplate).applyAlgorithm();
                                textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(CURRENT_ITERATION.toString()) + (countClicked) + System.lineSeparator() + PropertyManager.getManager().getPropertyValue(PROMPT_PROCEED.toString()));
                                countClicked++;
                                currentAlgorithm.setFinished(false);
                            }

                        } else {
                            if (algorithmType == Algorithm.AlgorithmType.RANDOMCLUSTERER) {
                                if (currentAlgorithm.getMaxIterations() / currentAlgorithm.getUpdateInterval() <= countClicked) {
                                    new AppData(applicationTemplate).applyAlgorithm();
                                    disableRadioButtons();
                                    textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(ALGO_FINISHED.toString()) + System.lineSeparator() + PropertyManager.getManager().getPropertyValue(TOTAL_ITERATION.toString()) + (countClicked));
                                    disableState(false);
                                    displayButton.setDisable(true);
                                    countClicked = 0;
                                    currentAlgorithm.setFinished(true);
                                } else {
                                    toggleButton.setDisable(true);
                                    comboBox.setDisable(true);
                                    new AppData(applicationTemplate).applyAlgorithm();
                                    textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(CURRENT_ITERATION.toString()) + (countClicked) + System.lineSeparator() + PropertyManager.getManager().getPropertyValue(PROMPT_PROCEED.toString()));
                                    countClicked++;
                                    currentAlgorithm.setFinished(false);
                                }
                            } else {
                                if (currentAlgorithm.isFinished()) {
                                    new AppData(applicationTemplate).applyAlgorithm();
                                    disableRadioButtons();
                                    textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(ALGO_FINISHED.toString()) + System.lineSeparator() + PropertyManager.getManager().getPropertyValue(TOTAL_ITERATION.toString()) + (countClicked));
                                    disableState(false);
                                    displayButton.setDisable(true);
                                    countClicked = 0;
                                    currentAlgorithm.setFinished(true);
                                } else {
                                    toggleButton.setDisable(true);
                                    comboBox.setDisable(true);
                                    new AppData(applicationTemplate).applyAlgorithm();
                                    textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(CURRENT_ITERATION.toString()) + (countClicked) + System.lineSeparator() + PropertyManager.getManager().getPropertyValue(PROMPT_PROCEED.toString()));
                                    countClicked++;
                                    currentAlgorithm.setFinished(false);
                                }
                            }

                        }
                    }
                }
            } catch (
                    Exception ex)

            {
                Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                error.show(applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.toString()), ex.getMessage() + System.lineSeparator());
                ((AppUI) applicationTemplate.getUIComponent()).setSaveDisabled();
            }


        });
        comboBox.setOnAction((event ->

        {
            if (!comboBox.getSelectionModel().isEmpty()) {
                displayButton.setDisable(true);
                VBox algorithmConfig = new VBox();
                algorithmConfig.setSpacing(5);
                Text algoName;
                if (comboBox.getSelectionModel().getSelectedItem().toString().equals(Algorithm.AlgorithmType.RANDOMCLUSTERER.toString())) {
                    algorithmType = Algorithm.AlgorithmType.RANDOMCLUSTERER;
                    algoName = new Text(PropertyManager.getManager().getPropertyValue(RANDOMCLUSTERING.toString()));
                } else if (comboBox.getSelectionModel().getSelectedItem().toString().equals(Algorithm.AlgorithmType.KMEANSCLUSTERER.toString())) {
                    algorithmType = Algorithm.AlgorithmType.KMEANSCLUSTERER;
                    algoName = new Text(PropertyManager.getManager().getPropertyValue(KMeans.toString()));
                } else {
                    algorithmType = Algorithm.AlgorithmType.RANDOMCLASSIFIER;
                    algoName = new Text((RANDOMCLASSIFIER.name()));
                }

                algoName.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS.toString()));
                algorithmConfig.getChildren().add(algoName);
                ImageView settings;
                ToggleGroup toggleGroup = new ToggleGroup();
                radioButtons = new RadioButton[3];

                for (int i = 0; i < 3; i++) {
                    settings = imageViews[i];
                    RadioButton radioButton;
                    radioButton = new RadioButton(PropertyManager.getManager().getPropertyValue(ALGORITHM.toString()) + (i + 1));
                    radioButton.setToggleGroup(toggleGroup);
                    radioButton.setFont(new Font(14));
                    radioButtons[i] = radioButton;
                    radioButton.getStyleClass().add(PropertyManager.getManager().getPropertyValue(RADIOBUTTONS.toString()));

                    int finalI = i;
                    radioButtons[i].setOnMouseClicked(e -> {
                        displayButton.setDisable(false);
                        switch (algorithmType) {
                            case RANDOMCLUSTERER:
                                currentAlgorithm = randomClusterers[finalI];
                                break;
                            case KMEANSCLUSTERER:
                                currentAlgorithm = kMeansClusterers[finalI];
                                break;
                            case RANDOMCLASSIFIER:
                                currentAlgorithm = randomClassifiers[finalI];
                                break;
                        }
                    });

                    HBox configRow = new HBox();
                    configRow.setSpacing(250);
                    configRow.getChildren().add(new Group(radioButton));
                    configRow.getChildren().add(new Group(settings));
                    algorithmConfig.getChildren().add(configRow);

                }

                enableSavedConfigs();

                if (workspace.getChildren().size() <= 5) {
                    workspace.getChildren().add(algorithmConfig);
                    workspace.getChildren().add(displayBox);
                } else {
                    workspace.getChildren().set(5, algorithmConfig);
                }
            } else {
                displayButton.setDisable(true);
            }

        }));
        textArea.textProperty().

                addListener((observable, oldValue, newValue) ->

                {
                    if (pendingText != null && pendingText.toString().length() != 0) {
                        StringBuilder oldStrbfr = new StringBuilder(oldValue);
                        StringBuilder newStrbfr = new StringBuilder(newValue);
                        String[] oldLines = oldStrbfr.toString().split(newLineRegex);
                        String[] newLines = newStrbfr.toString().split(newLineRegex);
                        int deletedLineCount = oldLines.length - newLines.length;
                        String[] pending = pendingText.toString().split(newLineRegex);
                        StringBuilder newText = new StringBuilder();
                        Stack<String> temp = new Stack<>();
                        temp.addAll(Arrays.asList(pending));
                        if (deletedLineCount >= 0) {
                            for (int count = 0; count < deletedLineCount; count++) {
                                if (!temp.isEmpty())
                                    newText.append(temp.pop() + System.lineSeparator());
                            }

                            pendingText.setLength(0);
                            pendingText.trimToSize();
                            while (!temp.isEmpty()) {
                                pendingText.append(temp.pop() + System.lineSeparator());
                            }
                            textArea.setText(newValue + newText.toString());
                        }
                    }

                    //Validity Check
                    currentText = newValue;
                    if (!oldValue.equals(newValue))
                        hasNewText = true;
                    else
                        hasNewText = false;

                    StringBuilder entireFormat = new StringBuilder();
                    Stream.of(regexString)
                            .map(line -> Arrays.asList(line.split(tabRegex)))
                            .forEach(list -> {
                                entireFormat.append(list.get(0));
                            });
                    String loaded = ((AppData) applicationTemplate.getDataComponent()).getUpdatedChartData();
                    Pattern formatPattern = Pattern.compile(entireFormat.toString());
                    final AtomicInteger atomicInteger = new AtomicInteger(0);
                    Stream.of(newValue.split(newLineRegex))
                            .forEach(line -> {
                                Matcher formatMatch = formatPattern.matcher(line);
                                atomicInteger.incrementAndGet();
                                if (formatMatch.matches()) {

                                    if (atomicInteger.get() == 1) {
                                        AppUI.super.saveButton.setDisable(false);
                                        AppUI.super.newButton.setDisable(false);
                                        if (isTextAreaEmpty())
                                            AppUI.super.saveButton.setDisable(true);

                                        if (loaded != null)
                                            if (loaded.equals(newValue))
                                                AppUI.super.saveButton.setDisable(true);


                                    }

                                } else if (!formatMatch.matches() && atomicInteger.get() == 1) {
                                    AppUI.super.newButton.setDisable(true);
                                    AppUI.super.saveButton.setDisable(true);
                                    disableScrnshot();
                                }
                                chart.getData().clear();
                                disableScrnshot();
                                tsdProcessor.update();
                                textWatchDisplay.setText(PropertyManager.getManager().getPropertyValue(EMPTY.toString()));
                            });


                });


        toggleButton.selectedProperty().

                addListener((observable, oldValue, newValue) ->

                {
                    if (newValue) {
                        String chartData;
                        if (pendingText != null)
                            chartData = textArea.getText() + pendingText.toString();
                        else
                            chartData = textArea.getText();
                        try {
                            if (!chartData.isEmpty()) {
                                tsdProcessor.processString(chartData);


                                if (workspace.getChildren().size() <= 5) {

                                    if (tsdProcessor.getDataLabelCount() == 2 && !tsdProcessor.checkNullLabel()) {

                                        comboBox.setItems(FXCollections.observableArrayList(
                                                Algorithm.AlgorithmType.KMEANSCLUSTERER,
                                                Algorithm.AlgorithmType.RANDOMCLUSTERER,
                                                Algorithm.AlgorithmType.RANDOMCLASSIFIER
                                        ));

                                    } else {
                                        comboBox.setItems(FXCollections.observableArrayList(
                                                Algorithm.AlgorithmType.KMEANSCLUSTERER,
                                                Algorithm.AlgorithmType.RANDOMCLUSTERER
                                        ));

                                    }


                                    if (workspace.getChildren().size() == 4 || workspace.getChildren().size() == 6 || workspace.getChildren().size() == 2) {
                                        comboBox.setPromptText(PropertyManager.getManager().getPropertyValue(ALGORITHM_TYPE.toString()));
                                        workspace.getChildren().add(4, comboBox); //Index: 4 , size: 5
                                    }


                                }
                                textArea.setDisable(true);
                            }
                        } catch (Exception ex) {
                            textArea.setDisable(false);
                            Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                            error.show(applicationTemplate.manager.getPropertyValue(WRONG_DATA_FORMAT_ERROR.toString()), ex.getMessage() + System.lineSeparator());
                        }
                        {

                            if (currentDataPath != null)
                                updateTextFlow(currentDataPath.toAbsolutePath().toString());
                            else
                                updateTextFlow(null);
                            tsdProcessor.update();
                        }
                    } else {
                        while (workspace.getChildren().size() >= 5)
                            workspace.getChildren().remove(workspace.getChildren().size() - 1);
                        textArea.setDisable(false);
                    }
                });


        scrnshotButton.setOnAction(e ->

        {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e1) {

            }
        });


    }

    public void setCurrentDataFilePath(Path path) {
        currentDataPath = path;
    }

    public void setReadOnly() {
        textArea.setDisable(true);
    }

    public boolean isTextAreaEmpty() {
        return textArea.getText().isEmpty();
    }

    public void setToggleSelected(boolean isEditable) {
        toggleButton.setSelected(isEditable);
    }

    public void setPendingText(StringBuilder pendingText) {
        this.pendingText = pendingText;
    }

    public void setSaveDisabled() {
        saveButton.setDisable(true);
    }

    public void enableScrnshot() {
        scrnshotButton.setDisable(false);
    }

    public void disableScrnshot() {
        scrnshotButton.setDisable(true);
    }

    public void updateTextFlow(String dataFilePath) {
        TextFlow textFlow = ((AppUI) applicationTemplate.getUIComponent()).getTextFlow();
        if (textFlow.getChildren().size() != 0) {
            ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();
        }
        textFlow.setLineSpacing(5);

        Text firstLine = new Text(tsdProcessor.getDataSize() + PropertyManager.getManager().getPropertyValue(FIRSTLINE.toString()));

        Text labelDescription = new Text(tsdProcessor.getDataLabelCount() + PropertyManager.getManager().getPropertyValue(LABELNAMES.toString()) + System.lineSeparator());
        firstLine.getStyleClass().add(PropertyManager.getManager().getPropertyValue(DIALOG_BACKGROUND.name()));
        labelDescription.getStyleClass().add(PropertyManager.getManager().getPropertyValue(DIALOG_BACKGROUND.name()));

        Text pathDescription;
        if (dataFilePath != null)
            pathDescription = new Text(PropertyManager.getManager().getPropertyValue(FROM.toString()) + System.lineSeparator() + dataFilePath + System.lineSeparator());
        else
            pathDescription = new Text(System.lineSeparator() + PropertyManager.getManager().getPropertyValue(NO_FILEPATH.name()) + System.lineSeparator());

        firstLine.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS.name()));
        pathDescription.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS.toString()));
        labelDescription.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS.toString()));
        textFlow.getChildren().add(firstLine);
        textFlow.getChildren().add(pathDescription);
        textFlow.getChildren().add(labelDescription);
        textFlow.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS.toString()));


        Iterator labelIterator = tsdProcessor.getLabels().iterator();

        for (int i = 0; i < tsdProcessor.getDataLabelCount(); i++) {
            Label labelName = new Label();
            labelName.getStyleClass().add(PropertyManager.getManager().getPropertyValue(ROUND_LABELNAMES.toString()));
            labelName.setText(labelIterator.next().toString());
            textFlow.getChildren().add(labelName);
            labelIterator.remove();
        }

    }


    public TextFlow getTextFlow() {
        return dataDescription;
    }

    public void setMoreThanTen(boolean bool) {
        moreThanTen = bool;
    }

    public boolean isToggleSelected() {
        return toggleButton.isSelected();
    }

    public Button getDisplayButton() {
        return displayButton;
    }

    public void disableState(boolean disable) {
        comboBox.setDisable(disable);
        toggleButton.setDisable(disable);
    }


    public ComboBox getComboBox() {
        return comboBox;
    }

    public ToggleButton getToggleButton() {
        return toggleButton;
    }

    public void showConfigDialog(boolean firstCheck, boolean secondCheck, boolean thirdCheck) {
        GridPane configDialog = new GridPane();
        configDialog.setPadding(new Insets(14, 14, 14, 14));
        configDialog.add(configImg, 0, 1);
        configDialog.setPrefSize(700, 151);
        configDialog.getStylesheets().add(newCss);
        configDialog.getStyleClass().add(PropertyManager.getManager().getPropertyValue(BACKGROUND.toString()));
        Button cancel = new Button(PropertyManager.getManager().getPropertyValue(CANCEL.toString()));
        Button ok = new Button(PropertyManager.getManager().getPropertyValue(OK.toString()));

        HBox buttonGroup = new HBox();
        ok.setDisable(true);
        ok.getStylesheets().add(newCss);
        ok.getStyleClass().add(PropertyManager.getManager().getPropertyValue(OTHER_BUTTON.name()));
        cancel.getStylesheets().add(newCss);
        cancel.getStyleClass().add(PropertyManager.getManager().getPropertyValue(OTHER_BUTTON.name()));

        buttonGroup.getChildren().addAll(cancel, ok);
        buttonGroup.setSpacing(14);
        buttonGroup.setPadding(new Insets(0, 0, 0, 120));
        VBox vBox = new VBox();
        vBox.setSpacing(7);
        vBox.setPrefSize(608, 83);
        TextField[] textFields = new TextField[3];
        Label textWatcher = new Label();
        textWatcher.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS_TEXTWATCHER.toString()));
        textWatchDisplay.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS_TEXTWATCHER.toString()));
        CheckBox isContinuous = new CheckBox();
        for (int i = 0; i < 4; i++) {
            HBox row = new HBox();
            Label label = new Label();


            TextField textField = new TextField();
            label.getStylesheets().add(newCss);
            label.getStyleClass().add(PropertyManager.getManager().getPropertyValue(RADIOBUTTONS.toString()));

            if (i == 0) {
                label.setText(PropertyManager.getManager().getPropertyValue(MAX_ITERATIONS.toString()));
                label.setPadding(new Insets(0, 50, 0, 0));
            } else if (i == 1) {
                label.setText(PropertyManager.getManager().getPropertyValue(INTERVAL.toString()));
                label.setPadding(new Insets(0, 44, 0, 0));
            } else if (i == 2) {
                if (algorithmType == Algorithm.AlgorithmType.KMEANSCLUSTERER || algorithmType == Algorithm.AlgorithmType.RANDOMCLUSTERER) {
                    label.setText(PropertyManager.getManager().getPropertyValue(CLUSTERS.toString()));
                    label.setPadding(new Insets(0, 25, 0, 0));
                } else continue;
            } else {
                label.setText(PropertyManager.getManager().getPropertyValue(CONTINUOUS.toString()));
                label.setPadding(new Insets(0, 168, 0, 0));
            }
            row.getChildren().add(label);

            if (i <= 2) {
                row.getChildren().add(textField);
                textFields[i] = textField;
                if (i == 1)
                    row.getChildren().add(buttonGroup);
            } else {

                row.getChildren().add(isContinuous);
                row.getChildren().add(textWatcher);
            }
            vBox.getChildren().add(row);
        }
        if (algorithmType == Algorithm.AlgorithmType.KMEANSCLUSTERER) {
            for (int i = 0; i < 3; i++) {
                if (kMeansClusterers[0] != null && firstCheck) {
                    textFields[0].setText(String.valueOf(kMeansClusterers[0].getMaxIterations()));
                    textFields[1].setText(String.valueOf(kMeansClusterers[0].getUpdateInterval()));
                    textFields[2].setText(String.valueOf(kMeansClusterers[0].getNumberOfClusters()));
                    isContinuous.setSelected(kMeansClusterers[0].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                } else if (kMeansClusterers[1] != null && secondCheck) {
                    textFields[0].setText(String.valueOf(kMeansClusterers[1].getMaxIterations()));
                    textFields[1].setText(String.valueOf(kMeansClusterers[1].getUpdateInterval()));
                    textFields[2].setText(String.valueOf(kMeansClusterers[1].getNumberOfClusters()));
                    isContinuous.setSelected(kMeansClusterers[1].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                } else if (kMeansClusterers[2] != null && thirdCheck) {
                    textFields[0].setText(String.valueOf(kMeansClusterers[2].getMaxIterations()));
                    textFields[1].setText(String.valueOf(kMeansClusterers[2].getUpdateInterval()));
                    textFields[2].setText(String.valueOf(kMeansClusterers[2].getNumberOfClusters()));
                    isContinuous.setSelected(kMeansClusterers[2].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                }

            }

        } else if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
            for (int i = 0; i < 3; i++) {

                if (randomClassifiers[0] != null && firstCheck) {
                    textFields[0].setText(String.valueOf(randomClassifiers[0].getMaxIterations()));
                    textFields[1].setText(String.valueOf(randomClassifiers[0].getUpdateInterval()));
                    isContinuous.setSelected(randomClassifiers[0].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                } else if (randomClassifiers[1] != null && secondCheck) {
                    textFields[0].setText(String.valueOf(randomClassifiers[1].getMaxIterations()));
                    textFields[1].setText(String.valueOf(randomClassifiers[1].getUpdateInterval()));
                    isContinuous.setSelected(randomClassifiers[1].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                } else if (randomClassifiers[2] != null && thirdCheck) {
                    textFields[0].setText(String.valueOf(randomClassifiers[2].getMaxIterations()));
                    textFields[1].setText(String.valueOf(randomClassifiers[2].getUpdateInterval()));
                    isContinuous.setSelected(randomClassifiers[2].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                }

            }
        } else {
            for (int i = 0; i < 3; i++) {

                if (randomClusterers[0] != null && firstCheck) {
                    textFields[0].setText(String.valueOf(randomClusterers[0].getMaxIterations()));
                    textFields[1].setText(String.valueOf(randomClusterers[0].getUpdateInterval()));
                    textFields[2].setText(String.valueOf(randomClusterers[0].getNumberOfClusters()));
                    isContinuous.setSelected(randomClusterers[0].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                } else if (randomClusterers[1] != null && secondCheck) {
                    textFields[0].setText(String.valueOf(randomClusterers[1].getMaxIterations()));
                    textFields[1].setText(String.valueOf(randomClusterers[1].getUpdateInterval()));
                    textFields[2].setText(String.valueOf(randomClusterers[1].getNumberOfClusters()));
                    isContinuous.setSelected(randomClusterers[1].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                } else if (randomClusterers[2] != null && thirdCheck) {
                    textFields[0].setText(String.valueOf(randomClusterers[2].getMaxIterations()));
                    textFields[1].setText(String.valueOf(randomClusterers[2].getUpdateInterval()));
                    textFields[2].setText(String.valueOf(randomClusterers[2].getNumberOfClusters()));
                    isContinuous.setSelected(randomClusterers[2].tocontinue());
                    if (validText(textFields))
                        ok.setDisable(false);
                }

            }

        }
        configDialog.add(vBox, 1, 1);
        configDialog.setEffect(new Glow(0.44));
        Stage newStage = new Stage();
        newStage.setMinWidth(700);
        newStage.setScene(new Scene(configDialog));
        newStage.setTitle(PropertyManager.getManager().getPropertyValue(RUN_CONFIG.toString()));
        newStage.show();

        checkConfigs(textFields, ok, textWatcher);

        ok.setOnAction(event -> {
            try {
                tsdProcessor.update();
                ((AppData) applicationTemplate.getDataComponent()).setDataSet(textArea.getText() + pendingText.toString());
            } catch (Exception e) {

            }
            try {
                Class[] clusterArgs = new Class[]{DataSet.class, int.class, int.class, int.class, boolean.class};
                Class[] classifierArgs = new Class[]{DataSet.class, int.class, int.class, boolean.class};
                Class<?> randomClassifierClass = Class.forName("algorithm_list.RandomClassifier");
                Constructor randomClassifierConst = randomClassifierClass.getConstructor(classifierArgs);
                Class<?> kmeansClass = Class.forName("algorithm_list.KMeansClusterer");
                Constructor kmeansConst = kmeansClass.getConstructor(clusterArgs);
                Class<?> randomClusterClass = Class.forName("algorithm_list.RandomClusterer");
                Constructor randomCluseterConst = randomClusterClass.getConstructor(clusterArgs);

                Map<String, String> labels = ((AppData) applicationTemplate.getDataComponent()).getLabels();
                Map<String, Point2D> datapoints = ((AppData) applicationTemplate.getDataComponent()).getDataPoints();


                if (firstCheck) {

                    radioButtons[0].setDisable(false);
                    if (algorithmType == Algorithm.AlgorithmType.KMEANSCLUSTERER) {
                        kMeansClusterers[0] = (KMeansClusterer) kmeansConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()),
                                Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());

                    } else if (algorithmType == Algorithm.AlgorithmType.RANDOMCLUSTERER) {
                        randomClusterers[0] = randomClusterers[0] = (RandomClusterer) randomCluseterConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()),
                                Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());
                    } else {

                        randomClassifiers[0] = (RandomClassifier) randomClassifierConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()), isContinuous.isSelected());
                    }

                } else if (secondCheck) {
                    radioButtons[1].setDisable(false);
                    if (algorithmType == Algorithm.AlgorithmType.KMEANSCLUSTERER) {
                        kMeansClusterers[1] = (KMeansClusterer) kmeansConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()),
                                Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());

                    } else if (algorithmType == Algorithm.AlgorithmType.RANDOMCLUSTERER) {
                        randomClusterers[1] = randomClusterers[0] = (RandomClusterer) randomCluseterConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()),
                                Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());
                    } else {

                        randomClassifiers[1] = (RandomClassifier) randomClassifierConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()), isContinuous.isSelected());
                    }

                } else {
                    radioButtons[2].setDisable(false);
                    if (algorithmType == Algorithm.AlgorithmType.KMEANSCLUSTERER) {
                        kMeansClusterers[2] = (KMeansClusterer) kmeansConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()),
                                Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());

                    } else if (algorithmType == Algorithm.AlgorithmType.RANDOMCLUSTERER) {
                        randomClusterers[2] = randomClusterers[0] = (RandomClusterer) randomCluseterConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()),
                                Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());
                    } else {

                        randomClassifiers[2] = (RandomClassifier) randomClassifierConst.newInstance(new DataSet(labels, datapoints),
                                Integer.valueOf(textFields[0].getText()),
                                Integer.valueOf(textFields[1].getText()), isContinuous.isSelected());
                    }

                }

            } catch (ClassNotFoundException | NoSuchMethodException e) {

            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            newStage.close();
        });
        cancel.setOnAction(event -> {
            newStage.close();
        });


    }

    public void checkConfigs(TextField[] textFields, Button ok, Label textWatcher) {
        for (int i = 0; i < textFields.length; i++) {
            if ((algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) && i == 2)
                continue;

            textFields[i].textProperty().
                    addListener((observable, oldValue, newValue) -> {

                        boolean isDigit = false;
                        boolean isValid = true;

                        Outer:
                        for (int j = 0; j < textFields.length; j++) {
                            if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
                                if (j == 2) {
                                    continue;
                                }
                                if (textFields[j].getText().length() == 0 || newValue.length() == 0) {
                                    ok.setDisable(true);
                                    isValid = false;
                                    break Outer;
                                }
                                for (int k = 0; k < textFields[j].getText().length(); k++) {
                                    if (!Character.isDigit(textFields[j].getText().charAt(k))) {
                                        textWatcher.setText(PropertyManager.getManager().getPropertyValue(TEXTWATCH_MSG.toString()));
                                        ok.setDisable(true);
                                        isValid = false;
                                        isDigit = false;
                                        break;
                                    } else
                                        isDigit = true;
                                }
                                for (int k = 0; k < newValue.length(); k++) {

                                    if (k == 0 && newValue.length() != 1 && newValue.charAt(0) == '-')
                                        continue;

                                    if (!Character.isDigit(newValue.charAt(k))) {
                                        textWatcher.setText(PropertyManager.getManager().getPropertyValue(TEXTWATCH_MSG.toString()));
                                        ok.setDisable(true);
                                        isDigit = false;
                                        isValid = false;
                                        break;
                                    } else
                                        isDigit = true;
                                }

                            } else {
                                if (textFields[j].getText().length() == 0 || newValue.length() == 0) {
                                    ok.setDisable(true);
                                    isValid = false;
                                    break Outer;
                                }

                                for (int k = 0; k < newValue.length(); k++) {

                                    if (k == 0 && newValue.length() != 1 && newValue.charAt(0) == '-')
                                        continue;

                                    if (!Character.isDigit(newValue.charAt(k))) {
                                        textWatcher.setText(PropertyManager.getManager().getPropertyValue(TEXTWATCH_MSG.toString()));
                                        ok.setDisable(true);
                                        isDigit = false;
                                        isValid = false;
                                        break;
                                    } else
                                        isDigit = true;
                                }

                            }
                            //Verified the string input is digit
                            if (isDigit) {
                                if (Integer.parseInt(newValue) <= 0) {
                                    for (int h = 0; h < textFields.length; h++) {
                                        if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
                                            if (h == 2) {
                                                continue;
                                            }
                                            if (!textFields[h].getText().isEmpty() && Integer.parseInt(textFields[h].getText()) <= 0) {
                                                textFields[h].setText(String.valueOf(1));
                                            }
                                        } else if (!textFields[2].getText().isEmpty() && Integer.parseInt(textFields[2].getText()) <= 1) {
                                            textFields[2].setText(String.valueOf(2));
                                            ok.setDisable(true);
                                            isValid = false;
                                            textWatcher.setText("Cluster numbers cannot exceed 4" + System.lineSeparator() + " Set to lower bound");
                                            break;
                                        } else if (!textFields[h].getText().isEmpty() && Integer.parseInt(textFields[h].getText()) <= 0) {
                                            textFields[h].setText(String.valueOf(1));
                                        }
                                    }

                                    textWatcher.setText(PropertyManager.getManager().getPropertyValue(TXT_WATCH_NEGATIVE.toString()) + " Set to lower bound");
                                    ok.setDisable(true);
                                    isValid = false;
                                    break;
                                } else if (algorithmType != Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
                                    if (!textFields[2].getText().isEmpty() && Integer.parseInt(textFields[2].getText()) > 4) {
                                        textFields[2].setText(String.valueOf(4));
                                        ok.setDisable(true);
                                        isValid = false;
                                        textWatcher.setText("Cluster numbers cannot exceed 4" + System.lineSeparator() + " Set to upper bound");
                                        break;
                                    }
                                }
                                if (!textFields[0].getText().isEmpty() && !textFields[1].getText().isEmpty() && Integer.parseInt(textFields[1].getText()) > Integer.parseInt(textFields[0].getText())) {
                                    textWatcher.setText(("Update interval cannot exceed maxIteration" + System.lineSeparator() + " Set to upper bound"));
                                    textFields[1].setText(textFields[0].getText());
                                    ok.setDisable(true);
                                    isValid = false;
                                    break;
                                }

                            }

                        }

                        if (validText(textFields)) {
                            ok.setDisable(false);
                        }
                    });


        }
    }

    public Algorithm getCurrentAlgorithm() {
        return currentAlgorithm;
    }

    public void enableSavedConfigs() {
        switch (algorithmType) {
            case RANDOMCLASSIFIER:
                for (int i = 0; i < randomClassifiers.length; i++) {

                    if (randomClassifiers[i] != null)
                        radioButtons[i].setDisable(false);
                    else
                        radioButtons[i].setDisable(true);
                }
                break;
            case RANDOMCLUSTERER:
                for (int i = 0; i < randomClusterers.length; i++) {
                    if (randomClusterers[i] != null)
                        radioButtons[i].setDisable(false);
                    else
                        radioButtons[i].setDisable(true);
                }
                break;
            case KMEANSCLUSTERER:
                for (int i = 0; i < kMeansClusterers.length; i++) {
                    if (kMeansClusterers[i] != null)
                        radioButtons[i].setDisable(false);
                    else
                        radioButtons[i].setDisable(true);
                }
                break;

        }
    }

    public static boolean validText1(String[] configTextFields, Algorithm.AlgorithmType algorithmType) {
        if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
            for (int i = 0; i < configTextFields.length; i++) {
                if (i == 2)
                    continue;
                if (Integer.parseInt(configTextFields[i]) <= 0) {
                    configTextFields[i] = String.valueOf(1);
                    if (Integer.parseInt(configTextFields[0]) < Integer.parseInt(configTextFields[1]))
                        configTextFields[1] = configTextFields[0];
                    return false;
                }

            }
            if (Integer.parseInt(configTextFields[0]) < Integer.parseInt(configTextFields[1])) {
                configTextFields[1] = configTextFields[0];
                return false;
            }
            return true;
        } else {
            if (Integer.parseInt(configTextFields[2]) <= 1) {
                configTextFields[2] = String.valueOf(2);
                return false;
            }
            for (int i = 0; i < configTextFields.length; i++) {
                if (Integer.parseInt(configTextFields[i]) < 0) {
                    configTextFields[i] = String.valueOf(1);
                    return false;
                }

            }
            if (Integer.parseInt(configTextFields[0]) < Integer.parseInt(configTextFields[1])) {
                configTextFields[1] = configTextFields[0];
                return false;
            } else if (Integer.parseInt(configTextFields[2]) > 4) {
                configTextFields[2] = String.valueOf(4);
                return false;
            }
            return true;
        }
    }

    public boolean validText(TextField[] textFields) {
        boolean isValid = true;
        Outer:
        for (int j = 0; j < textFields.length; j++) {
            if (algorithmType == Algorithm.AlgorithmType.RANDOMCLASSIFIER) {
                if (j == 2) {
                    continue;
                }
                if (textFields[j].getText().length() == 0) {
                    isValid = false;
                    break Outer;
                }
                for (int k = 0; k < textFields[j].getText().length(); k++) {
                    if (!Character.isDigit(textFields[j].getText().charAt(k))) {
                        isValid = false;
                        break Outer;
                    }
                }


            } else {
                if (textFields[j].getText().length() == 0) {

                    isValid = false;
                    break Outer;
                }
                for (int k = 0; k < textFields[j].getText().length(); k++) {
                    if (!Character.isDigit(textFields[j].getText().charAt(k))) {

                        isValid = false;
                        break Outer;
                    }
                }

            }


        }


        if (isValid)
            return true;

        else return false;
    }

    public Label getTextWatchDisplay() {
        return textWatchDisplay;
    }

    public Button getScrnshotButton() {
        return scrnshotButton;
    }

    public void disableRadioButtons() {
        for (int i = 0; i < 3; i++)
            radioButtons[i].setSelected(false);
    }
}



