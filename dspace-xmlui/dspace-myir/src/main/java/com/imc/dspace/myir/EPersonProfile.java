package com.imc.dspace.myir;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class EPersonProfile {

    /** log4j category */
    private static Logger log = Logger.getLogger(EPersonProfile.class);

    /** Our context */
    private Context ourContext;

    /** The table row corresponding to this item */
    private TableRow profileRow;

    /** The eperson */
    private EPerson eperson;

    private String profession;
    private String affiliation;
    private String websiteUrl;
    private Boolean isPublic;

    /**
     * Construct an ePerson Profile with the given table row
     *
     * @param context the context this object exists in
     * @param row the corresponding row in the table
     * @throws java.sql.SQLException
     */
    EPersonProfile(Context context, TableRow row) throws SQLException {
        ourContext = context;
        profileRow = row;

        eperson = EPerson.find(ourContext, profileRow.getIntColumn("eperson_id"));
        profession = profileRow.getStringColumn("profession");
        affiliation = profileRow.getStringColumn("affiliation");
        websiteUrl = profileRow.getStringColumn("website_url");
        isPublic = profileRow.getBooleanColumn("is_public");

        // Cache ourselves
        context.cache(this, row.getIntColumn("profile_id"));
    }

    public int getID() {
        return profileRow.getIntColumn("profile_id");
    }

    public EPerson getEperson() {
        return eperson;
    }

    public String getProfession() {
        return profession;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    /**
     * Update the profile metadata to the database. Inserts if this is a new profile.
     *
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public void update() throws SQLException, IOException, AuthorizeException {
        log.info(LogManager.getHeader(ourContext, "update_epersonprofile", "profile_id=" + getID()));
        DatabaseManager.update(ourContext, profileRow);
    }

    public boolean canEdit() {
        return ourContext.getCurrentUser().equals(eperson);
    }

    /**
     * Delete the profile row.
     *
     * @throws java.sql.SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public void delete() throws SQLException, AuthorizeException {

        if (!AuthorizeManager.isAdmin(ourContext) &&
                ((ourContext.getCurrentUser() == null) ||
                        (ourContext.getCurrentUser().getID() != this.getEperson().getID())))
        {
            throw new AuthorizeException("Must be an administrator or the owner to remove profile");
        }

        log.info(LogManager.getHeader(ourContext, "delete_epersonprofile", "profile_id=" + getID()));

        // Remove from cache
        ourContext.removeCached(this, getID());

        // Delete row
        DatabaseManager.delete(ourContext, profileRow);

    }

    /**
     * Create a new ePerson Profile, with a new ID. This method is not public, and
     * does not check authorisation.
     *
     * @param context DSpace context object
     *
     * @return the newly created profile
     * @throws java.sql.SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    static EPersonProfile create(Context context, int ePersonID, String profession, String affiliation, String websiteUrl,
                                 boolean isPublic) throws SQLException, AuthorizeException {

        EPersonProfile existingProfile = findEPersonsProfile(context, ePersonID);
        boolean profileExists = existingProfile != null;

        if (profileExists) {
            log.warn(LogManager.getHeader(context, "create_epersonprofile", "profile already exists for ePersonId=" + ePersonID));
            return existingProfile;
        }
        else {
            TableRow row = DatabaseManager.row("epersonprofile");
            row.setColumn("eperson_id", ePersonID);
            row.setColumn("profession", profession);
            row.setColumn("affiliation", affiliation);
            row.setColumn("website_url", websiteUrl);
            row.setColumn("is_public", isPublic);
            DatabaseManager.insert(context, row);

            EPersonProfile authorship = new EPersonProfile(context, row);

            log.info(LogManager.getHeader(context, "create_epersonprofile", "profile_id=" + row.getIntColumn("profile_id")));

            return authorship;
        }
    }

    static EPersonProfile create(Context context, int ePersonID, boolean isPublic) throws SQLException, AuthorizeException {
        return create(context, ePersonID, null, null, null, isPublic);
    }

    public static EPersonProfile update(Context context, int ePersonID, String profession, String affiliation, String websiteUrl,
                                        boolean isPublic) throws SQLException, AuthorizeException, IOException {

        EPersonProfile existingProfile = findEPersonsProfile(context, ePersonID);
        boolean profileExists = existingProfile != null;

        if (profileExists) {
            TableRow row = existingProfile.profileRow;
            row.setColumn("profession", profession);
            existingProfile.profession = profession;
            row.setColumn("affiliation", affiliation);
            existingProfile.affiliation = affiliation;
            row.setColumn("website_url", websiteUrl);
            existingProfile.websiteUrl = websiteUrl;
            row.setColumn("is_public", isPublic);
            existingProfile.isPublic = isPublic;
            existingProfile.update();
            return existingProfile;
        }
        else {
            return create(context, ePersonID, profession, affiliation, websiteUrl, isPublic);
        }
    }

    /**
     * Get an ePerson Profile from the database. Loads in the metadata
     *
     * @param context DSpace context object
     * @param id ID of the Profile
     *
     * @return the Profile, or null if the ID is invalid.
     * @throws java.sql.SQLException
     */
    public static EPersonProfile find(Context context, int id) throws SQLException {
        // First check the cache
        EPersonProfile fromCache = (EPersonProfile) context.fromCache(EPersonProfile.class, id);

        if (fromCache != null) {
            return fromCache;
        }

        TableRow row = DatabaseManager.find(context, "epersonprofile", id);

        if (row == null) {
            if (log.isDebugEnabled()) {
                log.debug(LogManager.getHeader(context, "find_epersonprofile", "not_found, profile_id=" + id));
            }
            return null;
        }

        // not null, return Profile
        if (log.isDebugEnabled()) {
            log.debug(LogManager.getHeader(context, "find_epersonprofile", "profile_id=" + id));
        }

        return new EPersonProfile(context, row);
    }

    /**
     * Get the profile for certain ePerson.
     *
     * @param context DSpace context object
     *
     * @return the profile
     * @throws java.sql.SQLException
     */
    public static EPersonProfile findEPersonsProfile(Context context, int ePersonID) throws SQLException {
        TableRow tr = DatabaseManager.querySingleTable(context, "epersonprofile", "SELECT * FROM epersonprofile WHERE eperson_id = ?", ePersonID);

        EPersonProfile profile = null;

        if (tr != null) {
            profile = new EPersonProfile(context, tr);
        }

        return profile;
    }

    public static EPersonProfile[] search(Context context, String query, int offset, int limit)
            throws SQLException
    {
        String[] queryTerms=query.toLowerCase().split(" ");
        String[][] queryTermsParams=new String[queryTerms.length][6];
        for (int i=0;i<queryTerms.length;i++)
        {
            String param = "%"+queryTerms[i].toLowerCase()+"%";
            queryTermsParams[i]=new String[]{param,param,param,param,param,param};
        }

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("SELECT * FROM epersonprofile INNER JOIN eperson on eperson.eperson_id=epersonprofile.eperson_id  WHERE  is_public='t' AND (eperson.eperson_id = ?");
        for (int i=0;i<queryTerms.length;i++) {
            queryBuf.append(" OR LOWER(firstname) LIKE LOWER(?) OR LOWER(lastname) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) OR LOWER(profession) LIKE LOWER(?) OR LOWER(affiliation) LIKE LOWER(?) OR eperson.eperson_id IN (SELECT eperson_id FROM academicinterest where LOWER(term) LIKE LOWER(?)) ");
        }
        queryBuf.append(") ");
        queryBuf.append(" ORDER BY lastname, firstname ASC ");

        // Add offset and limit restrictions - Oracle requires special code
        if ("oracle".equals(ConfigurationManager.getProperty("db.name")))
        {
            // First prepare the query to generate row numbers
            if (limit > 0 || offset > 0)
            {
                queryBuf.insert(0, "SELECT /*+ FIRST_ROWS(n) */ rec.*, ROWNUM rnum  FROM (");
                queryBuf.append(") ");
            }

            // Restrict the number of rows returned based on the limit
            if (limit > 0)
            {
                queryBuf.append("rec WHERE rownum<=? ");
                // If we also have an offset, then convert the limit into the maximum row number
                if (offset > 0)
                {
                    limit += offset;
                }
            }

            // Return only the records after the specified offset (row number)
            if (offset > 0)
            {
                queryBuf.insert(0, "SELECT * FROM (");
                queryBuf.append(") WHERE rnum>?");
            }
        }
        else
        {
            if (limit > 0)
            {
                queryBuf.append(" LIMIT ? ");
            }

            if (offset > 0)
            {
                queryBuf.append(" OFFSET ? ");
            }
        }

        String dbquery = queryBuf.toString();

        // When checking against the eperson-id, make sure the query can be made into a number
        Integer int_param;
        try {
            int_param = Integer.valueOf(query);
        }
        catch (NumberFormatException e) {
            int_param = Integer.valueOf(-1);
        }

        // Create the parameter array, including limit and offset if part of the query
        Object[] paramArr = new Object[] {};
        paramArr=ArrayUtils.add(paramArr,int_param);
        for (int i=0;i<queryTerms.length;i++)
        {
            paramArr=ArrayUtils.addAll(paramArr,queryTermsParams[i]);

        }

        if (limit > 0 && offset > 0)
        {
            paramArr = ArrayUtils.addAll(paramArr,new Object[]{limit, offset});
        }
        else if (limit > 0)
        {
            paramArr=ArrayUtils.add(paramArr,limit);
        }
        else if (offset > 0)
        {
            paramArr=ArrayUtils.add(paramArr,offset);
        }

        // Get all the epeople that match the query
        TableRowIterator rows = DatabaseManager.query(context,
                dbquery, paramArr);
        try
        {
            List<TableRow> epeopleRows = rows.toList();
            EPersonProfile[] epeople = new EPersonProfile[epeopleRows.size()];

            for (int i = 0; i < epeopleRows.size(); i++)
            {
                TableRow row = (TableRow) epeopleRows.get(i);

                // First check the cache
                EPersonProfile fromCache = (EPersonProfile) context.fromCache(EPersonProfile.class, row
                        .getIntColumn("eperson_id"));

                if (fromCache != null)
                {
                    epeople[i] = fromCache;
                }
                else
                {
                    epeople[i] = new EPersonProfile(context, row);
                }
            }

            return epeople;
        }
        finally
        {
            if (rows != null)
            {
                rows.close();
            }
        }
    }

    public static int searchResultCount(Context context, String query)
            throws SQLException
    {
        //String dbquery = "%"+query.toLowerCase()+"%";
        String[] queryTerms=query.toLowerCase().split(" ");
        String[][] queryTermsParams=new String[queryTerms.length][6];
        for (int i=0;i<queryTerms.length;i++)
        {
            String param = "%"+queryTerms[i].toLowerCase()+"%";
            queryTermsParams[i]=new String[]{param,param,param,param,param,param};
        }
        Long count;

        // When checking against the eperson-id, make sure the query can be made into a number
        Integer int_param;
        try {
            int_param = Integer.valueOf(query);
        }
        catch (NumberFormatException e) {
            int_param = Integer.valueOf(-1);
        }

        // Get all the epeople that match the query
        String queryBuf = "SELECT count(*) as epcount FROM epersonprofile, eperson WHERE eperson.eperson_id=epersonprofile.eperson_id  AND  is_public='t' AND (eperson.eperson_id = ? " ;
        for (int i=0;i<queryTerms.length;i++) {
            queryBuf+=" OR LOWER(firstname) LIKE LOWER(?) OR LOWER(lastname) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) OR LOWER(profession) LIKE LOWER(?) OR LOWER(affiliation) LIKE LOWER(?) OR eperson.eperson_id IN (SELECT eperson_id FROM academicinterest where LOWER(term) LIKE LOWER(?)) ";
        }
        queryBuf+=")";

        Object[] paramArr = new Object[] {};
        paramArr=ArrayUtils.add(paramArr,int_param);
        for (int i=0;i<queryTerms.length;i++)
        {
            paramArr=ArrayUtils.addAll(paramArr,queryTermsParams[i]);

        }
        TableRow row = DatabaseManager.querySingle(context,
                queryBuf,
                paramArr);

        // use getIntColumn for Oracle count data
        if ("oracle".equals(ConfigurationManager.getProperty("db.name")))
        {
            count = Long.valueOf(row.getIntColumn("epcount"));
        }
        else  //getLongColumn works for postgres
        {
            count = Long.valueOf(row.getLongColumn("epcount"));
        }

        return count.intValue();
    }


    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final EPersonProfile otherProfile = (EPersonProfile) other;

        if (this.getID() != otherProfile.getID()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.profileRow != null ? this.profileRow.hashCode() : 0);
        return hash;
    }

}
