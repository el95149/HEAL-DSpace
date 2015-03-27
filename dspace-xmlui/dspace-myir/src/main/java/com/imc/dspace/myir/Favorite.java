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
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;

public class Favorite {

    /** log4j category */
    private static Logger log = Logger.getLogger(Favorite.class);

    /** Our context */
    private Context ourContext;

    /** The table row corresponding to this item */
    private TableRow favoriteRow;

    /** The eperson */
    private EPerson eperson;

    /** The item */
    private Item item;

    private Date addDate;

    /**
     * Construct a favorite with the given table row
     *
     * @param context the context this object exists in
     * @param row the corresponding row in the table
     * @throws java.sql.SQLException
     */
    Favorite(Context context, TableRow row) throws SQLException {
        ourContext = context;
        favoriteRow = row;

        item = Item.find(ourContext, favoriteRow.getIntColumn("item_id"));
        eperson = EPerson.find(ourContext, favoriteRow.getIntColumn("eperson_id"));
        addDate = favoriteRow.getDateColumn("add_date");

        // Cache ourselves
        context.cache(this, row.getIntColumn("favorite_id"));
    }

    public int getID() {
        return favoriteRow.getIntColumn("favorite_id");
    }

    public Item getItem() {
        return item;
    }

    public EPerson getEperson() {
        return eperson;
    }

    public Date getAddDate() {
        return addDate;
    }

    /**
     * Update the favorite metadata to the database. Inserts if this is a new favorite.
     *
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    public void update() throws SQLException, IOException, AuthorizeException {
        log.info(LogManager.getHeader(ourContext, "update_favorite", "favorite_id=" + getID()));
        DatabaseManager.update(ourContext, favoriteRow);
    }

    public boolean canEdit() {
        return ourContext.getCurrentUser().equals(eperson);
    }

    /**
     * Delete the favorite.
     *
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void delete() throws SQLException, AuthorizeException {

        if (!AuthorizeManager.isAdmin(ourContext) &&
                ((ourContext.getCurrentUser() == null) ||
                        (ourContext.getCurrentUser().getID() != this.getEperson().getID())))
        {
            throw new AuthorizeException("Must be an administrator or the favorite owner to remove from favorites");
        }

        log.info(LogManager.getHeader(ourContext, "delete_favorite", "favorite_id=" + getID()));

        // Remove from cache
        ourContext.removeCached(this, getID());

        // Delete row
        DatabaseManager.delete(ourContext, favoriteRow);

    }

    /**
     * Create a new favorite, with a new ID. This method is not public, and
     * does not check authorisation.
     *
     * @param context DSpace context object
     *
     * @return the newly created favorite
     * @throws SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    static Favorite create(Context context, int ePersonID, int itemID) throws SQLException, AuthorizeException {

        TableRow row = DatabaseManager.row("favorite");
        row.setColumn("eperson_id", ePersonID);
        row.setColumn("item_id", itemID);
        row.setColumn("add_date", new Date());
        DatabaseManager.insert(context, row);

        Favorite fav = new Favorite(context, row);

        //context.addEvent(new Event(Event.CREATE, Constants.COLLECTION, fav.getID(), null));

        log.info(LogManager.getHeader(context, "create_favorite", "favorite_id=" + row.getIntColumn("favorite_id")));

        return fav;
    }

    /**
     * Get a favorite from the database. Loads in the metadata
     *
     * @param context DSpace context object
     * @param id ID of the favorite
     *
     * @return the favorite, or null if the ID is invalid.
     * @throws SQLException
     */
    public static Favorite find(Context context, int id) throws SQLException {
        // First check the cache
        Favorite fromCache = (Favorite) context.fromCache(Favorite.class, id);

        if (fromCache != null) {
            return fromCache;
        }

        TableRow row = DatabaseManager.find(context, "favorite", id);

        if (row == null) {
            if (log.isDebugEnabled()) {
                log.debug(LogManager.getHeader(context, "find_favorite", "not_found, favorite_id=" + id));
            }
            return null;
        }

        // not null, return Favorite
        if (log.isDebugEnabled()) {
            log.debug(LogManager.getHeader(context, "find_favorite", "favorite_id=" + id));
        }

        return new Favorite(context, row);
    }

    /**
     * Get all favorites in the system.
     *
     * @param context DSpace context object
     *
     * @return the favorites in the system
     * @throws SQLException
     */
    public static List<Favorite> findAll(Context context) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "favorite", "SELECT * FROM favorite ORDER BY add_date");

        List<Favorite> favorites = new ArrayList<Favorite>();

        try {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                // First check the cache
                Favorite fromCache = (Favorite) context.fromCache(Favorite.class, row.getIntColumn("favorite_id"));

                if (fromCache != null) {
                    favorites.add(fromCache);
                }
                else {
                    favorites.add(new Favorite(context, row));
                }
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return favorites;
    }

    /**
     * Get all favorites for certain ePerson.
     *
     * @param context DSpace context object
     *
     * @return the favorites
     * @throws SQLException
     */
    public static List<Favorite> findByEPerson(Context context, int ePersonID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "favorite", "SELECT * FROM favorite WHERE eperson_id = ? ORDER BY add_date", ePersonID);

        List<Favorite> favorites = new ArrayList<Favorite>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();
                favorites.add(new Favorite(context, row));
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return favorites;
    }

    private static List<Favorite> findEPersonsFavorite(Context context, int ePersonID, int itemID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "favorite", "SELECT * FROM favorite WHERE eperson_id = ? AND item_id = ?", ePersonID, itemID);
        List<Favorite> favorites = new ArrayList<Favorite>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();
                favorites.add(new Favorite(context, row));
            }
        }
        finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return favorites;
    }

    public static boolean checkEPersonsFavorite(Context context, int ePersonID, int itemID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context, "favorite", "SELECT * FROM favorite WHERE eperson_id = ? AND item_id = ?", ePersonID, itemID);
        return tri.hasNext();
    }

    public static boolean toggleEPersonsFavorite(Context context, int ePersonID, int itemID) throws SQLException, AuthorizeException {

        List<Favorite> favorites = findEPersonsFavorite(context, ePersonID, itemID);
        boolean created = false;

        if (favorites.size()==0) {
            // create
            Favorite.create(context, ePersonID, itemID);
            created = true;
        } else {
            // delete
            for (Favorite favorite : favorites) {
                favorite.delete();
            }
        }

        return created;
    }

    /**
     * Counts favorite items
     *
     * @return  total items
     */
    public static int countAll() throws SQLException {
        int count = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT count(*) FROM favorite";
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
     * Counts favorite items by eperson
     *
     * @return  total items
     */
    public static int countByEPerson(int ePersonID) throws SQLException {
        int count = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT count(*) FROM favorite WHERE eperson_id = ?";

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
        final Favorite otherFavorite = (Favorite) other;

        if (this.getID() != otherFavorite.getID()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.favoriteRow != null ? this.favoriteRow.hashCode() : 0);
        return hash;
    }

}
