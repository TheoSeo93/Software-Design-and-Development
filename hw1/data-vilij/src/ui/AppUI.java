package ui;

import actions.AppActions;
import dataprocessors.TSDProcessor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;       // text area for new data input
    private boolean hasNewText;     // whether or not the text area has any new data since last display
    private Pane hBox;
    private Pane vBox;
    private Text textAreaTitle;

    public ScatterChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {

        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;

        hBox = new HBox();
        textAreaTitle = new Text("Data File");
        textArea = new TextArea();
        displayButton = new Button("DISPLAY");

    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {

        super.setToolBar(applicationTemplate);

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
    public void initialize()  {
        layout();
        setWorkspaceActions();

    }

    @Override
    public void clear() {

    }

    private void layout() {
        final NumberAxis xAxis = new NumberAxis(0, 100, 10);
        final NumberAxis yAxis = new NumberAxis(0, 110, 10);
        chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle("Data Visualization");

        vBox = new VBox();
        vBox.getChildren().add(textAreaTitle);
        vBox.getChildren().add(textArea);
        vBox.getChildren().add(displayButton);
        VBox.setMargin(textAreaTitle, new Insets(0, 0, 0, 200));

        hBox.getChildren().add(vBox);
        hBox.getChildren().add(chart);

        super.appPane.getChildren().add(hBox);
    }

    private void setWorkspaceActions() {

        HashMap<String, XYChart.Series> seriesHashmap = new HashMap<>();

        displayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                String chartData = textArea.getText().toString();

                try {
                    tsdProcessor.processString(chartData);
                } catch (Exception ex) {

                    if(ex instanceof TSDProcessor.InvalidDataNameException) {
                        System.out.print("ASD");
                        Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        error.show("WRONG DATA FORMAT ERROR", ex.getMessage());
                    } else {
                        Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        error.show("WRONG DATA FORMAT ERROR", ex.getMessage());
                    }
                }

                tsdProcessor.toChartData(chart);
                AppUI.super.saveButton.setDisable(false);

            }
        });


        textArea.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {


                String entireFormat = "^@[^\\s]+[a-zA-Z0-9]*[\\s]+[a-zA-Z0-9]+[\\s]+?([0-9]*[.])?[0-9]+,+?([0-9]*[.])?[0-9]+\\s*$";

                Pattern formatPattern = Pattern.compile(entireFormat);
                final AtomicInteger atomicInteger = new AtomicInteger(0);
                Stream.of(newValue.split("\n"))
                        .forEach(line -> {

                            Matcher formatMatch = formatPattern.matcher(line);
                            atomicInteger.incrementAndGet();
                            if (formatMatch.matches() && atomicInteger.get() == 1) {
                                AppUI.super.newButton.setDisable(false);
                                AppUI.super.saveButton.setDisable(false);
                            }

                            else if (!formatMatch.matches() && atomicInteger.get() == 1) {
                                AppUI.super.newButton.setDisable(true);
                                AppUI.super.saveButton.setDisable(true);

                            }
                            chart.getData().clear();
                            tsdProcessor.update();;
                        });


            }


        });

    }

}
