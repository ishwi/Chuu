package core.apis.last.entities;

public final record LovePost(String method, String artist, String track,
                             String api_key, String sk) implements PostEntity {


}
