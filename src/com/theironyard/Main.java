package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static void insertBeer(Connection conn, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }
    static void editBeer(Connection conn, int selectNum, String name, String type) throws SQLException{
        PreparedStatement stmt2 = conn.prepareStatement("UPDATE beers SET name = ? SET type = ? WHERE ROWNUM = ?");
        stmt2.setString(1, name);
        stmt2.setString(2, type);
        stmt2.setInt(3, selectNum);
        stmt2.execute();
    }

    static void deleteBeer(Connection conn, int selectNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE ROWNUM = ?");
        stmt.setInt(1, selectNum);
        stmt.execute();
    }
    static ArrayList<Beer> selectBeers (Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beers = new ArrayList();
        int id = 1;
        while (results.next()){
            String name = results.getString("name");
            String type = results.getString("type");
            Beer beer = new Beer(name, type);
            beer.id = id;
            id++;
            beers.add(beer);
        }
        return beers;

    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers(name VARCHAR, type VARCHAR)");



       // ArrayList<Beer> beers = new ArrayList();
        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    ArrayList<Beer> beers = selectBeers(conn);
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", beers);
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
                    beer.id = Integer.valueOf("{{id}}");
                    insertBeer(conn, beer.name, beer.type);
                   // beers.add(beer);
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
                    String id = request.queryParams("id");
                    try{
                        int idNum = Integer.valueOf(id);
                        String name = request.queryParams("editName");
                        String type = request.queryParams("beertype");
                        editBeer(conn, idNum, name, type);
                    }
                    catch (Exception e){

                    }
                    response.redirect("/");
                    return "";
                })
        );


    }
}
