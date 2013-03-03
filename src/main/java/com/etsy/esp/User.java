package com.etsy.esp;

import org.cometd.Bayeux;
import org.cometd.Client;
import java.util.Map;
import java.util.HashMap;
import org.mortbay.log.Log;
import org.mortbay.cometd.ChannelImpl;
import java.io.IOException;
import org.json.JSONException;


public class User {

    private Game _game;
    private String _id;
    private ESPService _service;
    private String _name;

    public User(ESPService s, Client c, Map<String, Object> joindata) {
        _id = c.getId();
        _service = s;
        _name = (String)joindata.get("name");
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public void join(Game g) throws IOException, JSONException {
        _game = g;
        Log.info(this.toString() + " joining game.");

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("opponent", g.other(this).getAvatarUrl());
        send("/esp/newgame", m);
    }

    public void tag(String guess) throws IOException, JSONException {
        _game.tag(this, guess);
    }

    public String getAvatarUrl() throws IOException, JSONException {
        return EtsyAPI.getAvatarUrl(getName());
    }

    public String toString() {
        return "User " + _name;
    }

    public void send(String channel, Map<String, Object> message) {
        if(message == null) {
            message = new HashMap<String, Object>();
        }
        Client c = _service.getBayeux().getClient(_id);
        c.deliver(_service.getClient(), channel, message, "1");
    }

}
