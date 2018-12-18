package br.com.wellingtoncosta.javabytecode.playgroud.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Wellington Costa on 18/12/18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MainMethod {

    String message() default "Hello, World!";

}
