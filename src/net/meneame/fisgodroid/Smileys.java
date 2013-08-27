package net.meneame.fisgodroid;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;

public class Smileys
{
    public static final String URI_SCHEME = "smiley://";
    
    private static final Map<String, Integer> msResources = new HashMap<String, Integer>();
    
    private static void initializeResources ()
    {
        // Initialize the resource map
        if ( msResources.size() == 0 )
        {
            msResources.put("angry", R.drawable.angry);
            msResources.put("blank", R.drawable.blank);
            msResources.put("cheesy", R.drawable.cheesy);
            msResources.put("confused", R.drawable.confused);
            msResources.put("cool", R.drawable.cool);
            msResources.put("cry", R.drawable.cry);
            msResources.put("oops", R.drawable.embarassed);
            msResources.put("ffu", R.drawable.fu);
            msResources.put("goatse", R.drawable.goat);
            msResources.put("grin", R.drawable.grin);
            msResources.put("hug", R.drawable.hug);
            msResources.put("huh", R.drawable.huh);
            msResources.put("kiss", R.drawable.kiss);
            msResources.put("lol", R.drawable.laugh);
            msResources.put("lipssealed", R.drawable.lipsrsealed);
            msResources.put("palm", R.drawable.palm);
            msResources.put("roll", R.drawable.rolleyes);
            msResources.put("sad", R.drawable.sad);
            msResources.put("shame", R.drawable.shame);
            msResources.put("shit", R.drawable.shit);
            msResources.put("shocked", R.drawable.shocked);
            msResources.put("smiley", R.drawable.smiley);
            msResources.put("tongue", R.drawable.tongue);
            msResources.put("troll", R.drawable.trollface2);
            msResources.put("undecided", R.drawable.undecided);
            msResources.put("wall", R.drawable.wall);
            msResources.put("wink", R.drawable.wink);
            msResources.put("wow", R.drawable.wow);
        }
    }
    
    public static String parseMessage ( String message )
    {
        initializeResources();
        
        // Search smileys in the string and replace them by an <img> tag
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        int cur;
        
        while ( (cur = message.indexOf("{", pos)) > -1 )
        {
            int end = message.indexOf("}", cur);
            if ( end == -1 )
            {
                builder.append(message.substring(pos, cur+1));
                pos = cur + 1;
            }
            else
            {
                String smileyName = message.substring(cur+1, end);
                if ( msResources.containsKey(smileyName) == false )
                {
                    builder.append(message.substring(pos, end+1));
                    pos = end + 1;
                }
                else
                {
                    builder.append(message.substring(pos, cur));
                    builder.append("<img width=\"32\" height=\"32\" src=\"" + URI_SCHEME + smileyName + "\" /> ");
                    pos = end + 1;
                }
            }
        }
        builder.append(message.substring(pos));
        return builder.toString();
    }
    
    public static Html.ImageGetter getImageGetter ( final Context context )
    {
        initializeResources();

        return new Html.ImageGetter ()
        {
            @Override
            public Drawable getDrawable(String source)
            {
                AnimatedGIFDrawable drawable = null;
                if ( source.startsWith(URI_SCHEME) )
                {
                    String smileyName = source.substring(URI_SCHEME.length());
                    if ( msResources.containsKey(smileyName) )
                    {
                        int resource = msResources.get(smileyName);
                        drawable = new AnimatedGIFDrawable(context, resource);
                        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*1.5), (int)(drawable.getIntrinsicHeight()*1.5));
                        drawable.start();
                    }
                }
                return drawable;
            }
        };
    }
}
