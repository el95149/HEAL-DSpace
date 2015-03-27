<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

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
        exclude-result-prefixes="xalan encoder i18n dri mets dim  xlink xsl">

    <xsl:output indent="yes"/>



    <!-- simple search /discover reorder with javascript-->
    <xsl:template match="dri:div[@id='aspect.discovery.SimpleSearch.div.search']">

        <script type="text/javascript">
            $(document).ready(function(){
                //$('#ds-options').children().hide();

                $('form.discover-sort-box').each(function(){$(this).insertAfter('.bottom')});

                $('#gr_heal_dspace_app_xmlui_aspect_viewArtifacts_Navigation_list_browse').hide();
                $('#gr_heal_dspace_app_xmlui_aspect_viewArtifacts_Navigation_list_browse').prev().hide();

            });
        </script>

        <!-- make filter selection form user friendlier for certain fields -->
        <script type="text/javascript">
            <xsl:text>
            $(document).ready(function(){

                var filterSelector = $('#aspect_discovery_SimpleSearch_field_filtertype');
                var filterInput = $('#aspect_discovery_SimpleSearch_field_filter');

                var langOptions = {'en': '</xsl:text><i18n:text>heal.language.en</i18n:text>'<xsl:text>,
                        'el': '</xsl:text><i18n:text>heal.language.el</i18n:text>'<xsl:text>,
                        'de': '</xsl:text><i18n:text>heal.language.de</i18n:text>'<xsl:text>,
                        'fr': '</xsl:text><i18n:text>heal.language.fr</i18n:text>'<xsl:text>,
                        'other': '</xsl:text><i18n:text>heal.language.other</i18n:text>'<xsl:text>
                };

                var typeOptions =  {
                        'bachelorThesis': '</xsl:text><i18n:text>heal.type.bachelorThesis</i18n:text>'<xsl:text>,
                        'masterThesis': '</xsl:text><i18n:text>heal.type.masterThesis</i18n:text>'<xsl:text>,
                        'doctoralThesis': '</xsl:text><i18n:text>heal.type.doctoralThesis</i18n:text>'<xsl:text>,
                        'conferenceItem': '</xsl:text><i18n:text>heal.type.conferenceItem</i18n:text>'<xsl:text>,
                        'journalArticle': '</xsl:text><i18n:text>heal.type.journalArticle</i18n:text>'<xsl:text>,
                        'bookChapter': '</xsl:text><i18n:text>heal.type.bookChapter</i18n:text>'<xsl:text>,
                        'book': '</xsl:text><i18n:text>heal.type.book</i18n:text>'<xsl:text>,
                        'report': '</xsl:text><i18n:text>heal.type.report</i18n:text>'<xsl:text>,
                        'learningMaterial': '</xsl:text><i18n:text>heal.type.learningMaterial</i18n:text>'<xsl:text>,
                        'dataset': '</xsl:text><i18n:text>heal.type.dataset</i18n:text>'<xsl:text>,
                        'studentRecord': '</xsl:text><i18n:text>heal.type.studentRecord</i18n:text>'<xsl:text>,
                        'studentFile': '</xsl:text><i18n:text>heal.type.studentFile</i18n:text>'<xsl:text>,
                        'isoArchive': '</xsl:text><i18n:text>heal.type.isoArchive</i18n:text>'<xsl:text>,
                        'studentRegistry': '</xsl:text><i18n:text>heal.type.studentRegistry</i18n:text>'<xsl:text>,
                        'studentIndex': '</xsl:text><i18n:text>heal.type.studentIndex</i18n:text>'<xsl:text>,
                        'workRecord': '</xsl:text><i18n:text>heal.type.workRecord</i18n:text>'<xsl:text>,
                        'studentDegree': '</xsl:text><i18n:text>heal.type.studentDegree</i18n:text>'<xsl:text>,
                        'studyGuide': '</xsl:text><i18n:text>heal.type.studyGuide</i18n:text>'<xsl:text>,
                        'plan': '</xsl:text><i18n:text>heal.type.plan</i18n:text>'<xsl:text>,
                        'photo': '</xsl:text><i18n:text>heal.type.photo</i18n:text>'<xsl:text>,
                        'labInstrument': '</xsl:text><i18n:text>heal.type.labInstrument</i18n:text>'<xsl:text>,
                        'poster': '</xsl:text><i18n:text>heal.type.poster</i18n:text>'<xsl:text>,
                        'flyer': '</xsl:text><i18n:text>heal.type.flyer</i18n:text>'<xsl:text>,
                        'brochure': '</xsl:text><i18n:text>heal.type.brochure</i18n:text>'<xsl:text>,
                        'studentID': '</xsl:text><i18n:text>heal.type.studentID</i18n:text>'<xsl:text>,
                        'studentHealthRecord': '</xsl:text><i18n:text>heal.type.studentHealthRecord</i18n:text>'<xsl:text>,
                        'other': '</xsl:text><i18n:text>heal.type.other</i18n:text>'<xsl:text>
                };

                filterSelector.change(function() {

                    var filterName = $(this).val();

                    if (filterName === 'language') {
                        filterInput.addClass('hidden');
                        filterInput.val('');
                        var languageSelector = $('</xsl:text><select/>'<xsl:text>);
                        languageSelector.addClass('form-control input-sm');

                        for (var key in langOptions) {
                            var langOpt = $('</xsl:text><option/>'<xsl:text>);
                            langOpt.val(key);
                            langOpt.text(langOptions[key]);
                            langOpt.appendTo(languageSelector);
                        }

                        languageSelector.insertAfter(filterInput);
                        filterInput.val(languageSelector.find('option:first').val());

                        languageSelector.bind('change', function() {
                            filterInput.val(languageSelector.val());
                        });
                    }
                    else if (filterName === 'type') {
                        filterInput.addClass('hidden');
                        filterInput.val('');
                        var typeSelector = $('</xsl:text><select/>'<xsl:text>);
                        typeSelector.addClass('form-control input-sm');

                        for (var key in typeOptions) {
                            var typeOpt = $('</xsl:text><option/>'<xsl:text>);
                            typeOpt.val(key);
                            typeOpt.text(typeOptions[key]);
                            typeOpt.appendTo(typeSelector);
                        }

                        typeSelector.insertAfter(filterInput);
                        filterInput.val(typeSelector.find('option:first').val());

                        typeSelector.bind('change', function() {
                            filterInput.val(typeSelector.val());
                        });
                    }
                    else {
                        filterInput.removeClass('hidden');
                        filterInput.val('');
                        if (filterInput.siblings('select')) {
                            filterInput.siblings('select').remove();
                        }
                    }

                });

                filterSelector.find('option:first').attr("selected", true).trigger('change');

            });
            </xsl:text>
        </script>

        <xsl:copy-of select="@*"/>
        <xsl:apply-templates />
    </xsl:template>

    <!-- alter search message from "go" to "search" in advanced search -->
    <xsl:param name="searchMessage">
        <i18n:text>xmlui.general.search</i18n:text>
    </xsl:param>

    <xsl:template match="dri:field[@id='aspect.discovery.SimpleSearch.field.submit']/*">
        <xsl:attribute name="value">
            <xsl:value-of select="$searchMessage"/>
        </xsl:attribute>
    </xsl:template>

    <!-- <xsl:template match="dri:div/dri:div[@id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-controls']">
     <form id='aspect_artifactbrowser_ConfigurableBrowse_div_browse-controls'>
     <h2 class="page-header">View Options</h2>
     
     <xsl:copy-of select="@*"/>
                <xsl:apply-templates />
                </form>
    </xsl:template>-->

    <!-- browse by reorder with javascript-->
    <xsl:template match="dri:div[starts-with(@id,'aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-')]">

        <script type="text/javascript">
            $(document).ready(function(){
                //$('#ds-options').children().hide();
                //$('form#aspect_artifactbrowser_ConfigurableBrowse_div_browse-controls').each(function(){$(this).prependTo('#ds-options')});
                //$('form#aspect_artifactbrowser_ConfigurableBrowse_div_browse-navigation').each(function(){$(this).prependTo('#ds-options')});
                $('form#aspect_artifactbrowser_ConfigurableBrowse_div_browse-controls').each(function(){$(this).insertAfter('.ds-artifact-list')});
            });
        </script>

        <xsl:copy-of select="@*"/>
        <xsl:apply-templates />
    </xsl:template>

    <!-- open and close navigation community tree-->

    <xsl:template match="dri:div[@rend='primary' and starts-with(@id,'aspect.artifactbrowser.CommunityBrowser.div.comunity-browser')]">

        <script type="text/javascript">
            $(document).ready(function(){
                $('.detailedListOnly').removeClass("hidden");
                $('span.short-description').addClass('hidden');
                $('i.acc').click(function(){

                    if ($(this).attr("closed"))
                    {
                        $(this).removeClass( "glyphicon-plus" ).addClass( "glyphicon-minus" );
                        $(this).closest( "li" ).children('ul').show();
                        $(this).removeAttr("closed");

                    } else {
                        $(this).removeClass( "glyphicon-minus" ).addClass( "glyphicon-plus" );
                        $(this).closest( "li" ).children('ul').hide();
                        $(this).attr("closed","1");
                    }
                    $('span.short-description').addClass('hidden');

                });
            });
        </script>

        <xsl:copy-of select="@*"/>
        <xsl:apply-templates />
    </xsl:template>




    <!-- paging for Navigation -->
    <xsl:template match="dri:div[starts-with(@id,'aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-') and contains(@id,'-results')]">
        <!--<xsl:value-of select="node()|@nextPage"/>-->

        <xsl:copy-of select="@*"/>
        <xsl:apply-templates />

        <div class="pagination-masked clearfix bottom ">

            <p class="pagination-info">
                <i18n:translate>
                    <xsl:choose>
                        <xsl:when test="@itemsTotal = -1">
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-info.nototal</i18n:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text>xmlui.dri2xhtml.structural.pagination-info</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                    <i18n:param><xsl:value-of select="@firstItemIndex"/></i18n:param>
                    <i18n:param><xsl:value-of select="@lastItemIndex"/></i18n:param>
                    <i18n:param><xsl:value-of select="@itemsTotal"/></i18n:param>
                </i18n:translate>
                <!--
            <xsl:text>Now showing items </xsl:text>
            <xsl:value-of select="parent::node()/@firstItemIndex"/>
            <xsl:text>-</xsl:text>
            <xsl:value-of select="parent::node()/@lastItemIndex"/>
            <xsl:text> of </xsl:text>
            <xsl:value-of select="parent::node()/@itemsTotal"/>
                -->
            </p>
            <span style="float:right;">
                <xsl:if test="@previousPage">
                    <a class="previous-page-link" style="padding-left:10px;">
                        <xsl:attribute name="href">
                            <xsl:value-of select="@previousPage"/>
                        </xsl:attribute>
                        <i18n:text>xmlui.dri2xhtml.structural.pagination-previous</i18n:text>
                    </a>
                </xsl:if>
                <span>&#x00AD;</span>
                <span>&#x00AD;</span>
                <xsl:if test="@nextPage">
                    <a class="next-page-link" style="padding-left:10px;">
                        <xsl:attribute name="href">
                            <xsl:value-of select="@nextPage"/>
                        </xsl:attribute>
                        <i18n:text>xmlui.dri2xhtml.structural.pagination-next</i18n:text>
                    </a>
                </xsl:if>
            </span>
        </div>



    </xsl:template>


    <!--simple search primary search modify -->
    <xsl:template match="dri:div/dri:div/dri:list[@id='aspect.discovery.SimpleSearch.list.primary-search']">
        <div class="well">
            <xsl:apply-templates select="dri:item"/>
        </div>
    </xsl:template>

    <xsl:template match="dri:div/dri:div/dri:list[@id='aspect.discovery.SimpleSearch.list.primary-search']/dri:item/dri:field[@type='select']/*">
        <xsl:call-template name="standardAttributes">
            <xsl:with-param name="class">form-control input-sm</xsl:with-param>
        </xsl:call-template>

        <xsl:for-each select=".">
            <xsl:if test="self::node()[name()='option']">
                <option>
                    <xsl:attribute name="value"><xsl:value-of select="node()|@returnValue"/></xsl:attribute>
                    <xsl:apply-templates select="node()"/>
                </option>
            </xsl:if>
        </xsl:for-each>

    </xsl:template>

    <xsl:template match="dri:list[@id='aspect.discovery.SimpleSearch.list.primary-search']/dri:item/dri:field[@type='text']/*">
        <xsl:call-template name="standardAttributes">
            <xsl:with-param name="class">form-control</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--simple search secondary search modify -->
    <xsl:template match="dri:list[@id='aspect.discovery.SimpleSearch.list.secondary-search']">
        <div class="well">
            <xsl:apply-templates select="dri:item"/>
        </div>
    </xsl:template>

    <xsl:template match="dri:item[@id='aspect.discovery.SimpleSearch.item.search-filter-list']//dri:field[@type='select']/*">
        <xsl:call-template name="standardAttributes">
            <xsl:with-param name="class">form-control input-sm</xsl:with-param>
        </xsl:call-template>

        <xsl:for-each select=".">
            <xsl:if test="self::node()[name()='option']">
                <option>
                    <xsl:attribute name="value"><xsl:value-of select="node()|@returnValue"/></xsl:attribute>
                    <xsl:apply-templates select="node()"/>
                </option>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="dri:item[@id='aspect.discovery.SimpleSearch.item.search-filter-list']//dri:field[@type='text']/*">
        <xsl:call-template name="standardAttributes">
            <xsl:with-param name="class">form-control input-sm</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- <xsl:template match="dri:div/dri:div/dri:list[@id='aspect.discovery.SimpleSearch.list.primary-search']/dri:item/dri:field">
    <xsl:copy-of select="child::*[not(starts-with(name(),'label'))]"/>
    <xsl:apply-templates/>
    </xsl:template> -->

    <!-- turn all tables to bootstrap tables -->
    <xsl:template match="dri:table">
        <xsl:apply-templates select="dri:head"/>
        <table>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-table table table-striped</xsl:with-param>
            </xsl:call-template>

            <xsl:apply-templates select="dri:row"/>
        </table>
    </xsl:template>

    <!-- browse by navigation modify -->
    <xsl:template match="dri:div/dri:div[@id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-navigation-well']">
        <div class="well" style="text-align:center">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- filter by navigation modify -->
    <xsl:template match="dri:div/dri:div[@id='aspect.discovery.SearchFacetFilter.div.browse-navigation-well']">
        <div class="well" style="text-align:center">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- browse by controls modify -->
    <xsl:template match="dri:div/dri:div[@id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-controls']/dri:p[not(@id='aspect.artifactbrowser.ConfigurableBrowse.p.hidden-fields')]">
        <h3 class="ds-head"><i18n:text>xmlui.Discovery.SimpleSearch.sort_head</i18n:text></h3>
        <table class="table ">
            <tr>
                <xsl:for-each select="child::*[not(starts-with(name(),'field'))]">
                    <td class="col-md-4" style="text-align:left"><xsl:copy-of select="."/></td>
                </xsl:for-each>
            </tr>
            <tr>
                <xsl:for-each select="child::*[starts-with(name(),'field') and not(@n='update')]">
                    <td class="col-md-4" style="text-align:left">  <xsl:apply-templates select="."/></td>
                </xsl:for-each>
            </tr>
            <tr>
                <xsl:for-each select="child::*[starts-with(name(),'field') and @n='update']">
                    <td class="col-md-12" style="text-align:center" colspan="3">  <xsl:apply-templates select="."/></td>
                </xsl:for-each>
            </tr>
        </table>

    </xsl:template>

    <!-- search results controls modify -->
    <xsl:template match="dri:div/dri:div[@id='aspect.discovery.SimpleSearch.div.search-controls']/dri:list/dri:item[not(@id='aspect.artifactbrowser.ConfigurableBrowse.p.hidden-fields')]">
        <table class="table ">
            <tr>
                <xsl:for-each select="child::*[not(starts-with(name(),'field'))]">
                    <td class="col-md-4" style="text-align:left"><xsl:copy-of select="."/></td>
                </xsl:for-each>
            </tr>
            <tr>
                <xsl:for-each select="child::*[starts-with(name(),'field') and not(@n='submit_sort')]">
                    <td class="col-md-4" style="text-align:left">  <xsl:apply-templates select="."/></td>
                </xsl:for-each>
            </tr>
            <tr>
                <xsl:for-each select="child::*[starts-with(name(),'field') and @n='submit_sort']">
                    <td class="col-md-12" style="text-align:center" colspan="3">  <xsl:apply-templates select="."/></td>
                </xsl:for-each>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="dri:div/dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-search-browse']">
    </xsl:template>
    <xsl:template match="dri:div/dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-search-browse']">
    </xsl:template>

    <xsl:template match="dri:div/dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-view']/dri:p">
        <div class="row" style="text-align:center">
            <span>
                <xsl:attribute name="class">btn btn-success btn-lg</xsl:attribute>
                <xsl:copy-of select="@*"/>
                <xsl:apply-templates />
            </span>
        </div>
    </xsl:template>

    <xsl:template match="dri:div[@id='aspect.administrative.community.CreateCommunityForm.div.create-community']/dri:list[@id='aspect.administrative.community.CreateCommunityForm.list.metadataList']">
        <xsl:for-each select="child::*">
            <xsl:choose>
                <xsl:when test="local-name()='label'">
                    <div class="col-md-3">
                        <strong>
                            <xsl:copy-of select="@*"/>
                            <xsl:apply-templates />
                        </strong>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    <div class="col-md-9">
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates />
                    </div>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:for-each>

    </xsl:template>
    <xsl:template match="dri:div[@id='aspect.administrative.community.EditCommunityMetadataForm.div.community-metadata-edit']/dri:list[@id='aspect.administrative.community.EditCommunityMetadataForm.list.metadataList']">
        <xsl:for-each select="child::*">
            <xsl:choose>
                <xsl:when test="local-name()='label'">
                    <div class="col-md-3">
                        <strong>
                            <xsl:copy-of select="@*"/>
                            <xsl:apply-templates />
                        </strong>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    <div class="col-md-9">
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates />
                    </div>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

    </xsl:template>
    <xsl:template match="dri:div[@id='aspect.administrative.collection.EditCollectionMetadataForm.div.collection-metadata-edit']/dri:list[@id='aspect.administrative.collection.EditCollectionMetadataForm.list.metadataList']">
        <xsl:for-each select="child::*">
            <xsl:choose>
                <xsl:when test="local-name()='label'">
                    <div class="col-md-3">
                        <strong>
                            <xsl:copy-of select="@*"/>
                            <xsl:apply-templates />
                        </strong>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    <div class="col-md-9">
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates />
                    </div>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="dri:div[@id='aspect.administrative.collection.CreateCollectionForm.div.create-collection']/dri:list[@id='aspect.administrative.collection.CreateCollectionForm.list.metadataList']">
        <xsl:for-each select="child::*">
            <xsl:choose>
                <xsl:when test="local-name()='label'">
                    <div class="col-md-3">
                        <strong>
                            <xsl:copy-of select="@*"/>
                            <xsl:apply-templates />
                        </strong>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    <div class="col-md-9">
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates />
                    </div>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- Submission Steps -->
    <xsl:template match="dri:div//dri:list[starts-with(@id,'aspect.submission.StepTransformer.list.submit-') or
    					@id = 'aspect.submission.StepTransformer.list.licenseclasslist']/dri:head">
        <h2>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates />
        </h2>
        <hr/>
    </xsl:template>

    <!-- <xsl:template match="dri:div//dri:list[@id='aspect.submission.StepTransformer.list.submit-describe']">
        <xsl:variable name="currentNode" select="current()" />

        <xsl:for-each select="child::*">
            <xsl:choose>
                <xsl:when test="local-name()='head'">
                    <h2>
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates />
                    </h2>
                    <hr/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="position() != last()">
                        <div class="col-lg-12">
                            <xsl:if test="position() mod 2 = 1">
                                <xsl:attribute name="class">col-lg-12 odd</xsl:attribute>
                            </xsl:if>
                            <div class="col-lg-12">
                                <label  class="control-label">
                                    <xsl:value-of select="current()/dri:field/dri:label"/>
                                </label>
                            </div>
                            <div class="col-lg-12 form-submission">
                                <xsl:copy-of select="@*"/>
                                <xsl:apply-templates />
                            </div>
                            <div class="clearfix">&#160;</div>
                        </div>
                    </xsl:if>
                    <xsl:if test="position() = last()">
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates />
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template> -->

    <!-- <xsl:template match="dri:div[@id='aspect.submission.StepTransformer.div.submit-describe']/dri:head">
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates />
         <script type="text/javascript">
             $(document).ready(function(){
                 $('span.field-help').each(function(){var x=$(this).parent().prev();$(this).appendTo(x);});
                 $('select.ds-select-lang').each(function(){var x=$(this).parent();x.addClass('col-md-3');});
             });
         </script>
     </xsl:template>

     <xsl:template match="dri:field" mode="compositeComponent">
         <xsl:choose>
                 <xsl:when test="@type = 'checkbox'  or @type='radio'">
                     <xsl:apply-templates select="." mode="normalField"/>
                     <xsl:if test="dri:label">
                         <br/>
                         <xsl:apply-templates select="dri:label" mode="compositeComponent"/>
                     </xsl:if>
                 </xsl:when>
                 <xsl:otherwise>
                         <label class="ds-composite-component col-md-4">
                             <xsl:if test="position()=last()">
                                 <xsl:attribute name="class">ds-composite-component col-md-4 last</xsl:attribute>
                             </xsl:if>
                             <xsl:apply-templates select="." mode="normalField"/>
                             <xsl:if test="dri:label">
                                 <br/>
                                 <xsl:apply-templates select="dri:label" mode="compositeComponent"/>
                             </xsl:if>
                         </label>
                 </xsl:otherwise>
         </xsl:choose>
     </xsl:template>
    <xsl:template match="dri:div[@id='aspect.discovery.SimpleSearch.div.search']">
              <script type="text/javascript">
                    //$(document).ready(function(){
                   // $("input[type='radio']").click(function (){
                  //  alert(5);
                   // if ($(this).val!=""){
                  //  var elem='<input name="fq" type="checkbox" value="heal.fullTextAvailability:(true)" checked="checked" />aff';
                    //    $("form#aspect_discovery_SimpleSearch_div_search-filters").append(elem);
                   //     }
                  //  });

                 //  });
                </script>
              <xsl:copy-of select="@*"/>
                                <xsl:apply-templates />
            </xsl:template>-->

    <!--Advanced search: show parent communities for collections -->

    <xsl:template match="dri:div[@id='aspect.discovery.SimpleSearch.div.parents']/dri:p/dri:field">
        <xsl:variable name="currentNode" select="current()" />

        <xsl:for-each select=".">
            <script type="text/javascript">
                $(document).ready(function(){

                //var elem=$('a[href="/tei/handle/<xsl:value-of select="$currentNode/@n"/>"] span');
                var elem=$('a[href*="/handle/<xsl:value-of select="$currentNode/@n"/>"] span');
                var curElemVal=$(elem).html();
                //alert(curElemVal);
                $(elem).html(curElemVal+' (<xsl:value-of select="$currentNode"/>)');


                });
            </script>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="dri:xref[@rend='feedurl']">
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="current()/@target"/>
            </xsl:attribute>

            <xsl:attribute name="style">
                <xsl:text>background: url(</xsl:text>
                <xsl:value-of select="$context-path"/>
                <xsl:text>/static/icons/feed.png) no-repeat; padding-left:20px;</xsl:text>
            </xsl:attribute>

            <xsl:attribute name="target">
                <xsl:text>blank</xsl:text>
            </xsl:attribute>

            <xsl:choose>
                <xsl:when test="contains(., 'rss_1.0')">
                    <xsl:text>RSS 1.0</xsl:text>
                </xsl:when>
                <xsl:when test="contains(., 'rss_2.0')">
                    <xsl:text>RSS 2.0</xsl:text>
                </xsl:when>
                <xsl:when test="contains(., 'atom_1.0')">
                    <xsl:text>Atom</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@qualifier"/>
                </xsl:otherwise>
            </xsl:choose>
        </a>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates />
    </xsl:template>

</xsl:stylesheet>
