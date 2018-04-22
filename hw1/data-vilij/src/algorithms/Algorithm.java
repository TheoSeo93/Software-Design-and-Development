package algorithms;
/**
 * This interface provides a way to run an algorithm
 * on a thread as a {@link java.lang.Runnable} object.
 *
 * @author Ritwik Banerjee
 */
public interface Algorithm extends Runnable {

    enum AlgorithmType {
        CLASSIFICATION, CLUSTERING, RANDOMCLASSIFIER, RANDOMCLUSTERING
    }

    int getMaxIterations();

    int getUpdateInterval();

    boolean tocontinue();

    boolean isFinished();

    void setFinished(boolean finished);
}