package net.meneame.fisgodroid.notifications;

class NotificationElement
{
    private String type;
    private String baseUrl;
    private int title;
    private int count;

    public NotificationElement(String type, String baseUrl, int title, int count)
    {
        this.type = type;
        this.baseUrl = baseUrl;
        this.title = title;
        this.count = count;
    }

    public int getTitle()
    {
        return title;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
}
