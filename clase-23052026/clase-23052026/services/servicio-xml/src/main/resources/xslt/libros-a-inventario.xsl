<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSLT: transforma el catálogo complejo al formato simplificado "inventario".
  Demuestra cómo XSLT se usa para integración entre sistemas con
  diferentes estructuras XML (un caso de uso real en SOA).
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:bib="http://biblioteca.com/libros">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <!--
    Este XML de salida podría ser el formato que espera
    un sistema externo (ERP, sistema de inventario, etc.)
    Diferente estructura, mismos datos.
    -->
    <inventario sistema="biblioteca-soa">
      <xsl:attribute name="total">
        <xsl:value-of select="count(bib:catalogo/bib:libro)"/>
      </xsl:attribute>

      <!-- Solo libros disponibles -->
      <disponibles>
        <xsl:for-each select="bib:catalogo/bib:libro[bib:disponible='true']">
          <item>
            <codigo><xsl:value-of select="@id"/></codigo>
            <descripcion><xsl:value-of select="bib:titulo"/></descripcion>
            <responsable><xsl:value-of select="bib:autor"/></responsable>
            <valorUnitario><xsl:value-of select="bib:precio"/></valorUnitario>
            <stock>1</stock>
          </item>
        </xsl:for-each>
      </disponibles>

      <!-- Solo libros prestados -->
      <prestados>
        <xsl:for-each select="bib:catalogo/bib:libro[bib:disponible='false']">
          <item>
            <codigo><xsl:value-of select="@id"/></codigo>
            <descripcion><xsl:value-of select="bib:titulo"/></descripcion>
          </item>
        </xsl:for-each>
      </prestados>
    </inventario>
  </xsl:template>

</xsl:stylesheet>
