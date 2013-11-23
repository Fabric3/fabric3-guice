/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.guice.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.node.Bootstrap;
import org.fabric3.api.node.Fabric;

/**
 *
 */
public abstract class StartupServlet extends HttpServlet {
    private static final long serialVersionUID = -3953942017371982506L;
    protected transient Fabric fabric;
    private HttpServlet dispatcher;

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        dispatcher.service(req, res);
    }

    protected void init(ServletConfig config, URL systemConfig) throws ServletException {
        super.init(config);

        fabric = Bootstrap.initialize(systemConfig);

        String httpParameter = config.getServletContext().getInitParameter("http.port");
        int httpPort = (httpParameter == null) ? 8080 : Integer.parseInt(httpParameter);

        String httpsParameter = config.getServletContext().getInitParameter("https.port");
        int httpsPort = (httpParameter == null) ? -1 : Integer.parseInt(httpsParameter);

        String httpsUrlParameter = config.getServletContext().getInitParameter("http.url");

        URL httpUrl = getHttpBaseUrl(config, httpPort);

        URL httpsUrl = getHttpsBaseUrl(httpsPort, httpsUrlParameter);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("http.port", httpPort);
        properties.put("https.port", httpsPort);
        properties.put("http.url", httpUrl);
        properties.put("https.url", httpsUrl);

        dispatcher = fabric.createTransportDispatcher(HttpServlet.class, properties);
        dispatcher.init(config);

        fabric.start();
    }

    public void destroy() {
        if (fabric != null) {
            fabric.stop();
        }
    }

    protected URL getHttpsBaseUrl(int httpsPort, String httpsUrlParameter) throws ServletException {
        try {
            if (httpsUrlParameter == null) {
                String host = InetAddress.getLocalHost().getHostAddress();
                return new URL("https://" + host + ":" + httpsPort);
            } else {
                return new URL("https://" + httpsUrlParameter + ":" + httpsPort);
            }
        } catch (MalformedURLException e) {
            throw new ServletException("Invalid base HTTPS URL", e);
        } catch (UnknownHostException e) {
            throw new ServletException("Error calculating base HTTPS URL", e);
        }
    }

    protected URL getHttpBaseUrl(ServletConfig config, int httpPort) throws ServletException {
        String httpUrlParameter = config.getServletContext().getInitParameter("http.url");

        try {
            if (httpUrlParameter == null) {
                String host = InetAddress.getLocalHost().getHostAddress();
                return new URL("http://" + host + ":" + httpPort);
            } else {
                return new URL("http://" + httpUrlParameter + ":" + httpPort);
            }
        } catch (MalformedURLException e) {
            throw new ServletException("Invalid base HTTP URL", e);
        } catch (UnknownHostException e) {
            throw new ServletException("Error calculating base HTTP URL", e);
        }
    }

}
