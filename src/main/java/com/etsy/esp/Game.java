package com.etsy.esp;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONException;
import java.io.IOException;


public class Game {
    private User _user1;
    private User _user2;
    private ESPService _service;

    // user -> guesses
    private Map<User, ArrayList<String>> _guesses = 
        new HashMap<User, ArrayList<String>>();

    private int _listing_id;


    public Game(ESPService svc, User user1, User user2) {
        _service = svc;
        _user1 = user1;
        _user2 = user2;
        resetTags();
    }

    private void resetTags() {
        _guesses.put(_user1, new ArrayList<String>());
        _guesses.put(_user2, new ArrayList<String>());
    }


    public void init() throws IOException, JSONException {
        _user1.join(this);
        _user2.join(this);
    }


    public synchronized void newListing() throws IOException, JSONException {
        _listing_id = EtsyAPI.nextListingId();

        ArrayList<String> u1_tags = _guesses.get(_user1);
        ArrayList<String> u2_tags = _guesses.get(_user2);
        
        resetTags();

        _user1.send("/esp/newlisting", newListingArgs(u2_tags));
        _user2.send("/esp/newlisting", newListingArgs(u1_tags));
    }

    private Map<String, Object> newListingArgs(Object tags) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("listing_id", _listing_id);
        m.put("opponent_guesses", tags);
        return m;
    }

    
    public synchronized Boolean tag(User user, String guess) 
        throws IOException, JSONException {

        ArrayList<String> mine = _guesses.get(user);

        // If the user has already guessed this once, nevermind
        if(mine.contains(guess)) {
            return false;
        }

        // Record the guess
        mine.add(guess);

        User u2 = other(user);

        // If the other user guessed this, game over
        ArrayList<String> theirs = _guesses.get(u2);
        if(theirs.contains(guess)) {
            newListing();
            return true;
        }

        // notify the opponent that this user typed something.
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("taglength", guess.length());
        u2.send("/esp/tag", m);
	
        return false;
    }


    public User other(User user) {
        return (user == _user1 ? _user2 : _user1);
    }

}