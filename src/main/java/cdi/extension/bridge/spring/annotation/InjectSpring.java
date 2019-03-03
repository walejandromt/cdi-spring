package cdi.extension.bridge.spring.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Anotacion para Inyectar Bean de Spring
 * @author walejandromt
 */
@Qualifier @Retention(RUNTIME) @Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface InjectSpring {
	Class<?> type() default Object.class;
	String name() default "";
	@Nonbinding boolean required() default true;
}