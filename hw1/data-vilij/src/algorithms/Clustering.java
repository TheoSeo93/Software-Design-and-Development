package algorithms;

import static settings.AppPropertyTypes.CLUSTERING;

public class Clustering implements Algorithm {
    private int maxIterations;
    private int updateInterval;
    private boolean toContinue;
    private int clusters;

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
    public void run() {

    }

    public String getName() {
        return (CLUSTERING.toString());
    }
}
