package ui;

import actions.AppActions;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;
import java.util.Arrays;
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
    private Button scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number> chart;          // the chart where data will be displayed
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;       // text area for new data input
    private boolean hasNewText;     // whether or not the text area has any new data since last display
    private Pane hBox;
    private Text textAreaTitle;
    private String scrnShotIconPath;
    private String currentText;
    private CheckBox checkBox;
    private boolean moreThanTen;
    private StringBuilder pendingText;
    private String textAreaLbl;
    private TextFlow dataDescription;

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        hBox = new HBox();
        textAreaTitle = new Text(applicationTemplate.manager.getPropertyValue(TEXT_AREA_TITLE.toString()));
        textAreaLbl = applicationTemplate.manager.getPropertyValue(TEXT_AREA.toString());
        textArea = new TextArea();
        displayButton = new Button(applicationTemplate.manager.getPropertyValue(DISPLAY.toString()));
        dataDescription = new TextFlow();

    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        String iconsPath = applicationTemplate.manager.getPropertyValue(SEPARATOR.toString()) + String.join(applicationTemplate.manager.getPropertyValue(SEPARATOR.toString()),
                applicationTemplate.manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                applicationTemplate.manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnShotIconPath = String.join(applicationTemplate.manager.getPropertyValue(SEPARATOR.toString()), iconsPath, applicationTemplate.manager.getPropertyValue(SCREENSHOT_ICON.name()));


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
        checkBox = new CheckBox(applicationTemplate.manager.getPropertyValue(READ_ONLY.toString()));
        workspace.getChildren().add(textAreaTitle);
        workspace.getChildren().add(textArea);
        workspace.getChildren().add(displayButton);
        workspace.getChildren().add(dataDescription);
        workspace.getChildren().add(checkBox);

        VBox.setMargin(textAreaTitle, new Insets(0, 0, 0, 200));
        hBox.getChildren().add(workspace);
        hBox.getChildren().add(chart);
        super.appPane.getChildren().add(hBox);


    }

    private void setWorkspaceActions() {
        tsdProcessor.setManager(this.applicationTemplate.manager);
        displayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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

            }
        });
        textArea.textProperty().

                addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {


                        if (pendingText != null && pendingText.toString().length() != 0 && isMoreThanTen()) {

                            StringBuilder oldStrbfr = new StringBuilder(oldValue);
                            StringBuilder newStrbfr = new StringBuilder(newValue);


                            String[] oldLines = oldStrbfr.toString().split(newLineRegex);
                            String[] newLines = newStrbfr.toString().split(newLineRegex);

                            int deletedLineCount = oldLines.length - newLines.length;
                            String[] pending = pendingText.toString().split(newLineRegex);
                            StringBuilder newText = new StringBuilder();

                            if (pending.length >= deletedLineCount && deletedLineCount > 0) {

                                for (int count = 0; count < deletedLineCount; count++) {
                                    newText.append(pending[count] + System.lineSeparator());
                                }

                                textArea.setText(newValue + newText.toString());
                                pendingText.setLength(0);
                                pendingText.trimToSize();
                                for (int i = deletedLineCount; i < pending.length; i++) {
                                    pendingText.append(pending[i] + System.lineSeparator());
                                }

                            }


                        }
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
                        Pattern formatPattern = Pattern.compile(entireFormat.toString());
                        final AtomicInteger atomicInteger = new AtomicInteger(0);
                        Stream.of(newValue.split(newLineRegex))
                                .forEach(line -> {
                                    Matcher formatMatch = formatPattern.matcher(line);
                                    atomicInteger.incrementAndGet();
                                    if (formatMatch.matches() && atomicInteger.get() == 1) {
                                        AppUI.super.newButton.setDisable(false);
                                        AppUI.super.saveButton.setDisable(false);
                                    } else if (!formatMatch.matches() && atomicInteger.get() == 1) {
                                        AppUI.super.newButton.setDisable(true);
                                        AppUI.super.saveButton.setDisable(true);
                                        disableScrnshot();
                                    }
                                    chart.getData().clear();
                                    disableScrnshot();
                                    tsdProcessor.update();
                                });
                    }
                });


        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue)
                    textArea.setDisable(true);
                else
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

    public void setReadOnly() {
        checkBox.setSelected(true);
    }

    public void setEditable() {
        checkBox.setSelected(false);
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

    public boolean isMoreThanTen() {
        return moreThanTen;
    }

    public TextFlow getTextFlow(){ return dataDescription; }

    public void setMoreThanTen(boolean bool) {
        moreThanTen = bool;
    }

}
