package com.irdeto.multiscreen;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class SampleServer {

    static final Logger logger = Logger.getLogger(SampleServer.class);

    public static void main( String[] args ) throws Exception
    {
        //Configure logger
        BasicConfigurator.configure();
        logger.debug("Starting main service..");

        Server server = new Server(8080);

        ContextHandler context = new ContextHandler("/");
        context.setContextPath("/");
        context.setHandler(new Health());

        ContextHandler contextHW = new ContextHandler("/helloworld");
        contextHW.setHandler(new HelloWorld());

        ContextHandler contextSC = new ContextHandler("/servicecall");
        contextSC.setHandler(new ServiceCall());

        ContextHandler contextCS = new ContextHandler("/consulservices");
        contextCS.setHandler(new ConsulServices());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context, contextHW, contextSC, contextCS });

        server.setHandler(contexts);

        server.start();
        server.join();
    }
}
