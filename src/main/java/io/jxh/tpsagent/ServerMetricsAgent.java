package io.jxh.tpsagent;

import io.jxh.tpsagent.advice.IdentifyTickMethodAdvice;
import io.jxh.tpsagent.transform.IdentifyTickMethodTransform;
import io.jxh.tpsagent.transform.TickMethodTransform;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;

public class ServerMetricsAgent {

    private final Instrumentation inst;

    private ServerMetricsAgent(final Instrumentation inst) {
        this.inst = inst;
    }

    private void setup() {
        TickMethodHolder.registerCallback(() -> {
            IdentifyTickMethodTransform.reset(inst);
            TickMethodTransform.transform(inst);
            new TickMonitor().start();
        });

        IdentifyTickMethodTransform.transform(inst);
    }

    public static void main(final String[] args) throws Exception {
        String pid = args[0];
        File jar = getCurrentJar();

        System.out.println("Attaching Java agent");
        System.out.println("Agent: " + jar);
        System.out.println("PID: " + pid);

        ByteBuddyAgent.attach(jar, pid);
    }

    public static void agentmain(final String agentArgs, final Instrumentation inst) {
        System.out.println("Initializing agent");

        try {
            new ServerMetricsAgent(inst).setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Agent installed");
    }

    private static File getCurrentJar() throws URISyntaxException {
        return new File(ServerMetricsAgent.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
                        .getPath());
    }

}
