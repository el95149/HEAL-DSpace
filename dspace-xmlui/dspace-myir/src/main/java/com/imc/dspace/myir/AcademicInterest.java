package com.imc.dspace.myir;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AcademicInterest {

    /** log4j category */
    private static Logger log = Logger.getLogger(AcademicInterest.class);

    /** Our context */
    private Context ourContext;

    /** The table row corresponding to this item */
    private TableRow academicInterestRow;

    /** The eperson */
    private EPerson eperson;



    private String term;

    /**
     * Construct an academic interest with the given table row
     *
     * @param context the context this object exists in
     * @param row the corresponding row in the table
     * @throws java.sql.SQLException
     */
    AcademicInterest(Context context, TableRow row) throws SQLException {
        ourContext = context;
        academicInterestRow = row;


        eperson = EPerson.find(ourContext, academicInterestRow.getIntColumn("eperson_id"));
        term = academicInterestRow.getStringColumn("term");

        // Cache ourselves
        context.cache(this, row.getIntColumn("academicinterest_id"));
    }

    public int getID() {
        return academicInterestRow.getIntColumn("academicinterest_id");
    }



    public EPerson getEperson() {
        return eperson;
    }

    public String getTerm() {
        return term;
    }

    /**
     * Update the academic interest metadata to the database. Inserts if this is a new academic interest.
     *
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public void update() throws SQLException, IOException, AuthorizeException {
        log.info(LogManager.getHeader(ourContext, "update_academicInterest", "academicinterest_id=" + getID()));
        DatabaseManager.update(ourContext, academicInterestRow);
    }

    public boolean canEdit() {
        return ourContext.getCurrentUser().equals(eperson);
    }

    /**
     * Delete the academic interest row.
     *
     * @throws java.sql.SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public void delete() throws SQLException, AuthorizeException {

        if (!AuthorizeManager.isAdmin(ourContext) &&
                ((ourContext.getCurrentUser() == null) ||
                        (ourContext.getCurrentUser().getID() != this.getEperson().getID())))
        {
            throw new AuthorizeException("Must be an administrator or the owner to remove ");
        }

        log.info(LogManager.getHeader(ourContext, "delete_academicInterest", "academicinterest_id=" + getID()));

        // Remove from cache
        ourContext.removeCached(this, getID());

        // Delete row
        DatabaseManager.delete(ourContext, academicInterestRow);

    }

    /**
     * Create a new academic interest, with a new ID. This method is not public, and
     * does not check authorisation.
     *
     * @param context DSpace context object
     *
     * @return the newly created academic interest
     * @throws java.sql.SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    static AcademicInterest create(Context context, int ePersonID, String term) throws SQLException, AuthorizeException {

        TableRow row = DatabaseManager.row("academicinterest");
        row.setColumn("eperson_id", ePersonID);
        row.setColumn("term", term);
        DatabaseManager.insert(context, row);

        AcademicInterest academicInterest = new AcademicInterest(context, row);

        //context.addEvent(new Event(Event.CREATE, Constants.COLLECTION, fav.getID(), null));

        log.info(LogManager.getHeader(context, "create_academicInterest", "academicinterest_id=" + row.getIntColumn("academicinterest_id")));

        return academicInterest;
    }


    /**
     * Get an academic interest from the database. Loads in the metadata
     *
     * @param context DSpace context object
     * @param id ID of the academic interest
     *
     * @return the academic interest, or null if the ID is invalid.
     * @throws java.sql.SQLException
     */
    public static AcademicInterest find(Context context, int id) throws SQLException {
        // First check the cache
        AcademicInterest fromCache = (AcademicInterest) context.fromCache(AcademicInterest.class, id);

        if (fromCache != null) {
            return fromCache;
        }

        TableRow row = DatabaseManager.find(context, "academicinterest", id);

        if (row == null) {
            if (log.isDebugEnabled()) {
                log.debug(LogManager.getHeader(context, "find_academicInterest", "not_found, academicInterest_id=" + id));
            }
            return null;
        }

        // not null, return academic interest
        if (log.isDebugEnabled()) {
            log.debug(LogManager.getHeader(context, "find_academicInterest", "academicInterest_id=" + id));
        }

        return new AcademicInterest(context, row);
    }

    /**
     * Get all academic interests in the system.
     *
     * @param context DSpace context object
     *
     * @return the academic interests in the system
     * @throws java.sql.SQLException
     */
    public static List<AcademicInterest> findAll(Context context) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "academicinterest", "SELECT * FROM academicinterest ORDER BY academicinterest_id");

        List<AcademicInterest> academicInterests = new ArrayList<AcademicInterest>();

        try {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                // First check the cache
                AcademicInterest fromCache = (AcademicInterest) context.fromCache(AcademicInterest.class, row.getIntColumn("academicinterest_id"));

                if (fromCache != null) {
                    academicInterests.add(fromCache);
                }
                else {
                    academicInterests.add(new AcademicInterest(context, row));
                }
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return academicInterests;
    }

    /**
     * Get all academic interests for certain ePerson.
     *
     * @param context DSpace context object
     *
     * @return the academic interests
     * @throws java.sql.SQLException
     */
    public static List<AcademicInterest> findByEPerson(Context context, int ePersonID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "academicinterest", "SELECT * FROM academicinterest WHERE eperson_id = ? ORDER BY academicinterest_id", ePersonID);

        List<AcademicInterest> academicInterests = new ArrayList<AcademicInterest>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();
                academicInterests.add(new AcademicInterest(context, row));
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return academicInterests;
    }



    /**
     * Counts academic interests
     *
     * @return  total items
     */
    public static int countAll() throws SQLException {
        int count = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT count(*) FROM academicinterest";
            statement = DatabaseManager.getConnection().prepareStatement(query);

            rs = statement.executeQuery();
            if (rs != null) {
                rs.next();
                count = rs.getInt(1);
            }
        }
        finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException sqle) { }
            }

            if (statement != null) {
                try { statement.close(); } catch (SQLException sqle) { }
            }
        }

        return count;
    }

    public static boolean addEPersonsInterest(Context context, int ePersonID, String term) throws SQLException, AuthorizeException {
        // create
        AcademicInterest.create(context, ePersonID, term);
        return true;
    }

    /**
     * Counts academicinterests by eperson
     *
     * @return  total items
     */
    public static int countByEPerson(int ePersonID) throws SQLException {
        int count = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT count(*) FROM academicinterest WHERE eperson_id = ?";

            statement = DatabaseManager.getConnection().prepareStatement(query);
            statement.setInt(1, ePersonID);

            rs = statement.executeQuery();
            if (rs != null) {
                rs.next();
                count = rs.getInt(1);
            }
        }
        finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException sqle) { }
            }

            if (statement != null) {
                try { statement.close(); } catch (SQLException sqle) { }
            }
        }

        return count;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final AcademicInterest otherAcademicInterest = (AcademicInterest) other;

        if (this.getID() != otherAcademicInterest.getID()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.academicInterestRow != null ? this.academicInterestRow.hashCode() : 0);
        return hash;
    }

}
