package net.meneame.fisgodroid;

import java.util.Map;

public interface IHttpService
{
	public String get ( String uri );
	public String post ( String uri, Map<String, Object> params );
}
