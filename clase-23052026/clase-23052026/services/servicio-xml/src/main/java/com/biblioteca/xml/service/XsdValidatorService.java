package com.biblioteca.xml.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class XsdValidatorService {

    /**
     * Valida un documento XML contra el XSD catalogo.xsd.
     *
     * @param xmlContent El XML a validar (como String)
     * @return Lista de errores de validación (vacía = XML válido)
     */
    public List<String> validar(String xmlContent) {
        List<String> errores = new ArrayList<>();

        try {
            // 1. Cargar el XSD (el contrato de datos)
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(
                new StreamSource(new ClassPathResource("catalogo.xsd").getInputStream())
            );

            // 2. Crear el validador
            Validator validator = schema.newValidator();

            // 3. Recolectar errores en lugar de lanzar excepción inmediatamente
            validator.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    errores.add("ADVERTENCIA [línea " + e.getLineNumber() + "]: " + e.getMessage());
                }
                @Override
                public void error(SAXParseException e) {
                    errores.add("ERROR [línea " + e.getLineNumber() + "]: " + e.getMessage());
                }
                @Override
                public void fatalError(SAXParseException e) {
                    errores.add("ERROR FATAL [línea " + e.getLineNumber() + "]: " + e.getMessage());
                }
            });

            // 4. Ejecutar la validación
            validator.validate(new StreamSource(new StringReader(xmlContent)));

        } catch (SAXException | IOException e) {
            errores.add("Error al procesar el XSD: " + e.getMessage());
        }

        return errores;
    }
}
