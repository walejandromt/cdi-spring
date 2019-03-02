package cdi.extension.bridge.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cdi.extension.bridge.spring.annotation.Spring;
import cdi.extension.bridge.spring.annotation.SpringLookup;

/**
 * Portable Extension para manejar la reflexion de las
 * anotaciones de Spring y SpringLookup
 * @author walejandromt
 */
public class CDIPortableExtesionBridgeSpring implements Extension {
	
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(CDIPortableExtesionBridgeSpring.class);
	
	@SuppressWarnings("rawtypes")
	Map <String, SpringBean> springBeans = new HashMap<String, SpringBean>();
	private static final String RESOURCE_NAME = "Spring-All-Module.xml";
    private static final String CLASSLOADER_RESOURCE = "/spring/" + RESOURCE_NAME;
    private static final String CLASSPATH_RMANNIBUCAU_SPRING_CDI_XML = "classpath:" + CLASSLOADER_RESOURCE;
    
    /**
     * Start
     */
	public CDIPortableExtesionBridgeSpring() {
		log.debug(">>>>>>>>>>> Inicia la Extesion " + this.getClass().getName());
	}

	/**
	 * Al cargar la extension
	 * @param bbd
	 */
	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
		log.debug(">>>>>>>>>>> beginning the scanning process " + this.getClass().getName());
	}

	/**
	 * Procesa todo los bean CDI encontrados en el proyecto
	 * @param pat
	 */
	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		log.trace(">>>>>>>>>>> Scanning in " + this.getClass().getName());
		log.trace(">>>>>>>>>>> scanning type: " + pat.getAnnotatedType().getJavaClass().getName());
	}

	/**
	 * Procesa todo los bean CDI encontrados en el proyecto al inyectar las anotaciones de CDI
	 * @param pit
	 * @param bm
	 */
	public void processInjectionTarget (@Observes ProcessInjectionTarget<?> pit, BeanManager bm) {
		log.debug(">>>>>>>>>>> Inject the scanning process");
		Set<InjectionPoint> injectionPoints = pit.getInjectionTarget().getInjectionPoints();
		
		synchronized (springBeans) {
			for (InjectionPoint point: injectionPoints){
				
				if (!(point.getType() instanceof Class<?>)) {
					continue;
				}
				
				Class<?> injectionType = (Class<?>) point.getType();
				Spring spring = point.getAnnotated().getAnnotation(Spring.class);
				if (spring!=null) {
					log.debug(">>>>>>>>>>> Inject Spring Anotation");
					@SuppressWarnings({ "rawtypes", "unchecked" })
					SpringBean springBean = new SpringBean(pit.getAnnotatedType(), spring, injectionType, bm);
					springBeans.put(springBean.key(), springBean); //we can do some validation to make sure that this bean is compatible with the one we are replacing.
				} else {
					SpringLookup springLookup = point.getAnnotated().getAnnotation(SpringLookup.class);
					if (springLookup!=null) {
						log.debug(">>>>>>>>>>> Inject SpringLookup Anotation");
						@SuppressWarnings({ "rawtypes", "unchecked" })
						SpringBean springBean = new SpringBean(springLookup, injectionType, bm);
						springBeans.put(springBean.key(), springBean);
					}
				}
			}
		}
	}
	
	/**
	 * Al terminar de procesar los Beans CDI, se crea el contexto de Spring
	 * @param abd
	 * @param bm
	 */
	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
		log.debug(">>>>>>>>>>> finished the scanning process");
		
		ConfigurableApplicationContext ctx = null;
		if (Thread.currentThread().getContextClassLoader().getResource(CLASSLOADER_RESOURCE) != null) {
			log.debug(">>>>>>>>>>> Configuracion de Spring");
			ctx = new ClassPathXmlApplicationContext(CLASSPATH_RMANNIBUCAU_SPRING_CDI_XML);
        }else {
        	log.error("\n\n\n >>>>>>>>>>> El archivo " + CLASSLOADER_RESOURCE + " no existe en el resources de su aplicacion y el Context de Spring no se cargo \n\n\n");
        }
		
		if(ctx != null) {
			synchronized (springBeans) {
				for (@SuppressWarnings("rawtypes") SpringBean bean : springBeans.values()) {
					bean.setAppCtx(ctx);
					abd.addBean(bean);
				}	
			}
		}
		
		
	}
	
//	class SpringBean<T> implements Bean <T> {
//		//InjectionTarget<Object> it;
//		Spring spring;
//		SpringLookup lookup;
//		Class<T> injectionType; 
//		BeanManager bm;
//		ListableBeanFactory appCtx;
//	    InjectionTarget<T> injectionTarget;
//		AnnotatedType<T> annotatedType;
//		
//		SpringBean(AnnotatedType<T> annotatedType, Spring spring, Class<T> injectionType, BeanManager bm){
//			this.spring = spring;
//			this.injectionType = injectionType;//En el getName de esta variable se encuentra el nombre de la clase
//			this.bm = bm;
//			this.annotatedType = annotatedType;
//			injectionTarget = bm.createInjectionTarget(bm.createAnnotatedType(injectionType));
//		}
//		
//		public SpringBean(SpringLookup springLookup, Class<T> type,
//				BeanManager bm) {
//			this.lookup = springLookup;
//			this.injectionType = type;
//			this.bm = bm;
//		}
//
//		public String key () {
//			return "" + this.getName() + "::" + injectionType.toString();
//		}
//		
//		@SuppressWarnings("all")
//		class NamedLiteral extends AnnotationLiteral<Named> implements Named {
//
//			@Override
//			public String value() {
//				return (spring!=null) ? spring.name() : lookup.value();
//			}
//			
//		}
//
//		@Override
//		public Class<?> getBeanClass() {
//			return this.injectionType;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public Set<InjectionPoint> getInjectionPoints() {
//			return Collections.EMPTY_SET;
//		}
//		
//		@Override
//		public String getName() {
//			return spring!=null ? spring.name() : lookup.value();
//		}
//
//		@Override
//		public Set<Annotation> getQualifiers() {
//			Set<Annotation> qualifiers = new HashSet<Annotation>();	
//			if (lookup==null) {
//				qualifiers.add(new NamedLiteral()); //Added this because it causes OWB to fail if there is a Named
//			}
//			if (spring!=null) {
//				qualifiers.add( spring );
//			} else {
//				qualifiers.add( lookup );				
//			}
//			return qualifiers;
//		}
//		
//		@Override
//		public Class<? extends Annotation> getScope() {
//			return Dependent.class;
//		}
//
//		@Override
//		public Set<Class<? extends Annotation>> getStereotypes() {
//			return Collections.emptySet();
//		}
//
//		@Override
//		public Set<Type> getTypes() {
//			Set<Type> types = new HashSet<Type>(); 
//			types.add(this.injectionType); 
//			types.add(Object.class); 
//			return types;			
//		}
//
//		@Override
//		public boolean isAlternative() {
//			return false;
//		}
//
//		@Override
//		public boolean isNullable() {
//			return spring != null ? !spring.required() : false;
//		}
//		@Override
//		public T create(CreationalContext<T> ctx) {
//			
//			if  (appCtx==null) {
//				if (spring !=null) {
//					System.err.printf("############## spring name=%s type=%s \n\n\n", spring.name(), spring.type());
//				} else {
//					System.err.printf("############## lookup value=%s \n\n\n", lookup.value());					
//				}
//				throw new IllegalStateException("applicationContext was null");
//			}
//			
//			final T t = appCtx.getBean(getName(), injectionType);
//	        // postconstruct managed by spring
//	        injectionTarget.inject(t, ctx);
//	        return t;
//		}
//
//		@Override
//		public void destroy(T instance, CreationalContext<T> ctx) {
//			injectionTarget.dispose(instance);
//			ctx.release();
//		}
//
//		public String toString() {
//			return String.format("SpringBean(hc=%d, hc=%d, annotatedType=%s, qualifiers=%s)", this.hashCode(), CDIPortableExtesionBridgeSpring.this.hashCode(), this.annotatedType, this.getQualifiers() );
//		}
//		
//		public void setAppCtx(ListableBeanFactory appCtx) {
//			this.appCtx = appCtx;
//		}
//		
//	}

}