package pl.xt.jokii.pushnotifications.server.util;

import pl.xt.jokii.pushnotifications.server.model.LocationDb;

public interface OnLocationsGetListener {
	
    /**
     * Trigger when array of {@link LocationDb}s were get from server
     * @param locations array
     */
    public void onLocationsGet(LocationDb[] locations);
}
