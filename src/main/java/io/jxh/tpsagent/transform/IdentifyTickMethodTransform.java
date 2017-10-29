package io.jxh.tpsagent.transform;

import io.jxh.tpsagent.advice.IdentifyTickMethodAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class IdentifyTickMethodTransform {

    private static ResettableClassFileTransformer transformer;

    private static Set<String> getMinecraftServerTypes() throws ClassNotFoundException {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        final Class nmsClass = classLoader.loadClass("net.minecraft.server.MinecraftServer");

        final HashSet<String> nmsFieldSet = new HashSet<>();
        for (Field field : nmsClass.getDeclaredFields()) {
            nmsFieldSet.add(field.getType().getName());
        }

        return nmsFieldSet;
    }

    private static Set<String> getClassesFromCurrentJar() throws Exception {
        final Set<String> classes = new HashSet<>();

        final Set<String> nmsFieldTypes = getMinecraftServerTypes();
        final URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        for (URL jarUrl : classLoader.getURLs()) {
            JarFile jar = new JarFile(new File(jarUrl.getPath()));

            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String className = entry.getName();

                if (!className.endsWith(".class")) continue;

                className = className.replace(".class", "");
                if (!nmsFieldTypes.contains(className)) continue;

                classes.add(className);
            }
        }

        return classes;
    }

    private static ElementMatcher.Junction<TypeDescription> buildTypeMatcher() throws Exception {
        ElementMatcher.Junction<TypeDescription> typeMatcher = none();

        for (String className : getClassesFromCurrentJar()) {
            typeMatcher = typeMatcher.or(named(className));
        }

        return typeMatcher;
    }

    public static void transform(final Instrumentation inst) {
        System.out.println("Running transformer: " + IdentifyTickMethodTransform.class.getName());

        // Restricts our transformations to a specific method signature: public void method(String)
        // This matches the signature of Profiler#setSection(String)
        final ElementMatcher.Junction<MethodDescription> profilerMethodSignature = isPublic()
                .and(returns(TypeDescription.VOID))
                .and(takesArgument(0, String.class))
                .and(takesArguments(1));

        final AgentBuilder.Transformer bbTransformer = (builder, typeDescription, classLoader, javaModule) ->
                builder.visit(Advice.to(IdentifyTickMethodAdvice.class).on(profilerMethodSignature));

        try {
            transformer = new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                    .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                    // typeMatcher permits a narrow selection of obfuscated classes by name
                    // At this point, we don't know which class Profiler is, so we guess and then reset later on.
                    .type(buildTypeMatcher())
                    .transform(bbTransformer)
                    .installOn(inst);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void reset(final Instrumentation inst) {
        System.out.println("Resetting transformer: " + IdentifyTickMethodTransform.class.getName());

        if (transformer != null) {
            transformer.reset(inst, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        }
    }

}
