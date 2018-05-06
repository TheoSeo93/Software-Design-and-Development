package algorithms;

import javafx.geometry.Point2D;

import java.util.Map;

import static settings.AppPropertyTypes.CLUSTERING;

public class Clustering implements Algorithm {
    private int maxIterations;
    private int updateInterval;
    private boolean toContinue;
    private int clusters;
    private boolean isFinished;
    public Clustering() {

    }

    public Clustering(int maxIterations, int updateInterval, int clusters, boolean toContinue) {
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.toContinue = toContinue;
        this.clusters = clusters;
    }

    public int getClusters() {
        return clusters;
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
        return toContinue;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void setFinished(boolean finished) {
        this.isFinished=finished;
    }



    @Override
    public void run() {

    }

    public String getName() {
        return (CLUSTERING.toString());
    }
}
