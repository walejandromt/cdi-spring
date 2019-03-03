package cdi.extension.bridge.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

import org.springframework.beans.factory.ListableBeanFactory;

import cdi.extension.bridge.spring.annotation.InjectSpring;
import cdi.extension.bridge.spring.annotation.SpringLookup;

/**
 * Clase generica para manejar los Bean Injectados de Spring
 * @author walejandromt
 * @param <T>
 */
public class SpringBean<T> implements Bean <T> {
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(CDIPortableExtesionBridgeSpring.class);
	InjectSpring spring;
	SpringLookup lookup;
	Class<T> injectionType; 
	BeanManager bm;
	ListableBeanFactory appCtx;
    InjectionTarget<T> injectionTarget;
	AnnotatedType<T> annotatedType;
	
	SpringBean(AnnotatedType<T> annotatedType, InjectSpring spring, Class<T> injectionType, BeanManager bm){
		this.spring = spring;
		this.injectionType = injectionType;//En el getName de esta variable se encuentra el nombre de la clase
		this.bm = bm;
		this.annotatedType = annotatedType;
		this.injectionTarget = bm.createInjectionTarget(bm.createAnnotatedType(injectionType));
	}
	
	public SpringBean(SpringLookup springLookup, Class<T> type,
			BeanManager bm) {
		this.lookup = springLookup;
		this.injectionType = type;
		this.bm = bm;
		this.injectionTarget = bm.createInjectionTarget(bm.createAnnotatedType(injectionType));
	}

	public String key () {
		return "" + this.getName() + "::" + injectionType.toString();
	}
	
	@SuppressWarnings("all")
	class NamedLiteral extends AnnotationLiteral<Named> implements Named {

		@Override
		public String value() {
			return (spring!=null) ? spring.name() : lookup.value();
		}
		
	}

	@Override
	public Class<?> getBeanClass() {
		return this.injectionType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.EMPTY_SET;
	}
	
	@Override
	public String getName() {
		return spring!=null ? spring.name() : lookup.value();
	}

	@Override
	public Set<Annotation> getQualifiers() {
		Set<Annotation> qualifiers = new HashSet<Annotation>();	
		if (lookup==null) {
			qualifiers.add(new NamedLiteral()); //Added this because it causes OWB to fail if there is a Named
		}
		if (spring!=null) {
			qualifiers.add( spring );
		} else {
			qualifiers.add( lookup );				
		}
		return qualifiers;
	}
	
	@Override
	public Class<? extends Annotation> getScope() {
		return Dependent.class;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public Set<Type> getTypes() {
		Set<Type> types = new HashSet<Type>(); 
		types.add(this.injectionType); 
		types.add(Object.class); 
		return types;			
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return spring != null ? !spring.required() : false;
	}
	@Override
	public T create(CreationalContext<T> ctx) {
		
		if  (appCtx==null) {
			if (spring !=null) {
				log.error("############## spring name=%s type=%s \n\n\n", spring.name(), spring.type());
			} else {
				log.error("############## lookup value=%s \n\n\n", lookup.value());					
			}
			throw new IllegalStateException("applicationContext was null");
		}
		
		final T t = appCtx.getBean(getName(), injectionType);
        // postconstruct managed by spring
        injectionTarget.inject(t, ctx);
        return t;
	}

	@Override
	public void destroy(T instance, CreationalContext<T> ctx) {
		injectionTarget.dispose(instance);
		ctx.release();
	}

	public String toString() {
		return String.format("SpringBean(hc=%d, hc=%d, annotatedType=%s, qualifiers=%s)", this.hashCode(), this.hashCode(), this.annotatedType, this.getQualifiers() );
	}
	
	public void setAppCtx(ListableBeanFactory appCtx) {
		this.appCtx = appCtx;
	}
	
}