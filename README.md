# Chuu

Chuu is a Discord bot that integrates [Last.fm]([https://www.last.fm/](https://www.last.fm/)) with Discord. It also has some unique integrations with RateYourMusic.com.
There are a ton of available commands like:

- Your top of albums that were released in a given year<br>
- Image charts of both albums and artists<br>
- Play and scrobble music directly from Discord using Youtube,Bandcamp,Soundcloud,Twitch...
- Artist, albums and songs leaderboards (crowns)<br>
- Unique artists within a server<br>
- Genre information about your artist/albums<br>
- 150+ more commands!

## Invite Link

You can invite the bot to your discord server using [this link:](https://discordapp.com/oauth2/authorize?scope=bot&client_id=537353774205894676&permissions=387136)

There is also a support server if you want to ask any questions directly. [Join here:](https://discord.gg/HQGqYD7)
## Set-Up
You can also build the bot and install it on your own.
In order to do that you need:<br><br>

- Java 15+ with preview features enabled<br><br>
- MariaDB 10.5+<br><br>
- Postgresql 12+<br><br>
- [A Discogs developer account](https://www.discogs.com/developers)

  - DC_KY
  - DC_SC

- [A Spotify developer account](https://developer.spotify.com/)
  - client_ID
  - client_Secret

- [A last.fm developer account](https://secure.last.fm/login?next=/api/account/create)
  - LASTFM_APIKEY
  - LASTFM_APISECRET (If you want to enable scrobbling)
  - LASTFM_BOT_SESSION_KEY (If you want to log what the bot scrobbles to a last.fm account)

- [A Discord developer account](https://discordapp.com/login?redirect_to=%2Fdevelopers%2Fapplications%2F)
  - DISCORD_TOKEN

- OS
  - WALLPAPER_FOLDER (Directory with images to source backgrounds)
  - CACHE_FOLDER (Where to cache images)
  - IPV6_BLOCK  (A ipv6 block used to avoid getting ip banned by YT)

- OTHER
  - MODERATION_CHANNEL_ID (A discord channel where the bot will dump some info)

Then you should set all those properties in the
file [all.properties](https://github.com/ishwi/chuu/blob/master/src/main/resources/all.properties)
In MariaDB you should run
the [following SQL script](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/MariaBaseline.sql) and
adjust
the [datasource.properties](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/datasource.properties)
with the corresponding properties. In Postgres you should run
the [following SQL script](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/PostgresBaseline.sql) and
adjust
the [datasource.properties](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/datasource.properties)
with the corresponding properties.

For postgresql I would recommend you to use [mbdata](https://pypi.org/project/mbdata/) to set up the musicbrainz
database. Then adjust
the [mbiz.properties](https://github.com/ishwi/chuu/blob/master/src/main/resources/mbiz.properties) with the
corresponding properties. WARNING: It's like a 30GB database, so take note. It's recommended to set normal and lowercase
indexes on artist and album names to optimize some queries. Also the pg_trgm extension should be enabled.

After all that is installed you can run the task shadowjar of gradle and then you can execute

```bash 
java -jar executable.jar
```

and the bot should begin running.

## Test

If you were to do testing you should also fill the equivalent all.properties of the test module and also fill
the [tester.properties](https://github.com/ishwi/chuu/blob/master/src/test/resources/tester.properties) with a new
Discord bot token that will be used for testing, the ID of the Discord server where the testing will take place and the
ID of a user present in that server.
