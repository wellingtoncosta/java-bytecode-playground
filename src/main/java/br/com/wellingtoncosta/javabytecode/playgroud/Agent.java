package br.com.wellingtoncosta.javabytecode.playgroud;

import br.com.wellingtoncosta.javabytecode.playgroud.codegen.MainMethodTransformer;
import br.com.wellingtoncosta.javabytecode.playgroud.codegen.MethodProfilingTransformer;

import java.lang.instrument.Instrumentation;

/**
 * @author Wellington Costa on 17/12/18
 */
public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Starting " + Agent.class.getName() + "...");
        inst.addTransformer(new MethodProfilingTransformer());
        inst.addTransformer(new MainMethodTransformer());
    }

}
