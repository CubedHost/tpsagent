package io.jxh.tpsagent.advice;

import io.jxh.tpsagent.MinecraftServerHolder;
import io.jxh.tpsagent.TickTracker;
import net.bytebuddy.asm.Advice;

public class ServerTickAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.This Object nmsReference) {
        MinecraftServerHolder.set(nmsReference);
        TickTracker.increment();
    }

}
