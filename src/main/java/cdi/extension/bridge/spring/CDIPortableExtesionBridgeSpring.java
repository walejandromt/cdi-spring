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

import cdi.extension.bridge.spring.annotation.InjectSpring;
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
		log.trace(">>>>>>>>>>> Inicia la Extesion " + this.getClass().getName());
	}

	/**
	 * Al cargar la extension
	 * @param bbd
	 */
	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
		log.trace(">>>>>>>>>>> beginning the scanning process " + this.getClass().getName());
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
		log.trace(">>>>>>>>>>> Inject the scanning process");
		Set<InjectionPoint> injectionPoints = pit.getInjectionTarget().getInjectionPoints();
		
		synchronized (springBeans) {
			for (InjectionPoint point: injectionPoints){
				
				if (!(point.getType() instanceof Class<?>)) {
					continue;
				}
				
				Class<?> injectionType = (Class<?>) point.getType();
				InjectSpring spring = point.getAnnotated().getAnnotation(InjectSpring.class);
				if (spring!=null) {
					log.trace(">>>>>>>>>>> Inject Spring Anotation");
					@SuppressWarnings({ "rawtypes", "unchecked" })
					SpringBean springBean = new SpringBean(pit.getAnnotatedType(), spring, injectionType, bm);
					springBeans.put(springBean.key(), springBean); //we can do some validation to make sure that this bean is compatible with the one we are replacing.
				} else {
					SpringLookup springLookup = point.getAnnotated().getAnnotation(SpringLookup.class);
					if (springLookup!=null) {
						log.trace(">>>>>>>>>>> Inject SpringLookup Anotation");
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
		log.trace(">>>>>>>>>>> finished the scanning process");
		
		ConfigurableApplicationContext ctx = null;
		if (Thread.currentThread().getContextClassLoader().getResource(CLASSLOADER_RESOURCE) != null) {
			log.trace(">>>>>>>>>>> Configuracion de Spring");
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

}