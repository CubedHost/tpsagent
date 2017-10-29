package io.jxh.tpsagent.transform;

import io.jxh.tpsagent.advice.ServerTickAdvice;
import io.jxh.tpsagent.TickMethodHolder;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class TickMethodTransform {

    private static ResettableClassFileTransformer transformer;

    public static void transform(final Instrumentation inst) {
        System.out.println("Running transformer: " + TickMethodTransform.class.getName());

        transformer = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .type(named("net.minecraft.server.MinecraftServer"))
                .transform((builder, typeDescription, classLoader, javaModule) ->
                        builder.visit(Advice.to(ServerTickAdvice.class).on(named(TickMethodHolder.get())))
                )
                .installOn(inst);
    }

    public static void reset(final Instrumentation inst) {
        System.out.println("Resetting transformer: " + TickMethodTransform.class.getName());

        if (transformer != null) {
            transformer.reset(inst, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        }
    }

}
