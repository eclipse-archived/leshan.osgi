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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistry;
import org.eclipse.leshan.server.client.ClientRegistryListener;
import org.eclipse.leshan.server.client.ClientUpdate;
import org.eclipse.leshan.server.request.LwM2mRequestSender;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi based Implementation of the lwm2m {@link ClientRegistry}. The clients
 * are registered in this implementation to the OSGi service registry
 */
public class OsgiBasedClientRegistry implements ClientRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiBasedClientRegistry.class);

    private final BundleContext context;
    private final LwM2mRequestSender requestSender;
    private final Map<String, ServiceRegistration<LWM2MClientDevice>> registrations = new ConcurrentHashMap<String, ServiceRegistration<LWM2MClientDevice>>();
    private final List<ClientRegistryListener> crListeners = new CopyOnWriteArrayList<>();

    /**
     * ScheduledExecutorService checks the availability of a registered
     * lwm2m-client.
     */
    private final ScheduledExecutorService schedExecutor = Executors.newScheduledThreadPool(1);

    /**
     * Constructor for new OsgiBasedClientRegistry. A ClientRegistry to register
     * Clients at OSGi Service Registry.
     *
     * @param bundleContext {@link BundleContext}
     * @param requestSender {@link LwM2mRequestSender}
     */
    public OsgiBasedClientRegistry(final BundleContext bundleContext, final LwM2mRequestSender requestSender) {
        context = bundleContext;
        this.requestSender = requestSender;
        start();
    }

    @Override
    public Client get(final String endpoint) {
        final ServiceRegistration<LWM2MClientDevice> sreg = registrations.get(endpoint);
        final LWM2MClientDevice device = context.getService(sreg.getReference());
        return device.getClient();
    }

    @Override
    public Collection<Client> allClients() {
        final List<Client> result = new ArrayList<>(registrations.size());

        for (final ServiceRegistration<LWM2MClientDevice> lw : registrations.values()) {
            final LWM2MClientDevice device = context.getService(lw.getReference());
            result.add(device.getClient());
        }

        return result;
    }

    @Override
    public void addListener(final ClientRegistryListener listener) {
        crListeners.add(listener);
    }

    @Override
    public void removeListener(final ClientRegistryListener listener) {
        crListeners.remove(listener);
    }

    @Override
    public boolean registerClient(final Client client) {

        // Instantiate LWM2MDevice as wrapper around Client object and
        // register as DEVICE in OSGi registry
        final LWM2MClientDevice lwm2mclient = new LWM2MClientDevice(client, requestSender);
        registerClientAtOsgiRegistry(lwm2mclient);

        for (final ClientRegistryListener crl : crListeners) {
            crl.registered(client);
        }
        return true;

    }

    @Override
    public Client updateClient(final ClientUpdate clientUpdate) {
        final ServiceRegistration<LWM2MClientDevice> registration = getServiceRegistrationById(clientUpdate
            .getRegistrationId());
        if (registration == null) {
            LOG.warn("updateClient(); return null: no client is registered under the given Registration-ID {}",
                clientUpdate.getRegistrationId());
            return null;
        }

        final ServiceReference<LWM2MClientDevice> ref = registration.getReference();
        final LWM2MClientDevice device = context.getService(ref);

        if (device != null) {
            LOG.debug("Updating registration for client: {}", clientUpdate);
            applyUpdate(device, clientUpdate);

            final Dictionary<String, Object> newProps = device.getServiceRegistrationProperties(device.getClient());
            registration.setProperties(newProps);

            for (final ClientRegistryListener crl : crListeners) {
                crl.updated(device.getClient());
            }

            return device.getClient();
        } else {
            LOG.warn(String.format(
                "updateClient(); return null: no LWM2MClientDevice is found under the given Registration-ID %s",
                clientUpdate.getRegistrationId()));

            return null;
        }
    }

    @Override
    public Client deregisterClient(final String registrationId) {

        final ServiceRegistration<LWM2MClientDevice> registration = getServiceRegistrationById(registrationId);

        if (registration != null) {
            final ServiceReference<LWM2MClientDevice> ref = registration.getReference();
            final LWM2MClientDevice device = context.getService(ref);
            context.ungetService(ref);
            registration.unregister();

            if (registrations.remove(device.getClient().getEndpoint()) == null) {
                LOG.warn(String.format("[deregisterClient()] no Service found with endpointID = %s", device.getClient()
                    .getEndpoint()));
            }
            LOG.debug(String.format(
                "[deregisterClient()] ungetService and unregister Client with endpointID=%s  ,id=%s", device
                .getClient().getEndpoint(), device.getClient().getRegistrationId()));

            for (final ClientRegistryListener crl : crListeners) {
                crl.unregistered(device.getClient());
            }

            return device.getClient();
        }
        LOG.warn(String.format("[deregisterClient()] no Client found with registrationId = %s", registrationId));

        return null;
    }

    /**
     * register a new LWM2MClientDevice at the OSGI Service Registry with
     * service Properties: <br>
     * LWM2M_REGISTRATION_EXPIRATION <br>
     * LWM2M_REGISTRATIONID <br>
     * LWM2M_OBJECTS <br>
     * SERVICE_PID <br>
     * DEVICE_CATEGORY</br> <br>
     * If the LWM2MClientDevice has already registered, the InetAddress is
     * updated.<br>
     * If the ServiceRegistration object has already been unregistered, the
     * LWM2MClientDevice will be registered again.
     *
     * @param client
     * @return
     */
    private Client registerClientAtOsgiRegistry(final LWM2MClientDevice client) {

        if (!(registrations.containsKey(client.getClient().getEndpoint()))) {
            LOG.trace(
                "[registerClientAtOsgiRegistry()] Register new LWM2MClientDevice at osgi ServiceRegistry with ep= {}",
                client.getClient().getEndpoint());
            registerService(client);

            LOG.trace(String.format("[registerClientAtOsgiRegistry()] origin host: %s", client.getClient().getAddress()
                .toString()));

        } else {
            LOG.trace("[registerClientAtOsgiRegistry()] update a LWM2MClientDevice Servicereference while clientregistration");

            // lwm TS:
            // If the LWM2M Client sends a “Register” operation to the LWM2M
            // Server even though the LWM2M Server has registration information
            // of the LWM2M Client, the LWM2M Server removes the existing
            // registration information and performs the new “Register”
            // operation. This situation happens when the LWM2M Client forgets
            // the state of the LWM2M Server (e.g., factory reset).
            final ServiceRegistration<LWM2MClientDevice> reg = registrations.get(client.getClient().getEndpoint());
            try {
                final ServiceReference<LWM2MClientDevice> ref = reg.getReference();

                for (final ClientRegistryListener crl : crListeners) {
                    crl.unregistered(context.getService(ref).getClient());
                }

                // This is the stale registration information for the given
                // client's end-point name
                // This may happen, if a client somehow loses track of its
                // registration status with this server and simply starts over
                // with a new registration request in order to remedy the
                // situation.
                // According to the LWM2M spec an implementation must remove the
                // stale registration information in this case.
                final LWM2MClientDevice staleClient = context.getService(ref);

                reg.unregister();
                context.ungetService(ref);
                registerService(client);

                if (staleClient != null) {
                    return staleClient.getClient();
                }

                LOG.trace(String.format("[registerClientAtOsgiRegistry()] changed host: %s", client.getClient()
                    .getAddress().toString()));
            } catch (final IllegalStateException e) {
                // IllegalStateException - If this ServiceRegistration object
                // has already been unregistered
                registerService(client);

                LOG.warn(
                    "[registerClientAtOsgiRegistry()] ServiceRegistration object has already been unregistered, register again.",
                    e);
            }
        }
        // return null because no stale registration info exists for the
        // end-point
        return null;
    }

    /**
     * register the LWM2MClientDevice as service in osgi service registry.
     *
     * @param client
     */
    private void registerService(final LWM2MClientDevice client) {
        final ServiceRegistration<LWM2MClientDevice> registration = context.registerService(LWM2MClientDevice.class,
            client, client.getServiceRegistrationProperties(client.getClient()));

        registrations.put(client.getClient().getEndpoint(), registration);
    }

    protected ServiceRegistration<LWM2MClientDevice> getServiceRegistrationById(final String registrationId) {

        final Collection<ServiceRegistration<LWM2MClientDevice>> all = registrations.values();
        for (final ServiceRegistration<LWM2MClientDevice> sreg : all) {
            final String id = (String) sreg.getReference().getProperty(Property.REGISTRATION_ID);
            if (registrationId.equals(id)) {
                return sreg;
            }
        }
        return null;
    }

    private void applyUpdate(final LWM2MClientDevice device, final ClientUpdate update) {
        if (device == null) {
            LOG.warn("no client to update");
            return;
        } else {
            final Client cl = device.getClient();
            final Date lastUpdate = new Date();
            InetAddress address;
            int port;
            LinkObject[] lobj;
            long lifetime;
            BindingMode bindingMode;
            String sms;

            if (update.getAddress() != null) {
                address = update.getAddress();
            } else {
                address = cl.getAddress();
            }

            if (update.getPort() != null) {
                port = update.getPort();
            } else {
                port = cl.getPort();
            }

            if (update.getObjectLinks() != null) {
                lobj = update.getObjectLinks();
            } else {
                lobj = cl.getObjectLinks();
            }

            if (update.getLifeTimeInSec() != null) {
                lifetime = update.getLifeTimeInSec();
            } else {
                lifetime = cl.getLifeTimeInSec();
            }

            if (update.getBindingMode() != null) {
                bindingMode = update.getBindingMode();
            } else {
                bindingMode = cl.getBindingMode();
            }

            if (update.getSmsNumber() != null) {
                sms = update.getSmsNumber();
            } else {
                sms = cl.getSmsNumber();
            }

            final Client clientUpdated = new Client(cl.getRegistrationId(), cl.getEndpoint(), address, port,
                cl.getLwM2mVersion(), lifetime, sms, bindingMode, lobj, cl.getRegistrationEndpointAddress(),
                cl.getRegistrationDate(), lastUpdate);

            device.updateClient(clientUpdated);
        }
    }

    /**
     * start the registration manager, will start regular cleanup of dead
     * registrations.
     */
    private void start() {
        // every 5 seconds clean the registration list
        // It is also conceivable to configure period,
        final ScheduledFuture<?> future = schedExecutor.scheduleAtFixedRate(new Cleaner(), 1, 5, TimeUnit.SECONDS);
        LOG.trace("start ScheduledExecutorService with Cleaner Thread, with period 60s");
        if (future.isCancelled()) {
            LOG.trace("canceled");
        }
    }

    /**
     * Stop the underlying cleanup of the registrations.
     */
    public void stop() throws InterruptedException {
        schedExecutor.shutdownNow();
        schedExecutor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Cleaner Thread.
     */
    private class Cleaner implements Runnable {

        @Override
        public void run() {

            for (final Entry<String, ServiceRegistration<LWM2MClientDevice>> e : registrations.entrySet()) {
                // force de-registration
                try {
                    final ServiceReference<LWM2MClientDevice> ref = e.getValue().getReference();
                    final LWM2MClientDevice lwmClient = context.getService(ref);
                    if (lwmClient != null) {
                        if (lwmClient.isAlive()) {
                            LOG.trace(String.format("[Cleaner]: client: %s, id: %s, alive", lwmClient.getClient()
                                .getEndpoint(), lwmClient.getClient().getRegistrationId()));
                        } else {
                            LOG.trace(String.format("[Cleaner]: client: %s, id:%s deregisterd", lwmClient.getClient()
                                .getEndpoint(), lwmClient.getClient().getRegistrationId()));
                            deregisterClient(lwmClient.getClient().getRegistrationId());
                        }
                    }
                } catch (final IllegalStateException ex) {
                    // IllegalStateException - If this ServiceRegistration
                    // object has already been unregistered.
                    LOG.warn(String
                        .format("[Cleaner]: Device ServiceRegistration object with endpointId %s has already been unregistered: ",
                            e.getKey()));
                }
            }
        }
    }

}
