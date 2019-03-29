package main.last;

import DAO.ArtistData;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NonConcurrentLastFm implements LastFMService {

        private final String API_KEY = "&api_key=a5e08a41d7b5a3c71c45708190b792f4";
        private final String BASE = "http://ws.audioscrobbler.com/2.0/";
        private final String getAlbums = "?method=user.gettopalbums&user=";
        private final String ending = "&format=json";

    @Override
    public List<UserInfo> getUserInfo(List<String> lastFmNames) {
        return null;
    }

    @Override
    public LinkedList<ArtistData> getSimiliraties(String User) {
        return null;
    }

    public byte[] getUserList(String User, String weekly, int x, int y) {

            String url = BASE + getAlbums + User + API_KEY + ending+"&period=" + weekly;


            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(url);

            byte[] img = new byte[0];
            try {

                // Execute the method.
                int statusCode = client.executeMethod(method);

                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Method failed: " + method.getStatusLine());
                }

                // Read the response body.
                byte[] responseBody = method.getResponseBody();
                List<String> UrlList = new ArrayList<>();
                JSONObject obj = new JSONObject(new String(responseBody));
                obj = obj.getJSONObject("topalbums");

                JSONArray arr = obj.getJSONArray("album");
                for (int i = 0; i < arr.length() && i < 25 ; i++) {
                    JSONObject albumObj = arr.getJSONObject(i);
                    JSONArray image = albumObj.getJSONArray("image");
                    JSONObject bigImage = image.getJSONObject(3);
                    UrlList.add(bigImage.getString("#text"));

                }
                BufferedImage image = generateCollage(UrlList);
//
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", b);
                img = b.toByteArray();
                // Deal with the response.
                // Use caution: ensure correct character encoding and is not binary data

            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }  finally {
                // Release the connection.
                method.releaseConnection();
            }

            return img;
        }


        private BufferedImage generateCollage(List<String> urls) {
            int x =0;
            int y = 0;

            BufferedImage result = new BufferedImage(
                    1500   ,1500, //work these out
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = result.getGraphics();
            for (String  item : urls) {
                BufferedImage image ;
                URL url;
                try {

                    url = new URL(item);
                    image = ImageIO.read(url);
                    g.drawImage(image,x,y,null);
                    x+=300;
                    if(x >=result.getWidth()){
                        x = 0;
                        y += image.getHeight();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            return result;
        }
    }




