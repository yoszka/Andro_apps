package pl.xt.jokii.pushnotifications.server.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import pl.xt.jokii.pushnotifications.server.model.User;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

public class UsersGetter extends StreamTemplate{
	private static final String USERS_PAGE_URL = "http://www.pinnote.zz.mu/list_all.php";
	private User[] mUsers;
	private static OnUsersGetListener mListener;
	
	/**
	 * @hide
	 */
	@Override
	public InputStream createInputStream() throws Exception {
		return new URL(USERS_PAGE_URL).openStream();
	}
	
	/**
	 * @hide
	 */
	@Override
	public void useInputStream(InputStream is) throws Exception {
		Reader r = new InputStreamReader(is);
		Gson gson = new Gson();
		try{
			mUsers = gson.fromJson(r, User[].class);
		}catch(IllegalStateException ise){
			Log.e("UsersGetter", "IllegalStateException on parsing users");
			ise.printStackTrace();
		}
	}

	/**
	 * Synchronous users loader
	 * @return
	 */
	private void loadUsers(){
		execute();
	}
	
	/**
	 * Users getter
	 * @return
	 */
	private User[] getUsers() {
		return mUsers;
	}
	
	/**
	 * Asynchronous users getter
	 * @param listener
	 */
	public static void getUsers(OnUsersGetListener listener){
		if(listener == null){
			throw new NullPointerException("OnUsersGetListener - listener cannot be null.");
		}
		mListener = listener;
		new UsersAsyncGetter().execute();
	}
	
	/**
	 * Auxiliary class to perform background operation
	 * @author Tomek
	 *
	 */
	private static class UsersAsyncGetter extends AsyncTask<String, Void, User[]>{
		@Override
		protected User[] doInBackground(String... params) {
			UsersGetter ug = new UsersGetter();
			ug.loadUsers();
			return ug.getUsers();
		}
		@Override
		protected void onPostExecute(User[] users) {
			super.onPostExecute(users);
			if(mListener != null){
				mListener.onUsersGet(users);
			}
		}
	}
}
