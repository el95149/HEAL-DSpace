package com.imc.dspace.app.xmlui.aspect.myir;

import com.imc.dspace.myir.Authorship;
import com.imc.dspace.myir.Favorite;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

public class MyItemsHandler extends AbstractGenerator {

    private final static String FAVORITES = "favorite";
    private final static String AUTHORED = "authored";
    private final static String ALL = "all";  // default

    private static Logger log = Logger.getLogger(MyItemsHandler.class);

    public void generate() throws IOException, SAXException, ProcessingException {

        int itemId = Integer.valueOf(parameters.getParameter("itemId", "0"));
        Boolean toggle = Boolean.valueOf(parameters.getParameter("toggle", "false"));
        String listType = parameters.getParameter("listType", ALL);
        if (StringUtils.isEmpty(listType)) listType = ALL;

        StringBuilder xml = new StringBuilder();
        xml.append("<result>");

        String out = Boolean.toString(false);
        log.info("got in here with "+toggle+" in "+listType+" for "+itemId);
        try {

            Context context = ContextUtil.obtainContext(objectModel);

            if (context.getCurrentUser() != null && itemId > 0 && StringUtils.isNotEmpty(listType)) {

                int currentUserId = context.getCurrentUser().getID();
                boolean state = false;

                // toggle
                if (toggle) {

                    if (listType.equals(FAVORITES)) {
                        state = Favorite.toggleEPersonsFavorite(context, currentUserId, itemId);
                        log.info("toggled favorite item "+itemId+", now: " + state);
                    }
                    else if (listType.equals(AUTHORED)) {
                        state = Authorship.toggleEPersonsAuthorship(context, currentUserId, itemId);
                        log.info("toggled authored item "+itemId+", now: " + state);
                    }

                    out = Boolean.toString(state);

                }
                // just check
                else {

                    if (listType.equals(FAVORITES)) {
                        state = Favorite.checkEPersonsFavorite(context, currentUserId, itemId);
                        out = Boolean.toString(state);
                        log.info("checked favorite item "+itemId+ ", answer: "+ state);
                    }
                    else if (listType.equals(AUTHORED)) {
                        state = Authorship.checkEPersonsAuthorship(context, currentUserId, itemId);
                        out = Boolean.toString(state);
                        log.info("checked authored item "+itemId+ ", answer: "+ state);
                    } else if (listType.equals(ALL)) {
                        boolean favoriteState = Favorite.checkEPersonsFavorite(context, currentUserId, itemId);
                        boolean authoredState = Authorship.checkEPersonsAuthorship(context, currentUserId, itemId);

                        out = "{\"" + FAVORITES +"\":" + favoriteState + "," +
                                "\"" + AUTHORED + "\":" + authoredState + "}";

                        log.info("checked all my items "+itemId+ ", answer: "+ out);
                    }

                }
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (AuthorizeException aue) {
            aue.printStackTrace();
        }

        xml.append("<state>").append(out).append("</state>");
        xml.append("</result>");

        // Return the XML to Cocoon's pipeline
        XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setContentHandler(super.xmlConsumer);
        InputSource source = new InputSource(new StringReader(xml.toString()));
        xmlreader.parse(source);

        try {
            this.finalize();
        } catch (Throwable e) {
        }

    }
}
