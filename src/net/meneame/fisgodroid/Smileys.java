package net.meneame.fisgodroid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;

public class Smileys
{
    public static final String URI_SCHEME = "smiley://";
    
    private static final Map<String, Smiley> msSmileys = new HashMap<String, Smiley>();
    
    private static void addSmiley(Smiley smiley)
    {
        msSmileys.put(smiley.getChatText(), smiley);
    }
    
    private static void initializeResources ()
    {
        // Initialize the resource map
        if ( msSmileys.size() == 0 )
        {
            addSmiley(new Smiley(">:(",      "angry",       R.drawable.angry));
            addSmiley(new Smiley(":|",       "blank",       R.drawable.blank));
            addSmiley(new Smiley(":>",       "cheesy",      R.drawable.cheesy));
            addSmiley(new Smiley(":S",       "confused",    R.drawable.confused));
            addSmiley(new Smiley("8-)",      "cool",        R.drawable.cool));
            addSmiley(new Smiley(":'(",      "cry",         R.drawable.cry));
            addSmiley(new Smiley(":$",       "oops",        R.drawable.embarassed));
            addSmiley(new Smiley(":ffu:",    "ffu",         R.drawable.fu));
            addSmiley(new Smiley(":goatse:", "goatse",      R.drawable.goat));
            addSmiley(new Smiley(":D",       "grin",        R.drawable.grin));
            addSmiley(new Smiley(":hug:",    "hug",         R.drawable.hug));
            addSmiley(new Smiley("?(",       "huh",         R.drawable.huh));
            addSmiley(new Smiley(":*",       "kiss",        R.drawable.kiss));
            addSmiley(new Smiley("xD",       "lol",         R.drawable.laugh));
            addSmiley(new Smiley(":X",       "lipssealed",  R.drawable.lipsrsealed));
            addSmiley(new Smiley(":palm:",   "palm",        R.drawable.palm));
            addSmiley(new Smiley(":roll:",   "roll",        R.drawable.rolleyes));
            addSmiley(new Smiley(":(",       "sad",         R.drawable.sad));
            addSmiley(new Smiley("¬¬",       "shame",       R.drawable.shame));
            addSmiley(new Smiley(":shit:",   "shit",        R.drawable.shit));
            addSmiley(new Smiley(":O",       "shocked",     R.drawable.shocked));
            addSmiley(new Smiley(":)",       "smiley",      R.drawable.smiley));
            addSmiley(new Smiley(":P",       "tongue",      R.drawable.tongue));
            addSmiley(new Smiley(":troll:",  "troll",       R.drawable.trollface2));
            addSmiley(new Smiley(":/",       "undecided",   R.drawable.undecided));
            addSmiley(new Smiley(":wall:",   "wall",        R.drawable.wall));
            addSmiley(new Smiley(";)",       "wink",        R.drawable.wink));
            addSmiley(new Smiley(":wow:",    "wow",         R.drawable.wow));
        }
    }
    
    public static Collection<Smiley> getSmileys ()
    {
        initializeResources();
        return msSmileys.values();
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
                if ( msSmileys.containsKey(smileyName) == false )
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

    private static Drawable getAppropiateDrawable ( Context context, int resource )
    {
        Drawable drawable = null;
        float size;
        
        // We are not supporting animated gifs before Android 3
        //if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB )
       // {
            drawable = context.getResources().getDrawable(resource);
            size = 1.5f;
        //}
        //else
        //{
        //    AnimatedGIFDrawable temp = new AnimatedGIFDrawable(context, resource);
        //    temp.start();
        //    drawable = temp;
        //    size = 1.5f;
        //}
        
        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*size), (int)(drawable.getIntrinsicHeight()*size));
        return drawable;
    }
    
    public static Html.ImageGetter getImageGetter ( final Context context )
    {
        initializeResources();

        return new Html.ImageGetter ()
        {
            @Override
            public Drawable getDrawable(String source)
            {
                Drawable drawable = null;
                if ( source.startsWith(URI_SCHEME) )
                {
                    String smileyName = source.substring(URI_SCHEME.length());
                    if ( msSmileys.containsKey(smileyName) )
                    {
                        int resource = msSmileys.get(smileyName).getResource();
                        drawable = getAppropiateDrawable(context, resource);
                    }
                }
                return drawable;
            }
        };
    }
}
