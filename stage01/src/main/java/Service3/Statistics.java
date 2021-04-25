package Service3;

/**
 * @create :2021-04-23 14:55:00
 * @Version: 0.0
 * @Descriptions:
 */
public class Statistics {

    private int countUnloaded;
    private int countLoose;
    private int countLiquid;
    private int countContainer;
    private int totalFine;
    private double sumWaitDuration;
    private double avrWaitDuration;
    private double sumDelay;
    private double avrDelay;
    private double maxDelay;
    private double avrWaitLength;

    @Override
    public String toString() {
        return "ResultStatistics{" +
                "countUnloaded=" + countUnloaded +
                ", countLoose=" + countLoose +
                ", countLiquid=" + countLiquid +
                ", countContainer=" + countContainer +
                ", totalFine=" + totalFine +
                ", sumWaitDuration=" + sumWaitDuration +
                ", avrWaitDuration=" + avrWaitDuration +
                ", sumDelay=" + sumDelay +
                ", avrWaitLength=" + avrWaitLength +
                ", avrDelay=" + avrDelay +
                ", maxDelay=" + maxDelay +
                '}';
    }

    public double getAvrWaitLength() {
        return avrWaitLength;
    }

    public void setAvrWaitLength(double avrWaitLength) {
        this.avrWaitLength = avrWaitLength;
    }

    public Statistics(int countUnloaded, int countLoose, int countLiquid, int countContainer, int totalFine, double sumWaitDuration, double avrWaitDuration, double sumDelay, double sumUD, double avrUD, double avrWaitLength, double avrDelay, double maxDelay) {
        this.countUnloaded = countUnloaded;
        this.countLoose = countLoose;
        this.countLiquid = countLiquid;
        this.countContainer = countContainer;
        this.totalFine = totalFine;
        this.sumWaitDuration = sumWaitDuration;
        this.avrWaitDuration = avrWaitDuration;
        this.sumDelay = sumDelay;
        this.avrWaitLength = avrWaitLength;
        this.avrDelay = avrDelay;
        this.maxDelay = maxDelay;
    }

    public Statistics(int countUnloaded, int countLoose, int countLiquid, int countContainer, int totalFine, double sumWaitDuration, double avrWaitDuration, double sumDelay, double sumUD, double arvUD, double avrDelay, double maxDelay) {
        this.countUnloaded = countUnloaded;
        this.countLoose = countLoose;
        this.countLiquid = countLiquid;
        this.countContainer = countContainer;
        this.totalFine = totalFine;
        this.sumWaitDuration = sumWaitDuration;
        this.avrWaitDuration = avrWaitDuration;
        this.sumDelay = sumDelay;
        this.avrDelay = avrDelay;
        this.maxDelay = maxDelay;
    }

    public Statistics(int countUnloaded, int countLoose, int countLiquid, int countContainer, int totalFine, double sumWaitDuration, double avrWaitDuration, double sumDelay, double avrDelay, double maxDelay) {
        this.countUnloaded = countUnloaded;
        this.countLoose = countLoose;
        this.countLiquid = countLiquid;
        this.countContainer = countContainer;
        this.totalFine = totalFine;
        this.sumWaitDuration = sumWaitDuration;
        this.avrWaitDuration = avrWaitDuration;
        this.sumDelay = sumDelay;
        this.avrDelay = avrDelay;
        this.maxDelay = maxDelay;
    }

    public Statistics() {

    }

    public double getSumDelay() {
        return sumDelay;
    }

    public void setSumDelay(double sumDelay) {
        this.sumDelay = sumDelay;
    }

    public int getCountUnloaded() {
        return countUnloaded;
    }

    public void setCountUnloaded(int countUnloaded) {
        this.countUnloaded = countUnloaded;
    }

    public int getCountLoose() {
        return countLoose;
    }

    public void setCountLoose(int countLoose) {
        this.countLoose = countLoose;
    }

    public int getCountLiquid() {
        return countLiquid;
    }

    public void setCountLiquid(int countLiquid) {
        this.countLiquid = countLiquid;
    }

    public int getCountContainer() {
        return countContainer;
    }

    public void setCountContainer(int countContainer) {
        this.countContainer = countContainer;
    }

    public int getTotalFine() {
        return totalFine;
    }

    public void setTotalFine(int totalFine) {
        this.totalFine = totalFine;
    }

    public double getSumWaitDuration() {
        return sumWaitDuration;
    }

    public void setSumWaitDuration(double sumWaitDuration) {
        this.sumWaitDuration = sumWaitDuration;
    }

    public double getAvrWaitDuration() {
        return avrWaitDuration;
    }

    public void setAvrWaitDuration(double avrWaitDuration) {
        this.avrWaitDuration = avrWaitDuration;
    }

    public double getAvrDelay() {
        return avrDelay;
    }

    public void setAvrDelay(double avrDelay) {
        this.avrDelay = avrDelay;
    }

    public double getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(double maxDelay) {
        this.maxDelay = maxDelay;
    }

}
