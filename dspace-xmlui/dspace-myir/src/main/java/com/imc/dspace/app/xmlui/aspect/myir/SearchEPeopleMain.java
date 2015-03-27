/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.imc.dspace.app.xmlui.aspect.myir;

import com.imc.dspace.myir.EPersonProfile;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;

/**
 * The manage epeople page is the starting point page for managing 
 * epeople. From here the user is able to browse or search for epeople, 
 * once identified the user can selected them for deletition by selecting 
 * the checkboxes and clicking delete or click their name to edit the 
 * eperson.
 *
 * @author Alexey Maslov
 * @author Scott Phillips
 */
public class SearchEPeopleMain extends AbstractDSpaceTransformer
{

    private static Logger log = Logger.getLogger(SearchEPeopleMain.class);
    /** Language Strings */
    private static final Message T_title =
            message("xmlui.administrative.eperson.myir.SearchEPeopleMain.title");

    private static final Message T_eperson_trail =
            message("xmlui.administrative.eperson.myir.search_epeople_trail");

    private static final Message T_main_head =
            message("xmlui.administrative.eperson.myir.SearchEPeopleMain.main_head");



    private static final Message T_actions_search =
            message("xmlui.administrative.eperson.myir.SearchEPeopleMain.actions_search");

    private static final Message T_search_help =
            message("xmlui.administrative.eperson.myir.SearchEPeopleMain.search_help");

    private static final Message T_dspace_home =
            message("xmlui.general.dspace_home");

    private static final Message T_go =
            message("xmlui.general.go");

    private static final Message T_search_head =
            message("xmlui.administrative.eperson.ManageEPeopleMain.search_head");

    private static final Message T_search_column1 = message("xmlui.administrative.eperson.myir.SearchEPeopleMain.search_column1");
    private static final Message T_search_column2 = message("xmlui.MyIR.Profile.email");
    private static final Message T_search_column3 = message("xmlui.MyIR.Profile.profession");
    private static final Message T_search_column4 = message("xmlui.MyIR.Profile.affiliation");



    private static final Message T_no_results =
            message("xmlui.administrative.eperson.ManageEPeopleMain.no_results");

    /**
     * The total number of entries to show on a page
     */
    private static final int PAGE_SIZE = 10;


    public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(null,T_eperson_trail);
    }


    public void addBody(Body body) throws WingException, SQLException
    {
        /* Get and setup our parameters */
        //int page          = parameters.getParameterAsInteger("page",0);
        int highlightID   = parameters.getParameterAsInteger("highlightID",-1);
        Request request = ObjectModelHelper.getRequest(objectModel);
        int page=0;
        if (request.getParameter("page")!=null)
        {
            try {
                page=Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException e) {
               // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        String query = request.getParameter("query");
        //String query      = decodeFromURL(parameters.getParameter("query",null));
        String baseURL    = contextPath;//+"/admin/epeople?administrative-continue="+knot.getId();
        int resultCount   = EPersonProfile.searchResultCount(context, query==null?"":query);
        EPersonProfile[] epeople = EPersonProfile.search(context, query==null?"":query, page * PAGE_SIZE, PAGE_SIZE);

        log.error("query is "+query);
        // DIVISION: eperson-main
        Division main = body.addInteractiveDivision("epeople-main", contextPath
                + "/profilesearch", Division.METHOD_POST,
                "primary administrative eperson");
        main.setHead(T_main_head);

        // DIVISION: eperson-actions
        Division actions = main.addDivision("epeople-actions");


        List actionsList = actions.addList("actions");


        actionsList.addLabel(T_actions_search);
        org.dspace.app.xmlui.wing.element.Item actionItem = actionsList.addItem();
        Text queryField = actionItem.addText("query");
        if (query != null)
        {
            queryField.setValue(query);
        }
        queryField.setHelp(T_search_help);
        actionItem.addButton("submit_search").setValue(T_go);

        // DIVISION: eperson-search
        Division search = main.addDivision("eperson-search");
        search.setHead(T_search_head);

        // If there are more than 10 results the paginate the division.
        if (resultCount > PAGE_SIZE)
        {
            // If there are enough results then paginate the results
            int firstIndex = page*PAGE_SIZE+1;
            int lastIndex = page*PAGE_SIZE + epeople.length;

            String nextURL = null, prevURL = null;
            if (page < (resultCount / PAGE_SIZE))
            {
                nextURL = baseURL + "/profilesearch?page=" + (page + 1);
            }
            if (page > 0)
            {
                prevURL = baseURL + "/profilesearch?page=" + (page - 1);
            }

            search.setSimplePagination(resultCount,firstIndex,lastIndex,prevURL, nextURL);
        }

        Table table = search.addTable("eperson-search-table", epeople.length + 1, 1);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCell().addContent(T_search_column1);
        header.addCell().addContent(T_search_column2);
        header.addCell().addContent(T_search_column3);
        header.addCell().addContent(T_search_column4);

        CheckBox selectEPerson;
        for (EPersonProfile personprofile : epeople)
        {
            EPerson person=personprofile.getEperson();
            log.error("1st profile "+personprofile.getID());
            if(person!=null)
            {
                log.error("1st person "+person.getID()+" "+person.getEmail());
                String epersonID = String.valueOf(person.getID());
                String fullName = person.getFullName();
                String email = person.getEmail();
                String url = baseURL+"/user/"+epersonID;
                java.util.List<String> deleteConstraints = person.getDeleteConstraints();

                Row row;
                if (person.getID() == highlightID)
                {
                    // This is a highlighted eperson
                    row = table.addRow(null, null, "highlight");
                }
                else
                {
                    row = table.addRow();
                }



                row.addCell().addXref(url, fullName);
                row.addCell().addXref(url, email);
                row.addCellContent(personprofile.getProfession()==null?"":personprofile.getProfession());
                row.addCellContent(personprofile.getAffiliation()==null?"":personprofile.getAffiliation());
            }

        }

        if (epeople.length <= 0)
        {
            Cell cell = table.addRow().addCell(1, 4);
            cell.addHighlight("italic").addContent(T_no_results);
        }
        else
        {
            //search.addPara().addButton("submit_delete").setValue(T_submit_delete);
        }

        //main.addHidden("administrative-continue").setValue(knot.getId());

    }
}
