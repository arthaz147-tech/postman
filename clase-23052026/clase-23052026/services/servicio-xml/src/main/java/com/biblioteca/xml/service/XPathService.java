package com.biblioteca.xml.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class XPathService {

    private static final String NAMESPACE_URI    = "http://biblioteca.com/libros";
    private static final String NAMESPACE_PREFIX = "bib";

    public List<String> ejecutar(String xmlContent, String xpathExpression) throws Exception {

        // 1. Parsear el XML a DOM
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlContent)));

        // 2. Configurar XPath con namespace
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return NAMESPACE_PREFIX.equals(prefix) ? NAMESPACE_URI
                       : javax.xml.XMLConstants.NULL_NS_URI;
            }
            @Override public String getPrefix(String ns) { return null; }
            @Override public Iterator<String> getPrefixes(String ns) { return null; }
        });

        XPathExpression expr = xpath.compile(xpathExpression);
        List<String> resultados = new ArrayList<>();

        // 3. Intentar evaluar como NODESET (elementos, atributos)
        //    Si la expresión retorna un escalar (count, string, number)
        //    el cast a NodeList lanza XPathExpressionException — lo capturamos.
        try {
            NodeList nodos = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodos.getLength(); i++) {
                Node nodo = nodos.item(i);
                // Atributos: getTextContent() devuelve el valor del atributo
                // Elementos:  getTextContent() devuelve el texto del elemento
                String valor = nodo.getTextContent().trim();
                if (!valor.isEmpty()) {
                    resultados.add(valor);
                }
            }
            if (!resultados.isEmpty()) {
                return resultados;
            }
        } catch (XPathExpressionException | ClassCastException ignored) {
            // No era un nodeset — continuar con evaluación escalar
        }

        // 4. Evaluar como STRING (cubre count(), string(), number(), etc.)
        //    XPathConstants.STRING siempre funciona para cualquier expresión XPath
        String escalar = (String) expr.evaluate(document, XPathConstants.STRING);
        if (escalar != null && !escalar.trim().isEmpty()) {
            resultados.add(escalar.trim());
        }

        return resultados;
    }
}
