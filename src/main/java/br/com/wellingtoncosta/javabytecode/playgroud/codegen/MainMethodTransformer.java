package br.com.wellingtoncosta.javabytecode.playgroud.codegen;

import br.com.wellingtoncosta.javabytecode.playgroud.annotation.MainMethod;
import com.squareup.javapoet.MethodSpec;
import javassist.*;
import javassist.bytecode.annotation.Annotation;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * @author Wellington Costa on 18/12/18
 */
public class MainMethodTransformer implements ClassFileTransformer {

    private static final String MAIN_METHOD_NAME = "main";

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
                System.out.println("The current class is " + ctClass.getName());

                for(Object annotation : ctClass.getAnnotations()) {
                    System.out.println("The annotation " + annotation + " is present.");
                }

                checkIfMainMethodIsPresent(ctClass);

                Annotation annotation = (Annotation) ctClass.getAnnotation(MainMethod.class);
                if (annotation != null) {
                    System.out.println("***** Creating method main method in class " + ctClass.getName() + " *****");
                    String message = ((MainMethod) annotation).message();
                    createMainMethodBlock(ctClass, message);
                }

                return ctClass.toBytecode();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    private static void checkIfMainMethodIsPresent(CtClass ctClass) throws UnsupportedOperationException {
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if (method.getName().equals(MAIN_METHOD_NAME)) {
                throw new UnsupportedOperationException(
                        "Unable to generate main method because it already exists in class " + ctClass.getName() + "."
                );
            }
        }
    }

    private static void createMainMethodBlock(CtClass ctClass, String message) throws CannotCompileException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(PUBLIC, STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, message)
                .build();

        ctClass.addMethod(CtMethod.make(main.toString(), ctClass));
    }

}
