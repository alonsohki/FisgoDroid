package net.meneame.fisgodroid;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface IHttpService
{
	public String get ( String uri );
	public boolean get ( String uri, OutputStream stream );
	public String post ( String uri, Map<String, Object> params );
	public boolean post ( String uri, Map<String, Object> params, OutputStream stream );
	public String postData ( String uri, InputStream data );
	public boolean postData ( String uri, InputStream data, OutputStream stream );
}
