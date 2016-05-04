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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.ObserveSpec;
import org.eclipse.leshan.client.LwM2mClient;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.request.LwM2mClientRequestSender;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.util.LinkFormatHelper;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.json.ObjectModelSerializer;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.codec.LwM2mNodeEncoder;
import org.eclipse.leshan.core.node.codec.json.LwM2mNodeJsonDecoder;
import org.eclipse.leshan.core.node.codec.opaque.LwM2mNodeOpaqueDecoder;
import org.eclipse.leshan.core.node.codec.text.LwM2mNodeTextDecoder;
import org.eclipse.leshan.core.node.codec.tlv.LwM2mNodeTlvEncoder;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.LwM2mRequest;
import org.eclipse.leshan.core.request.exception.ResourceAccessException;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.json.LwM2mJson;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.bootstrap.SecurityMode;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientUpdate;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.observation.ObservationRegistry;
import org.eclipse.leshan.server.registration.RegistrationHandler;
import org.eclipse.leshan.server.request.LwM2mRequestSender;
import org.eclipse.leshan.server.security.SecurityInfo;
import org.eclipse.leshan.tlv.Tlv;
import org.eclipse.leshan.util.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * TestCases for lwm2m Interface Client Registration. These tests are executed
 * as integration test with PaxExam in real osgi environment.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiRegistryTest extends TestSetupConfig {

    private static final String[] LESHAN_CORE_CLASSES_TO_LOAD = new String[]{
                            // one class from each exported package
                            ObserveSpec.class.getName(),
                            ResourceModel.class.getName(),
                            ObjectModelSerializer.class.getName(),
                            LwM2mNode.class.getName(),
                            LwM2mNodeEncoder.class.getName(),
                            LwM2mNodeJsonDecoder.class.getName(),
                            LwM2mNodeOpaqueDecoder.class.getName(),
                            LwM2mNodeTextDecoder.class.getName(),
                            LwM2mNodeTlvEncoder.class.getName(),
                            Observation.class.getName(),
                            LwM2mRequest.class.getName(),
                            ResourceAccessException.class.getName(),
                            LwM2mResponse.class.getName(),
                            LwM2mJson.class.getName(),
                            Tlv.class.getName()
    };
    private static final String[] LESHAN_SERVER_CORE_CLASSES_TO_LOAD = new String[]{
                            // one class from each exported package
                            LwM2mServer.class.getName(),
                            SecurityMode.class.getName(),
                            Client.class.getName(),
                            LwM2mModelProvider.class.getName(),
                            ObservationRegistry.class.getName(),
                            RegistrationHandler.class.getName(),
                            LwM2mRequestSender.class.getName(),
                            SecurityInfo.class.getName()
    };
    private static final String[] LESHAN_SERVER_CF_CLASSES_TO_LOAD = new String[]{
                            // one class from each exported package
                            LeshanServerBuilder.class.getName()
    };
    private static final String[] LESHAN_CLIENT_CORE_CLASSES_TO_LOAD = new String[]{
                            // one class from each exported package
                            LwM2mClient.class.getName(),
                            LwM2mClientRequestSender.class.getName(),
                            LwM2mObjectEnabler.class.getName(),
                            LinkFormatHelper.class.getName()
    };
    private static final String[] LESHAN_CLIENT_CF_CLASSES_TO_LOAD = new String[]{
                            // one class from each exported package
                            LeshanClient.class.getName()
    };

    @Test
    public void checkBundleContext() {
        assertNotNull(context);
    }

    @Test
    public void checkLwm2mBundles() {
        boolean leshancoreFound = false;
        boolean leshancoreActive = false;

        boolean leshanosgiFound = false;
        boolean leshanosgiActive = false;

        final Bundle[] bundles = context.getBundles();
        for (final Bundle bundle : bundles) {
            if (bundle != null && bundle.getSymbolicName() != null) {
                if (bundle.getSymbolicName().equals("leshan-core")) {
                    leshancoreFound = true;
                    if (bundle.getState() == Bundle.ACTIVE) {
                        leshancoreActive = true;
                    }
                }
            }
            if (bundle.getSymbolicName().equals("leshan-osgi")) {
                leshanosgiFound = true;
                if (bundle.getState() == Bundle.ACTIVE) {
                    leshanosgiActive = true;
                }
            }
        }

        assertTrue(leshancoreFound);
        assertTrue(leshancoreActive);
        assertTrue(leshanosgiFound);
        assertTrue(leshanosgiActive);
    }

    @Test
    public void testLeshanClassesCanBeLoaded() {
        loadClasses(LESHAN_CORE_CLASSES_TO_LOAD);
        loadClasses(LESHAN_SERVER_CORE_CLASSES_TO_LOAD);
        loadClasses(LESHAN_SERVER_CF_CLASSES_TO_LOAD);
        loadClasses(LESHAN_CLIENT_CORE_CLASSES_TO_LOAD);
        loadClasses(LESHAN_CLIENT_CF_CLASSES_TO_LOAD);
    }

    private void loadClasses(final String[] classNames) {
        for (final String clazzName : classNames) {
            try {
                context.getBundle().loadClass(clazzName);
            } catch (final ClassNotFoundException e) {
                Assert.fail(String.format("Should have been able to load class %s", clazzName));
            }
        }
    }

    @Test
    public void testRegisterClientAtOsgiRegistry() throws InvalidSyntaxException {
        registerSeveralCients();
        final String id = registerClientReturnID();
        Assert.assertNotNull(findByRegistrationId(id));
    }

    @Test
    public void testUpdateClientAtOsgiRegistry() throws InvalidSyntaxException {
        // registerSeveralCients();
        final Client client = newClient();
        osgiRegistry.registerClient(client);
        ServiceReference<LWM2MClientDevice> ref = findByRegistrationId(client.getRegistrationId());
        final String originalExpiry = (String) ref.getProperty(Property.REGISTRATION_EXPIRATION);

        final String updateSms = "00000000";
        final BindingMode updatebinding = BindingMode.UQS;
        final Long updateLifetime = client.getLifeTimeInSec() + 50000L;
        final Map<String, String> attribs = new HashMap<>();
        final LinkObject[] objectLinks = new LinkObject[] { new LinkObject("/3/1", attribs) };

        final ClientUpdate up = new ClientUpdate(client.getRegistrationId(), client.getAddress(), client.getPort(),
            updateLifetime, updateSms, updatebinding, objectLinks);
        osgiRegistry.updateClient(up);
        ref = findByRegistrationId(client.getRegistrationId());
        final LWM2MClientDevice device = context.getService(ref);

        Assert.assertEquals(updatebinding, device.getClient().getBindingMode());
        Assert.assertEquals(updateSms, device.getClient().getSmsNumber());
        Assert.assertEquals(updateLifetime, device.getClient().getLifeTimeInSec());
        // Assert.assertArrayEquals(objectLinks,
        // device.getClient().getObjectLinks());
        final String updatedExpiry = (String) ref.getProperty(Property.REGISTRATION_EXPIRATION);
        Assert.assertTrue("Expiration should have been extended after updating Client",
            Long.valueOf(originalExpiry) < Long.valueOf(updatedExpiry));
    }

    private ServiceReference<LWM2MClientDevice> findByRegistrationId(final String id) throws InvalidSyntaxException {
        final String query = String.format("(%s=%s)", Property.REGISTRATION_ID, id);
        final Collection<ServiceReference<LWM2MClientDevice>> col = context.getServiceReferences(
            LWM2MClientDevice.class, query);
        if (!col.isEmpty()) {
            return col.iterator().next();
        } else {
            return null;
        }
    }

    @Test
    public void testDeregisterClientAtOsgiRegistry() throws InvalidSyntaxException {
        registerSeveralCients();
        final String id = registerClientReturnID();
        osgiRegistry.deregisterClient(id);
        Assert.assertNull(findByRegistrationId(id));
    }

    @Test
    public void getClientByEndpoint() {
        final String epID = registerSeveralCients();

        final Client c = osgiRegistry.get(epID);
        final String ep = c.getEndpoint();
        Assert.assertEquals(epID, ep);
    }

    private String registerClientReturnEp() {
        final Client client = newClient();
        osgiRegistry.registerClient(client);

        return client.getEndpoint();

    }

    private String registerClientReturnID() {
        final Client client = newClient();
        osgiRegistry.registerClient(client);

        return client.getRegistrationId();

    }

    private String registerSeveralCients() {

        registerClientReturnEp();
        registerClientReturnEp();
        final String id = registerClientReturnEp();
        registerClientReturnEp();

        return id;
    }

    private Client newClient() {
        final String registrationId = RandomStringUtils.random(10, true, true);
        final String endpoint = "test" + registrationId;
        final InetAddress address = this.address;
        final int port = 5683;
        final String lwM2mVersion = "0.1.1";
        final Long lifetimeInSec = 10000L;
        final String smsNumber = "0170" + RandomStringUtils.random(7, false, true);
        final BindingMode bindingMode = BindingMode.U;
        final Map<String, String> attribs = new HashMap<>();
        final LinkObject[] objectLinks = new LinkObject[] { new LinkObject("/3/1", attribs),
                new LinkObject("/1", attribs), new LinkObject("/1/52343", attribs),
                new LinkObject("/13/52343", attribs), new LinkObject("/567/45", attribs) };
        Client.Builder builder = new Client.Builder(registrationId, endpoint, address, port,
                InetSocketAddress.createUnresolved("localhost", 5683));
        final Client c = builder.lwM2mVersion(lwM2mVersion).lifeTimeInSec(lifetimeInSec).smsNumber(smsNumber)
                .bindingMode(bindingMode).objectLinks(objectLinks).build();
        return c;
    }
}
