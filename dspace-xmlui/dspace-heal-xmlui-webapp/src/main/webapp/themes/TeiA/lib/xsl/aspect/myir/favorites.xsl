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

    <xsl:variable name="loggedIn" select="/dri:document/dri:meta/dri:userMeta/@authenticated = 'yes'"/>
    <xsl:variable name="context" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>

    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <div class="item-summary-view-metadata">
            <xsl:call-template name="itemSummaryView-DIM-fields"/>
        </div>
        <xsl:if test="$loggedIn">
            <xsl:variable name="itemId" select="substring-after(//@OBJEDIT, '=')"/>
            <div class="col-lg-12 my-block">
                <div class="pull-left">
                    <p id="auth-info">&#x00AD;</p>
                    <a href="#" id="auth-modal-btn" class="bold btn btn-sm btn-primary" data-toggle="modal" data-target="#authModal">
                        <i18n:text>xmlui.MyIR.Authorship.add.short</i18n:text>
                    </a>
                </div>
                <div class="pull-right">
                    <a href="#" id="add-fav-btn" class="btn btn-sm btn-primary">
                        <span class="glyphicon glyphicon-star">&#x00AD;</span>
                        <xsl:text>&#160;</xsl:text>
                        <i18n:text>xmlui.MyIR.Favorites.add</i18n:text>
                    </a>
                </div>
                <div class="clearfix">&#160;</div>
            </div>

            <!-- Authorship Modal -->
            <div class="modal fade" id="authModal" tabindex="-1" role="dialog" aria-labelledby="#authsModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><xsl:text>&#215;</xsl:text></button>
                            <h4 class="modal-title" id="#authsModalLabel">
                                <i18n:text>xmlui.MyIR.Authorship.title</i18n:text>
                            </h4>
                        </div>
                        <div class="modal-body">
                            <p><em><i18n:text>xmlui.MyIR.Authorship.disclaimer</i18n:text></em></p>
                        </div>
                        <div class="modal-footer">
                            <button id="add-auth-btn" type="button" class="btn btn-primary"><i18n:text>xmlui.general.save</i18n:text></button>
                            <button type="button" class="btn btn-default" data-dismiss="modal"><i18n:text>xmlui.general.cancel</i18n:text></button>
                        </div>
                    </div>
                </div>
            </div>

            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of select="concat($theme-path,'/lib/js/myitems.js')"/>
                </xsl:attribute>&#160;
            </script>
            <script type="text/javascript">
                <xsl:text>
                $(document).ready(function () {
                    initMyItems({ favoriteBtnId: 'add-fav-btn',
                        authoredBtnId: 'add-auth-btn',
                        authoredInfoId: 'auth-info',
                        authoredModalBtnId: 'auth-modal-btn',
                        itemId:  </xsl:text><xsl:value-of select="$itemId"/><xsl:text>,
                        context: '</xsl:text><xsl:value-of select="$context"/><xsl:text>',
                        labels: {
                            isAuthored: '</xsl:text><i18n:text>xmlui.MyIR.Authorship.item.available</i18n:text><xsl:text>',
                            add: '</xsl:text><i18n:text>xmlui.MyIR.Authorship.add</i18n:text><xsl:text>',
                            remove: '</xsl:text><i18n:text>xmlui.MyIR.Authorship.remove</i18n:text><xsl:text>',
                            addModal: '</xsl:text><i18n:text>xmlui.MyIR.Authorship.add.short</i18n:text><xsl:text>',
                            removeModal: '</xsl:text><i18n:text>xmlui.MyIR.Authorship.remove.short</i18n:text><xsl:text>',
                        }
                    });
                });
                </xsl:text>
            </script>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>