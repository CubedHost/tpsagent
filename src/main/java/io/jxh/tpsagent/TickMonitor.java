package io.jxh.tpsagent;

public class TickMonitor {

    private int checkInterval;
    private int lastTickCount = -1;
    private double tps = 0d;
    private double memoryPercent = 0d;

    public TickMonitor() {
        this.checkInterval = 1;
    }

    public void start() {
        new Thread(this::tickMonitorLoop).start();
    }

    private void tickMonitorLoop() {
        System.out.println("Beginning tick monitor loop");

        try {
            while (true) {
                calculateTicksPerSecond();
                calculateMemoryUsage();

                Thread.sleep(checkInterval * 1000);
            }
        } catch (final InterruptedException ex) {
            ex.printStackTrace();
        }

        System.out.println("Ending tick monitor loop");
    }

    private void calculateTicksPerSecond() {
        System.out.println("Checking TPS");

        final int newTickCount = TickTracker.get();
        final int elapsedTicks = newTickCount - lastTickCount;

        if (lastTickCount >= 0) {
            System.out.println("Elapsed: " + elapsedTicks);
            tps =  (double) elapsedTicks / (double) checkInterval;
        }

        lastTickCount = newTickCount;

        System.out.println("TPS: " + tps);
    }

    private void calculateMemoryUsage() {
        final Runtime runtime = Runtime.getRuntime();

        final double freeMemory = (double) runtime.freeMemory();
        final double maxMemory = (double) runtime.maxMemory();

        this.memoryPercent = (maxMemory - freeMemory) * 100 / maxMemory;

        System.out.println("Memory Usage: " + memoryPercent + "%");
    }

    public double getTicksPerSecond() {
        return tps;
    }

    public double getMemoryUsage() {
        return memoryPercent;
    }

}
