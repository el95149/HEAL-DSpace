<xsl:stylesheet
        xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
        xmlns:dri="http://di.tamu.edu/DRI/1.0/"
        xmlns:mets="http://www.loc.gov/METS/"
        xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
        xmlns:xlink="http://www.w3.org/TR/xlink/"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns="http://www.w3.org/1999/xhtml"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:encoder="xalan://java.net.URLEncoder"
        xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
        exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util">

    <xsl:output indent="yes"/>

    <!-- User Public Profile -->
    <xsl:template match="dri:div[@id='com.imc.dspace.app.xmlui.aspect.myir.PublicProfile.div.title']">
        <xsl:if test="string-length('dri:p') > 0">
            <div class="title"><xsl:apply-templates/></div>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dri:div[@id='com.imc.dspace.app.xmlui.aspect.myir.PublicProfile.div.collections_panel']/dri:list/dri:head">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#com_imc_dspace_app_xmlui_aspect_myir_PublicProfile_list_collections_list">
                    <i class="glyphicon glyphicon-chevron-right">&#x00AD;</i>
                    <xsl:apply-templates/>
                </a>
            </h4>
        </div>
    </xsl:template>
    <xsl:template match="dri:div[@id='com.imc.dspace.app.xmlui.aspect.myir.PublicProfile.div.authored_panel']/dri:list/dri:head">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#com_imc_dspace_app_xmlui_aspect_myir_PublicProfile_list_authored_list">
                    <i class="glyphicon glyphicon-chevron-right">&#x00AD;</i>
                    <xsl:apply-templates/>
                </a>
            </h4>
        </div>
    </xsl:template>


    <xsl:template match="dri:div[@id='com.imc.dspace.app.xmlui.aspect.myir.PublicProfile.div.collections_panel']/dri:list/dri:item/*">
        <li>
            <a>
                <xsl:attribute name="href"><xsl:value-of select="@target"/></xsl:attribute>
                <i class="glyphicon glyphicon-folder-open">&#160;</i>
                <xsl:apply-templates/>
            </a>
        </li>
    </xsl:template>

    <xsl:template match="dri:div[@id='com.imc.dspace.app.xmlui.aspect.myir.PublicProfile.div.authored_panel']/dri:list/dri:item/*">
        <li>
            <a>
                <xsl:attribute name="href"><xsl:value-of select="@target"/></xsl:attribute>
                <i class="glyphicon glyphicon-file">&#160;</i>
                <xsl:apply-templates/>
            </a>
        </li>
    </xsl:template>

    <!-- Edit Profile -->
    <xsl:template match="dri:div[@id='com.imc.dspace.app.xmlui.aspect.myir.EditProfile.div.information']/dri:list/dri:list/dri:head">
        <h3>
            <xsl:attribute name="class">
                <xsl:text>form-header</xsl:text>
            </xsl:attribute>
            <xsl:apply-templates/>
        </h3>
    </xsl:template>

    <xsl:template match="dri:div[@id='com.imc.dspace.app.xmlui.aspect.myir.EditProfile.div.information']/dri:list/dri:head">
        <h3>
            <xsl:attribute name="class">
                <xsl:text>form-header</xsl:text>
            </xsl:attribute>
            <xsl:apply-templates/>
        </h3>
    </xsl:template>

    <!-- Profession Selection -->
    <xsl:template match="dri:list[starts-with(@id,'com.imc.dspace.app.xmlui.aspect.myir.EditProfile.list.identity')]">
        <xsl:apply-templates />
        <script type="text/javascript">
            $(document).ready(function(){
                $( "#com_imc_dspace_app_xmlui_aspect_myir_EditProfile_field_profession" ).autocomplete({
                    source: [ "&#922;&#945;&#952;&#951;&#947;&#951;&#964;&#942;&#962;", "&#913;&#957;&#945;&#960;&#955;&#951;&#961;&#969;&#964;&#942;&#962; &#922;&#945;&#952;&#951;&#947;&#951;&#964;&#942;&#962;", "&#917;&#960;&#943;&#954;&#959;&#965;&#961;&#959;&#962; &#922;&#945;&#952;&#951;&#947;&#951;&#964;&#942;&#962;", "&#922;&#945;&#952;&#951;&#947;&#951;&#964;&#942;&#962; &#917;&#966;&#945;&#961;&#956;&#959;&#947;&#974;&#957;", "&#917;&#961;&#949;&#965;&#957;&#951;&#964;&#942;&#962;", "&#933;&#960;&#959;&#968;&#942;&#966;&#953;&#959;&#962; &#916;&#953;&#948;&#940;&#954;&#964;&#959;&#961;&#945;&#962;", "&#924;&#949;&#964;&#945;&#960;&#964;&#965;&#967;&#953;&#945;&#954;&#972;&#962; &#934;&#959;&#953;&#964;&#951;&#964;&#942;&#962;",
                    "Professor","Associate Professor","Assistant Professor","Lecturer","Researcher","PhD Candidate","Postgraduate Student"
                    ],
                    minLength: 0
                });
                $( "#com_imc_dspace_app_xmlui_aspect_myir_EditProfile_field_affiliation" ).autocomplete({
                                    source: [ "&#913;&#957;&#969;&#964;&#940;&#964;&#951; &#931;&#967;&#959;&#955;&#942; &#922;&#945;&#955;&#974;&#957; &#932;&#949;&#967;&#957;&#974;&#957;",
                                    "&#913;&#957;&#974;&#964;&#945;&#964;&#951; &#931;&#967;&#959;&#955;&#942; &#928;&#945;&#953;&#948;&#945;&#947;&#969;&#947;&#953;&#954;&#942;&#962; &#954;&#945;&#953; &#932;&#949;&#967;&#957;&#959;&#955;&#959;&#947;&#953;&#954;&#942;&#962;&#917;&#954;&#960;&#945;&#943;&#948;&#949;&#965;&#963;&#951;&#962;",
                                    "&#913;&#961;&#953;&#963;&#964;&#959;&#964;&#941;&#955;&#949;&#953;&#959; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#920;&#949;&#963;&#963;&#945;&#955;&#959;&#957;&#943;&#954;&#951;&#962;",
                                    "&#915;&#949;&#969;&#960;&#959;&#957;&#953;&#954;&#972; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#913;&#952;&#951;&#957;&#974;&#957;",
                                    "&#916;&#951;&#956;&#959;&#954;&#961;&#943;&#964;&#949;&#953;&#959; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#920;&#961;&#940;&#954;&#951;&#962;",
                                    "&#916;&#953;&#949;&#952;&#957;&#941;&#962; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#917;&#955;&#955;&#940;&#948;&#959;&#962;",
                                    "&#917;&#952;&#957;&#953;&#954;&#972; &#954;&#945;&#953; &#922;&#945;&#960;&#959;&#948;&#953;&#963;&#964;&#961;&#953;&#945;&#954;&#972; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#913;&#952;&#951;&#957;&#974;&#957;",
                                    "&#917;&#952;&#957;&#953;&#954;&#972; &#924;&#949;&#964;&#963;&#972;&#946;&#953;&#959; &#928;&#959;&#955;&#965;&#964;&#949;&#967;&#957;&#949;&#943;&#959;",
                                    "&#917;&#955;&#955;&#951;&#957;&#953;&#954;&#972; &#913;&#957;&#959;&#953;&#954;&#964;&#972; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959;",
                                    "&#921;&#972;&#957;&#953;&#959; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959;",
                                    "&#927;&#953;&#954;&#959;&#957;&#959;&#956;&#953;&#954;&#972; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#913;&#952;&#951;&#957;&#974;&#957;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#916;&#965;&#964;&#953;&#954;&#942;&#962; &#917;&#955;&#955;&#940;&#948;&#945;&#962;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#916;&#965;&#964;&#953;&#954;&#942;&#962; &#924;&#945;&#954;&#949;&#948;&#959;&#957;&#943;&#945;&#962;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#920;&#949;&#963;&#963;&#945;&#955;&#943;&#945;&#962;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#921;&#969;&#945;&#957;&#957;&#943;&#957;&#969;&#957;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#922;&#961;&#942;&#964;&#951;&#962;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#924;&#945;&#954;&#949;&#948;&#959;&#957;&#943;&#945;&#962;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#931;&#964;&#949;&#961;&#949;&#940;&#962; &#917;&#955;&#955;&#940;&#948;&#959;&#962;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#928;&#945;&#964;&#961;&#974;&#957;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#928;&#949;&#953;&#961;&#945;&#953;&#974;&#962;",
                                    "&#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959; &#928;&#949;&#955;&#959;&#960;&#959;&#957;&#957;&#942;&#963;&#959;&#965;",
                                    "&#928;&#940;&#957;&#964;&#949;&#953;&#959; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959;",
                                    "&#928;&#959;&#955;&#965;&#964;&#949;&#967;&#957;&#949;&#943;&#959; &#922;&#961;&#942;&#964;&#951;&#962;",
                                    "&#935;&#945;&#961;&#959;&#954;&#972;&#960;&#949;&#953;&#959; &#928;&#945;&#957;&#949;&#960;&#953;&#963;&#964;&#942;&#956;&#953;&#959;",
                                    "&#932;.&#917;.&#921;. &#913;&#952;&#942;&#957;&#945;&#962;",
                                    "&#932;.&#917;.&#921;. &#916;&#965;&#964;&#953;&#954;&#942;&#962; &#924;&#945;&#954;&#949;&#948;&#959;&#957;&#943;&#945;&#962;",
                                    "&#932;.&#917;.&#921;. &#919;&#960;&#949;&#943;&#961;&#959;&#965;",
                                    "&#932;.&#917;.&#921;. &#920;&#949;&#963;&#963;&#945;&#955;&#959;&#957;&#943;&#954;&#951;&#962;",
                                    "&#932;.&#917;.&#921;. &#921;&#959;&#957;&#943;&#969;&#957; &#925;&#942;&#963;&#969;&#957;",
                                    "&#932;.&#917;.&#921;. &#922;&#945;&#946;&#940;&#955;&#945;&#962;",
                                    "&#932;.&#917;.&#921;. &#922;&#945;&#955;&#945;&#956;&#940;&#964;&#945;&#962;",
                                    "&#932;.&#917;.&#921;. &#922;&#961;&#942;&#964;&#951;&#962;",
                                    "&#932;.&#917;.&#921;. &#923;&#945;&#956;&#943;&#945;&#962;",
                                    "&#932;.&#917;.&#921;. &#923;&#940;&#961;&#953;&#963;&#945;&#962;",
                                    "&#932;.&#917;.&#921;. &#924;&#949;&#963;&#959;&#955;&#959;&#947;&#947;&#943;&#959;&#965;",
                                    "&#932;.&#917;.&#921;. &#928;&#945;&#964;&#961;&#974;&#957;",
                                    "&#932;.&#917;.&#921;. &#928;&#949;&#953;&#961;&#945;&#953;&#940;",
                                    "&#932;.&#917;.&#921;. &#931;&#949;&#961;&#961;&#974;&#957;",
                                    "&#932;.&#917;.&#921;. &#935;&#945;&#955;&#954;&#943;&#948;&#945;&#962;",
                                    "Athens School of Fine Arts",
                                    "School of Pedagogical and Technological Education",
                                    "Aristotle Unversity of Thessaloniki",
                                    "Agricultural University of Athens",
                                    "Demokritos University of Thrace",
                                    "International Hellenic University",
                                    "National and Kapodestrian University of Athens",
                                    "National Technical University of Athens",
                                    "Hellenic Open University",
                                    "Ionian University",
                                    "Athens University of Economics and Business",
                                    "University of Western Greece",
                                    "University of Western Macedonia",
                                    "University of Thessaly",
                                    "University of Ioannina",
                                    "University of Crete",
                                    "University of Macedonia",
                                    "University of Central Greece",
                                    "University of Patras",
                                    "University of Piraeus",
                                    "University of Peloponnese",
                                    "Panteion University",
                                    "Technical University of Crete",
                                    "Harokopion University",
                                    "Technological Educational Institute of Athens",
                                    "Technological Educational Institute of Western Macedonia",
                                    "Epirus Institute of Technology",
                                    "Technological Educational Institute of Thessaloniki",
                                    "Technological Educational Institution of Ionian Islands",
                                    "Kavala Institute of Technology",
                                    "Technological Educational Institute of Kalamata",
                                    "Technological Educational Institute of Crete",
                                    "Technological Educational Institute of Lamia",
                                    "Technological Educational Institute of Larissa",
                                    "Technological Educational Institute of Messolonghi",
                                    "Technological Educational Institute of Patras",
                                    "Technological Educational Institute of Piraeus",
                                    "Technological Educational Institute of Serres",
                                    "Technological Educational Institute of Chalkida" ],
                                    minLength: 0
                                });
            });
        </script>
    </xsl:template>

</xsl:stylesheet>