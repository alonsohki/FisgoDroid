package net.meneame.fisgodroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProfileFetcher
{
    private static final Pattern mUsernamePattern = Pattern.compile("<strong>usuario:</strong>&nbsp;([^<]+)");
    private static final Pattern mAvatarPattern = Pattern.compile("<img class=\"avatar big\" style=\"margin-right: 5px\" src=\"([^\"]+)\"");
    private static final Pattern mNamePattern = Pattern.compile("<strong>nombre:</strong>&nbsp;([^<]+)<");
    private static final Pattern mBioPattern = Pattern.compile("<strong>bio</strong>:<br/>(.*)", Pattern.DOTALL);
    private static final Pattern mFriendshipPattern = Pattern.compile("width=\"18\" height=\"16\" title=\"([^\"]+)\"/>");

    public static UserProfile fetch(FisgoService.FisgoBinder service, String userid)
    {
        UserProfile profile = null;
        IHttpService http = new HttpService();
        String result = service.getUserInfo(userid);

        if ( result != null && !result.equals("") && !result.equals("usuario inexistente") )
        {
            profile = new UserProfile();

            // User name
            Matcher m = mUsernamePattern.matcher(result);
            if ( !m.find() )
                return null;
            profile.setUsername(m.group(1));

            // Avatar
            m = mAvatarPattern.matcher(result);
            if ( m.find() )
            {
                profile.setAvatarUrl(m.group(1));
            }

            // Name
            m = mNamePattern.matcher(result);
            if ( m.find() )
            {
                profile.setName(m.group(1));
            }

            // Bio
            m = mBioPattern.matcher(result);
            if ( m.find() )
            {
                profile.setBio(m.group(1));
            }

            // Friendship status
            m = mFriendshipPattern.matcher(result);
            if ( m.find() )
            {
                profile.setFriendship(FriendshipStatus.fromName(m.group(1)));
            }
        }

        return profile;
    }
}
