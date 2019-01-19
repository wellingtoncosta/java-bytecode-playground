package br.com.wellingtoncosta.javabytecode.playgroud;

import br.com.wellingtoncosta.javabytecode.playgroud.annotation.MainMethod;
import br.com.wellingtoncosta.javabytecode.playgroud.annotation.Profiling;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * @author Wellington Costa on 18/01/19
 */
public class ClassTransformer implements ClassFileTransformer {

    private boolean modified = false;

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
                processMainMethoAnnotation(ctClass);
                processProfilingAnnotation(ctClass);
            }

            if(modified) {
                return ctClass.toBytecode();
            }
        } catch (IOException | CannotCompileException | NotFoundException | ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }

        return null;
    }

    private void processMainMethoAnnotation(CtClass ctClass)
            throws CannotCompileException, ClassNotFoundException {
        Annotation annotation = getMainMethodAnnotation(ctClass);

        if(annotation != null) {
            checkIfMainMethodIsPresent(ctClass);
            Object annotationType = annotation.toAnnotationType(ClassLoader.getSystemClassLoader(), pool);
            createMainMethodBlock(ctClass, ((MainMethod)annotationType).message());
            modified = true;
        }
    }

    private static Annotation getMainMethodAnnotation(CtClass ctClass) {
        ClassFile classFile = ctClass.getClassFile();
        String visibleTag = AnnotationsAttribute.visibleTag;
        AnnotationsAttribute attribute = (AnnotationsAttribute) classFile.getAttribute(visibleTag);
        return attribute != null ? attribute.getAnnotation(MainMethod.class.getName()) : null;
    }

    private static void checkIfMainMethodIsPresent(CtClass ctClass)
            throws UnsupportedOperationException {
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if (method.getName().equals(MAIN_METHOD_NAME)) {
                throw new UnsupportedOperationException(
                        "Unable to generate main method because it already exists in " + ctClass.getName() + " class."
                );
            }
        }
    }

    private static void createMainMethodBlock(CtClass ctClass, String message)
            throws CannotCompileException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(PUBLIC, STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, message)
                .build();

        ctClass.addMethod(CtMethod.make(main.toString(), ctClass));
    }

    private void processProfilingAnnotation(CtClass ctClass)
            throws CannotCompileException {
        for (CtMethod currentMethod : ctClass.getDeclaredMethods()) {
            Annotation annotation = getProfilingAnnotation(currentMethod);
            if (annotation != null) {
                createStartOfExecutionBlock(currentMethod);
                createEndOfExecutionBlock(currentMethod);
                if(!modified) {
                    modified = true;
                }
            }
        }
    }

    private static Annotation getProfilingAnnotation(CtMethod method) {
        MethodInfo methodInfo = method.getMethodInfo();
        AnnotationsAttribute attInfo = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
        if (attInfo != null) {
            return attInfo.getAnnotation(Profiling.class.getName());
        }
        return null;
    }

    private static void createStartOfExecutionBlock(CtMethod ctMethod) throws CannotCompileException {
        ctMethod.addLocalVariable("startTime", CtClass.longType);
        String code = CodeBlock.builder()
                .addStatement("startTime = $T.currentTimeMillis()", System.class)
                .build()
                .toString();

        ctMethod.insertBefore(code);
    }

    private static void createEndOfExecutionBlock(CtMethod ctMethod) throws CannotCompileException {
        ctMethod.addLocalVariable("endTime", CtClass.longType);
        ctMethod.addLocalVariable("total", CtClass.longType);
        String code = CodeBlock.builder()
                .addStatement("endTime = $T.currentTimeMillis()", System.class)
                .addStatement("total = endTime - startTime")
                .addStatement(
                        "$T.out.println($L)",
                        System.class,
                        "\"The execution time of method " + ctMethod.getName() + " was \" + total + \"ms.\""
                )
                .build()
                .toString();

        ctMethod.insertAfter(code);
    }

}
