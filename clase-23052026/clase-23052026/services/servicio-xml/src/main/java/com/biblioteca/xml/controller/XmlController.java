package com.biblioteca.xml.controller;

import com.biblioteca.xml.service.XPathService;
import com.biblioteca.xml.service.XsdValidatorService;
import com.biblioteca.xml.service.XsltService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/xml")
@Tag(name = "XML Lab", description = "Laboratorio de XPath, XSLT, XSD — Sesiones 5-6 SOA")
public class XmlController {

    // Catálogo XML de ejemplo (el "documento de trabajo" del laboratorio)
    private static final String CATALOGO_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <bib:catalogo xmlns:bib="http://biblioteca.com/libros" version="1.0">
            <bib:libro id="lib-001" categoria="arquitectura">
                <bib:titulo>Clean Architecture</bib:titulo>
                <bib:autor>Robert C. Martin</bib:autor>
                <bib:isbn>978-0134494166</bib:isbn>
                <bib:precio>45.99</bib:precio>
                <bib:disponible>true</bib:disponible>
            </bib:libro>
            <bib:libro id="lib-002" categoria="soa">
                <bib:titulo>Designing SOA</bib:titulo>
                <bib:autor>Thomas Erl</bib:autor>
                <bib:isbn>978-0137135790</bib:isbn>
                <bib:precio>55.00</bib:precio>
                <bib:disponible>false</bib:disponible>
            </bib:libro>
            <bib:libro id="lib-003" categoria="arquitectura">
                <bib:titulo>Building Microservices</bib:titulo>
                <bib:autor>Sam Newman</bib:autor>
                <bib:isbn>978-1492034025</bib:isbn>
                <bib:precio>49.99</bib:precio>
                <bib:disponible>true</bib:disponible>
            </bib:libro>
            <bib:libro id="lib-004" categoria="tecnologia">
                <bib:titulo>Enterprise Integration Patterns</bib:titulo>
                <bib:autor>Gregor Hohpe</bib:autor>
                <bib:isbn>978-0321200686</bib:isbn>
                <bib:precio>62.50</bib:precio>
                <bib:disponible>true</bib:disponible>
            </bib:libro>
        </bib:catalogo>
        """;

    private final XsdValidatorService validatorService;
    private final XPathService xpathService;
    private final XsltService xsltService;

    public XmlController(XsdValidatorService validatorService,
                         XPathService xpathService,
                         XsltService xsltService) {
        this.validatorService = validatorService;
        this.xpathService     = xpathService;
        this.xsltService      = xsltService;
    }

    // ─────────────────────────────────────────────────────────────

    @GetMapping(value = "/catalogo", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(
        summary = "Ver el XML del catálogo",
        description = "Retorna el documento XML de ejemplo que se usa en todos los demás endpoints. Observa la estructura: namespace, atributos, elementos anidados."
    )
    public String getCatalogo() {
        return CATALOGO_XML;
    }

    // ─────────────────────────────────────────────────────────────

    @PostMapping(value = "/validar", consumes = MediaType.APPLICATION_XML_VALUE)
    @Operation(
        summary = "Validar XML contra el XSD (catalogo.xsd)",
        description = """
            Valida el XML enviado en el body contra el esquema XSD del catálogo.

            PRUEBA ESTOS CASOS:
            1. Enviar el catálogo completo y correcto → sin errores
            2. Quitar el atributo id de un libro → error de validación
            3. Poner precio negativo o cero → error (PrecioType > 0)
            4. ISBN sin formato 978-XXXXXXXXXX → error de patrón
            5. Categoría no permitida (ej. "novela") → error de enumeración

            Tip: usa GET /xml/catalogo para copiar el XML de ejemplo.
            """
    )
    public ResponseEntity<Map<String, Object>> validar(@RequestBody String xmlContent) {
        List<String> errores = validatorService.validar(xmlContent);
        if (errores.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "valido",   true,
                "mensaje",  "El XML es válido según el XSD catalogo.xsd",
                "errores",  List.of()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "valido",   false,
                "mensaje",  "El XML contiene " + errores.size() + " error(es) de validación",
                "errores",  errores
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────

    @PostMapping(value = "/xpath", consumes = MediaType.APPLICATION_XML_VALUE)
    @Operation(
        summary = "Ejecutar expresión XPath sobre el XML",
        description = """
            Body: el documento XML a consultar (Content-Type: application/xml)
            Query param ?expresion=: la expresión XPath a evaluar

            Usa el prefijo "bib:" para el namespace http://biblioteca.com/libros.

            EXPRESIONES SUGERIDAS:
            - /bib:catalogo/bib:libro/bib:titulo
            - //bib:libro[@id='lib-001']/bib:autor
            - //bib:libro[bib:disponible='true']/bib:titulo
            - count(//bib:libro)
            - //bib:libro[bib:precio > 50]/bib:titulo
            - //@id
            """
    )
    public ResponseEntity<?> ejecutarXPath(
            @RequestBody String xmlContent,
            @RequestParam("expresion") String expresion) {

        try {
            List<String> resultados = xpathService.ejecutar(xmlContent, expresion);
            return ResponseEntity.ok(Map.of(
                "expresion",  expresion,
                "resultados", resultados,
                "total",      resultados.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error",     "XPath inválido: " + e.getMessage(),
                "expresion", expresion
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────

    @PostMapping(value = "/transformar/html",
                 consumes = MediaType.APPLICATION_XML_VALUE,
                 produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
        summary = "Transformar XML a HTML (XSLT)",
        description = """
            Aplica la hoja XSLT libros-a-html.xsl al XML del body.
            El resultado es una página HTML con tabla de libros, lista para abrir en el navegador.

            En Postman: copia el body de la respuesta, pégalo en un archivo .html y ábrelo.
            """
    )
    public ResponseEntity<String> transformarAHtml(@RequestBody String xmlContent) {
        try {
            String html = xsltService.transformar(xmlContent, "xslt/libros-a-html.xsl");
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("<p>Error en transformación XSLT: " + e.getMessage() + "</p>");
        }
    }

    @PostMapping(value = "/transformar/inventario",
                 consumes = MediaType.APPLICATION_XML_VALUE,
                 produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(
        summary = "Transformar XML a formato inventario (XSLT)",
        description = """
            Aplica la hoja XSLT libros-a-inventario.xsl al XML del body.
            Demuestra cómo XSLT reestructura XML de un formato a otro.
            Caso de uso SOA real: adaptadores entre sistemas con diferentes esquemas XML.
            """
    )
    public ResponseEntity<String> transformarAInventario(@RequestBody String xmlContent) {
        try {
            String xml = xsltService.transformar(xmlContent, "xslt/libros-a-inventario.xsl");
            return ResponseEntity.ok(xml);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("<error>" + e.getMessage() + "</error>");
        }
    }

    // ─────────────────────────────────────────────────────────────

    @GetMapping("/conceptos")
    @Operation(summary = "Resumen de tecnologías XML usadas en este servicio")
    public Map<String, Object> conceptos() {
        return Map.of(
            "tecnologias_en_este_servicio", Map.of(
                "XSD",   "Valida estructura del XML → endpoint POST /xml/validar",
                "XPath", "Consulta nodos del XML → endpoint POST /xml/xpath",
                "XSLT",  "Transforma XML → endpoints POST /xml/transformar/*"
            ),
            "relacion_con_soap", Map.of(
                "XSD",  "Define los tipos en el WSDL (ver servicio puerto 8004)",
                "XPath","El ESB lo usa para enrutar mensajes SOAP según su contenido",
                "XSLT", "El ESB lo usa para adaptar mensajes entre servicios"
            ),
            "api_java_usada", Map.of(
                "XSD",  "javax.xml.validation.Validator",
                "XPath","javax.xml.xpath.XPath",
                "XSLT", "javax.xml.transform.Transformer"
            )
        );
    }
}
