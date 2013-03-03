package com.etsy.esp;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.cometd.Bayeux;

import org.mortbay.cometd.ext.AcknowledgedMessagesExtension;


public class ESPServlet extends GenericServlet {
    public ESPServlet() {}

    public void init() throws ServletException {
        super.init();
        Bayeux b = (Bayeux)getServletContext().getAttribute(Bayeux.ATTRIBUTE);
        new ESPService(b);
        b.addExtension(new AcknowledgedMessagesExtension());
    }

    public void service(ServletRequest req, ServletResponse res) 
        throws ServletException, IOException {
        ((HttpServletResponse)res).sendError(503);
    }
}
