package net.meneame.fisgodroid;

public enum FriendshipStatus
{
    UNKNOWN,
    NONE,
    FRIEND_ME,
    FRIEND_THEY,
    FRIENDS,
    IGNORED;
    
    public static FriendshipStatus fromName(String statusName) {
        FriendshipStatus status = UNKNOWN;
        
        if ( statusName.equals("ignorar") )
        {
            status = IGNORED;
        }
        else if ( statusName.equals("elegido") )
        {
            status = FRIEND_THEY;
        }
        else if ( statusName.equals("amigos") )
        {
            status = FRIENDS;
        }
        else if ( statusName.equals("amigo") )
        {
            status = FRIEND_ME;
        }
        else if ( statusName.equals("agregar lista amigos") )
        {
            status = NONE;
        }
        
        return status;
    }
}
