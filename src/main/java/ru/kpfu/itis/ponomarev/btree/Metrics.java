package ru.kpfu.itis.ponomarev.btree;

public class Metrics {
    private long operations;
    private long timeNano;

    public Metrics(long operations, long timeNano) {
        this.operations = operations;
        this.timeNano = timeNano;
    }

    public long getOperations() {
        return operations;
    }

    public long getTimeNano() {
        return timeNano;
    }

    @Override
    public String toString() {
        return operations + "," + timeNano;
    }
}
