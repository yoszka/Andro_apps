package pl.xt.jokii.pushnotifications.server.util;

import pl.xt.jokii.pushnotifications.server.model.User;

public interface OnUsersGetListener {

    /**
     * Trigger when array of {@link User}s were get from server
     * @param users array
     */
    public void onUsersGet(User[] users);
}
