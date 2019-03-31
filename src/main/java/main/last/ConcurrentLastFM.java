package main.last;

import DAO.Entities.ArtistData;
import DAO.Entities.UrlCapsule;
import DAO.Entities.UserInfo;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;


public class ConcurrentLastFM implements LastFMService {
	private final String API_KEY = "&api_key=***REMOVED***";
	private final String BASE = "http://ws.audioscrobbler.com/2.0/";
	private final String GET_ALBUMS = "?method=user.gettopalbums&user=";
	private final String GET_LIBRARY = "?method=library.getartists&user=";
	private final String GET_USER = "?method=user.getinfo&user=";
	private final String ending = "&format=json";
	private final String GET_NOW_PLAYINH = "?method=user.getrecenttracks&limit=1&user=";
	private final BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();

	@Override
	public void getNowPlayingInfo(String user) {
		HttpClient client = new HttpClient();
		String url = BASE + GET_NOW_PLAYINH + user + API_KEY + ending;
		GetMethod method = new GetMethod(url);
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
				return;
			}
			byte[] responseBody = method.getResponseBody();
			JSONObject obj = new JSONObject(new String(responseBody));
			obj = obj.getJSONObject("recenttracks");
			JSONObject tracltObj = obj.getJSONArray("track").getJSONObject(0);
			JSONObject artistObj = tracltObj.getJSONObject("artist");
			String artistname = artistObj.getString("#text");
			String mbid = artistObj.getString("#mbid");
			Boolean nowPlayin = tracltObj.getJSONObject("@attr").getBoolean("nowplaying");
			String albumName = tracltObj.getJSONObject("album").getString("#text");
			String songName = tracltObj.getString("name");
			String image_url = tracltObj.getJSONArray("image").getJSONObject(2).getString("#text");
			int a = 2;
			a++;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<UserInfo> getUserInfo(List<String> lastFmNames) {
		HttpClient client = new HttpClient();
		List<UserInfo> returnList = new ArrayList<>();

		try {

			for (String lastFmName : lastFmNames) {
				String url = BASE + GET_USER + lastFmName + API_KEY + ending;
				GetMethod method = new GetMethod(url);
				int statusCode = client.executeMethod(method);
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + method.getStatusLine());
				}
				byte[] responseBody = method.getResponseBody();
				JSONObject obj = new JSONObject(new String(responseBody));
				obj = obj.getJSONObject("user");
				JSONArray image = obj.getJSONArray("image");
				JSONObject bigImage = image.getJSONObject(2);
				String image2 = bigImage.getString("#text");
				int playcount = obj.getInt("playcount");
				returnList.add(new UserInfo(playcount, image2, lastFmName));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Execute the method.


		// Read the response body.


		return returnList;

	}

	@Override
	public LinkedList<ArtistData> getSimiliraties(String User) {
		String url = BASE + GET_LIBRARY + User + API_KEY + ending;
		int page = 1;
		int pages = 1;
		HttpClient client = new HttpClient();
		url += "&limit=500";

		LinkedList<ArtistData> linkedlist = new LinkedList<>();
		Map<String, Integer> map = new HashMap<>();
		while (page <= pages) {

			String urlPage = url + "&page=" + page;
			GetMethod method = new GetMethod(urlPage);

			try {

				// Execute the method.
				int statusCode = client.executeMethod(method);

				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + method.getStatusLine());
				}

				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				JSONObject obj = new JSONObject(new String(responseBody));
				obj = obj.getJSONObject("artists");
				if (page++ == 1) {
					pages = obj.getJSONObject("@attr").getInt("totalPages");
				}

				JSONArray arr = obj.getJSONArray("artist");
				for (int i = 0; i < arr.length(); i++) {
					JSONObject artistObj = arr.getJSONObject(i);
					String mbid = artistObj.getString("name");

					int count = artistObj.getInt("playcount");
					JSONArray image = artistObj.getJSONArray("image");
					JSONObject bigImage = image.getJSONObject(2);
					linkedlist.add(new ArtistData(mbid, count, bigImage.getString("#text")));
				}


			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return linkedlist;
	}


	public byte[] getUserList(String User, String weekly, int x, int y) {

		String url = BASE + GET_ALBUMS + User + API_KEY + ending + "&period=" + weekly;


		HttpClient client = new HttpClient();

		int requestedSize = x * y;
		int size = 0;
		int page = 1;
		while (size < requestedSize) {

			String urlPage = url + "&page=" + page;
			GetMethod method = new GetMethod(urlPage);
			++page;
			System.out.println(page + " :page             size: " + size);
			try {

				// Execute the method.
				int statusCode = client.executeMethod(method);

				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + method.getStatusLine());
				}

				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				JSONObject obj = new JSONObject(new String(responseBody));
				obj = obj.getJSONObject("topalbums");

				JSONArray arr = obj.getJSONArray("album");
				for (int i = 0; i < arr.length() && size < requestedSize; i++) {
					JSONObject albumObj = arr.getJSONObject(i);
					JSONArray image = albumObj.getJSONArray("image");
					JSONObject bigImage = image.getJSONObject(3);
					queue.add(new UrlCapsule(bigImage.getString("#text"), size));
					++size;
				}


			} catch (HttpException e) {
				System.err.println("Fatal protocol violation: " + e.getMessage());
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Release the connection.
				method.releaseConnection();
			}
		}
		byte[] img;
		BufferedImage image = generateCollage(x, y);
		ByteArrayOutputStream b = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, "jpg", b);
		} catch (IOException e) {
			e.printStackTrace();
		}

		img = b.toByteArray();
		// Deal with the response.
		// Use caution: ensure correct character encoding and is not binary data

		return img;
	}


	private BufferedImage generateCollage(int x, int y) {

		BufferedImage result = new BufferedImage(
				x * 300, y * 300, //work these out
				BufferedImage.TYPE_INT_RGB);

		Graphics g = result.getGraphics();
		System.out.println("a");


		ExecutorService es = Executors.newCachedThreadPool();
		for (int i = 0; i < 4; i++)
			es.execute((new ThreadQueue(queue, g, x, y)));
		es.shutdown();
		try {
			boolean finished = es.awaitTermination(10, TimeUnit.MINUTES);
			System.out.println(finished);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

//                for (String  item : urls) {
//            BufferedImage image ;
//            URL url;
//            try {
//
//                url = new URL(item);
//                image = ImageIO.read(url);
//                g.drawImage(image,x,y,null);
//                x+=300;
//                if(x >=result.getWidth()){
//                    x = 0;
//                    y += image.getHeight();
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//        }
		return result;
	}

}


