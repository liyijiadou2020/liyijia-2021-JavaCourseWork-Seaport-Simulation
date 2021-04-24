package pojo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Performance {
    private int loosePerformance;
    private int liquidPerformance;
    private int containerPerformance;

    public int getLoosePerformance() {
        return loosePerformance;
    }

    public void setLoosePerformance(int loosePerformance) {
        this.loosePerformance = loosePerformance;
    }

    public int getLiquidPerformance() {
        return liquidPerformance;
    }

    public void setLiquidPerformance(int liquidPerformance) {
        this.liquidPerformance = liquidPerformance;
    }

    public int getContainerPerformance() {
        return containerPerformance;
    }

    public void setContainerPerformance(int containerPerformance) {
        this.containerPerformance = containerPerformance;
    }

    public Performance() {
    }

    public Performance(int performanceLoose, int performanceLiquid, int performanceContainer) {
        this.loosePerformance = performanceLoose;
        this.liquidPerformance = performanceLiquid;
        this.containerPerformance = performanceContainer;
    }

    @Override
    public String toString() {
        return "PerformanceCranes{" +
                "performanceLoose=" + loosePerformance +
                ", performanceLiquid=" + liquidPerformance +
                ", performanceContainer=" + containerPerformance +
                '}';
    }
}
