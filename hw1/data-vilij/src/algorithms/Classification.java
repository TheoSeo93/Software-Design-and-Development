package algorithms;

import static settings.AppPropertyTypes.CLASSIFICATION;

public class Classification extends Classifier  {
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
        return (CLASSIFICATION.toString());
    }
}
