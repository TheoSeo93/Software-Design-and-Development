package algorithms;

import static settings.AppPropertyTypes.CLUSTERING;

public class Clustering implements Algorithm {
    @Override
    public int getMaxIterations() {
        return 0;
    }

    @Override
    public int getUpdateInterval() {
        return 0;
    }

    @Override
    public boolean tocontinue() {
        return false;
    }

    @Override
    public void run() {

    }
    public String getName(){
        return (CLUSTERING.toString());
    }
}
