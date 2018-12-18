package br.com.wellingtoncosta.javabytecode.playgroud.agent;

import br.com.wellingtoncosta.javabytecode.playgroud.codegen.MainMethodTransformer;

import java.lang.instrument.Instrumentation;

/**
 * @author Wellington Costa on 18/12/18
 */
public class MainMethodAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Starting " + MainMethodAgent.class.getName() + "...");
        inst.addTransformer(new MainMethodTransformer());
    }

}
