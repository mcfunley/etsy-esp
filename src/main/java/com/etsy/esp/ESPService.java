package com.etsy.esp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.cometd.Bayeux;
import org.cometd.Client;
import org.mortbay.cometd.BayeuxService;
import org.mortbay.log.Log;
import org.json.JSONException;
import java.io.IOException;


/*
  Channels:
    /esp            - handles connection
    /esp/join       - receives a join request
    /esp/newgame    - sent game start data
*/


public class ESPService extends BayeuxService {

    // The user waiting for a partner, or null if nobody is. _join 
    // synchronizes this object. 
    private User _waiting = null;
    private Object _join = new Object();

    // clientId -> User. One entry per user, two users per game. 
    private ConcurrentMap<String, User> _users = 
        new ConcurrentHashMap<String, User>();


    public ESPService(Bayeux b) {
        super(b, "esp");
        subscribe("/esp", "trackUsers");
        subscribe("/esp/join", "joinGame");
        subscribe("/esp/tag", "tag");

        subscribe("/meta/disconnect", "disconnect");
    }


    public void trackUsers(Client c, Map<String, Object> data) {
        if(Boolean.TRUE.equals(data.get("connect"))) {
            User u = new User(this, c, data);
            _users.put(u.getId(), u);
            Log.info("User connected: " + u);
        }
    }


    public void tag(Client c, Map<String, Object> data) 
        throws IOException, JSONException {

        User u = _users.get(c.getId());
        try {
            u.tag((String)data.get("tag"));
        } catch(NullPointerException e) {
            Log.info("Opponent dead, " + u + "rejoining");
            rejoin(c);
        }
    }


    private void rejoin(Client c) {
        c.deliver(getClient(), "/esp/rejoin", null, "1");
    }


    public void joinGame(Client c, Map<String, Object> data) 
        throws IOException, JSONException {

        User u = _users.get(c.getId());
        if(u == null) {
            Log.info("Received join before connect");
            rejoin(c);
            return;
        }

        Game g = null;

        // Pair up if someone is waiting. Otherwise wait for a partner.
        synchronized(_join) {
            if(_waiting == null) {
                Log.info(u + " waiting for partner");
                _waiting = u;
            } else {
                User u2 = _waiting;
                _waiting = null;
                g = new Game(this, u2, u);
                try { 
                    g.init();
                } catch(NullPointerException e) {
                    Log.info("Old waiting user dead, waiting for partner");
                    _waiting = u;
                }
            }
        }

        if(g != null) {
            g.newListing();
        }
    }


    public void disconnect(Client c, Map<String, Object> data) {
        Log.info("disconnect");
    }

}
