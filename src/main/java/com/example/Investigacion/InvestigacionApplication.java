package com.example.Investigacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/*
 * BLOQUE: Punto de entrada
 * Arranca el contexto Spring Boot y el servidor Tomcat embebido.
 */
public class InvestigacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestigacionApplication.class, args);
	}

}
