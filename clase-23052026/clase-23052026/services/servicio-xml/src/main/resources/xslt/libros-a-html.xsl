<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSLT: transforma el XML del catálogo en una página HTML con tabla.
  Usado por el endpoint POST /xml/transformar/html
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:bib="http://biblioteca.com/libros">

  <xsl:output method="html" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <html>
      <head>
        <meta charset="UTF-8"/>
        <title>Catálogo de Libros — Biblioteca SOA</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 30px; background: #f5f5f5; }
          h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
          table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.2); }
          th { background: #2c3e50; color: white; padding: 12px; text-align: left; }
          td { padding: 10px 12px; border-bottom: 1px solid #ddd; }
          tr:hover { background: #ecf0f1; }
          .disponible { color: #27ae60; font-weight: bold; }
          .no-disponible { color: #e74c3c; font-weight: bold; }
          .badge { padding: 2px 8px; border-radius: 10px; font-size: 0.8em; }
          .badge-soa { background: #3498db; color: white; }
          .badge-tecnologia { background: #9b59b6; color: white; }
          .badge-arquitectura { background: #e67e22; color: white; }
          footer { margin-top: 20px; color: #7f8c8d; font-size: 0.85em; }
        </style>
      </head>
      <body>
        <h1>Catálogo de la Biblioteca</h1>
        <p>
          Versión:
          <strong><xsl:value-of select="bib:catalogo/@version"/></strong>
          — Total de libros:
          <strong><xsl:value-of select="count(bib:catalogo/bib:libro)"/></strong>
          — Disponibles:
          <strong><xsl:value-of select="count(bib:catalogo/bib:libro[bib:disponible='true'])"/></strong>
        </p>

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Título</th>
              <th>Autor</th>
              <th>ISBN</th>
              <th>Precio</th>
              <th>Categoría</th>
              <th>Disponible</th>
            </tr>
          </thead>
          <tbody>
            <!-- XSL for-each: itera sobre todos los libros, ordenados por título -->
            <xsl:for-each select="bib:catalogo/bib:libro">
              <xsl:sort select="bib:titulo" order="ascending"/>
              <tr>
                <td><code><xsl:value-of select="@id"/></code></td>
                <td><strong><xsl:value-of select="bib:titulo"/></strong></td>
                <td><xsl:value-of select="bib:autor"/></td>
                <td><xsl:value-of select="bib:isbn"/></td>
                <td>$<xsl:value-of select="bib:precio"/></td>
                <td>
                  <!-- xsl:if: condicional -->
                  <xsl:if test="@categoria">
                    <span>
                      <xsl:attribute name="class">badge badge-<xsl:value-of select="@categoria"/></xsl:attribute>
                      <xsl:value-of select="@categoria"/>
                    </span>
                  </xsl:if>
                </td>
                <td>
                  <xsl:choose>
                    <xsl:when test="bib:disponible = 'true'">
                      <span class="disponible">✔ Disponible</span>
                    </xsl:when>
                    <xsl:otherwise>
                      <span class="no-disponible">✘ Prestado</span>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
              </tr>
            </xsl:for-each>
          </tbody>
        </table>

        <footer>
          <p>Generado con XSLT | Curso SOA — UTP 2026</p>
        </footer>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
