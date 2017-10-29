package io.jxh.tpsagent;

public class TickTracker {

    private static int ticks = 0;

    public static int increment() {
        return ++ticks;
    }

    public static int get() {
        return ticks;
    }

}
