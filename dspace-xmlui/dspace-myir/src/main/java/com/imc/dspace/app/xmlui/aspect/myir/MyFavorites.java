package com.imc.dspace.app.xmlui.aspect.myir;

import com.imc.dspace.myir.Favorite;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class MyFavorites extends AbstractDSpaceTransformer {

    /** General Language Strings */
    protected static final Message T_title = message("xmlui.MyIR.Favorites.title");
    protected static final Message T_dspace_home = message("xmlui.general.dspace_home");
    protected static final Message T_trail = message("xmlui.MyIR.Favorites.trail");
    protected static final Message T_head = message("xmlui.MyIR.Favorites.head");
    protected static final Message T_untitled = message("xmlui.MyIR.untitled");

    protected static final Message T_f_info = message("xmlui.MyIR.Favorites.info");
    protected static final Message T_f_no_results = message("xmlui.MyIR.Favorites.no.results");
    protected static final Message T_f_column1 = message("xmlui.MyIR.Favorites.column1");
    protected static final Message T_f_column2 = message("xmlui.MyIR.Favorites.column2");
    protected static final Message T_f_column3 = message("xmlui.MyIR.Favorites.column3");
    protected static final Message T_f_limit = message("xmlui.MyIR.Favorites.limit");
    protected static final Message T_f_remove = message("xmlui.MyIR.Favorites.remove");
    protected static final Message T_f_displayall = message("xmlui.MyIR.Favorites.displayall");
    protected static final Message T_f_total = message("xmlui.MyIR.Favorites.total.results");

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
        boolean displayAll = false;
        //This param decides whether we display all of the user's previous
        // submissions, or just a portion of them
        if (request.getParameter("all") != null) {
            displayAll=true;
        }

        Division div = body.addDivision("favorites", "primary");
        div.setHead(T_head);

        this.addMyFavoritesList(div, displayAll);
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
    private void addMyFavoritesList(Division division, boolean displayAll)
            throws SQLException,WingException
    {

        java.util.List<Favorite> favoritesList = Favorite.findByEPerson(context, context.getCurrentUser().getID());

        // No tasks, so don't show the table.
        if (favoritesList.size() == 0) {
            Division noFavoritesDiv = division.addDivision("favorites", "alert alert-info");
            noFavoritesDiv.addPara(T_f_no_results);

            return;
        }

        Division favoritesDiv = division.addInteractiveDivision("favorites", contextPath+"/favorites", Division.METHOD_POST, "primary");
        favoritesDiv.addPara(T_f_info);

        // Create table, headers
        Table table = favoritesDiv.addTable("favorites",favoritesList.size() + 2,3);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCellContent(""); // ACTIONS
        header.addCellContent(T_f_column1); // ADD DATE
        header.addCellContent(T_f_column2); // ITEM TITLE (LINKED)
        header.addCellContent(T_f_column3); // COLLECTION NAME (LINKED)

        //Limit to showing just 50 favorites, unless overridden
        //(This is a saftey measure for Admins who may have submitted
        // thousands of items under their account via bulk ingest tools, etc.)
        int limit = 50;
        int count = 0;

        CheckBox removeFavoriteCbox;

        for (Favorite favorite : favoritesList) {
            count++;

            //exit loop if we've gone over our limit of submissions to display
            if(count>limit && !displayAll)
                break;

            org.dspace.content.Item favoriteItem = favorite.getItem();
            String collUrl = contextPath+"/handle/"+favoriteItem.getOwningCollection().getHandle();
            String itemUrl = contextPath+"/handle/"+favoriteItem.getHandle();
            DCValue[] titles = favoriteItem.getMetadata("dc", "title", null, org.dspace.content.Item.ANY);
            String collectionName = favoriteItem.getOwningCollection().getMetadata("name");
            Date addDate = favorite.getAddDate();

            Row row = table.addRow();

            // delete checkbox
            removeFavoriteCbox = row.addCell().addCheckBox("favoriteID");
            removeFavoriteCbox.setLabel(String.valueOf(favorite.getID()));
            removeFavoriteCbox.addOption(favorite.getID());

            // Item accession date
            if (addDate != null) {
                Cell cellDate = row.addCell();
                cellDate.addContent(dateFormatter.format(addDate));
            }
            else
                row.addCell().addContent("");

            // The item description
            if (titles != null && titles.length > 0 &&
                    titles[0].value != null)
            {
                String displayTitle = titles[0].value;
                if (displayTitle.length() > 50)
                    displayTitle = displayTitle.substring(0,50)+ " ...";
                row.addCell().addXref(itemUrl,displayTitle);
            }
            else
                row.addCell().addXref(itemUrl,T_untitled);

            // Owning Collection
            row.addCell().addXref(collUrl, collectionName);

        }

        Division resultsDiv = favoritesDiv.addDivision("results", "my-results");
        resultsDiv.addPara(T_f_total.parameterize(favoritesList.size()));

        favoritesDiv.addPara().addButton("submit_favorites_remove", "btn btn-primary").setValue(T_f_remove);

        //Display limit text & link to allow user to override this default limit
        if(!displayAll && count>limit) {
            Para limitedList = favoritesDiv.addPara();
            limitedList.addContent(T_f_limit);
            limitedList.addXref(contextPath + "/favorites?all", T_f_displayall);
        }
    }

}
