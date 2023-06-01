package com.driver;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User();
        user.setName(name);
        user.setMobile(mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist();
        artist.setName(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Optional<Artist> isArtistPresent = artists
                .stream()
                .filter(artist -> artist.getName().equals(artistName))
                .findFirst();

        Artist artist = isArtistPresent.orElseGet(() -> createArtist(artistName));
        Album album = new Album();
        album.setTitle(title);
        albums.add(album);

        if(!artistAlbumMap.containsKey(artist))
            artistAlbumMap.put(artist, new ArrayList<>());
        artistAlbumMap.get(artist).add(album);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Optional<Album> isAlbumPresent = albums
                .stream()
                .filter(album -> album.getTitle().equals(albumName))
                .findFirst();

        if(isAlbumPresent.isEmpty())
            throw new Exception("Album does not exist");

        Song song = new Song();
        song.setTitle(title);
        song.setLength(length);
        song.setLikes(0);
        songs.add(song);
        Album album = isAlbumPresent.get();

        if(!albumSongMap.containsKey(album))
            albumSongMap.put(album, new ArrayList<>());

        albumSongMap.get(album).add(song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Optional<User> isUserPresent = users
                .stream()
                .filter(user -> user.getMobile().equals(mobile))
                .findFirst();

        if(isUserPresent.isEmpty())
            throw new Exception("User does not exist");

        User user = isUserPresent.get();
        Playlist playlist = new Playlist();
        playlist.setTitle(title);
        playlists.add(playlist);

        List<Song> songs1 = songs
                .stream()
                .filter(song -> song.getLength() == length)
                .collect(Collectors.toList());

        playlistSongMap.put(playlist, songs1);
        creatorPlaylistMap.put(user, playlist);

        if(!userPlaylistMap.containsKey(user))
            userPlaylistMap.put(user, new ArrayList<>());
        userPlaylistMap.get(user).add(playlist);

        if(!playlistListenerMap.containsKey(playlist))
            playlistListenerMap.put(playlist, new ArrayList<>());

        playlistListenerMap.get(playlist).add(user);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Optional<User> isUserPresent = users
                .stream()
                .filter(user -> user.getMobile().equals(mobile))
                .findFirst();

        if(isUserPresent.isEmpty())
            throw new Exception("User does not exist");

        User user = isUserPresent.get();
        Playlist playlist = new Playlist();
        playlist.setTitle(title);
        playlists.add(playlist);

        List<Song> songs1 = songs
                .stream()
                .filter(song -> songTitles.contains(song.getTitle()))
                .collect(Collectors.toList());

        playlistSongMap.put(playlist, songs1);
        creatorPlaylistMap.put(user, playlist);

        if(!userPlaylistMap.containsKey(user))
            userPlaylistMap.put(user, new ArrayList<>());
        userPlaylistMap.get(user).add(playlist);

        if(!playlistListenerMap.containsKey(playlist))
            playlistListenerMap.put(playlist, new ArrayList<>());
        playlistListenerMap.get(playlist).add(user);

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        Optional<User> isUserPresent = users
                .stream()
                .filter(user -> user.getMobile().equals(mobile))
                .findFirst();

        if(isUserPresent.isEmpty())
            throw new Exception("User does not exist");

        User user = isUserPresent.get();

        Optional<Playlist> isPlaylistPresent = playlists
                .stream()
                .filter(playlist -> playlist.getTitle().equals(playlistTitle))
                .findFirst();

        if(isPlaylistPresent.isEmpty())
            throw new Exception("Playlist does not exist");

        Playlist playlist = isPlaylistPresent.get();
        List<User> userList = playlistListenerMap.get(playlist);

        boolean isListenerUserPresent = userList
                .stream()
                .anyMatch(user1 -> user1 == user);

        if(!isListenerUserPresent)
            userList.add(user);

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        Optional<User> isUserPresent = users
                .stream()
                .filter(user -> user.getMobile().equals(mobile))
                .findFirst();

        if(isUserPresent.isEmpty())
            throw new Exception("User does not exist");

        User user = isUserPresent.get();

        Optional<Song> isSongPresent = songs
                .stream()
                .filter(song -> song.getTitle().equals(songTitle))
                .findFirst();

        if(isSongPresent.isEmpty())
            throw new Exception("Song does not exist");

        Song song = isSongPresent.get();

        if(!songLikeMap.containsKey(song))
            songLikeMap.put(song, new ArrayList<>());

        List<User> userList = songLikeMap.get(song);

        boolean isLikedUserPresent = userList
                .stream()
                .anyMatch(user1 -> user1 == user);

        if(!isLikedUserPresent) {
            song.setLikes(song.getLikes() + 1);
            userList.add(user);
            outer:
            for(Artist artist : artistAlbumMap.keySet()) {
                List<Album> albumList = artistAlbumMap.get(artist);
                for(Album album : albumList) {
                    List<Song> songList = albumSongMap.get(album);
                    for(Song song1 : songList) {
                        if(song1 == song) {
                            artist.setLikes(artist.getLikes() + 1);
                            break outer;
                        }
                    }
                }
            }
        }

        return song;
    }

    public String mostPopularArtist() {
        return artists
                .stream()
                .max(Comparator.comparingInt(Artist::getLikes))
                .map(Artist::getName)
                .orElse(null);
    }

    public String mostPopularSong() {
        return songs
                .stream()
                .max(Comparator.comparingInt(Song::getLikes))
                .map(Song::getTitle)
                .orElse(null);
    }
}
