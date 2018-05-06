package classification;


import javafx.geometry.Point2D;

import java.util.Map;

import static settings.AppPropertyTypes.CLASSIFICATION;

public class Classification extends Classifier {
    private int maxIterations;
    private int updateInterval;
    private boolean toContinue;
    private boolean isFinished;
    public Classification(){}

    public Classification(int maxIterations, int updateInterval,boolean toContinue){
        this.maxIterations=maxIterations;
        this.updateInterval=updateInterval;
        this.toContinue=toContinue;
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
        isFinished=finished;
    }


    @Override
    public void run() {

    }
    public String getName(){
        return (CLASSIFICATION.toString());
    }
}
