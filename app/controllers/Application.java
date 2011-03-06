package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.Album;
import models.Artist;
import models.Genre;
import play.Logger;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import play.Play;

public class Application extends Controller {

    public static void index() {
        render();
    }


    /**
     * List albums
     *
     * @param filter
     */
    public static void list(String filter) {
        List<Album> albums = Album.findAll(filter);
        render(albums);
    }

    /**
     * List albums by genre
     *
     * @param genre
     */
    public static void listByGenreAndYear(String genre, String year) {
        //genre and year are mandatory
        notFoundIfNull(genre);
        notFoundIfNull(year);
        List<Album> albums = Album.findByGenreAndYear(genre, year);
        render(genre, year, albums);
    }


    /**
     * List albums in xml or json format
     *
     * @param genre
     * @param year
     */
    public static void listByApi(String genre, String year) {
        List<Album> albums;
        if (genre != null) {
            Genre genreEnum = Genre.valueOf(genre.toString().toUpperCase());
            albums = Album.find("byGenre", genreEnum).fetch();
        } else {
            albums = Album.findAll();
        }
        if (year != null) {
            albums = Album.filterByYear(albums, year);
        }
        if (request.format.equals("json"))
            renderJSON(albums);
        render(albums);
    }

    /**
     * List artists in xml or json format
     */
    public static void listArtistsByApi() {
        List<Artist> artists = Artist.findAll();
        if (request.format.equals("json"))
            renderJSON(artists);
        render(artists);
    }

    /**
     * Create album
     */
    public static void form() {
        render();
    }

    /**
     * Create or update album
     *
     * @param album
     * @param artist
     * @param cover
     */
    public static void save(@Valid Album album, @Valid Artist artist, File cover) {
        if (Validation.hasErrors()) {
            render("@form", album);
        }
        // set the album
        album.artist = artist;
        album.save();

        //album cover
        if (cover != null) {
            String path = "/public/shared/covers/" + album.id;
            album.hasCover = true;
            File newFile = Play.getFile(path);
            //delete old cover if exists
            if (newFile.exists())
                newFile.delete();
            cover.renameTo(newFile);

            album.save();
        }

        list(null);
    }


    /**
     * Save album via API
     */
    public static void saveAlbumJson() {
        Gson gson = new Gson();
        Album album = gson.fromJson(new InputStreamReader(request.body),Album.class);
        //TODO
//        if(Artist.findByName(album.artist.name).isEmpty()){
//            Artist foundDuplicate = album.findDuplicateArtist(album.artist);
//        }
        album.save();
        Logger.info("name : "+album.name);
    }


    /**
     * @param id
     */
    public static void vote(String id) {
        Album album = Album.findById(Long.parseLong(id));
        album.vote();
        renderText(album.nbVotes);
    }

    /**
     * Years to display for top albums form
     *
     * @return
     */
    public static List<String> getYearsToDisplay() {
        List<String> years = new ArrayList<String>();
        for (int i = Album.getFirstAlbumYear(); i <= Album.getLastAlbumYear(); i++) {
            years.add(String.valueOf(i));
        }
        Collections.reverse(years);
        return years;
    }
}
