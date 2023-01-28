package dao.entities;

import org.jetbrains.annotations.Nullable;

public record ProfileEntity(String lastmId, String discordName, String imageUrl,
                            int scrobbles, int albums, int artist, int randomCount,
                            String date, CommandStats commandStats, @Nullable ScrobbledArtist topArtist,
                            @Nullable ScrobbledAlbum topAlbum) {

}
