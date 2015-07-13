package net.meneame.fisgodroid;

public class Smiley
{
    private String inputText;
    private String chatText;
    private int resource;

    public Smiley(String inputText, String chatText, int resource)
    {
        this.inputText = inputText;
        this.chatText = chatText;
        this.resource = resource;
    }

    public String getInputText()
    {
        return inputText;
    }

    public String getChatText()
    {
        return chatText;
    }

    public int getResource()
    {
        return resource;
    }
}
