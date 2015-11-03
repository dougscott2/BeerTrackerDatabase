package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {



    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers(id IDENTITY, name VARCHAR, type VARCHAR)");


        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    //ArrayList<Beer> beers = selectBeers(conn);
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", selectBeers(conn));
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    Beer beer = new Beer();

                   // beer.id = beers.size() + 1;
                   // beer.id = selectBeers(conn).size() + 1;
                    beer.name = request.queryParams("beername");
                    beer.type = request.queryParams("beertype");
                    //beer.id = request.queryParams("{{id}}");
                    insertBeer(conn, beer.name, beer.type);
                   //beers.add(beer);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                      //  beers.remove(idNum-1);
                        deleteBeer(conn, idNum);
                     //   for (int i = 0; i < beers.size(); i++) {
                      //      beers.get(i).id = i + 1;
                     //   }
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );
    /*    Spark.get(
                "/edit-beer",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String id = request.queryParams("id");
                    m.put("id", id);
                    return new ModelAndView(m, "/edit-beer.html");
                }),
                new MustacheTemplateEngine()
        );*/
        Spark.post(
                "/edit-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    String name = request.queryParams("beername");
                    String type = request.queryParams("beertype");
                    try{
                        int idNum = Integer.valueOf(id);
                        updateBeer(conn, idNum, name, type);
                    }
                    catch (Exception e){
                    }
                    response.redirect("/");
                    return "";
                })
        );


    }

    static void insertBeer(Connection conn, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (NULL, ?, ?)");//null for auto increment for id
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }
    static void updateBeer(Connection conn, int id, String name, String type) throws SQLException{
        PreparedStatement stmt2 = conn.prepareStatement("UPDATE beers SET name = ?, type = ? WHERE id = ?");
        stmt2.setString(1, name);
        stmt2.setString(2, type);
        stmt2.setInt(3, id);
        stmt2.execute();
    }

    static void deleteBeer(Connection conn, int selectNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id = ?");
        stmt.setInt(1, selectNum);
        stmt.execute();
    }
    static ArrayList<Beer> selectBeers (Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beers = new ArrayList();

        while (results.next()){
            Beer beer = new Beer();
            beer.name = results.getString("name");  //could just use a constructor instead
            beer.type = results.getString("type");
            beer.id = results.getInt("id");
            beers.add(beer);
        }
        return beers;

    }
}
