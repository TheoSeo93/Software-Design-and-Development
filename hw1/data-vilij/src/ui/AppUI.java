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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private ImageView configImg;

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {

        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        PropertyManager manager = applicationTemplate.manager;
        hBox = new HBox();
        textAreaTitle = new Text(manager.getPropertyValue(TEXT_AREA_TITLE.toString()));
        textAreaTitle.setFont(new Font(17));
        //Text FontSize to be corrected
        textAreaLbl = manager.getPropertyValue(TEXT_AREA.toString());
        textArea = new TextArea();
        dataDescription = new TextFlow();
        dataDescription.setPrefWidth(textArea.getWidth());
        toggleButton = new ToggleButton(manager.getPropertyValue(READ_ONLY.toString()));
        comboBox = new ComboBox();
        displayButton.getStyleClass().add("toolbar-button");
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
        scrnshotButton = setToolbarButton(scrnShotIconPath, applicationTemplate.manager.getPropertyValue(SCREENSHOT_TOOLTIP.toString()), true);
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
        chart = new LineChart<>(xAxis, yAxis);
        chart.getStylesheets().add(this.getClass().getResource(applicationTemplate.manager.getPropertyValue(CSS_ADDRESS.toString())).toExternalForm());
        chart.setTitle(applicationTemplate.manager.getPropertyValue(DATA_VISUALIZATION.toString()));
        workspace = new VBox();
        workspace.getChildren().add(textAreaTitle);
        workspace.getChildren().add(textArea);
        workspace.getChildren().add(dataDescription);
        workspace.getChildren().add(toggleButton);
        ((VBox) workspace).setSpacing(5);
        VBox.setMargin(textAreaTitle, new Insets(12, 0, 0, 200));
        hBox.getChildren().add(workspace);
        hBox.getChildren().add(chart);
        super.appPane.getChildren().add(hBox);


    }

    private void setWorkspaceActions() {
        tsdProcessor.setManager(this.applicationTemplate.manager);
        settings1.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            showConfigDialog();
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
            showConfigDialog();
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
            showConfigDialog();
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
                    algorithmConfig.getChildren().add(algoName);
                    algoName.setFont(new Font(14));
                    ImageView settings;
                    ToggleGroup toggleGroup = new ToggleGroup();
                    RadioButton[] radioButtons = new RadioButton[3];
                    for (int i = 0; i < 3; i++) {
                        settings = imageViews[i];

                        RadioButton radioButton = new RadioButton("  Algorithm " + (i + 1));
                        radioButton.setToggleGroup(toggleGroup);
                        radioButton.setFont(new Font(14));
                        radioButtons[i] = radioButton;
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
                    algorithmConfig.getChildren().add(algoName);
                    algoName.setFont(new Font(14));
                    ImageView settings;
                    ToggleGroup toggleGroup = new ToggleGroup();
                    RadioButton[] radioButtons = new RadioButton[3];
                    for (int i = 0; i < 3; i++) {
                        settings = imageViews[i];

                        RadioButton radioButton = new RadioButton("  Algorithm " + (i + 1));
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
                            comboBox.setPromptText("Set Algorithm Type");
                            workspace.getChildren().add(comboBox); //Index: 4 , size: 5
                        }

                    }
                    textArea.setDisable(true);
                } catch (Exception ex) {
                    textArea.setDisable(false);
                    Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    error.show(applicationTemplate.manager.getPropertyValue(WRONG_DATA_FORMAT_ERROR.toString()), ex.getMessage() + System.lineSeparator());
                } finally {

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
        Text firstLine = new Text(tsdProcessor.getDataSize() + " instances are loaded ");

        Text labelDescription = new Text(tsdProcessor.getDataLabelCount() + " labels are named as " + System.lineSeparator());

        Text pathDescription;
        if (dataFilePath != null)
            pathDescription = new Text("from" + System.lineSeparator() + dataFilePath + System.lineSeparator());
        else
            pathDescription = new Text(System.lineSeparator() + "The file path has not been specified yet" + System.lineSeparator());

        textFlow.getChildren().add(firstLine);
        textFlow.getChildren().add(pathDescription);
        textFlow.getChildren().add(labelDescription);
        Iterator labelIterator = tsdProcessor.getLabels().iterator();

        for (int i = 0; i < tsdProcessor.getDataLabelCount(); i++) {
            textFlow.getChildren().add(new Text("-" + labelIterator.next() + System.lineSeparator()));
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

    public void layoutRenew() {
        workspace.getChildren().remove(workspace.getChildren().size() - 1);
    }

    public void showConfigDialog() {
        GridPane configDialog = new GridPane();
        configDialog.setHgap(14);
        configDialog.setVgap(20);
        configDialog.setPadding(new Insets(14, 14, 14, 14));
        configDialog.add(configImg, 0, 1);
        configDialog.setPrefSize(700, 151);
        Button cancel = new Button("Cancel");
        Button ok = new Button("Ok");

        HBox buttonGroup = new HBox();
        cancel.setMinSize(80, 25);
        ok.setMinSize(80, 25);
        ok.setDisable(true);
        buttonGroup.getChildren().addAll(cancel, ok);
        buttonGroup.setSpacing(14);
        buttonGroup.setPadding(new Insets(0, 0, 0, 120));
        VBox vBox = new VBox();
        vBox.setSpacing(7);
        vBox.setPrefSize(608, 83);
        TextField[] textFields = new TextField[3];
        Label textWatcher = new Label();
        CheckBox isContinuous = new CheckBox();
        for (int i = 0; i < 4; i++) {
            HBox row = new HBox();
            Label label = new Label();
            label.setMinSize(96, 19);
            label.setFont(Font.font(null, FontWeight.EXTRA_BOLD, 13));

            TextField textField = new TextField();

            if (i == 0) {
                label.setText("Max Iterations");
                label.setPadding(new Insets(0, 50, 0, 0));
            } else if (i == 1) {
                label.setText("Update Interval");
                label.setPadding(new Insets(0, 44, 0, 0));
            } else if (i == 2) {
                if (isClassification)
                    continue;
                else {
                    label.setText("Number of Clusters");
                    label.setPadding(new Insets(0, 22, 0, 0));
                }
            } else {
                label.setText("Continuous Run?");
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

        configDialog.add(vBox, 1, 1);
        configDialog.setEffect(new Glow(0.44));
        Stage newStage = new Stage();
        newStage.setMinWidth(700);
        newStage.setScene(new Scene(configDialog));
        newStage.setTitle("Run Configurations");
        newStage.show();

        for (int i = 0; i < textFields.length; i++) {
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
                                if(textFields[j].getText().length()==0||newValue.length()==0) {
                                    ok.setDisable(true);
                                    isValid = false;
                                    break Outer;
                                }
                                for (int k = 0; k < textFields[j].getText().length(); k++) {
                                    if (!Character.isDigit(textFields[j].getText().charAt(k))) {
                                        textWatcher.setText("The input has to be a positive integer.");
                                        ok.setDisable(true);
                                        isValid = false;
                                        break Outer;
                                    }
                                }
                                for (int k = 0; k < newValue.length(); k++) {
                                    if (!Character.isDigit(newValue.charAt(k))) {
                                        textWatcher.setText("The input has to be a positive integer.");
                                        ok.setDisable(true);
                                        isValid = false;
                                        break Outer;
                                    }
                                    isDigit = true;
                                }
                                //Verified the string input is digit
                                if (isDigit) {
                                    if (Integer.parseInt(newValue) <= 0) {
                                        textWatcher.setText("The input has to be positive.");
                                        ok.setDisable(true);
                                        isValid = false;
                                    }
                                }
                            }


                        }


                        if (isValid) {
                            textWatcher.setText("");
                            ok.setDisable(false);
                        }
                    });

        }
        ok.setOnAction(event -> {

            newStage.close();
        });
        cancel.setOnAction(event -> {
            newStage.close();
        });


    }
}
