package br.com.cesaravb.zabbixincident.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
	
    // ====================================
    // # addCorsMappings - Configura CORS para permitir requisições do frontend
    // ====================================
    @Override
    public void addCorsMappings(CorsRegistry registry) {
    	
    	// ====================================
        // # Converter String para Array
        // ====================================
        String[] origins = allowedOrigins.split(",");
    	
        registry.addMapping("/**")
        		.allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}