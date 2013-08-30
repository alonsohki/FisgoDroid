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

import java.io.OutputStream;
import java.util.Map;

public interface IHttpService
{
	public String get ( String uri );
	public boolean get ( String uri, OutputStream stream );
	public String post ( String uri, Map<String, Object> params );
	public boolean post ( String uri, Map<String, Object> params, OutputStream stream );
}
