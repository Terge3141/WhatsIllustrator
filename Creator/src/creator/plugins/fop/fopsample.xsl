<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	<xsl:template match="messages">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simpleA4"
					page-height="29.7cm" page-width="21cm" margin-top="2cm"
					margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
					<fo:region-body />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="simpleA4">
				<fo:flow flow-name="xsl-region-body">
					<fo:block font-size="10pt">
						<xsl:apply-templates />
					</fo:block>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	<xsl:template match="date">
		<fo:block font-weight="italic" margin-top="5mm"
			margin-bottom="1mm" text-align="center">
			<xsl:value-of select="." />
		</fo:block>
	</xsl:template>

	<xsl:template match="textmessage">
		<fo:block>
			<fo:inline font-weight="bold">
				<xsl:value-of select="sender" />
			</fo:inline>
			(
			<xsl:value-of select="time" />
			):
			<xsl:value-of select="text" />
		</fo:block>
	</xsl:template>

	<xsl:template match="imagemessage">
		<fo:block>
			<fo:inline font-weight="bold">
				<xsl:value-of select="sender" />
			</fo:inline>
			(
			<xsl:value-of select="time" />
			):
		</fo:block>

		<fo:block text-align="center">
			<fo:external-graphic height="50pt"
				content-height="50pt">
				<xsl:attribute name="src">
					<xsl:value-of select="src" />
				</xsl:attribute>
			</fo:external-graphic>
		</fo:block>

		<fo:block text-align="center">
			<fo:inline font-style="italic">
				<xsl:value-of select="subscription" />
			</fo:inline>
		</fo:block>
	</xsl:template>
	<xsl:template match="mediamessage">
		<fo:block>
			<fo:inline font-weight="bold">
				<xsl:value-of select="sender" />
			</fo:inline>

			(
			<xsl:value-of select="time" />
			):

			<fo:inline font-style="italic">
				<xsl:value-of select="filename" />
				<xsl:if test="subscription">
					- <xsl:value-of select="subscription" />
				</xsl:if>
			</fo:inline>

		</fo:block>
	</xsl:template>
</xsl:stylesheet>
