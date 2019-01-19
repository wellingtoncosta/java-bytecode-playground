package br.com.wellingtoncosta.javabytecode.playgroud;

import java.lang.instrument.Instrumentation;

/**
 * @author Wellington Costa on 18/01/19
 */
public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassTransformer());
    }

}
