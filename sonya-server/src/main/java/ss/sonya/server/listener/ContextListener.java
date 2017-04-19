/*
 * Copyright (C) 2017 ss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ss.sonya.server.listener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.Logger;

/**
 * Server context listener.
 * @author ss
 */
@WebListener
public class ContextListener implements ServletContextListener {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ContextListener.class);
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        LOG.info("application context initialized...");
    }
    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        Driver d = null;
        while (drivers.hasMoreElements()) {
            try {
                d = drivers.nextElement();
                DriverManager.deregisterDriver(d);
                LOG.debug("driver derigistered [" + d + "]");
            } catch (SQLException ex) {
                LOG.error("error deregister driver [" + d + "]", ex);
            }
        }
        try {
            AbandonedConnectionCleanupThread.shutdown();
        } catch (InterruptedException ex) {
            LOG.error("can't stop Abandoned thread!", ex);
        }
        LOG.info("application context destroyed...");
    }
}
