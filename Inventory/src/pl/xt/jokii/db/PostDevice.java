package pl.xt.jokii.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class PostDevice {
	
	private String destUrl;
	private List<NameValuePair> parameters;
	
	/**
	 * Public constructor
	 */
	public PostDevice()
	{
		this.destUrl 	= new String();
		this.parameters = new ArrayList<NameValuePair>();
	}	
	
	/**
	 * Public constructor
	 * @param destUrl 		- destination url
	 */
	public PostDevice(String destUrl)
	{
		this.destUrl 	= destUrl;
		this.parameters = new ArrayList<NameValuePair>();
	}	
	
	/**
	 * Public constructor
	 * @param destUrl 		- destination url
	 * @param parameters 	- list of pair of parameters <tag, value>
	 */
	public PostDevice(String destUrl, List<NameValuePair> parameters)
	{
		this.destUrl 	= destUrl;
		this.parameters = parameters;
	}

	/**
	 * Get destination url
	 * @return
	 */
	public String getDestUrl() {
		return destUrl;
	}

	/**
	 * Set destination url
	 * @param destUrl
	 */
	public void setDestUrl(String destUrl) {
		this.destUrl = destUrl;
	}

	/**
	 * Get POST parameters
	 * @return
	 */
	public List<NameValuePair> getParameters() {
		return parameters;
	}

	/**
	 * set POST parameters
	 * @param parameters
	 */
	public void setParameters(List<NameValuePair> parameters) {
		this.parameters = parameters;
		
	}
	
	/**
	 * Add parameter to list of parameters
	 * @param parameterPair - pair of POST parameters <tag, value>
	 */
	public void addParameter(BasicNameValuePair parameterPair)
	{
		this.parameters.add(parameterPair);	
	}
	
	/**
	 * Add parameter to list of parameters
	 * @param parameterPair - pair of POST parameters <tag, value>
	 */
	public void addParameter(String tag, String value)
	{
		this.parameters.add(new BasicNameValuePair(tag, value));
	}	

	/**
	* Send data via POST
	* @param v - related View
	*/ 
	public void send()
	{
		//final String url = "http://www.testeruploadu.w8w.pl/note/index.php?action=new";
		//Uri	uri	= Uri.parse("http://www.testeruploadu.w8w.pl/note/index.php?action=new");		
		if((parameters != null) && (destUrl != null) && (parameters.size() > 0) && (destUrl.length() > 0))
		{
			new Thread(new Runnable() 
			{
		
				public void run() 
				{
			
					try 
					{
						// If want make this not inside thread uncomment two line below, but it's not good solution
						//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
						//StrictMode.setThreadPolicy(policy);
					
						DefaultHttpClient httpClient = new DefaultHttpClient();
						HttpPost 		  httpPost   = new HttpPost(destUrl);
					
			//			List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
					
			//			parameters.add(new BasicNameValuePair("tekst", "a"));
			//			parameters.add(new BasicNameValuePair("nots_add", "tak"));
			//			parameters.add(new BasicNameValuePair("pas", "all"));	
					
						httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
						httpClient.execute(httpPost);
					
					}
					catch (Exception e) 
					{
						throw new RuntimeException("Nie udana próba wys³ania danych za pomoc¹ POST", e);
					}	
		
				}
			}).start();
		}
		else
		{
			Log.e("sendPostData ERROR", "Destination URL or/and parameters didn't set");
		}
	}
}
