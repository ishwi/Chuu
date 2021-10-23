# Chuu

Chuu is a Discord bot that integrates [Last.fm]([https://www.last.fm/](https://www.last.fm/)) with Discord. Highly
customizable at the user and server level. It also has some unique integrations with RateYourMusic.com. There are a ton
of available commands like:

- Your top of albums that were released in a given year<br>
- Image charts of both albums and artists<br>
- Play and scrobble music directly from Discord using Youtube,Bandcamp,Soundcloud,Twitch...
- Artist, albums and songs leaderboards (crowns)<br>
- Unique artists within a server<br>
- Genre information about your artist/albums<br>
- 200+ more commands!
- Full support for slash commands

## Invite Link

You can invite the bot to your discord server
using [this link:](https://discord.com/oauth2/authorize?client_id=537353774205894676&scope=bot%20applications.commands&permissions=387136)

There is also a support server if you want to ask any questions directly. [Join here:](https://discord.gg/3tYsPMWvQG)

## Set-Up

You can also build the bot and install it on your own. In order to do that you need:<br><br>

- Java 17+ with preview features enabled<br><br>
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
    - MODERATION_CHANNEL_2_ID (Another discord channel where the bot will dump some info)

Then you should set all those properties in the
file [all.properties](https://github.com/ishwi/chuu/blob/master/src/main/resources/all.properties)

In MariaDB you should first create a database within MariaDB, and then inside that database run first
the [following SQL script](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/db/MariaBaseline.sql), and
then all the sql scripts
under [migrations](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/db/migrations) sequentially

Finally adjust
the [datasource.properties](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/datasource.properties)
with the corresponding properties.

For Postgres first check [the Musicbrainz section](#musicbrainz-database). Once that is done you should run
the [following SQL script](https://github.com/ishwi/chuu/blob/master/model/src/main/resources/db/PostgresBaseline.sql)
and adjust the [mbiz.properties](https://github.com/ishwi/chuu/blob/master/src/main/resources/mbiz.properties) with the
corresponding properties.

If you are interested in posting info of the bot to a given botlist, fill the
file [botlists.properties](https://github.com/ishwi/chuu/blob/master/src/main/resources/botlists.properties). The keys
are specified
in [BotLists.java](https://github.com/ishwi/chuu/blob/master/src/main/java/core/util/botlists/BotLists.java)

### Musicbrainz Database

For Postgresql I would recommend you to use [mbdata](https://github.com/lalinsky/mbdata) to set up the musicbrainz
database. Then adjust
the [mbiz.properties](https://github.com/ishwi/chuu/blob/master/src/main/resources/mbiz.properties) with the
corresponding properties. WARNING: It's like a 30GB database, so take note. It's recommended to set normal and lowercase
indexes on artist and album names to optimize some queries. Also, the pg_trgm extension should be enabled.

You have two options if you don't want to set up the whole musicbrainz instance:

- Use a mock musicbrainz instance as explained in [here](https://github.com/lalinsky/mbdata#development)
- Modify
  the [MusicBrainzServiceProvider](https://github.com/ishwi/Chuu/blob/master/model/src/main/java/dao/musicbrainz/MusicBrainzServiceSingleton.java)
  with
  the [dummy instance](https://github.com/ishwi/Chuu/blob/master/model/src/main/java/dao/musicbrainz/EmptyMusicBrainzServiceImpl.java)
  that does nothing. Uncommenting line 14 and commenting line 13.

Both this options will result on a lot of commands misbehaving and these options are not fully supported and a lot of
errors can be produced because of that so please be aware of that!.

### Running

After all that is installed you can run the task shadowjar of gradle and then you can execute, with `executable` being
the name of the jar that is on `build/libs`

```bash 
java -jar executable.jar
```

and the bot should begin running.

## Test

##### Currently not working

If you were to do testing you should also fill the equivalent all.properties of the test module and also fill
the [tester.properties](https://github.com/ishwi/chuu/blob/master/src/test/resources/tester.properties) with a new
Discord bot token that will be used for testing, the ID of the Discord server where the testing will take place and the
ID of a user present in that server.
