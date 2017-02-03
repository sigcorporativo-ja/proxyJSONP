package es.juntadeandalucia.framework.ticket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.util.ResourceBundle;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

public class TicketFactory {

	

	private static Log log = LogFactory.getLog(TicketFactory.class);
	private static final ResourceBundle msg = 
			ResourceBundle.getBundle("es.juntadeandalucia.framework.ticket.impl.messages"); 

	public static Ticket createInstance(String implClass) {

		try {

			log.info("Implementación Ticket: " + implClass);
			Configuration configuration = initConfiguration();
			Configuration configSubSet = configuration.subset("ticket." + implClass);
			String impl = configuration.getString("ticket.impl." + implClass);
			Constructor<?> constructor = Class.forName(impl).getConstructor(new Class[] { Configuration.class });

			return (Ticket) constructor.newInstance(new Object[] { configSubSet });
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}

	}

	public static Ticket createInstance() {

		log.debug("Creamos instancia por defecto de Ticket");
		Configuration configuration = initConfiguration();
		String implDefault = configuration.getString("ticket.impl");
		return createInstance(implDefault);
		
	}

	private static Configuration initConfiguration() {
		
		CompositeConfiguration config = new CompositeConfiguration();
		
		try {
			
			config.addConfiguration(new PropertiesConfiguration("ticket.properties"));
			
		} catch (Exception e) {			
			e = new Exception(msg.getString("ticket.error.propertieserror")); //$NON-NLS-1$
			log.warn(e);
		}
				return config;
	}

}