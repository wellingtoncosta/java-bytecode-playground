package br.com.wellingtoncosta.javabytecode.playgroud.agent;

import br.com.wellingtoncosta.javabytecode.playgroud.codegen.MethodProfilingTransformer;

import java.lang.instrument.Instrumentation;

/**
 * @author Wellington Costa on 17/12/18
 */
public class MethodProfilingAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Starting " + MethodProfilingAgent.class.getName() + "...");
        inst.addTransformer(new MethodProfilingTransformer());
    }

}
