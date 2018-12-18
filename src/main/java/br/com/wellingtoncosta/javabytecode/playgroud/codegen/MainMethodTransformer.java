package br.com.wellingtoncosta.javabytecode.playgroud.codegen;

import br.com.wellingtoncosta.javabytecode.playgroud.annotation.MainMethod;
import com.squareup.javapoet.MethodSpec;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
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
                Annotation annotation = isAnnotationPresent(ctClass);

                if(annotation == null) {
                    return null;
                }

                System.out.println("Processing class " + ctClass.getName());

                checkIfMainMethodIsPresent(ctClass);
                Object annotationType = annotation.toAnnotationType(ClassLoader.getSystemClassLoader(), pool);
                createMainMethodBlock(ctClass, ((MainMethod)annotationType).message());
            }

            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    private static Annotation isAnnotationPresent(CtClass ctClass) {
        ClassFile classFile = ctClass.getClassFile();
        String visibleTag = AnnotationsAttribute.visibleTag;
        AnnotationsAttribute attribute = (AnnotationsAttribute) classFile.getAttribute(visibleTag);
        return attribute != null ? attribute.getAnnotation(MainMethod.class.getName()) : null;
    }

    private static void checkIfMainMethodIsPresent(CtClass ctClass) throws UnsupportedOperationException {
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if (method.getName().equals(MAIN_METHOD_NAME)) {
                throw new UnsupportedOperationException(
                        "Unable to generate main method because it already exists in " + ctClass.getName() + " class."
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
