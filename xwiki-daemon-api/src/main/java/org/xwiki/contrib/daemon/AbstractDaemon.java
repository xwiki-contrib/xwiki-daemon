/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.environment.System;

/**
 * @version $Id: $
 */
public abstract class AbstractDaemon implements Daemon, Initializable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDaemon.class);

    protected Thread thread;

    private String daemonName;

    @Override
    public void initialize() throws InitializationException
    {
        initializeShutdownHook();
    }

    private void initializeShutdownHook()
    {
        final AbstractDaemon daemon = this;
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try {
                    LOGGER.info("Stopping {}, please wait...", getDaemonName());
                    daemon.stop();
                    daemon.join();
                    LOGGER.info("{} has been stopped.", getDaemonName());
                } catch (Exception e) {
                    LOGGER.error("Unexpected error", e);
                }
            }
        });
    }

    @Override
    public void start() throws Exception
    {
        LOGGER.info("Starting {}...", getDaemonName());
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void join() throws Exception
    {
        if (thread == null) {
            throw new Exception(String.format("%s is not running.", getDaemonName()));
        }
        thread.join();
    }

    /**
     * Helper to create a daemon and run it.
     * @return the running daemon
     */
    public static Daemon initAndStartDaemon() throws Exception
    {
        try {
            // Create the component manager
            ComponentManager componentManager = System.initialize();

            // Get and run the daemon
            Daemon daemon = componentManager.getInstance(Daemon.class);
            daemon.start();

            return daemon;
        } catch (Exception e) {
            LOGGER.error("Unexpected error.", e);
            throw e;
        }
    }

    public String getDaemonName()
    {
        return daemonName;
    }

    public void setDaemonName(String daemonName)
    {
        this.daemonName = daemonName;
    }
}
