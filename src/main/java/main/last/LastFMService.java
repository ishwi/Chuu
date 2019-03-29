package main.last;


import java.util.Map;

public interface LastFMService {

	Map<String, Integer> getSimiliraties(String User);

	byte[] getUserList(String username, String time, int x, int y);
}
