package core.apis.last;


public enum TopEntity {
    ALBUM, TRACK, ARTIST;


    String getLeadingObject() {
        String returnValue;
        switch (this) {
            case ALBUM:
                returnValue = "topalbums";
                break;
            case TRACK:
                returnValue = "toptracks";
                break;
            case ARTIST:
                returnValue = "topartists";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return returnValue;
    }

    String getApiMethod() {
        String returnValue;
        switch (this) {
            case ALBUM:
                returnValue = ConcurrentLastFM.GET_ALBUMS;
                break;

            case TRACK:
                returnValue = ConcurrentLastFM.GET_TOP_TRACKS;
                break;
            case ARTIST:
                returnValue = ConcurrentLastFM.GET_ARTIST;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return returnValue;
    }

    String getArrayObject() {
        String returnValue;
        switch (this) {
            case ALBUM:
                returnValue = "album";
                break;
            case TRACK:
                returnValue = "track";
                break;
            case ARTIST:
                returnValue = "artist";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return returnValue;
    }


}
