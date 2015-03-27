/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.imc.dspace.app.xmlui.aspect.myir;

import com.imc.dspace.myir.Favorite;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.core.Context;

import java.util.Map;

public class RemoveFavoritesAction extends AbstractAction
{

    /**
     * Remove all selected favorite items
     * 
     * @param redirector
     * @param resolver
     * @param objectModel Cocoon's object model
     * @param source
     * @param parameters
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        
        Context context = ContextUtil.obtainContext(objectModel);
        Request request = ObjectModelHelper.getRequest(objectModel);
        
    	String[] favoriteIDs = request.getParameterValues("favoriteID");
    	
    	if (favoriteIDs != null)
    	{
        	for (String favoriteID : favoriteIDs)
        	{
        		Favorite favorite = Favorite.find(context, Integer.valueOf(favoriteID));
    			favorite.delete();
        	}
        	context.commit();
    	}
    
        return null;
    }

}
