package core.apis.youtube;
/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import core.Chuu;
import core.exceptions.ChuuServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Print a list of videos matching a search term.
 *
 * @author Jeremy Walker
 */
public class Search {

    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
    private static final String PROPERTIES_FILENAME = "all.properties";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    private static final String BASE_URL = "https://www.youtube.com/watch?v=";
    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private YouTube youtube;
    private String apiKey;

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     */
    Search() {

        Properties properties = new Properties();
        try (InputStream in = Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME)) {
            properties.load(in);
            apiKey = properties.getProperty("YT_API");
            youtube = new YouTube.Builder(com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport()
                    , new JacksonFactory(), request -> {
            }).setApplicationName("discordBot").build();
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                               + " : " + e.getMessage());
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }

        // This object is used to make YouTube Data API requests. The last
        // argument is required, but since we don't need anything
        // initialized when the HttpRequest is initialized, we override
        // the interface and provide a no-op function.


//setApplicationName("Discord Searcher").

    }

    /*
     * Prompt the user to enter a query term and return the user-specified term.
     */


    /*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */


    public String doSearch(String queryTerm) {
        // Prompt the user to enter a query term.
        String responseUrl = "";
        try {
            // Define the API request for retrieving search results.

            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the Google Developers Console for
            // non-authenticated requests. See:
            // https://console.developers.google.com/
            String apiKey = this.apiKey;
            search.setKey(apiKey);
            search.setQ(queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                Optional<SearchResult> opt = searchResultList.stream().findFirst();
                if (opt.isPresent())
                    responseUrl = BASE_URL + opt.get().getId().getVideoId();
            }
        } catch (IOException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return responseUrl;
    }
}