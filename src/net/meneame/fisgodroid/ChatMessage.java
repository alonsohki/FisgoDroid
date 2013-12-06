package net.meneame.fisgodroid;

import java.util.Date;
import android.text.format.DateFormat;

public class ChatMessage
{
    private Date when;
    private String user;
    private String message;
    private ChatType type;
    private String icon;
    private String userid;

    public ChatMessage(Date when, String user, String userid, String message, ChatType type, String icon)
    {
        setWhen(when);
        setUser(user);
        setUserid(userid);
        setMessage(message);
        setType(type);
        setIcon(icon);
    }

    public Date getWhen()
    {
        return when;
    }

    public void setWhen(Date when)
    {
        this.when = when;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getUserid()
    {
        return userid;
    }

    public void setUserid(String userid)
    {
        this.userid = userid;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public ChatType getType()
    {
        return type;
    }

    public void setType(ChatType type)
    {
        this.type = type;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public String logify()
    {
        StringBuilder logString = new StringBuilder();

        logString.append(DateFormat.format("kk:mm:ss ", this.when));

        logString.append(this.user);

        logString.append(": " + this.message);

        return logString.toString();
    }
}
