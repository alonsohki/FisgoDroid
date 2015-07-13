package net.meneame.fisgodroid;

public class UserProfile
{
    private String mUsername;
    private String mName;
    private String mAvatarUrl;
    private String mBio;
    private FriendshipStatus mFriendship = FriendshipStatus.UNKNOWN;

    public UserProfile()
    {
    }

    public UserProfile setUsername(String username)
    {
        mUsername = username;
        return this;
    }

    public UserProfile setName(String name)
    {
        mName = name;
        return this;
    }

    public UserProfile setAvatarUrl(String url)
    {
        mAvatarUrl = url;
        return this;
    }

    public UserProfile setBio(String bio)
    {
        mBio = bio;
        return this;
    }

    public UserProfile setFriendship(FriendshipStatus status)
    {
        mFriendship = status;
        return this;
    }

    public String getUsername()
    {
        return mUsername;
    }

    public String getName()
    {
        return mName;
    }

    public String getAvatarUrl()
    {
        return mAvatarUrl;
    }

    public String getBio()
    {
        return mBio;
    }
    
    public FriendshipStatus getFriendship()
    {
        return mFriendship;
    }
}
