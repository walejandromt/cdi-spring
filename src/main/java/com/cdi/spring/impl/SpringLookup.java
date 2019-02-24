package com.cdi.spring.impl;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Anotacion de Spring para Inyectar Bean en base a 
 * la reflexion Lookup
 * @author walejandromt
 */
@Retention(RUNTIME) @Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface SpringLookup {
	String value();
}