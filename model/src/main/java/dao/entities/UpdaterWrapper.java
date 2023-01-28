package dao.entities;

record UpdaterWrapper<T>(int timestamp, String username, T nowPlayingArtistList) {
}
