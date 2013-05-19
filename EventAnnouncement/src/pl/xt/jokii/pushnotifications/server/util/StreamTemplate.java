package pl.xt.jokii.pushnotifications.server.util;

import java.io.InputStream;

abstract public class StreamTemplate {
	
	public abstract void useInputStream(InputStream is) throws Exception;	
	public abstract InputStream createInputStream() throws Exception ;	
	
	public void execute() {
		InputStream is = null;
		try {
			
			is = createInputStream();
			
			useInputStream(is);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}
	
	
	

}
