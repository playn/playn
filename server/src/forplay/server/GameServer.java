/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.http.HttpServlet;

public class GameServer {

  private Server server;
  private ServletContextHandler servletHandler;
  private ResourceHandler resourceHandler;

  public GameServer(int port) {
    server = new Server(port);

    servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    servletHandler.setContextPath("/rpc");

    resourceHandler = new ResourceHandler();
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[] {"index.html"});
    resourceHandler.setResourceBase("www");

    HandlerList list = new HandlerList();
    list.setHandlers(new Handler[] {resourceHandler, servletHandler, new DefaultHandler()});
    server.setHandler(list);
  }

  public void addServlet(String pathSpec, HttpServlet servlet) {
    servletHandler.addServlet(new ServletHolder(servlet), pathSpec);
  }

  public void run() throws Exception {
    server.start();
    server.join();
  }
}
