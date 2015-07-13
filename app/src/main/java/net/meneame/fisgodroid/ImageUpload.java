package net.meneame.fisgodroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.view.View;

public class ImageUpload
{
    public interface Listener
    {
        public void onProgressUpdate(float progress);

        public void onFinished(String url);

        public void onStart();
    }

    private static class Task extends AsyncTask<Bitmap, Integer, String>
    {
        private int mTotalBytes = 0;
        private FisgoService.FisgoBinder mFisgoBinder;
        private Listener mListener;

        public Task(FisgoService.FisgoBinder binder, Listener listener)
        {
            mFisgoBinder = binder;
            updateListener(listener);
        }

        @Override
        protected String doInBackground(Bitmap... arg0)
        {
            Bitmap bmp = arg0[0];

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(CompressFormat.JPEG, 90, stream);

            byte[] array = stream.toByteArray();
            mTotalBytes = array.length;
            ByteArrayInputStream is = new ByteArrayInputStream(array);
            return mFisgoBinder.sendPicture(is, new IHttpService.ProgressUpdater()
            {
                @Override
                public void progress(int byteCount)
                {
                    if ( mListener != null )
                    {
                        publishProgress(byteCount);
                    }
                }
            });
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {
            if ( mListener != null )
            {
                int bytes = progress[0];
                mListener.onProgressUpdate((float) bytes / mTotalBytes);
            }
        }

        @Override
        protected void onPostExecute(String pictureUrl)
        {
            msTask = null;
            if ( mListener != null )
            {
                mListener.onFinished(pictureUrl);
            }
        }

        public void updateListener(Listener listener)
        {
            mListener = listener;
            if ( mListener != null )
            {
                mListener.onStart();
            }
        }
    }

    private static Task msTask = null;

    public static void upload(FisgoService.FisgoBinder binder, Bitmap bitmap, Listener listener)
    {
        if ( msTask == null )
        {
            msTask = new Task(binder, listener);
            msTask.execute(bitmap);
        }
    }

    public static void updateListener(Listener listener)
    {
        if ( msTask != null )
        {
            msTask.updateListener(listener);
        }
    }
}
