package classification;

import data.DataSet;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

            if(tocontinue.get()) {
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
//            ((AppUI) applicationTemplate.getUIComponent()).getChart().getYAxis().setAutoRanging(false);
//            ((AppUI) applicationTemplate.getUIComponent()).getChart().getXAxis().setAutoRanging(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getTextWatchDisplay().setText("Continuous Algorithm Running..");
                    ((AppUI) applicationTemplate.getUIComponent()).disableState(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().set(0, line);
                });
                int iterationSimulation = 0;
                for (int i = 1; i <= maxIterations; i++) {
                    if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                        iterationSimulation = i;
                        break;
                    } else iterationSimulation = i;
                }
                iterationSimulation/=updateInterval;
                timeline.setCycleCount(iterationSimulation);
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
                timeline.setOnFinished(e -> {
                    ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                    ((AppUI) applicationTemplate.getUIComponent()).disableState(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getTextWatchDisplay().setText("Continuous Algorithm Running finished ");
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
                    line.getData().add(new XYChart.Data<>(startX, startY - 20));
                    line.getData().add(new XYChart.Data<>(endX, endY + 20));
                    ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
        //            ((AppUI) applicationTemplate.getUIComponent()).getChart().getYAxis().setAutoRanging(false);
        //            ((AppUI) applicationTemplate.getUIComponent()).getChart().getXAxis().setAutoRanging(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().set(0, line);
                });

                timeline.setCycleCount(1);
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
                timeline.setOnFinished(e -> {
                    if(!finished)
                        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(true);
                    else {
                        ((AppUI) applicationTemplate.getUIComponent()).disableState(false);
                        finished=false;
                    }
                    ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                });
            }
    }

    public void setApplicationTemplate(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }
    public void setFinished(boolean finished){
        this.finished=finished;
    }

}