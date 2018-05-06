package classification;

import data.DataSet;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;
import ui.AppUI;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static settings.AppPropertyTypes.CONTINUOUS_FINISHED;
import static settings.AppPropertyTypes.RUNNING_STATE;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private ApplicationTemplate applicationTemplate;
    private final int maxIterations;
    private final int updateInterval;
    private boolean finished;
    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    @Override
    public void run() {

        if (tocontinue.get()) {
            finished = false;
            Timeline timeline = new Timeline();
            KeyFrame keyFrame = new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {
                int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int constant = new Double(RAND.nextDouble() * 100).intValue();
                output = Arrays.asList(xCoefficient, yCoefficient, constant);
                ArrayList<Point2D> points = new ArrayList<>();
                points.addAll(dataset.getLocations().values());
                double startY = points.get(0).getY();
                double endY = points.get(0).getY();

                for (int j = 0; j < points.size(); j++) {
                    if (startY < points.get(j).getY())
                        startY = points.get(j).getY();
                    if (endY > points.get(j).getY())
                        endY = points.get(j).getY();
                }
                double startX = (-yCoefficient * (startY - 20) - constant) / xCoefficient;
                double endX = (-yCoefficient * (endY + 20) - constant) / xCoefficient;
                XYChart.Series<Number, Number> line = new XYChart.Series<>();
                line.getData().add(new XYChart.Data<>(startX, startY - 20));
                line.getData().add(new XYChart.Data<>(endX, endY + 20));
                ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
                ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                ((AppUI) applicationTemplate.getUIComponent()).getTextWatchDisplay().setText(PropertyManager.getManager().getPropertyValue(RUNNING_STATE.toString()));
                ((AppUI) applicationTemplate.getUIComponent()).disableState(true);
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().set(0, line);
                ((AppUI) applicationTemplate.getUIComponent()).getDisplayButton().setDisable(true);
                line.setName("Classifying Line");
                Node fill = line.getNode().lookup(".chart-series-line");
                fill.setStyle("-fx-stroke: red;");
                for (int i = 0; i < line.getData().size(); i++) {
                    Node symbol = line.getData().get(i).getNode().lookup(".chart-line-symbol");
                    symbol.setStyle(" -fx-background-color: transparent, transparent;");
                }

            });
            int iterationSimulation = 0;
            for (int i = 1; i <= maxIterations; i++) {
                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    iterationSimulation = i;
                    break;
                } else iterationSimulation = i;
            }
            iterationSimulation /= updateInterval;
            timeline.setCycleCount(iterationSimulation);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
            timeline.setOnFinished(e -> {
                ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                ((AppUI) applicationTemplate.getUIComponent()).disableState(false);
                ((AppUI) applicationTemplate.getUIComponent()).getDisplayButton().setDisable(true);
                ((AppUI) applicationTemplate.getUIComponent()).getTextWatchDisplay().setText(PropertyManager.getManager().getPropertyValue(CONTINUOUS_FINISHED.toString()));
                ((AppUI) applicationTemplate.getUIComponent()).disableRadioButtons();
                finished = true;
            });
        } else {
            Timeline timeline = new Timeline();
            KeyFrame keyFrame = new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {

                int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int constant = new Double(RAND.nextDouble() * 100).intValue();

                output = Arrays.asList(xCoefficient, yCoefficient, constant);

                ArrayList<Point2D> points = new ArrayList<>();
                points.addAll(dataset.getLocations().values());
                double startY = points.get(0).getY();
                double endY = points.get(0).getY();

                for (int j = 0; j < points.size(); j++) {
                    if (startY < points.get(j).getY())
                        startY = points.get(j).getY();
                    if (endY > points.get(j).getY())
                        endY = points.get(j).getY();
                }
                double startX = (-yCoefficient * (startY - 20) - constant) / xCoefficient;
                double endX = (-yCoefficient * (endY + 20) - constant) / xCoefficient;
                XYChart.Series<Number, Number> line = new XYChart.Series<>();
                line.setName("Classifying Line");
                line.getData().add(new XYChart.Data<>(startX, startY - 20));
                line.getData().add(new XYChart.Data<>(endX, endY + 20));
                ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
                ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().set(0, line);
                ((AppUI) applicationTemplate.getUIComponent()).getComboBox().setDisable(true);
                Node fill = line.getNode().lookup(".default-color0.chart-series-line");
                fill.setStyle("-fx-stroke: red;");
                for (int i = 0; i < line.getData().size(); i++) {
                    Node symbol = line.getData().get(i).getNode().lookup(".default-color0.chart-line-symbol");
                    symbol.setStyle(" -fx-background-color: transparent, transparent;");
                }


            });

            timeline.setCycleCount(1);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
            timeline.setOnFinished(e -> {
                if (!finished) {
                    ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getComboBox().setDisable(true);
                } else {
                    ((AppUI) applicationTemplate.getUIComponent()).disableState(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getDisplayButton().setDisable(true);
                    finished = true;
                }
                ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
            });
        }
    }

    public void setApplicationTemplate(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

}