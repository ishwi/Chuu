package main.last;


import DAO.Entities.ArtistData;
import DAO.Entities.UserInfo;

import java.util.LinkedList;
import java.util.List;

public interface LastFMService {

    void getNowPlayingInfo(String user);

    List<UserInfo> getUserInfo(List<String> lastFmNames);

	LinkedList<ArtistData> getSimiliraties(String User);

	byte[] getUserList(String username, String time, int x, int y);
}
