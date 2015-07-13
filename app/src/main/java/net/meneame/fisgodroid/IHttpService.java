package net.meneame.fisgodroid;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface IHttpService
{
    public interface ProgressUpdater {
        public void progress(int byteCount);
    }
    
	public String get ( String uri );
	public boolean get ( String uri, OutputStream stream );
	public String post ( String uri, Map<String, Object> params );
	public String post ( String uri, Map<String, Object> params, ProgressUpdater updater );
	public boolean post ( String uri, Map<String, Object> params, OutputStream stream );
	public boolean post ( String uri, Map<String, Object> params, OutputStream stream, ProgressUpdater updater );
    public String postData ( String uri, InputStream data );
    public boolean postData ( String uri, InputStream data, OutputStream stream );
    public String postData ( String uri, InputStream data, ProgressUpdater updater );
    public boolean postData ( String uri, InputStream data, OutputStream stream, ProgressUpdater updater );
}
