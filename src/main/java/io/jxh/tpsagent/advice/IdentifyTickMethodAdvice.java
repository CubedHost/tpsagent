package io.jxh.tpsagent.advice;

import io.jxh.tpsagent.TickMethodHolder;
import net.bytebuddy.asm.Advice;

public class IdentifyTickMethodAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) String tag) {
        if (tag == null) return;
        if (!tag.equals("root")) return;

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) return;

        final String serverClassName = stackTrace[2].getClassName();
        if (!serverClassName.equals("net.minecraft.server.MinecraftServer")) return;

        final String tickMethodName = stackTrace[2].getMethodName();

        TickMethodHolder.set(tickMethodName);
    }
}
