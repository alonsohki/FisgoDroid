/**
 DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2013 TheWonderWall 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO.
 */

package net.meneame.fisgodroid;

import java.util.Date;

public class ChatMessage
{
    private Date when;
    private String user;
    private String message;
    private ChatType type;
    private String icon;

    public ChatMessage(Date when, String user, String message, ChatType type, String icon)
    {
        setWhen(when);
        setUser(user);
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
}
