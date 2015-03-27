<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Rendering specific to the item display page.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->

<xsl:stylesheet
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:atom="http://www.w3.org/2005/Atom"
    xmlns:ore="http://www.openarchives.org/ore/terms/"
    xmlns:oreatom="http://www.openarchives.org/ore/atom/"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:encoder="xalan://java.net.URLEncoder"
    xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
    exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util">

    <xsl:output indent="yes"/>

    <xsl:template name="itemSummaryView-DIM">
        <!-- Generate the info about the item from the metadata section -->
        <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
        mode="itemSummaryView-DIM"/>

        <!-- Generate the bitstream information from the file section -->
        <xsl:choose>
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']">
                    <xsl:with-param name="context" select="."/>
                    <xsl:with-param name="primaryBitstream" select="./mets:structMap[@TYPE='LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:fptr/@FILEID"/>
                </xsl:apply-templates>
            </xsl:when>
            <!-- Special case for handling ORE resource maps stored as DSpace bitstreams -->
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='ORE']">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='ORE']"/>
            </xsl:when>
            <xsl:otherwise>
                <h2><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h2>
                <table class="ds-table file-list">
                    <tr class="ds-table-header-row">
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-file</i18n:text></th>
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text></th>
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text></th>
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-view</i18n:text></th>
                    </tr>
                    <tr>
                        <td colspan="4">
                            <p><i18n:text>xmlui.dri2xhtml.METS-1.0.item-no-files</i18n:text></p>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default)-->
        <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>

    </xsl:template>


    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <div class="item-summary-view-metadata">
            <xsl:call-template name="itemSummaryView-DIM-fields"/>
        </div>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-fields">
      <xsl:param name="clause" select="'1'"/>
      <xsl:param name="phase" select="'even'"/>
      <xsl:variable name="otherPhase">
            <xsl:choose>
              <xsl:when test="$phase = 'even'">
                <xsl:text>odd</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>even</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
      </xsl:variable>

      <xsl:choose>
          <!-- Title row -->
          <xsl:when test="$clause = 1">

              <xsl:choose>
                  <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) &gt; 1">
                      <!-- display first title as h1 -->
                      <h2 class="page-header">
                          <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                      </h2>
                      <div class="simple-item-view-other">
                          <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>:</span>
                          <span>
                              <xsl:for-each select="dim:field[@element='title'][not(@qualifier)]">
                                  <xsl:value-of select="./node()"/>
                                  <xsl:if test="count(following-sibling::dim:field[@element='title'][not(@qualifier)]) != 0">
                                      <xsl:text>; </xsl:text>
                                      <br/>
                                  </xsl:if>
                              </xsl:for-each>
                          </span>
                      </div>
                  </xsl:when>
                  <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) = 1">
                      <h2 class="page-header">
                          <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                      </h2>
                  </xsl:when>
                  <xsl:otherwise>
                      <h2 class="page-header">
                          <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                      </h2>
                  </xsl:otherwise>
              </xsl:choose>
            <xsl:call-template name="itemSummaryView-DIM-fields">
              <xsl:with-param name="clause" select="($clause + 1)"/>
              <xsl:with-param name="phase" select="$otherPhase"/>
            </xsl:call-template>
          </xsl:when>

          <!-- Author(s) row -->
          <!--<xsl:when test="$clause = 2 and (dim:field[@element='contributor'][@qualifier='author'] or dim:field[@element='creator'] or dim:field[@element='contributor'])">-->
          <xsl:when test="$clause = 2 and (dim:field[@element='contributor'][@qualifier='author'])">
                <div class="simple-item-view-authors">
	                    <xsl:choose>
	                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
	                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                        <span>
                                          <xsl:if test="@authority">
                                            <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
                                          </xsl:if>
	                                <xsl:copy-of select="node()"/>
                                        </span>
	                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:when test="dim:field[@element='creator']">
	                            <xsl:for-each select="dim:field[@element='creator']">
	                                <xsl:copy-of select="node()"/>
	                                <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:when test="dim:field[@element='contributor']">
	                            <xsl:for-each select="dim:field[@element='contributor']">
	                                <xsl:copy-of select="node()"/>
	                                <xsl:if test="count(following-sibling::dim:field[@element='contributor']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:otherwise>
	                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
	                        </xsl:otherwise>
	                    </xsl:choose>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- identifier.uri row -->
          <xsl:when test="$clause = 3 and (dim:field[@element='identifier' and @qualifier='uri'])">
                    <div class="simple-item-view-other">
	                <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:</span>
	                <span>
	                	<xsl:for-each select="dim:field[@element='identifier' and @qualifier='uri']">
		                    <a>
		                        <xsl:attribute name="href">
		                            <xsl:copy-of select="./node()"/>
		                        </xsl:attribute>
		                        <xsl:copy-of select="./node()"/>
		                    </a>
		                    <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='uri']) != 0">
		                    	<br/>
		                    </xsl:if>
	                    </xsl:for-each>
	                </span>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- publicationDate row -->
          <xsl:when test="$clause = 4 and ((dim:field[@element='publicationDate' and @mdschema='heal']) or
                (dim:field[@element='dateCreated' and @mdschema='heal']))">
              <xsl:if test="(dim:field[@element='publicationDate' and @mdschema='heal'])">
                  <div class="simple-item-view-other">
                      <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-publication-date</i18n:text>:</span>
                      <span>
                          <xsl:for-each select="dim:field[@element='publicationDate' and @mdschema='heal']">
                              <xsl:copy-of select="substring(./node(),1,10)"/>
                              <xsl:if test="count(following-sibling::dim:field[@element='publicationDate' and @mdschema='heal']) != 0">
                                  <br/>
                              </xsl:if>
                          </xsl:for-each>
                      </span>
                  </div>
              </xsl:if>
              <xsl:if test="(dim:field[@element='dateCreated' and @mdschema='heal'])">
                  <div class="simple-item-view-other">
                      <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-create-date</i18n:text>:</span>
                      <span>
                          <xsl:for-each select="dim:field[@element='dateCreated' and @mdschema='heal']">
                              <xsl:copy-of select="substring(./node(),1,10)"/>
                              <xsl:if test="count(following-sibling::dim:field[@element='dateCreated' and @mdschema='heal']) != 0">
                                  <br/>
                              </xsl:if>
                          </xsl:for-each>
                      </span>
                  </div>
              </xsl:if>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                  <xsl:with-param name="clause" select="($clause + 1)"/>
                  <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- date.issued row -->
          <!-- <xsl:when test="$clause = 4 and (dim:field[@element='date' and @qualifier='issued'])">
              <div class="simple-item-view-other">
                  <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:</span>
                  <span>
                      <xsl:for-each select="dim:field[@element='date' and @qualifier='issued']">
                          <xsl:copy-of select="substring(./node(),1,10)"/>
                          <xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">
                              <br/>
                          </xsl:if>
                      </xsl:for-each>
                  </span>
              </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                  <xsl:with-param name="clause" select="($clause + 1)"/>
                  <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when> -->

          <!-- type row -->
          <xsl:when test="$clause = 5 and (dim:field[@element='type' and not(@qualifier)])">
                    <div class="simple-item-view-other">
	                <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-type</i18n:text>:</span>
	                <span>
                        <xsl:if test="count(dim:field[@element='type' and not(@qualifier)]) = 1">
                            <i18n:text><xsl:value-of select="concat('heal.type.', dim:field[@element='type' and not(@qualifier)])"/></i18n:text>
		                	<!--<xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">-->
	                    	<br/>
                        </xsl:if>
	                </span>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- journal name / conference name -->
          <xsl:when test="$clause = 6 and ((dim:field[@element='journalName' and @mdschema='heal']) or
                (dim:field[@element='conferenceName' and @mdschema='heal']))">
              <xsl:if test="(dim:field[@element='journalName' and @mdschema='heal'])">
                  <div class="simple-item-view-other">
                      <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-journal-name</i18n:text>:</span>
                      <span>
                          <xsl:for-each select="dim:field[@element='journalName' and @mdschema='heal']">
                              <xsl:copy-of select="./node()"/>
                              <xsl:if test="count(following-sibling::dim:field[@element='journalName' and @mdschema='heal']) != 0">
                                  <br/>
                              </xsl:if>
                          </xsl:for-each>
                      </span>
                  </div>
              </xsl:if>
              <xsl:if test="(dim:field[@element='conferenceName' and @mdschema='heal'])">
                  <div class="simple-item-view-other">
                      <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-conference-name</i18n:text>:</span>
                      <span>
                          <xsl:for-each select="dim:field[@element='conferenceName' and @mdschema='heal']">
                              <xsl:copy-of select="./node()"/>
                              <xsl:if test="count(following-sibling::dim:field[@element='conferenceName' and @mdschema='heal']) != 0">
                                  <br/>
                              </xsl:if>
                          </xsl:for-each>
                      </span>
                  </div>
              </xsl:if>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                  <xsl:with-param name="clause" select="($clause + 1)"/>
                  <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- Description.Abstract row -->
          <xsl:when test="$clause = 7 and (dim:field[@element='description' and @qualifier='abstract' and descendant::text()])">
                    <div class="simple-item-view-description">
	                <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:</h3>
	                <div>
	                <xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
	                	<div class="spacer">&#160;</div>
	                </xsl:if>
	                <xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:copy-of select="node()"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='abstract']) != 0">
                            <div class="spacer">&#160;</div>
	                    </xsl:if>
	              	</xsl:for-each>
	              	<xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
                          <div class="spacer">&#160;</div>
	                </xsl:if>
	                </div>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- Description row -->
          <xsl:when test="$clause = 8 and (dim:field[@element='description' and not(@qualifier)])">
                <div class="simple-item-view-description">
	                <h3 class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:</h3>
	                <div>
	                <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1 and not(count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1)">
                        <div class="spacer">&#160;</div>
	                </xsl:if>
	                <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">
		                <xsl:copy-of select="./node()"/>
		                <xsl:if test="count(following-sibling::dim:field[@element='description' and not(@qualifier)]) != 0">
                            <div class="spacer">&#160;</div>
	                    </xsl:if>
	               	</xsl:for-each>
	               	<xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1">
                           <div class="spacer">&#160;</div>
	                </xsl:if>
	                </div>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- Abstract row -->
          <xsl:when test="$clause = 9 and (dim:field[@element='abstract' and not(@qualifier)])">
              <div class="simple-item-view-description">
                  <div>
                      <xsl:if test="count(dim:field[@element='abstract' and not(@qualifier)]) &gt; 1 and not(count(dim:field[@element='abstract' and @qualifier='abstract']) &gt; 1)">
                          <div class="spacer">&#160;</div>
                      </xsl:if>
                      <xsl:for-each select="dim:field[@element='abstract' and not(@qualifier)]">
                          <xsl:copy-of select="./node()"/>
                          <xsl:if test="count(following-sibling::dim:field[@element='abstract' and not(@qualifier)]) != 0">
                              <div class="spacer">&#160;</div>
                          </xsl:if>
                      </xsl:for-each>
                      <xsl:if test="count(dim:field[@element='abstract' and not(@qualifier)]) &gt; 1">
                          <div class="spacer">&#160;</div>
                      </xsl:if>
                  </div>
              </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                  <xsl:with-param name="clause" select="($clause + 1)"/>
                  <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- Source row -->
          <xsl:when test="$clause = 10 and (dim:field[@element='source' and not(@qualifier)])">
              <xsl:if test="count(dim:field[@element='source' and not(@qualifier)])">
                  <div class="simple-item-view-subjects">
                      <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-source</i18n:text>:</span>
                      <xsl:for-each select="dim:field[@element='source' and not(@qualifier)]">
                          <span>
                              <xsl:choose>
                                  <xsl:when test="starts-with(node(), 'http://') or starts-with(node(), 'https://')">
                                      <a>
                                          <xsl:attribute name="href">
                                              <xsl:copy-of select="node()"/>
                                          </xsl:attribute>
                                          <xsl:attribute name="target">_blank</xsl:attribute>
                                          <xsl:copy-of select="node()"/>
                                      </a>
                                  </xsl:when>
                                  <xsl:otherwise>
                                      <xsl:copy-of select="node()"/>
                                  </xsl:otherwise>
                              </xsl:choose>
                          </span>
                          <xsl:if test="count(following-sibling::dim:field[@element='source' and not(@qualifier)]) != 0">
                              <xsl:text> / </xsl:text>
                          </xsl:if>
                      </xsl:for-each>
                  </div>
              </xsl:if>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                  <xsl:with-param name="clause" select="($clause + 1)"/>
                  <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- Classification row -->
          <xsl:when test="$clause = 11 and (dim:field[@element='classification' and @mdschema='heal'])">
              <xsl:if test="count(dim:field[@element='classification' and @mdschema='heal'])">
                  <div class="simple-item-view-subjects">
                      <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-subject</i18n:text>:</span>
                      <xsl:for-each select="dim:field[@element='classification'][@mdschema='heal']">
                          <span>
                              <xsl:copy-of select="node()"/>
                          </span>
                          <xsl:if test="count(following-sibling::dim:field[@element='classification'][@mdschema='heal']) != 0">
                              <xsl:text>, </xsl:text>
                          </xsl:if>
                      </xsl:for-each>
                  </div>
              </xsl:if>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                  <xsl:with-param name="clause" select="($clause + 1)"/>
                  <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <xsl:when test="$clause = 12 and $ds_item_view_toggle_url != ''">
              <p class="ds-paragraph item-view-toggle item-view-toggle-bottom">
                  <a>
                      <xsl:attribute name="href"><xsl:value-of select="$ds_item_view_toggle_url"/></xsl:attribute>
                      <i18n:text>xmlui.ArtifactBrowser.ItemViewer.show_full</i18n:text>
                  </a>
              </p>
          </xsl:when>

          <!-- recurse without changing phase if we didn't output anything -->
          <xsl:otherwise>
            <!-- IMPORTANT: This test should be updated if clauses are added! -->
            <xsl:if test="$clause &lt; 13">
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$phase"/>
              </xsl:call-template>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>

         <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default) -->
        <xsl:apply-templates select="mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>
    </xsl:template>


    <xsl:template match="dim:dim" mode="itemDetailView-DIM">
        <table class="ds-includeSet-table detailtable">
		    <xsl:apply-templates mode="itemDetailView-DIM"/>
		</table>
        <span class="Z3988">
            <xsl:attribute name="title">
                 <xsl:call-template name="renderCOinS"/>
            </xsl:attribute>
            &#xFEFF; <!-- non-breaking space to force separating the end tag -->
        </span>
    </xsl:template>

    <xsl:template match="dim:field" mode="itemDetailView-DIM">
            <tr>
                <xsl:attribute name="class">
                    <xsl:text>ds-table-row </xsl:text>
                    <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                    <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                </xsl:attribute>
                <td class="label-cell">
                    <xsl:value-of select="./@mdschema"/>
                    <xsl:text>.</xsl:text>
                    <xsl:value-of select="./@element"/>
                    <xsl:if test="./@qualifier">
                        <xsl:text>.</xsl:text>
                        <xsl:value-of select="./@qualifier"/>
                    </xsl:if>
                </td>
            <td>
              <xsl:copy-of select="./node()"/>
              <xsl:if test="./@authority and ./@confidence">
                <xsl:call-template name="authorityConfidenceIcon">
                  <xsl:with-param name="confidence" select="./@confidence"/>
                </xsl:call-template>
              </xsl:if>
            </td>
                <td><xsl:value-of select="./@language"/></td>
            </tr>
    </xsl:template>

    <!--dont render the item-view-toggle automatically in the summary view, only when it get's called-->
    <xsl:template match="dri:p[contains(@rend , 'item-view-toggle') and
        (preceding-sibling::dri:referenceSet[@type = 'summaryView'] or following-sibling::dri:referenceSet[@type = 'summaryView'])]">
    </xsl:template>

    <!-- dont render the head on the item view page -->
    <xsl:template match="dri:div[@n='item-view']/dri:head" priority="5">
    </xsl:template>

        <xsl:template match="mets:fileGrp[@USE='CONTENT']">
        <xsl:param name="context"/>
        <xsl:param name="primaryBitstream" select="-1"/>

        <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h3>
        <ul class="file-list list-group">
            <xsl:choose>
                <!-- If one exists and it's of text/html MIME type, only display the primary bitstream -->
                <xsl:when test="mets:file[@ID=$primaryBitstream]/@MIMETYPE='text/html'">
                    <xsl:apply-templates select="mets:file[@ID=$primaryBitstream]">
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:when>
                <!-- Otherwise, iterate over and display all of them -->
                <xsl:otherwise>
                    <xsl:apply-templates select="mets:file">
                     	<!--Do not sort any more bitstream order can be changed-->
                        <!--<xsl:sort data-type="number" select="boolean(./@ID=$primaryBitstream)" order="descending" />-->
                        <!--<xsl:sort select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>-->
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </ul>
    </xsl:template>

    <xsl:template match="mets:file">
        <xsl:param name="context" select="."/>
        <li class="list-group-item">
        <div class="file-wrapper media">
            <div class="thumbnail-wrapper pull-left">
                <a class="image-link">
                    <xsl:attribute name="href">
                        <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/
                        mets:file[@GROUPID=current()/@GROUPID]">
                            <img alt="Thumbnail">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/
                                    mets:file[@GROUPID=current()/@GROUPID]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                                </xsl:attribute>
                            </img>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/pdf'">
                            <img alt="[PDF]" src="{concat($theme-path, '/images/mimes/pdf.png')}" style="height: 48px;" width="48" height="48" title="PDF file"/>
                        </xsl:when>
                        <!--<xsl:when test="starts-with(@MIMETYPE, 'audio/')">
                            <img alt="[Audio]" src="{concat($theme-path, '/images/mimes/audio.png')}" style="height: 48px;" width="48" height="48" title="Audio file"/>
                        </xsl:when>-->
                        <xsl:when test="starts-with(@MIMETYPE, 'video/')">
                            <img alt="[Video]" src="{concat($theme-path, '/images/mimes/video.png')}" style="height: 48px;" width="48" height="48" title="Video file"/>
                        </xsl:when>
                        <xsl:when test="starts-with(@MIMETYPE, 'image/')">
                            <img alt="[Image]" src="{concat($theme-path, '/images/mimes/image.png')}" style="height: 48px;" width="48" height="48" title="Image file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.ms-powerpoint'">
                            <img alt="[PP]" src="{concat($theme-path, '/images/mimes/ppt.png')}" style="height: 48px;" width="48" height="48" title="MS Powerpoint file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.openxmlformats-officedocument.presentationml.presentation'">
                            <img alt="[PP]" src="{concat($theme-path, '/images/mimes/pptx.png')}" style="height: 48px;" width="48" height="48" title="MS Powerpoint file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.oasis.opendocument.presentation'">
                            <img alt="[Presentation]" src="{concat($theme-path, '/images/mimes/odp.png')}" style="height: 48px;" width="48" height="48" title="Presentation slide deck"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/msword'">
                            <img alt="[Word]" src="{concat($theme-path, '/images/mimes/doc.png')}" style="height: 48px;" width="48" height="48" title="MS Word file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'">
                            <img alt="[Word]" src="{concat($theme-path, '/images/mimes/docx.png')}" style="height: 48px;" width="48" height="48" title="MS Word file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.oasis.opendocument.text'">
                            <img alt="[Writer]" src="{concat($theme-path, '/images/mimes/odt.png')}" style="height: 48px;" width="48" height="48" title="Word processor file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.ms-excel'">
                            <img alt="[Excel]" src="{concat($theme-path, '/images/mimes/xls.png')}" style="height: 48px;" width="48" height="48" title="MS Excel file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'">
                            <img alt="[Excel]" src="{concat($theme-path, '/images/mimes/xlsx.png')}" style="height: 48px;" width="48" height="48" title="MS Excel file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/vnd.oasis.opendocument.spreadsheet'">
                            <img alt="[Spreadsheet]" src="{concat($theme-path, '/images/mimes/ods.png')}" style="height: 48px;" width="48" height="48" title="Spreadsheet"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'application/zip'">
                            <img alt="[ZIP]" src="{concat($theme-path, '/images/mimes/zip.png')}" style="height: 48px;" width="48" height="48" title="ZIP archive"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'text/richtext'">
                            <img alt="[RTF]" src="{concat($theme-path, '/images/mimes/rtf.png')}" style="height: 48px;" width="48" height="48" title="RTF file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'text/xml'">
                            <img alt="[XML]" src="{concat($theme-path, '/images/mimes/xml.png')}" style="height: 48px;" width="48" height="48" title="XML file"/>
                        </xsl:when>
                        <xsl:when test="@MIMETYPE = 'text/html'">
                            <img alt="[HTML]" src="{concat($theme-path, '/images/mimes/html.png')}" style="height: 48px;" width="48" height="48" title="HTML file"/>
                        </xsl:when>
                        <xsl:when test="starts-with(@MIMETYPE, 'text/')">
                            <img alt="[Text]" src="{concat($theme-path, '/images/mimes/txt.png')}" style="height: 48px;" width="48" height="48" title="Text file"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <img alt="[File]" src="{concat($theme-path, '/images/mimes/blank.png')}" style="height: 48px;" width="48" height="48"/>
                        </xsl:otherwise>
                        <!--<xsl:otherwise>-->
                            <!--<img alt="Icon" src="{concat($theme-path, '/images/mime.png')}" style="height: {$thumbnail.maxheight}px;"/>-->
                        <!--</xsl:otherwise>-->
                    </xsl:choose>
                </a>
            </div>
            <div class="media-body">
                <div class="col-lg-10">
                    <div>
                        <span class="bold">
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-name</i18n:text>
                            <xsl:text>:</xsl:text>
                        </span>
                        <span>
                            <xsl:attribute name="title"><xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/></xsl:attribute>
                            <xsl:value-of select="util:shortenString(mets:FLocat[@LOCTYPE='URL']/@xlink:title, 30, 5)"/>
                        </span>
                    </div>
                    <!-- File size always comes in bytes and thus needs conversion -->
                    <div>
                        <span class="bold">
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text>
                            <xsl:text>:</xsl:text>
                        </span>
                        <span>
                            <xsl:choose>
                                <xsl:when test="@SIZE &lt; 1024">
                                    <xsl:value-of select="@SIZE"/>
                                    <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
                                </xsl:when>
                                <xsl:when test="@SIZE &lt; 1024 * 1024">
                                    <xsl:value-of select="substring(string(@SIZE div 1024),1,5)"/>
                                    <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
                                </xsl:when>
                                <xsl:when test="@SIZE &lt; 1024 * 1024 * 1024">
                                    <xsl:value-of select="substring(string(@SIZE div (1024 * 1024)),1,5)"/>
                                    <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="substring(string(@SIZE div (1024 * 1024 * 1024)),1,5)"/>
                                    <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </span>
                    </div>
                    <!-- Lookup File Type description in local messages.xml based on MIME Type.
             In the original DSpace, this would get resolved to an application via
             the Bitstream Registry, but we are constrained by the capabilities of METS
             and can't really pass that info through. -->
                    <div>
                        <span class="bold">
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text>
                            <xsl:text>:</xsl:text>
                        </span>
                        <span>
                            <xsl:call-template name="getFileTypeDesc">
                                <xsl:with-param name="mimetype">
                                    <xsl:value-of select="substring-before(@MIMETYPE,'/')"/>
                                    <xsl:text>/</xsl:text>
                                    <xsl:value-of select="substring-after(@MIMETYPE,'/')"/>
                                </xsl:with-param>
                            </xsl:call-template>
                        </span>
                    </div>
                    <!---->
                    <!-- Display the contents of 'Description' only if bitstream contains a description -->
                    <xsl:if test="mets:FLocat[@LOCTYPE='URL']/@xlink:label != ''">
                        <div>
                            <span class="bold">
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-description</i18n:text>
                                <xsl:text>:</xsl:text>
                            </span>
                            <span>
                                <xsl:attribute name="title"><xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/></xsl:attribute>
                                <!--<xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/>-->
                                <xsl:value-of select="util:shortenString(mets:FLocat[@LOCTYPE='URL']/@xlink:label, 17, 5)"/>
                            </span>
                        </div>
                    </xsl:if>
                </div>
                <div class="col-lg-2">
                    <a>
                        <xsl:attribute name="class">btn btn-primary btn-sm pull-right</xsl:attribute>
                        <xsl:attribute name="href">
                            <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                        </xsl:attribute>
                        <i class="glyphicon glyphicon-download-alt">&#x00AD;</i>
                        <xsl:text>&#160;</xsl:text>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-open</i18n:text>
                    </a>
                </div>
            </div>
        </div>
        </li>


    </xsl:template>

</xsl:stylesheet>
