/*******************************************************************************
 * Copyright (c) 2015, Bosch Software Innovations GmbH
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Bosch Software Innovations GmbH - OSGi support
 *******************************************************************************/
package leshan.server.lwm2m.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistryListener;
import org.eclipse.leshan.server.client.ClientUpdate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * A OSGi based ClientRegistryListener which sent a event via {@link EventAdmin}
 * if one of the methods is called.
 */
public class OsgiBasedClientRegistryListener implements ClientRegistryListener {

    private final BundleContext context;

    /**
     * Creates a new OsgiBasedClientRegistryListener.
     *
     * @param bundleContext
     */
    public OsgiBasedClientRegistryListener(final BundleContext bundleContext) {
        context = bundleContext;
    }

    /**
     * Invoked when a new client has been registered on the server. <br>
     * Sent a CLIENT_UNREGISTERED via {@link EventAdmin}
     *
     * @param client
     */
    @Override
    public void registered(final Client client) {
        sendEvent(Property.REGISTERED_EVENT, client);
    }

    /**
     * Invoked when a client has been updated. <br>
     * Sent a CLIENT_UPDATED via {@link EventAdmin}
     *
     * @param clientUpdated the client after the update
     */
    @Override
    public void updated(final ClientUpdate update, final Client clientUpdated) {
        sendEvent(Property.UPDATED_EVENT, clientUpdated);

    }

    /**
     * Invoked when a new client has been unregistered from the server. <br>
     * Sent a CLIENT_REGISTERED via {@link EventAdmin}
     *
     * @param client
     */
    @Override
    public void unregistered(final Client client) {
        sendEvent(Property.UNREGISTERED_EVENT, client);

    }

    private void sendEvent(final String topic, final Client client) {

        final ServiceReference<?> ref = context.getServiceReference(EventAdmin.class.getName());
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();

        properties.put(Property.CLIENT, client);

        final Event notifyEvent = new Event(topic, properties);

        final EventAdmin eventAdmin = (EventAdmin) context.getService(ref);
        eventAdmin.postEvent(notifyEvent);
    }
}
