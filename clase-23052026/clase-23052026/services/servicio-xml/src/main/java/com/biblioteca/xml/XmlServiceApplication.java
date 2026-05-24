package com.biblioteca.xml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Servicio XML — Laboratorio de tecnologías XML en SOA
 *
 * Puerto: 8005
 * Demuestra el uso de XPath, XSLT y validación XSD
 * usando exclusivamente la API estándar de Java (JAXP).
 * No requiere librerías externas de XML.
 */
@SpringBootApplication
public class XmlServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmlServiceApplication.class, args);
    }
}
