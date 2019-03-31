package DAO.Entities;

public class UrlCapsule {


    private final String url;
    private final int pos;



    public UrlCapsule(String url, int pos) {
        this.url = url;
        this.pos = pos;
    }
    public int getPos() {
        return pos;
    }
    public String getUrl() {
        return url;
    }
}
