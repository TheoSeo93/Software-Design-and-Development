package ui;

import actions.AppActions;
import algorithms.Classification;
import algorithms.Clustering;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
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
import javafx.util.StringConverter;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static settings.AppPropertyTypes.*;
import static ui.DataVisualizer.newLineRegex;
import static ui.DataVisualizer.regexString;
import static ui.DataVisualizer.tabRegex;
import static vilij.settings.PropertyTypes.*;

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
    private boolean isClassification;
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
    private TextFlow dataDescription;
    private ImageView[] imageViews;
    private RadioButton[] radioButtons;
    private ImageView configImg;
    private String newCss;
    private Clustering[] clusterConfigs;
    private Classification[] classificationConfigs;

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {

        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        newCss = this.getClass().getResource(applicationTemplate.manager.getPropertyValue(CSS_ADDRESS.toString())).toExternalForm();
        PropertyManager manager = applicationTemplate.manager;
        clusterConfigs = new Clustering[3];
        classificationConfigs = new Classification[3];
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
        comboBox.setConverter(new StringConverter<Object>() {
            @Override
            public String toString(Object object) {
                if (object instanceof Classification) {
                    return ((Classification) object).getName();
                } else if (object instanceof Clustering) {
                    return ((Clustering) object).getName();
                }
                return null;
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });


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

        ((VBox) workspace).setSpacing(5);
        VBox.setMargin(textAreaTitle, new Insets(4, 0, 0, 200));
        hBox.getChildren().add(workspace);
        hBox.getChildren().add(chart);
        hBox.getStylesheets().setAll(newCss);
        hBox.getStyleClass().add(PropertyManager.getManager().getPropertyValue(BACKGROUND.toString()));
        super.appPane.getChildren().add(hBox);


    }

    private void setWorkspaceActions() {
        tsdProcessor.setManager(this.applicationTemplate.manager);
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
            String chartData = textArea.getText().toString();
            if (hasNewText) {
                try {
                    new AppData(applicationTemplate).loadData(chartData);
                    AppUI.super.saveButton.setDisable(false);
                    hasNewText = false;

                } catch (Exception ex) {
                    Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    error.show(applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.toString()), ex.getMessage() + System.lineSeparator());
                    ((AppUI) applicationTemplate.getUIComponent()).setSaveDisabled();
                }

            }


        });
        comboBox.setOnAction((event -> {
            if (!comboBox.getSelectionModel().isEmpty()) {
                VBox algorithmConfig = new VBox();
                algorithmConfig.setSpacing(5);

                if (comboBox.getSelectionModel().getSelectedItem().toString().toLowerCase().contains(CLUSTERING.toString().toLowerCase())) {
                    isClassification = false;
                    Text algoName = new Text(PropertyManager.getManager().getPropertyValue(CLUSTERING.name()));
                    algoName.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS.toString()));
                    algorithmConfig.getChildren().add(algoName);

                    ImageView settings;
                    ToggleGroup toggleGroup = new ToggleGroup();
                    radioButtons = new RadioButton[3];
                    for (int i = 0; i < 3; i++) {
                        settings = imageViews[i];

                        RadioButton radioButton = new RadioButton(PropertyManager.getManager().getPropertyValue(ALGORITHM.toString())+ (i + 1));
                        radioButton.setToggleGroup(toggleGroup);
                        radioButton.setFont(new Font(14));
                        radioButtons[i] = radioButton;

                        radioButton.getStyleClass().add(PropertyManager.getManager().getPropertyValue(RADIOBUTTONS.toString()));
                        HBox configRow = new HBox();
                        configRow.setSpacing(250);
                        configRow.getChildren().add(new Group(radioButton));
                        configRow.getChildren().add(new Group(settings));
                        algorithmConfig.getChildren().add(configRow);

                    }
                    for (int i = 0; i < radioButtons.length; i++) {
                        radioButtons[i].setDisable(true);
                    }


                } else {
                    isClassification = true;
                    Text algoName = new Text(PropertyManager.getManager().getPropertyValue(CLASSIFICATION.name()));
                    algoName.getStyleClass().add(PropertyManager.getManager().getPropertyValue(LABELS.toString()));
                    algorithmConfig.getChildren().add(algoName);

                    ImageView settings;
                    ToggleGroup toggleGroup = new ToggleGroup();
                      radioButtons = new RadioButton[3];

                    for (int i = 0; i < 3; i++) {
                        settings = imageViews[i];

                        RadioButton radioButton = new RadioButton(PropertyManager.getManager().getPropertyValue(ALGORITHM.toString()) + (i + 1));
                        radioButton.getStyleClass().add(PropertyManager.getManager().getPropertyValue(RADIOBUTTONS.toString()));
                        radioButton.setToggleGroup(toggleGroup);
                        radioButton.setFont(new Font(14));
                        radioButtons[i] = radioButton;
                        HBox configRow = new HBox();
                        configRow.setSpacing(250);
                        radioButton.setToggleGroup(toggleGroup);
                        configRow.getChildren().add(new Group(radioButton));
                        configRow.getChildren().add(new Group(settings));
                        algorithmConfig.getChildren().add(configRow);

                    }
                    for (int i = 0; i < radioButtons.length; i++) {
                        radioButtons[i].setDisable(true);
                    }


                }
                if (workspace.getChildren().size() <= 5) {
                    workspace.getChildren().add(algorithmConfig);
                    workspace.getChildren().add(displayButton);
                } else {
                    workspace.getChildren().set(5, algorithmConfig);
                }
            } else {
                displayButton.setDisable(true);
            }
        }));
        textArea.textProperty().

                addListener((observable, oldValue, newValue) -> {
                    if (pendingText != null && pendingText.toString().length() != 0 && isMoreThanTen()) {

                        StringBuilder oldStrbfr = new StringBuilder(oldValue);
                        StringBuilder newStrbfr = new StringBuilder(newValue);
                        String[] oldLines = oldStrbfr.toString().split(newLineRegex);
                        String[] newLines = newStrbfr.toString().split(newLineRegex);

                        int deletedLineCount = oldLines.length - newLines.length;
                        String[] pending = pendingText.toString().split(newLineRegex);
                        StringBuilder newText = new StringBuilder();

                        if (pending.length >= deletedLineCount && deletedLineCount > 0) {
                            for (int count = 0; count < deletedLineCount; count++)
                                newText.append(pending[count] + System.lineSeparator());

                            textArea.setText(newValue + newText.toString());
                            pendingText.setLength(0);
                            pendingText.trimToSize();
                            for (int i = deletedLineCount; i < pending.length; i++) {
                                pendingText.append(pending[i] + System.lineSeparator());
                            }

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
                            });


                });


        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                String chartData = textArea.getText().toString();
                try {
                    if(!chartData.isEmpty()) {
                        tsdProcessor.processString(chartData);


                        if (workspace.getChildren().size() < 5) {

                            if (tsdProcessor.getDataLabelCount() == 2 && !tsdProcessor.checkNullLabel()) {
                                comboBox.setItems(FXCollections.observableArrayList(
                                        new Classification(),
                                        new Clustering()
                                ));
                            } else
                                comboBox.setItems(FXCollections.observableArrayList(
                                        new Clustering()
                                ));


                            if (workspace.getChildren().size() == 4) {
                                comboBox.setPromptText(PropertyManager.getManager().getPropertyValue(ALGORITHM_TYPE.toString()));
                                workspace.getChildren().add(comboBox); //Index: 4 , size: 5
                            }

                        }
                        textArea.setDisable(true);
                    }
                } catch (Exception ex) {
                    textArea.setDisable(false);
                    Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    error.show(applicationTemplate.manager.getPropertyValue(WRONG_DATA_FORMAT_ERROR.toString()), ex.getMessage() + System.lineSeparator());
                }  {

                    if (currentDataPath != null)
                        updateTextFlow(currentDataPath.toAbsolutePath().toString());
                    else
                        updateTextFlow(null);
                    tsdProcessor.update();
                }
            } else {
                for (int i = workspace.getChildren().size() - 1; i > 3; i--)
                    workspace.getChildren().remove(i);
                textArea.setDisable(false);
            }
        });


        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e1) {
                e1.printStackTrace();
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
        firstLine.getStyleClass().add( PropertyManager.getManager().getPropertyValue(DIALOG_BACKGROUND.name()));
        labelDescription.getStyleClass().add(PropertyManager.getManager().getPropertyValue(DIALOG_BACKGROUND.name()));

        Text pathDescription;
        if (dataFilePath != null)
            pathDescription = new Text(PropertyManager.getManager().getPropertyValue(FROM.toString())+ System.lineSeparator() + dataFilePath + System.lineSeparator());
        else
            pathDescription = new Text(System.lineSeparator()+PropertyManager.getManager().getPropertyValue(NO_FILEPATH.name())+ System.lineSeparator());

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


    public boolean isMoreThanTen() {
        return moreThanTen;
    }

    public TextFlow getTextFlow() {
        return dataDescription;
    }

    public void setMoreThanTen(boolean bool) {
        moreThanTen = bool;
    }

    public boolean isToggleSelected(){
        return toggleButton.isSelected();
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
                if (isClassification)
                    continue;
                else {
                    label.setText(PropertyManager.getManager().getPropertyValue(CLUSTERS.toString()));
                    label.setPadding(new Insets(0, 25, 0, 0));
                }
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
        if (isClassification) {
            for (int i = 0; i < 3; i++) {
                if (classificationConfigs[i] != null) {
                    if (firstCheck) {
                        textFields[0].setText(String.valueOf(classificationConfigs[0].getMaxIterations()));
                        textFields[1].setText(String.valueOf(classificationConfigs[0].getUpdateInterval()));
                        isContinuous.setSelected(classificationConfigs[0].tocontinue());
                    } else if (secondCheck) {
                        textFields[0].setText(String.valueOf(classificationConfigs[1].getMaxIterations()));
                        textFields[1].setText(String.valueOf(classificationConfigs[1].getUpdateInterval()));
                        isContinuous.setSelected(classificationConfigs[1].tocontinue());
                    } else {
                        textFields[0].setText(String.valueOf(classificationConfigs[2].getMaxIterations()));
                        textFields[1].setText(String.valueOf(classificationConfigs[2].getUpdateInterval()));
                        isContinuous.setSelected(classificationConfigs[2].tocontinue());
                    }
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {

                    if (clusterConfigs[0]!=null&&firstCheck) {
                        textFields[0].setText(String.valueOf(clusterConfigs[0].getMaxIterations()));
                        textFields[1].setText(String.valueOf(clusterConfigs[0].getUpdateInterval()));
                        textFields[2].setText(String.valueOf(clusterConfigs[0].getClusters()));
                        isContinuous.setSelected(clusterConfigs[0].tocontinue());
                    } else if (clusterConfigs[1]!=null&&secondCheck) {
                        textFields[0].setText(String.valueOf(clusterConfigs[1].getMaxIterations()));
                        textFields[1].setText(String.valueOf(clusterConfigs[1].getUpdateInterval()));
                        textFields[2].setText(String.valueOf(clusterConfigs[1].getClusters()));
                        isContinuous.setSelected(clusterConfigs[1].tocontinue());
                    } else if (clusterConfigs[2]!=null&&thirdCheck){
                        textFields[0].setText(String.valueOf(clusterConfigs[2].getMaxIterations()));
                        textFields[1].setText(String.valueOf(clusterConfigs[2].getUpdateInterval()));
                        textFields[2].setText(String.valueOf(clusterConfigs[2].getClusters()));
                        isContinuous.setSelected(clusterConfigs[2].tocontinue());
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

        for (int i = 0; i < textFields.length; i++) {
            if(isClassification&&i==2)
                continue;
            textFields[i].textProperty().
                    addListener((observable, oldValue, newValue) -> {
                        boolean isDigit = false;
                        boolean isValid = true;
                        Outer:
                        for (int j = 0; j < textFields.length; j++) {
                            if (isClassification) {
                                if (j == 2) {
                                    continue;
                                }
                            } else {
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
                                        break Outer;
                                    }
                                }
                                for (int k = 0; k < newValue.length(); k++) {
                                    if (!Character.isDigit(newValue.charAt(k))) {
                                        textWatcher.setText(PropertyManager.getManager().getPropertyValue(TEXTWATCH_MSG.toString()));
                                        ok.setDisable(true);
                                        isValid = false;
                                        break Outer;
                                    }
                                    isDigit = true;
                                }
                                //Verified the string input is digit
                                if (isDigit) {
                                    if (Integer.parseInt(newValue) <= 0) {
                                        textWatcher.setText(PropertyManager.getManager().getPropertyValue(TXT_WATCH_NEGATIVE.toString()));
                                        ok.setDisable(true);
                                        isValid = false;
                                    }
                                }
                            }


                        }


                        if (isValid) {
                            textWatcher.setText(PropertyManager.getManager().getPropertyValue(EMPTY.name()));
                            ok.setDisable(false);
                        }
                    });

        }
        ok.setOnAction(event -> {
            if (firstCheck) {
                radioButtons[0].setDisable(false);
                if (isClassification) {
                    classificationConfigs[0] = new Classification(Integer.valueOf(textFields[0].getText()), Integer.valueOf(textFields[1].getText()), isContinuous.isSelected());
                } else {
                    clusterConfigs[0] = new Clustering(Integer.valueOf(textFields[0].getText()), Integer.valueOf(textFields[1].getText()), Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());
                }
                radioButtons[0].setOnMouseClicked(e -> {
                    displayButton.setDisable(false);
                });
            } else if (secondCheck) {
                radioButtons[1].setDisable(false);
                if (isClassification) {
                    classificationConfigs[1] = new Classification(Integer.valueOf(textFields[0].getText()), Integer.valueOf(textFields[1].getText()), isContinuous.isSelected());
                } else {
                    clusterConfigs[1] = new Clustering(Integer.valueOf(textFields[0].getText()), Integer.valueOf(textFields[1].getText()), Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());
                }

                radioButtons[1].setOnMouseClicked(e -> {
                    displayButton.setDisable(false);
                });
            } else {
                radioButtons[2].setDisable(false);
                if (isClassification) {
                    classificationConfigs[2] = new Classification(Integer.valueOf(textFields[0].getText()), Integer.valueOf(textFields[1].getText()), isContinuous.isSelected());
                } else {
                    clusterConfigs[2] = new Clustering(Integer.valueOf(textFields[0].getText()), Integer.valueOf(textFields[1].getText()), Integer.valueOf(textFields[2].getText()), isContinuous.isSelected());
                }

                radioButtons[2].setOnMouseClicked(e -> {
                    displayButton.setDisable(false);
                });
            }

            newStage.close();
        });
        cancel.setOnAction(event -> {
            newStage.close();
        });


    }

}
