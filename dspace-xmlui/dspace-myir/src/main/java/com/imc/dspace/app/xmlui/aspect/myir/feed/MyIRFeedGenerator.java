package com.imc.dspace.app.xmlui.aspect.myir.feed;

import com.sun.syndication.io.FeedException;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.util.HashUtil;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;
import org.dspace.app.util.SyndicationFeed;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.FeedUtils;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.browse.BrowseEngine;
import org.dspace.browse.BrowseException;
import org.dspace.browse.BrowseIndex;
import org.dspace.browse.BrowserScope;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoverySortConfiguration;
import org.dspace.discovery.configuration.DiscoverySortFieldConfiguration;
import org.dspace.eperson.Group;

import org.dspace.sort.SortException;
import org.dspace.sort.SortOption;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MyIRFeedGenerator extends AbstractGenerator
        implements Configurable, CacheableProcessingComponent, Recyclable
{
    private static final Logger log = Logger.getLogger(MyIRFeedGenerator.class);

    /** The feed's requested format */
    private String format = null;

    /** The feed's scope, null if no scope */
    private String keyword = null;

    /** number of DSpace items per feed */
    private static final int ITEM_COUNT = ConfigurationManager.getIntProperty("webui.feed.items");

    /**
     * How long should RSS feed cache entries be valid? milliseconds * seconds *
     * minutes * hours default to 24 hours if config parameter is not present or
     * wrong
     */
    private static final long CACHE_AGE;
    static
    {
        final String ageCfgName = "webui.feed.cache.age";
        final long ageCfg = ConfigurationManager.getIntProperty(ageCfgName, 24);
        CACHE_AGE = 1000 * 60 * 60 * ageCfg;
    }

    /** configuration option to include Item which does not have READ by Anonymous enabled **/
    private static boolean includeRestrictedItems = ConfigurationManager.getBooleanProperty("harvest.includerestricted.rss", true);


    /** Cache of this object's validitity */
    private DSpaceValidity validity = null;

    /** The cache of recently submitted items */
    private Item recentSubmissionItems[];

    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey()
    {
        String key = "key:" + this.keyword + ":" + this.format;
        return HashUtil.hash(key);
    }

    /**
     * Generate the cache validity object.
     *
     * The validity object will include the collection being viewed and
     * all recently submitted items. This does not include the community / collection
     * hierarch, when this changes they will not be reflected in the cache.
     */
    public SourceValidity getValidity()
    {
        if (this.validity == null)
        {
            try
            {
                DSpaceValidity validity = new FeedValidity();

                Context context = ContextUtil.obtainContext(objectModel);

                //DSpaceObject dso = null;

                //if (handle != null && !handle.contains("site"))
                //{
                //dso = HandleManager.resolveToObject(context, handle);
                //}

                //validity.add(dso);

                // add reciently submitted items
                for(Item item : performSearch(context))
                {
                    validity.add(item);
                }

                this.validity = validity.complete();
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                return null;
            }
        }
        return this.validity;
    }



    /**
     * Setup component wide configuration
     */
    public void configure(Configuration conf) throws ConfigurationException
    {
    }


    /**
     * Setup configuration for this request
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par) throws ProcessingException, SAXException,
            IOException
    {
        super.setup(resolver, objectModel, src, par);

        this.format = par.getParameter("feedFormat", null);
        if (!(this.format.equals("rss_1.0")||this.format.equals("rss_2.0")||this.format.equals("atom_1.0")))
        {
            this.format="rss_2.0";
        }
        this.keyword = par.getParameter("keyword",null);
    }


    /**
     * Generate the syndication feed.
     */
    public void generate() throws IOException, SAXException, ProcessingException
    {
        try
        {
            Context context = ContextUtil.obtainContext(objectModel);
            DSpaceObject dso = null;

            Item[] feeditems=new Item[]{};
            try {
                feeditems=performSearch(context);
            } catch (UIException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SearchServiceException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SortException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            SyndicationFeed feed = new SyndicationFeed(SyndicationFeed.UITYPE_XMLUI);
            feed.populate(ObjectModelHelper.getRequest(objectModel),
                    dso,feeditems , FeedUtils.i18nLabels);
            feed.setType(this.format);
            Document dom = feed.outputW3CDom();
            FeedUtils.unmangleI18N(dom);
            DOMStreamer streamer = new DOMStreamer(contentHandler, lexicalHandler);
            streamer.stream(dom);
        }
        catch (IllegalArgumentException iae)
        {
            throw new ResourceNotFoundException("Syndication feed format, '"+this.format+"', is not supported.", iae);
        }
        catch (FeedException fe)
        {
            throw new SAXException(fe);
        }
        catch (SQLException sqle)
        {
            throw new SAXException(sqle);
        }
    }

    /**
     * @return recently submitted Items within the indicated scope
     */
    @SuppressWarnings("unchecked")
    private Item[] getRecentlySubmittedItems(Context context, DSpaceObject dso)
            throws SQLException
    {
        if (recentSubmissionItems != null)
        {
            return recentSubmissionItems;
        }

        String source = ConfigurationManager.getProperty("recent.submissions.sort-option");
        BrowserScope scope = new BrowserScope(context);
        if (dso instanceof Collection)
        {
            scope.setCollection((Collection) dso);
        }
        else if (dso instanceof Community)
        {
            scope.setCommunity((Community) dso);
        }
        scope.setResultsPerPage(ITEM_COUNT);

        // FIXME Exception handling
        try
        {
            scope.setBrowseIndex(BrowseIndex.getItemBrowseIndex());
            for (SortOption so : SortOption.getSortOptions())
            {
                if (so.getName().equals(source))
                {
                    scope.setSortBy(so.getNumber());
                    scope.setOrder(SortOption.DESCENDING);
                }
            }

            BrowseEngine be = new BrowseEngine(context);
            this.recentSubmissionItems = be.browseMini(scope).getItemResults(context);

            // filter out Items taht are not world-readable
            if (!includeRestrictedItems)
            {
                List<Item> result = new ArrayList<Item>();
                for (Item item : this.recentSubmissionItems)
                {
                    checkAccess:
                    for (Group group : AuthorizeManager.getAuthorizedGroups(context, item, Constants.READ))
                    {
                        if ((group.getID() == 0))
                        {
                            result.add(item);
                            break checkAccess;
                        }
                    }
                }
                this.recentSubmissionItems = result.toArray(new Item[result.size()]);
            }
        }
        catch (BrowseException bex)
        {
            log.error("Caught browse exception", bex);
        }
        catch (SortException e)
        {
            log.error("Caught sort exception", e);
        }
        return this.recentSubmissionItems;
    }


    protected DiscoverQuery queryArgs;

    public Item[] performSearch(Context context) throws UIException, SearchServiceException, SQLException, SortException {

        log.error("performing s3arch");
        Item[] results= new Item[]{};
        if (this.recentSubmissionItems != null)
        {
            return results;
        }


        String query = this.keyword;

        this.queryArgs = new DiscoverQuery();

        //Add the configured default filter queries
        DiscoveryConfiguration discoveryConfiguration = SearchUtils.getDiscoveryConfiguration(null);
        List<String> defaultFilterQueries = discoveryConfiguration.getDefaultFilterQueries();
        queryArgs.addFilterQueries(defaultFilterQueries.toArray(new String[defaultFilterQueries.size()]));



        queryArgs.setMaxResults(ITEM_COUNT);

        String sortBy = "dc.date.issued_dt";
       
        /*for (SortOption so : SortOption.getSortOptions())
        {
            sortBy=so.getName();
            log.error("sorting by "+so.getName());
        }*/

        //if(sortBy == null){
            //Attempt to find the default one, if none found we use SCORE
           // sortBy = "score";

       // }

        queryArgs.setSortField(sortBy, DiscoverQuery.SORT_ORDER.desc);


        queryArgs.setQuery(query != null && !query.trim().equals("") ? query : null);

        // Use mlt
        // queryArgs.add("mlt", "true");

        // The fields to use for similarity. NOTE: if possible, these should have a stored TermVector
        // queryArgs.add("mlt.fl", "author");

        // Minimum Term Frequency - the frequency below which terms will be ignored in the source doc.
        // queryArgs.add("mlt.mintf", "1");

        // Minimum Document Frequency - the frequency at which words will be ignored which do not occur in at least this many docs.
        // queryArgs.add("mlt.mindf", "1");

        //queryArgs.add("mlt.q", "");

        // mlt.minwl
        // minimum word length below which words will be ignored.

        // mlt.maxwl
        // maximum word length above which words will be ignored.

        // mlt.maxqt
        // maximum number of query terms that will be included in any generated query.

        // mlt.maxntp
        // maximum number of tokens to parse in each example doc field that is not stored with TermVector support.

        // mlt.boost
        // [true/false] set if the query will be boosted by the interesting term relevance.

        // mlt.qf
        // Query fields and their boosts using the same format as that used in DisMaxRequestHandler. These fields must also be specified in mlt.fl.


        //filePost.addParameter("fl", "handle, "search.resourcetype")");
        //filePost.addParameter("field", "search.resourcetype");

        //Set the default limit to 11
        /*
        ClientUtils.escapeQueryChars(location)
        //f.category.facet.limit=5

        for(Enumeration en = request.getParameterNames(); en.hasMoreElements();)
        {
            String key = (String)en.nextElement();
            if(key.endsWith(".facet.limit"))
            {
                filePost.addParameter(key, request.getParameter(key));
            }
        }
        */
        //Context context = ContextUtil.obtainContext(objectModel);

        DiscoverResult queryResults = SearchUtils.getSearchService().search(context, null, queryArgs);
        List<DSpaceObject> objects=queryResults.getDspaceObjects();
        Item[] items=new Item[objects.size()];
        for (int i=0;i<objects.size();i++)
        {
           items[i]=(Item)objects.get(i);
        }
        return items;
    }

    /**
     * Recycle
     */

    public void recycle()
    {
        this.format = null;
        this.keyword = null;
        this.validity = null;
        this.recentSubmissionItems = null;
        super.recycle();
    }

    /**
     * Extend the standard DSpaceValidity object to support assumed
     * caching. Since feeds will constantly be requested we want to
     * assume that a feed is still valid instead of checking it
     * against the database anew everytime.
     *
     * This validity object will assume that a cache is still valid,
     * without rechecking it, for 24 hours.
     *
     */
    private static class FeedValidity extends DSpaceValidity
    {
        private static final long serialVersionUID = 1L;

        /** When the cache's validity expires */
        private long expires = 0;

        /**
         * When the validity is completed record a timestamp to check later.
         */
        public DSpaceValidity complete()
        {
            this.expires = System.currentTimeMillis() + CACHE_AGE;

            return super.complete();
        }


        /**
         * Determine if the cache is still valid
         */
        public int isValid()
        {
            // Return true if we have a hash.
            if (this.completed)
            {
                if (System.currentTimeMillis() < this.expires)
                {
                    // If the cache hasn't expired the just assume that it is still valid.
                    return SourceValidity.VALID;
                }
                else
                {
                    // The cache is past its age
                    return SourceValidity.UNKNOWN;
                }
            }
            else
            {
                // This is an error, state. We are being asked whether we are valid before
                // we have been initialized.
                return SourceValidity.INVALID;
            }
        }

        /**
         * Determine if the cache is still valid based
         * upon the other validity object.
         *

         *          The other validity object.
         */
        public int isValid(SourceValidity otherValidity)
        {
            if (this.completed && otherValidity instanceof FeedValidity)
            {
                FeedValidity other = (FeedValidity) otherValidity;
                if (hash == other.hash)
                {
                    // Update both cache's expiration time.
                    this.expires = System.currentTimeMillis() + CACHE_AGE;
                    other.expires = System.currentTimeMillis() + CACHE_AGE;

                    return SourceValidity.VALID;
                }
            }

            return SourceValidity.INVALID;
        }

    }
}