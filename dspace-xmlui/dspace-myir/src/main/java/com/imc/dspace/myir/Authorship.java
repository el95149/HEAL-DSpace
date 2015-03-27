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

public class Authorship {

    /** log4j category */
    private static Logger log = Logger.getLogger(Authorship.class);

    /** Our context */
    private Context ourContext;

    /** The table row corresponding to this item */
    private TableRow authorshipRow;

    /** The eperson */
    private EPerson eperson;

    /** The item */
    private Item item;

    private String attribute;

    /**
     * Construct an authorship with the given table row
     *
     * @param context the context this object exists in
     * @param row the corresponding row in the table
     * @throws java.sql.SQLException
     */
    Authorship(Context context, TableRow row) throws SQLException {
        ourContext = context;
        authorshipRow = row;

        item = Item.find(ourContext, authorshipRow.getIntColumn("item_id"));
        eperson = EPerson.find(ourContext, authorshipRow.getIntColumn("eperson_id"));
        attribute = authorshipRow.getStringColumn("attr");

        // Cache ourselves
        context.cache(this, row.getIntColumn("authorship_id"));
    }

    public int getID() {
        return authorshipRow.getIntColumn("authorship_id");
    }

    public Item getItem() {
        return item;
    }

    public EPerson getEperson() {
        return eperson;
    }

    public String getAttribute() {
        return attribute;
    }

    /**
     * Update the authorship metadata to the database. Inserts if this is a new authorship.
     *
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public void update() throws SQLException, IOException, AuthorizeException {
        log.info(LogManager.getHeader(ourContext, "update_authorship", "authorship_id=" + getID()));
        DatabaseManager.update(ourContext, authorshipRow);
    }

    public boolean canEdit() {
        return ourContext.getCurrentUser().equals(eperson);
    }

    /**
     * Delete the authorship row.
     *
     * @throws java.sql.SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public void delete() throws SQLException, AuthorizeException {

        if (!AuthorizeManager.isAdmin(ourContext) &&
                ((ourContext.getCurrentUser() == null) ||
                        (ourContext.getCurrentUser().getID() != this.getEperson().getID())))
        {
            throw new AuthorizeException("Must be an administrator or the owner to remove from authorship");
        }

        log.info(LogManager.getHeader(ourContext, "delete_authorship", "authorship_id=" + getID()));

        // Remove from cache
        ourContext.removeCached(this, getID());

        // Delete row
        DatabaseManager.delete(ourContext, authorshipRow);

    }

    /**
     * Create a new authorship, with a new ID. This method is not public, and
     * does not check authorisation.
     *
     * @param context DSpace context object
     *
     * @return the newly created authorship
     * @throws java.sql.SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    static Authorship create(Context context, int ePersonID, int itemID, String attribute) throws SQLException, AuthorizeException {

        TableRow row = DatabaseManager.row("authorship");
        row.setColumn("eperson_id", ePersonID);
        row.setColumn("item_id", itemID);
        row.setColumn("attr", attribute);
        DatabaseManager.insert(context, row);

        Authorship authorship = new Authorship(context, row);

        //context.addEvent(new Event(Event.CREATE, Constants.COLLECTION, fav.getID(), null));

        log.info(LogManager.getHeader(context, "create_authorship", "authorship_id=" + row.getIntColumn("authorship_id")));

        return authorship;
    }

    static Authorship create(Context context, int ePersonID, int itemID) throws SQLException, AuthorizeException {
        return create(context, ePersonID, itemID, null);
    }

    /**
     * Get an authorship from the database. Loads in the metadata
     *
     * @param context DSpace context object
     * @param id ID of the authorship
     *
     * @return the authorship, or null if the ID is invalid.
     * @throws java.sql.SQLException
     */
    public static Authorship find(Context context, int id) throws SQLException {
        // First check the cache
        Authorship fromCache = (Authorship) context.fromCache(Authorship.class, id);

        if (fromCache != null) {
            return fromCache;
        }

        TableRow row = DatabaseManager.find(context, "authorship", id);

        if (row == null) {
            if (log.isDebugEnabled()) {
                log.debug(LogManager.getHeader(context, "find_authorship", "not_found, authorship_id=" + id));
            }
            return null;
        }

        // not null, return Authorship
        if (log.isDebugEnabled()) {
            log.debug(LogManager.getHeader(context, "find_authorship", "authorship_id=" + id));
        }

        return new Authorship(context, row);
    }

    /**
     * Get all authorships in the system.
     *
     * @param context DSpace context object
     *
     * @return the authorships in the system
     * @throws java.sql.SQLException
     */
    public static List<Authorship> findAll(Context context) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "authorship", "SELECT * FROM authorship ORDER BY authorship_id");

        List<Authorship> authorships = new ArrayList<Authorship>();

        try {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                // First check the cache
                Authorship fromCache = (Authorship) context.fromCache(Authorship.class, row.getIntColumn("authorship_id"));

                if (fromCache != null) {
                    authorships.add(fromCache);
                }
                else {
                    authorships.add(new Authorship(context, row));
                }
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return authorships;
    }

    /**
     * Get all authorships for certain ePerson.
     *
     * @param context DSpace context object
     *
     * @return the authorships
     * @throws java.sql.SQLException
     */
    public static List<Authorship> findByEPerson(Context context, int ePersonID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "authorship", "SELECT * FROM authorship WHERE eperson_id = ? ORDER BY authorship_id", ePersonID);

        List<Authorship> authorships = new ArrayList<Authorship>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();
                authorships.add(new Authorship(context, row));
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return authorships;
    }

    private static List<Authorship> findEPersonsAuthorship(Context context, int ePersonID, int itemID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "authorship", "SELECT * FROM authorship WHERE eperson_id = ? AND item_id = ?", ePersonID, itemID);
        List<Authorship> authorships = new ArrayList<Authorship>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();
                authorships.add(new Authorship(context, row));
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return authorships;
    }

    public static boolean checkEPersonsAuthorship(Context context, int ePersonID, int itemID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "authorship", "SELECT * FROM authorship WHERE eperson_id = ? AND item_id = ?", ePersonID, itemID);
        return tri.hasNext();
    }

    public static boolean toggleEPersonsAuthorship(Context context, int ePersonID, int itemID) throws SQLException, AuthorizeException {

        List<Authorship> authorships = findEPersonsAuthorship(context, ePersonID, itemID);
        boolean created = false;

        if (authorships.size()==0) {
            // create
            Authorship.create(context, ePersonID, itemID);
            created = true;
        } else {
            // delete
            for (Authorship authorship : authorships) {
                authorship.delete();
            }
        }

        return created;
    }

    /**
     * Counts authorships
     *
     * @return  total items
     */
    public static int countAll() throws SQLException {
        int count = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT count(*) FROM authorship";
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

    /**
     * Counts authorships by eperson
     *
     * @return  total items
     */
    public static int countByEPerson(int ePersonID) throws SQLException {
        int count = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT count(*) FROM authorship WHERE eperson_id = ?";

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
        final Authorship otherAuthorship = (Authorship) other;

        if (this.getID() != otherAuthorship.getID()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.authorshipRow != null ? this.authorshipRow.hashCode() : 0);
        return hash;
    }

}
