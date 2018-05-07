package algorithm_list;

import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import ui.AppUI;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static settings.AppPropertyTypes.CONTINUOUS_FINISHED;
import static settings.AppPropertyTypes.RUNNING_STATE;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean tocontinue;
    private final AtomicBoolean isContinuous;
    private boolean finished;
    private ApplicationTemplate applicationTemplate;
    private boolean firstRun;

    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean isContinuous) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(false);
        this.isContinuous= new AtomicBoolean(isContinuous);
        firstRun=true;
    }



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
        return isContinuous.get();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    public void setApplicationTemplate(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }
    @Override
    public void run() {

        if (isContinuous.get()) {
                finished = false;
                Timeline timeline = new Timeline();
                KeyFrame keyFrame = new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {
                    ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                    assignLabels();
                    AppData appData = new AppData(applicationTemplate);
                    appData.displayData(dataset.getLocations(), dataset.getLabels());
                    ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getTextWatchDisplay().setText(PropertyManager.getManager().getPropertyValue(RUNNING_STATE.toString()));
                    ((AppUI) applicationTemplate.getUIComponent()).disableState(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getDisplayButton().setDisable(true);
                    recomputeCentroids();
                });
            initializeCentroids();
            int iteration = 0;
            DataSet temp = new DataSet(dataset.getLabels(),dataset.getLocations());
            while (iteration++ < maxIterations & tocontinue.get()) {
                assignLabels();
                recomputeCentroids();
            }
            dataset=temp;
            initializeCentroids();
                timeline.setCycleCount(iteration);
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

        }else {
            if(firstRun)
            initializeCentroids();
            firstRun=false;

            Timeline timeline = new Timeline();
            KeyFrame keyFrame = new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {
                assignLabels();
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                AppData appData = new AppData(applicationTemplate);
                ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
                ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                ((AppUI) applicationTemplate.getUIComponent()).getComboBox().setDisable(true);
                appData.displayData(dataset.getLocations(),dataset.getLabels());
            });

            timeline.setCycleCount(1);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
            timeline.setOnFinished(e -> {
                if (!finished) {
                    ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getComboBox().setDisable(true);
                    firstRun=false;
                    recomputeCentroids();
                    if(!tocontinue.get())
                    finished=true;
                } else {
                    ((AppUI) applicationTemplate.getUIComponent()).disableState(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getDisplayButton().setDisable(true);
                    firstRun=true;
                    finished = true;
                    recomputeCentroids();
                }
                ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
            });
        }

    }

    private void initializeCentroids() {
        Set<String> chosen = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random r = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                ++i;
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {

        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance = Double.MAX_VALUE;
            int minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }

            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));

        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }


}