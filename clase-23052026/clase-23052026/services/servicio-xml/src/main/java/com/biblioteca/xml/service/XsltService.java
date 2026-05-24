package com.biblioteca.xml.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class XsltService {

    /**
     * Aplica una hoja XSLT a un documento XML.
     *
     * @param xmlContent  El XML de entrada
     * @param xsltPath    Path del archivo XSL en classpath (ej: "xslt/libros-a-html.xsl")
     * @return            El resultado de la transformación (HTML, XML, texto)
     */
    public String transformar(String xmlContent, String xsltPath) throws Exception {

        // 1. Crear el factory de transformaciones (JAXP)
        TransformerFactory factory = TransformerFactory.newInstance();

        // 2. Cargar la hoja XSLT desde el classpath
        Source xsltSource = new StreamSource(
            new ClassPathResource(xsltPath).getInputStream()
        );

        // 3. Compilar la hoja XSLT (el "programa" de transformación)
        Transformer transformer = factory.newTransformer(xsltSource);

        // 4. Configurar salida (encoding, indentación)
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        // 5. Ejecutar la transformación: XML + XSLT → resultado
        StringWriter resultado = new StringWriter();
        transformer.transform(
            new StreamSource(new StringReader(xmlContent)),  // entrada: el XML
            new StreamResult(resultado)                      // salida: StringWriter
        );

        return resultado.toString();
    }
}
