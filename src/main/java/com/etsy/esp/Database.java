package com.etsy.esp;

import java.sql.*;
import java.util.Random;


public class Database {

    static Object _init = null;

    public static Connection connect() 
        throws SQLException, ClassNotFoundException {

        if(_init == null) {
            _init = Class.forName("org.postgresql.Driver");
        }
        Connection c = DriverManager.getConnection(
            "jdbc:postgresql://dev-db2.ny4dev.etsy.com:6666/etsy_v2",
            "etsy",
            "sekret");
        return c;
    }

    public static int nextListingId() 
        throws SQLException, ClassNotFoundException {

        Connection c = null;
        Random rand = new Random();
        try {
            c = connect();
            Statement s = c.createStatement();
            ResultSet r = s.executeQuery(
                "select listing_id from listings where " + 
                "state='active' order by creation_tsz desc limit 150");
            int i = 0;
            r.next();
            while(r.next() && (i++ < 100) && rand.nextInt(100) != 50);
            return r.getInt("listing_id");
        } finally {
            if(c != null) {
                c.close();
            }
        }
    }
}
