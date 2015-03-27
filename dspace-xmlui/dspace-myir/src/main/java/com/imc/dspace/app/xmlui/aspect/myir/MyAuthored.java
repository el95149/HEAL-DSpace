package com.imc.dspace.app.xmlui.aspect.myir;

import com.imc.dspace.myir.Authorship;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCValue;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MyAuthored extends AbstractDSpaceTransformer {

    /** General Language Strings */
    protected static final Message T_title = message("xmlui.MyIR.Authorship.title");
    protected static final Message T_dspace_home = message("xmlui.general.dspace_home");
    protected static final Message T_trail = message("xmlui.MyIR.Authorship.trail");
    protected static final Message T_head = message("xmlui.MyIR.Authorship.head");
    protected static final Message T_untitled = message("xmlui.MyIR.untitled");

    protected static final Message T_a_info = message("xmlui.MyIR.Authorship.info");
    protected static final Message T_a_no_results = message("xmlui.MyIR.Authorship.no.results");
    protected static final Message T_a_column1 = message("xmlui.MyIR.Authorship.column1");
    protected static final Message T_a_column2 = message("xmlui.MyIR.Authorship.column2");
    protected static final Message T_a_limit = message("xmlui.MyIR.Authorship.limit");
    protected static final Message T_a_remove = message("xmlui.MyIR.Authorship.remove");
    protected static final Message T_a_displayall = message("xmlui.MyIR.Authorship.displayall");
    protected static final Message T_a_total = message("xmlui.MyIR.Authorship.total.results");

    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        pageMeta.addMetadata("title").addContent(T_title);

        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrailLink(null,T_trail);
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        boolean displayAll = false;
        //This param decides whether we display all of the user's previous
        // submissions, or just a portion of them
        if (request.getParameter("all") != null) {
            displayAll=true;
        }

        Division div = body.addDivision("authored", "primary");
        div.setHead(T_head);

        this.addMyAuthoredList(div, displayAll);
    }

    /**
     * Show the user's authored items.
     *
     * If the user has no personal production, display nothing.
     * If 'displayAll' is true, then display all user's items.
     * Otherwise, default to only displaying 50 authored items.
     *
     * @param division div to put archived submissions in
     * @param displayAll whether to display all or just a limited number.
     */
    private void addMyAuthoredList(Division division, boolean displayAll)
            throws SQLException,WingException
    {

        java.util.List<Authorship> authorshipList = Authorship.findByEPerson(context, context.getCurrentUser().getID());

        // No tasks, so don't show the table.
        if (authorshipList.size() == 0) {
            Division noAuthoredDiv = division.addDivision("authored", "alert alert-info");
            noAuthoredDiv.addPara(T_a_no_results);

            return;
        }

        Division authoredDiv = division.addInteractiveDivision("authored", contextPath + "/production", Division.METHOD_POST, "primary");
        authoredDiv.addPara(T_a_info);

        // Create table, headers
        Table table = authoredDiv.addTable("authored",authorshipList.size() + 2,3);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCellContent(""); // ACTIONS
        header.addCellContent(T_a_column1); // ITEM TITLE (LINKED)
        header.addCellContent(T_a_column2); // COLLECTION NAME (LINKED)

        //Limit to showing just 50 items, unless overridden
        int limit = 50;
        int count = 0;

        CheckBox removeAuthoredCbox;

        for (Authorship authorship : authorshipList) {
            count++;

            //exit loop if we've gone over our limit of submissions to display
            if(count>limit && !displayAll)
                break;

            org.dspace.content.Item authoredItem = authorship.getItem();
            String collUrl = contextPath+"/handle/"+authoredItem.getOwningCollection().getHandle();
            String itemUrl = contextPath+"/handle/"+authoredItem.getHandle();
            DCValue[] titles = authoredItem.getMetadata("dc", "title", null, org.dspace.content.Item.ANY);
            String collectionName = authoredItem.getOwningCollection().getMetadata("name");

            Row row = table.addRow();

            // delete checkbox
            removeAuthoredCbox = row.addCell().addCheckBox("authorshipID");
            removeAuthoredCbox.setLabel(String.valueOf(authorship.getID()));
            removeAuthoredCbox.addOption(authorship.getID());

            // The item description
            if (titles != null && titles.length > 0 && titles[0].value != null) {
                String displayTitle = titles[0].value;
                if (displayTitle.length() > 100)
                    displayTitle = displayTitle.substring(0,100)+ " ...";
                row.addCell().addXref(itemUrl,displayTitle);
            }
            else
                row.addCell().addXref(itemUrl,T_untitled);

            // Owning Collection
            row.addCell().addXref(collUrl, collectionName);

        }

        Division resultsDiv = authoredDiv.addDivision("results", "my-results");
        resultsDiv.addPara(T_a_total.parameterize(authorshipList.size()));

        authoredDiv.addPara().addButton("submit_authored_remove", "btn btn-primary").setValue(T_a_remove);

        //Display limit text & link to allow user to override this default limit
        if(!displayAll && count>limit) {
            Para limitedList = authoredDiv.addPara();
            limitedList.addContent(T_a_limit);
            limitedList.addXref(contextPath + "/production?all", T_a_displayall);
        }
    }

}
