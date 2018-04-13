package algorithms;

import static settings.AppPropertyTypes.CLASSIFICATION;

public class Classification extends Classifier  {
    private int maxIterations;
    private int updateInterval;
    private boolean toContinue;
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
    public void run() {

    }
    public String getName(){
        return (CLASSIFICATION.toString());
    }
}
