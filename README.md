# Chuu

Chuu is a discord bot that integrates [Last.fm]([https://www.last.fm/](https://www.last.fm/)) with discord.
There are a ton of available commands like:
  
 -Your top of albums that were released in a given year<br>
 -Image charts of both albums and artists<br>
 -Artist, albums and songs leaderboards (crowns)<br>
 -Unique artists within a server<br>
 -40+ more commands!

## Invite Link

You can invite the bot to your discord server using this [link:](https://discordapp.com/oauth2/authorize?scope=bot&client_id=537353774205894676&permissions=387136)
## Set-Up
You can also build the bot and install it on your own.
In order to do that you need:<br><br>
-Java 8+ (Only tested on 11 and 12) but should work on 8 or 9<br><br>
-MariaDB<br><br>
-Postgresql <br><br>
-[A discogs developer account](https://www.discogs.com/developers)

 - DC_KY
 - DC_SC
 
-[A spotify developer account](https://developer.spotify.com/)
 - client_ID
 - client_Secret
	
-[A last.fm developer account](https://secure.last.fm/login?next=/api/account/create)
 - LASTFM_APIKEY
 
-[A discord developer account](https://discordapp.com/login?redirect_to=%2Fdevelopers%2Fapplications%2F)
 - DISCORD_TOKEN

-[A Youtube developer account](https://www.youtube.com/intl/en-GB/yt/dev/)

 - TY_API

Then you should set all those properties in the file [all.properties](https://github.com/ishwi/discordBot/blob/master/src/main/resources/all.properties)
In MariaDB you should run the [following SQL script](https://github.com/ishwi/discordBot/blob/master/src/main/resources/MariaDBNew.sql) and adjust the [datasource.properties](https://github.com/ishwi/discordBot/blob/master/src/main/resources/datasource.properties) with the corresponding properties.

For postgresql I would recommend you to use [mbdata](https://pypi.org/project/mbdata/) to set up the musicbrainz database.
Then adjust the [mbiz.properties](https://github.com/ishwi/discordBot/blob/master/src/main/resources/mbiz.properties) with the corresponding properties.
WARNING: Its like a 30GB database, so yeah that.

After all that is installed you can run the task shadowjar of gradle and then you can execute 
```bash 
java -jar executable.jar
```
 and the bot should be running
## Test
If you were to do testing you should also fill the equivalent all.properties of the test module
and also fill the [tester.properties](https://github.com/ishwi/discordBot/blob/master/src/test/resources/tester.properties) with a new Discord bot token that will be used for testing,
The id of the discord server were the testing will take place and the id of an user present in that server.
