package com.cdi.spring.impl;

import javax.inject.Inject;

/**
 * Puente CDI Bean a Spring Beans
 * @author walejandromt
 */
public class MyCdiBean1 {
	
    @Inject @Spring(name="spring1")
    private MySpringBean1 spring;

    public String getSpring() {
        return spring.getClass().getName();
    }

    public MySpringBean1 bean() {
        return spring;
    }
}
