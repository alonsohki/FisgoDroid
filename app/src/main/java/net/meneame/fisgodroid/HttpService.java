package net.meneame.fisgodroid;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class HttpService implements IHttpService
{
    private HttpClient mClient;
    private HttpContext mHttpContext = new BasicHttpContext();

    public HttpService() {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.DEFAULT_CONTENT_CHARSET);
        params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30 * 1000);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30 * 1000);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
        
        mClient = new DefaultHttpClient(conMgr, params);
    }
    
    @Override
    public String get(String uri)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        performRequest(new HttpGet(uri), os);
        return new String(os.toByteArray());
    }

    @Override
    public boolean get(String uri, OutputStream os)
    {
        return performRequest(new HttpGet(uri), os);
    }

    private HttpPost buildPostRequest(String uri, Map<String, Object> params, final ProgressUpdater updater)
    {
        HttpPost req = new HttpPost(uri);

        // Set the POST parameters in the HTTP request
        if ( params.size() > 0 )
        {
            // We need to transform the Map given in the parameter to the
            // implementation-specific
            // list of NameValuePair elements.
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet())
            {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }

            try
            {
                req.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8")
                {
                    @Override
                    public void writeTo(final OutputStream outstream) throws IOException
                    {
                        super.writeTo(getProgressUpdatingStream(outstream, updater));
                    }
                });
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        return req;
    }

    @Override
    public String post(String uri, Map<String, Object> params)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        performRequest(buildPostRequest(uri, params, null), os);
        return new String(os.toByteArray());
    }

    @Override
    public String post(String uri, Map<String, Object> params, ProgressUpdater updater)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        performRequest(buildPostRequest(uri, params, updater), os);
        return new String(os.toByteArray());
    }

    @Override
    public boolean post(String uri, Map<String, Object> params, OutputStream os)
    {
        return performRequest(buildPostRequest(uri, params, null), os);
    }

    @Override
    public boolean post(String uri, Map<String, Object> params, OutputStream os, ProgressUpdater updater)
    {
        return performRequest(buildPostRequest(uri, params, updater), os);
    }

    public boolean performRequest(HttpUriRequest req, OutputStream out)
    {
        try
        {
            mClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

            // Perform the request
            HttpResponse response = mClient.execute(req, mHttpContext);

            // Get the response data and transform it into a String
            if ( out != null )
            {
                InputStream content = response.getEntity().getContent();
                byte[] buffer = new byte[512];
                int bytesRead;
                while ((bytesRead = content.read(buffer)) != -1)
                {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return true;
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private HttpUriRequest buildPostDataRequest(String uri, InputStream data, final ProgressUpdater updater)
    {
        HttpPost req = new HttpPost(uri);

        // Dump the input data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] temp = new byte[512];
        int length;
        try
        {
            while ((length = data.read(temp)) > 0)
                bos.write(temp, 0, length);

            byte[] byteArray = bos.toByteArray();
            req.setEntity(new ByteArrayEntity(byteArray)
            {
                @Override
                public void writeTo(final OutputStream outstream) throws IOException
                {
                    super.writeTo(getProgressUpdatingStream(outstream, updater));
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return req;
    }

    @Override
    public String postData(String uri, InputStream data)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        performRequest(buildPostDataRequest(uri, data, null), os);
        return new String(os.toByteArray());
    }

    @Override
    public boolean postData(String uri, InputStream data, OutputStream stream)
    {
        return performRequest(buildPostDataRequest(uri, data, null), stream);
    }

    @Override
    public String postData(String uri, InputStream data, ProgressUpdater updater)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        performRequest(buildPostDataRequest(uri, data, updater), os);
        return new String(os.toByteArray());
    }

    @Override
    public boolean postData(String uri, InputStream data, OutputStream stream, ProgressUpdater updater)
    {
        return performRequest(buildPostDataRequest(uri, data, updater), stream);
    }

    private FilterOutputStream getProgressUpdatingStream(OutputStream outstream, final ProgressUpdater updater)
    {
        return new FilterOutputStream(outstream)
        {
            private int mBytes = 0;

            private void report(int bytes)
            {
                mBytes += bytes;
                if ( updater != null )
                {
                    updater.progress(mBytes);
                }
            }

            @Override
            public void write(int b) throws IOException
            {
                out.write(b);
                report(1);
            }

            @Override
            public void write(byte[] b) throws IOException
            {
                write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException
            {
                // Write in chunks
                int CHUNK_SIZE = 64 * 1024;
                for (int n = 0; n < len; n += CHUNK_SIZE)
                {
                    int curLen = Math.min(CHUNK_SIZE, len - n);
                    out.write(b, n + off, curLen);
                    report(curLen);
                }
            }
        };
    }
}
