<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.tei-c.org/ns/1.0"
    version="1.0">

<xsl:output method="xml" omit-xml-declaration="no" indent="no" />

<xsl:strip-space elements="*" />

<xsl:template match="eu.skqs.type.Tei">
<TEI>
<teiHeader>
<fileDesc>
<titleStmt>
<title xml:lang="zh"><xsl:value-of select="@title" /></title>
<title xml:lang="en"><xsl:value-of select="@titleEn" /></title>
<author>
<name>
<choice>
<sic><xsl:value-of select="@author" /></sic>
</choice>
</name>
</author>
</titleStmt>

<publicationStmt>
<p>This document is published under a CC Attribution-Share Alike License</p>
</publicationStmt>

<sourceDesc>
<p>XXX</p>
</sourceDesc>
</fileDesc>
</teiHeader>
<xsl:apply-templates/>
</TEI>
</xsl:template>

<xsl:template match="eu.skqs.type.Div[@teitype='null']">
<div>
<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template match="eu.skqs.type.Div[@teitype!='null']">
<div>
<xsl:attribute name="type"><xsl:value-of select="@teitype" /></xsl:attribute>
<xsl:attribute name="n"><xsl:value-of select="@n" /></xsl:attribute>
<xsl:apply-templates/>
</div>
</xsl:template>


<xsl:template match="eu.skqs.type.P">
<p>
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match="eu.skqs.type.Text">
<text>
<xsl:apply-templates/>
</text>
</xsl:template>

<xsl:template match="eu.skqs.type.Body">
<body>
<xsl:apply-templates/>
</body>
</xsl:template>

<xsl:template match="eu.skqs.type.Interpunction">
<pc>
<xsl:apply-templates/>
</pc>
</xsl:template>

<xsl:template match="eu.skqs.type.Pc">
<pc>
<xsl:apply-templates/>
</pc>
</xsl:template>

<xsl:template match="eu.skqs.type.Name">
<name>
<xsl:attribute name="key"><xsl:value-of select="@key" /></xsl:attribute>
<xsl:attribute name="type"><xsl:value-of select="@TEItype" /></xsl:attribute>
<xsl:apply-templates/>
</name>
</xsl:template>

<xsl:template match="eu.skqs.type.PersName">
<persName>
<xsl:apply-templates/>
</persName>
</xsl:template>

<xsl:template match="eu.skqs.type.PlaceName">
<placeName>
<xsl:apply-templates/>
</placeName>
</xsl:template>

<xsl:template match="eu.skqs.type.Term">
<term>
<xsl:apply-templates/>
</term>
</xsl:template>

<xsl:template match="eu.skqs.type.Time">
<time>
<xsl:attribute name="when"><xsl:value-of select="@when" /></xsl:attribute>
<xsl:apply-templates/>
</time>
</xsl:template>

<xsl:template match="eu.skqs.type.Date[@when!='null']">
<date>
<xsl:attribute name="when"><xsl:value-of select="@when" /></xsl:attribute>
<xsl:apply-templates/>
</date>
</xsl:template>

<xsl:template match="eu.skqs.type.Date[@notBefore!='null']">
<date>
<xsl:attribute name="notBefore"><xsl:value-of select="@notBefore" /></xsl:attribute>
<xsl:attribute name="notAfter"><xsl:value-of select="@notAfter" /></xsl:attribute>
<xsl:apply-templates/>
</date>
</xsl:template>

<xsl:template match="eu.skqs.type.Measure">
<measure>
<xsl:attribute name="quantity"><xsl:value-of select="@quantity" /></xsl:attribute>
<xsl:attribute name="unit"><xsl:value-of select="@unit" /></xsl:attribute>
<xsl:apply-templates/>
</measure>
</xsl:template>

<xsl:template match="eu.skqs.type.Title">
<title>
<xsl:apply-templates/>
</title>
</xsl:template>

<xsl:template match="eu.skqs.type.Quote">
<quote>
<xsl:apply-templates/>
</quote>
</xsl:template>

<xsl:template match="eu.skqs.type.Num">
<num>
<xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
<xsl:apply-templates/>
</num>
</xsl:template>

</xsl:stylesheet>
