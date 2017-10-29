package io.jxh.tpsagent;

public class MinecraftServerHolder {

    private static Object nmsReference = null;

    public static void set(final Object nms) {
        if (nmsReference != null) return;

        nmsReference = nms;
        System.out.println("Assigned NMS reference: " + nms.getClass().getName());
    }

    public static Object get() {
        return nmsReference;
    }

}
