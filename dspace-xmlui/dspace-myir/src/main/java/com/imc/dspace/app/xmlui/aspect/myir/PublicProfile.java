package com.imc.dspace.app.xmlui.aspect.myir;

import com.imc.dspace.myir.AcademicInterest;
import com.imc.dspace.myir.Authorship;
import com.imc.dspace.myir.EPersonProfile;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCValue;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Subscribe;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

public class PublicProfile extends AbstractDSpaceTransformer {

    /**
     * General Language Strings
     */
    protected static final Message T_dspace_home = message("xmlui.general.dspace_home");
    protected static final Message T_generic_profile = message("xmlui.MyIR.Profile.generic.profile");
    protected static final Message T_no_public_profile = message("xmlui.MyIR.Profile.owner.private.warning");
    protected static final Message T_no_access = message("xmlui.MyIR.Profile.no.access");
    protected static final Message T_email = message("xmlui.MyIR.Profile.email");
    protected static final Message T_phone = message("xmlui.EPerson.EditProfile.telephone");
    protected static final Message T_academic_interests = message("xmlui.MyIR.Profile.academic.interests");
    protected static final Message T_profession = message("xmlui.MyIR.Profile.profession");
    protected static final Message T_affiliation = message("xmlui.MyIR.Profile.affiliation");
    protected static final Message T_website = message("xmlui.MyIR.Profile.website");

    private static final Message T_collection_subscriptions = message("xmlui.MyIR.Profile.collections_subscriptions");
    private static final Message T_authored_items = message("xmlui.MyIR.Profile.authored_items");

    protected static final Message T_untitled = message("xmlui.MyIR.Favorites.untitled");
    protected static final Message T_f_limit = message("xmlui.MyIR.Favorites.limit");
    protected static final Message T_f_displayall = message("xmlui.MyIR.Favorites.displayall");
    protected static final Message T_f_total = message("xmlui.MyIR.Favorites.total.results");

    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private static Logger log = Logger.getLogger(PublicProfile.class);

    private EPerson profileEPerson;
    private EPersonProfile personProfile;
    boolean profileExists;
    boolean isPublic;
    boolean isCurrentUser;

    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);

        profileEPerson = null;
        personProfile = null;
        isPublic = false;

        try {
            int ePersonId = Integer.valueOf(parameters.getParameter("epersonId", "0"));
            profileEPerson = EPerson.find(context, ePersonId);
            personProfile = EPersonProfile.findEPersonsProfile(context, ePersonId);
        } catch (NumberFormatException nfe) {
            log.error(nfe);
        } catch (SQLException se) {
            log.error(se);
        }

        if (profileEPerson == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        profileExists = personProfile != null;
        isCurrentUser = context.getCurrentUser() != null && (context.getCurrentUser().getID() == profileEPerson.getID());
        isPublic = profileExists && personProfile.isPublic();
    }

    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException, SQLException,
            IOException, AuthorizeException {

        if (profileEPerson != null) {
            if (isPublic || isCurrentUser) {
                pageMeta.addMetadata("title").addContent(profileEPerson.getFullName());
                pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
                pageMeta.addTrailLink(null, profileEPerson.getFullName());
            } else {
                pageMeta.addMetadata("title").addContent(T_generic_profile);
                pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
            }
        }
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        boolean displayAll = false;
        //This param decides whether we display all of the user's previous
        // submissions, or just a portion of them
        if (request.getParameter("all") != null) {
            displayAll = true;
        }

        Division div = body.addDivision("profile_container");

        if (isPublic || isCurrentUser) {
            div.setHead(profileEPerson.getFullName());

            if (isCurrentUser && !isPublic) {
                this.addProfileNotPublic(div, T_no_public_profile);
            }

            this.addProfileData(div);
            this.addCollections(div);
            this.addAuthoredItems(div, displayAll);

        } else {

            div.setHead(T_generic_profile);
            this.addProfileNotPublic(div, T_no_access);

        }

    }

    /**
     * Show the user's personal details.
     * <p/>
     *
     * @param division   div to put archived submissions in
     */
    private void addProfileData(Division division) throws SQLException, WingException {

        Division profileDiv = division.addDivision("profile", "primary user-data");

        //Profession & Affiliation
        if (personProfile!=null) {
            StringBuilder shortDescription = new StringBuilder();

            if (!StringUtils.isEmpty(personProfile.getProfession())) {
                shortDescription.append(personProfile.getProfession());
            }

            if (!StringUtils.isEmpty(personProfile.getAffiliation()))   {

                if (shortDescription.length()>0) {
                    shortDescription.append(", ");
                }
                shortDescription.append(personProfile.getAffiliation());
            }

            profileDiv.addDivision("title").addPara(shortDescription.toString());

        }

        List personalData = profileDiv.addList("identity", List.TYPE_GLOSS, "dl-horizontal");

        // Email
        if (profileEPerson.getEmail() != null) {
            personalData.addLabel(T_email);
            personalData.addItem(profileEPerson.getEmail());
        }

        // Phone
        if (!StringUtils.isEmpty(profileEPerson.getMetadata("phone"))) {
            personalData.addLabel(T_phone);
            personalData.addItem(profileEPerson.getMetadata("phone"));
        }

        // Website URL
        if (personProfile!= null && !StringUtils.isEmpty(personProfile.getWebsiteUrl())) {
            personalData.addLabel(T_website);
            personalData.addItemXref(personProfile.getWebsiteUrl(), personProfile.getWebsiteUrl());
        }

        // Academic Interests
        java.util.List<AcademicInterest> academicInterests = AcademicInterest.findByEPerson(context, profileEPerson.getID());

        if (academicInterests.size() != 0) {
            StringBuilder interestsStr = new StringBuilder();
            for (int i=0; i<academicInterests.size(); i++) {
                interestsStr.append(academicInterests.get(i).getTerm());
                if (i < academicInterests.size()-1) interestsStr.append(", ");
            }
            personalData.addLabel(T_academic_interests);
            personalData.addItem(interestsStr.toString());
        }
    }

    private void addCollections(Division division) throws SQLException, WingException {
        Collection[] collectionsArray = Subscribe.getSubscriptions(context, profileEPerson);

        if (collectionsArray.length > 0) {
            Division collectionsDiv = division.addDivision("collections_panel", "primary panel panel-default");
            List collectionsListSection = collectionsDiv.addList("collections_list", List.TYPE_BULLETED, "collection-list panel-collapse collapse");
            collectionsListSection.setHead(T_collection_subscriptions);

            for (Collection col : collectionsArray)
            {

                Community temp=(Community)col.getParentObject();
                String current_name=temp.getName()+" > "+ col.getMetadata("name");

                try {
                    while (temp.getParentObject()!=null)
                    {
                        temp=(Community)temp.getParentObject();
                        current_name=temp.getName()+" > "+ current_name;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                if (current_name.length() > 120)
                {

                    current_name = current_name.substring(0, 67) + "..."+current_name.substring(current_name.length()-120);
                }
                String colUrl = contextPath + "/handle/" + col.getHandle();

                collectionsListSection.addItemXref(colUrl, current_name);

                /* String colName = col.getParentObject().getName() + " > " + col.getMetadata("name");
               String colUrl = contextPath + "/handle/" + col.getHandle();

               if (colName.length() > 80) {
                   colName = colName.substring(0, 77) + "...";
               }

               collectionsListSection.addItemXref(colUrl, colName);*/
            }
        }
    }

    private void addAuthoredItems(Division division, boolean displayAll) throws SQLException, WingException {

        java.util.List<Authorship> authorshipList = Authorship.findByEPerson(context, profileEPerson.getID());

        if (authorshipList.size() != 0) {

            Division authorshipDiv = division.addDivision("authored_panel", "primary panel panel-default");

            List authorshipListSection = authorshipDiv.addList("authored_list", List.TYPE_BULLETED, "authored-list panel-collapse collapse");
            authorshipListSection.setHead(T_authored_items);

            int limit = 50;
            int count = 0;

            for (Authorship authorship : authorshipList) {
                count++;

                if(count>limit && !displayAll)
                    break;

                org.dspace.content.Item authoredItem = authorship.getItem();
                String itemUrl = contextPath+"/handle/"+authoredItem.getHandle();
                DCValue[] titles = authoredItem.getMetadata("dc", "title", null, org.dspace.content.Item.ANY);

                // The item title
                if (titles != null && titles.length > 0 && titles[0].value != null) {
                    String displayTitle = titles[0].value;
                    if (displayTitle.length() > 100)
                        displayTitle = displayTitle.substring(0,100)+ " ...";
                    authorshipListSection.addItemXref(itemUrl, displayTitle);
                }
                else
                    authorshipListSection.addItemXref(itemUrl, T_untitled);
            }

            if (!displayAll && count > limit) {
                Para limitedList = authorshipDiv.addPara();
                limitedList.addContent(T_f_limit);
                limitedList.addXref(contextPath + "/favorites?all", T_f_displayall);
            }

        }

    }

    private void addProfileNotPublic(Division division, Message message) throws SQLException, WingException {

        Division messageDiv = division.addDivision("profile", "primary alert alert-warning");
        messageDiv.addPara(message);

    }
}
