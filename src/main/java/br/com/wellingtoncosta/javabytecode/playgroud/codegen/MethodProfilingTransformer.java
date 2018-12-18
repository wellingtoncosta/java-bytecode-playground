package br.com.wellingtoncosta.javabytecode.playgroud.codegen;

import br.com.wellingtoncosta.javabytecode.playgroud.annotation.Profiling;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * @author Wellington Costa on 17/12/18
 */
public class MethodProfilingTransformer implements ClassFileTransformer {

    private final ClassPool pool = ClassPool.getDefault();

    @Override public byte[] transform(
            ClassLoader classLoader,
            String className,
            Class<?> clazz,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
    ) {
        try {
            pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
            CtClass ctClass = pool.get(className.replaceAll("/", "."));
            if (!ctClass.isFrozen()) {
                for (CtMethod currentMethod : ctClass.getDeclaredMethods()) {
                    Annotation annotation = getAnnotation(currentMethod);
                    if (annotation != null) {
                        System.out.println("***** Profiing method " + currentMethod.getName() + " *****");

                        createStartOfExecutionBlock(currentMethod);
                        createEndOfExecutionBlock(currentMethod);
                    }
                }
                return ctClass.toBytecode();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    private static Annotation getAnnotation(CtMethod method) {
        MethodInfo methodInfo = method.getMethodInfo();
        AnnotationsAttribute attInfo = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
        if (attInfo != null) {
            return attInfo.getAnnotation(Profiling.class.getName());
        }
        return null;
    }

    private static void createStartOfExecutionBlock(CtMethod ctMethod) throws CannotCompileException {
        ctMethod.addLocalVariable("startTime", CtClass.longType);
        String codeBlock = "{startTime = System.currentTimeMillis();" +
                "System.out.println(\"The method execution was started at \" + startTime + \".\");}";

        ctMethod.insertBefore(codeBlock);
    }

    private static void createEndOfExecutionBlock(CtMethod ctMethod) throws CannotCompileException {
        ctMethod.addLocalVariable("endTime", CtClass.longType);
        String codeBlock = "{endTime = System.currentTimeMillis();" +
                "System.out.println(\"The method execution was finished at \" + endTime + \".\");" +
                "System.out.println(\"The execution time was \" + (endTime - startTime) + \".\");}";

        ctMethod.insertAfter(codeBlock);
    }

}
