package com.imc.dspace.app.xmlui.aspect.myir;

import com.imc.dspace.myir.AcademicInterest;
import com.imc.dspace.myir.Favorite;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MyAcademicInterests extends AbstractDSpaceTransformer {

    /** General Language Strings */
    protected static final Message T_title = message("xmlui.MyIR.AcademicInterests.title");
    protected static final Message T_dspace_home = message("xmlui.general.dspace_home");
    protected static final Message T_trail = message("xmlui.MyIR.AcademicInterests.trail");
    protected static final Message T_head = message("xmlui.MyIR.AcademicInterests.head");

    protected static final Message T_f_info = message("xmlui.MyIR.AcademicInterests.info");
    protected static final Message T_f_no_results = message("xmlui.MyIR.AcademicInterests.no.results");
    protected static final Message T_f_column2 = message("xmlui.MyIR.AcademicInterests.column2");
    protected static final Message T_f_column3 = message("xmlui.MyIR.AcademicInterests.column3");
    protected static final Message T_f_limit = message("xmlui.MyIR.AcademicInterests.limit");
    protected static final Message T_f_remove = message("xmlui.MyIR.AcademicInterests.remove");
    protected static final Message T_f_add = message("xmlui.MyIR.AcademicInterests.add");
    protected static final Message T_f_displayall = message("xmlui.MyIR.AcademicInterests.displayall");
    protected static final Message T_f_total = message("xmlui.MyIR.AcademicInterests.total.results");
    protected static final Message T_f_addterm = message("xmlui.MyIR.AcademicInterests.addterm");
    protected static final Message T_f_addtermhint = message("xmlui.MyIR.AcademicInterests.addtermhint");
    protected static final Message T_f_addinterest = message("xmlui.MyIR.AcademicInterests.addinterest");

    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
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
        String query      = decodeFromURL(parameters.getParameter("query",null));
        boolean displayAll = false;
        //This param decides whether we display all of the user's previous
        // submissions, or just a portion of them
        if (request.getParameter("all") != null) {
            displayAll=true;
        }

        Division div = body.addDivision("interests", "primary");
        div.setHead(T_head);

        this.addMyInterestsList(div, displayAll);

         // DIVISION: eperson-actions
        Division actions = body.addInteractiveDivision("interests", contextPath + "/interests", Division.METHOD_POST, "primary");
        actions.setHead(T_f_addinterest);

        List actionsList = actions.addList("actions");


        actionsList.addLabel(T_f_addterm);
        org.dspace.app.xmlui.wing.element.Item actionItem = actionsList.addItem();
        Text queryField = actionItem.addText("query");
        if (query != null)
        {
            queryField.setValue(query);
            //AcademicInterest.addEPersonsInterest(context,eperson.getID(),query);
        }
        queryField.setHelp(T_f_addtermhint);
        actionItem.addButton("submit_interest_add","btn btn-primary").setValue(T_f_add);
    }

    /**
     * Show the user's favorites.
     *
     * If the user has no favorites, display nothing.
     * If 'displayAll' is true, then display all user's favorites.
     * Otherwise, default to only displaying 50 favorites.
     *
     * @param division div to put archived submissions in
     * @param displayAll whether to display all or just a limited number.
     */
    private void addMyInterestsList(Division division, boolean displayAll)
            throws SQLException,WingException
    {

        java.util.List<AcademicInterest> interestList = AcademicInterest.findByEPerson(context, context.getCurrentUser().getID());

        // No tasks, so don't show the table.
        if (interestList.size() == 0) {
            Division noInterestsDiv = division.addDivision("interests", "alert alert-info");
            noInterestsDiv.addPara(T_f_no_results);

            return;
        }

        Division interestsDiv = division.addInteractiveDivision("interests", contextPath + "/interests", Division.METHOD_POST, "primary");
        interestsDiv.addPara(T_f_info);



        // Create table, headers
        Table table = interestsDiv.addTable("interests",interestList.size() + 2, 3);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCellContent(""); // ACTIONS

        header.addCellContent(T_f_column2); // ITEM TITLE (LINKED)
        header.addCellContent(T_f_column3); // URL (LINKED)

        //Limit to showing just 50 favorites, unless overridden
        //(This is a saftey measure for Admins who may have submitted
        // thousands of items under their account via bulk ingest tools, etc.)
        int limit = 50;
        int count = 0;

        CheckBox removeInterestbox;

        for (AcademicInterest facademicInterest : interestList) {
            count++;

            //exit loop if we've gone over our limit of submissions to display
            if(count>limit && !displayAll)
                break;


            Row row = table.addRow();

            // delete checkbox
            removeInterestbox = row.addCell().addCheckBox("interestID");
            removeInterestbox.setLabel(String.valueOf(facademicInterest.getID()));
            removeInterestbox.addOption(facademicInterest.getID());



            row.addCell().addContent(facademicInterest.getTerm());
            String encodedQuery="";
            try {
                encodedQuery=URLEncoder.encode(facademicInterest.getTerm(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            String feedUrl = contextPath+"/myir/feed/rss_2.0/"+ encodedQuery;
            row.addCell().addXref(feedUrl, "RSS 2.0 Feed","feedurl");

        }

        Division resultsDiv = interestsDiv.addDivision("results", "my-results");
        resultsDiv.addPara(T_f_total.parameterize(interestList.size()));

        interestsDiv.addPara().addButton("submit_interests_remove", "btn btn-primary").setValue(T_f_remove);

        //Display limit text & link to allow user to override this default limit
        if(!displayAll && count>limit) {
            Para limitedList = interestsDiv.addPara();
            limitedList.addContent(T_f_limit);
            limitedList.addXref(contextPath + "/interests?all", T_f_displayall);
        }
    }

}
