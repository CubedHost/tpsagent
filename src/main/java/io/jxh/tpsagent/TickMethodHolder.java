package io.jxh.tpsagent;

public class TickMethodHolder {

    private static String methodName = null;
    private static Runnable callback = null;

    public static void set(final String method) {
        if (methodName != null) return;

        methodName = method;
        System.out.println("Found tick method: " + methodName);

        tryRunCallback();
    }

    private static void tryRunCallback() {
        if (callback == null) return;
        if (methodName == null) return;

        System.out.println("Running callback");
        new Thread(callback).start();
    }

    public static String get() {
        return methodName;
    }

    public static void registerCallback(final Runnable runnable) {
        callback = runnable;
    }

}
