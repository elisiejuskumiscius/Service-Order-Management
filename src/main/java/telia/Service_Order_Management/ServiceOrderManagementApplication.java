package telia.Service_Order_Management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@SpringBootApplication
@EnableWs
public class ServiceOrderManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceOrderManagementApplication.class, args);
	}

	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
			ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/ws/*");
	}

	@Bean(name = "teliaService")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema teliaSchema) {
		DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
		wsdl11Definition.setPortTypeName("TeliaServicePort");
		wsdl11Definition.setLocationUri("/ws");
		wsdl11Definition.setTargetNamespace("https://telia.se");
		wsdl11Definition.setSchema(teliaSchema);
		return wsdl11Definition;
	}

	@Bean
	public XsdSchema teliaSchema() {
		return new SimpleXsdSchema(new ClassPathResource("xsd/telia-service.xsd"));
	}
}
