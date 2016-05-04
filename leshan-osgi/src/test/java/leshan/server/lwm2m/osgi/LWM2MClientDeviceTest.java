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

import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.ObserveSpec;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.DiscoverRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteAttributesRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.request.LwM2mRequestSender;
import org.eclipse.leshan.util.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * TODO, please write javadoc for class 'Lwm2mUtilTest'!
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LWM2MClientDeviceTest {

    private static final long DEFAULT_REQUEST_TIMEOUT_MILLIS = 2000L;

    @Mock
    LwM2mRequestSender lwM2mRequestSenderMock;

    private Client client;

    @Before
    public void setUp() throws UnknownHostException {
        client = newClient();
    }

    @Test
    public void testRead() throws UnsupportedEncodingException, InterruptedException, UnknownHostException {

        final ReadRequest readRequest = new ReadRequest(3, 0, 9);
        final LWM2MClientDevice clientUnderTest = new LWM2MClientDevice(client, lwM2mRequestSenderMock);
        clientUnderTest.read(readRequest);

        verify(lwM2mRequestSenderMock).send(client, readRequest, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    @Test
    public void testWrite() throws UnsupportedEncodingException, InterruptedException, UnknownHostException {

        final WriteRequest writeRequest =
                new WriteRequest(3, 0, 9, 55L); // new battery level: 55%
        final LWM2MClientDevice clientUnderTest = new LWM2MClientDevice(client, lwM2mRequestSenderMock);
        clientUnderTest.write(writeRequest);

        verify(lwM2mRequestSenderMock).send(client, writeRequest, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    @Test
    public void testWriteAttribute() throws UnsupportedEncodingException, InterruptedException, UnknownHostException {

        final WriteAttributesRequest readRequest = new WriteAttributesRequest(3, ObserveSpec());
        final LWM2MClientDevice clientUnderTest = new LWM2MClientDevice(client, lwM2mRequestSenderMock);
        clientUnderTest.writeAttribute(readRequest);

        verify(lwM2mRequestSenderMock).send(client, readRequest, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    @Test
    public void testExecute() throws UnsupportedEncodingException, InterruptedException, UnknownHostException {

        final ExecuteRequest readRequest = new ExecuteRequest(3, 0, 9);
        final LWM2MClientDevice clientUnderTest = new LWM2MClientDevice(client, lwM2mRequestSenderMock);
        clientUnderTest.execute(readRequest);

        verify(lwM2mRequestSenderMock).send(client, readRequest, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    @Test
    public void testObserve() throws UnsupportedEncodingException, InterruptedException, UnknownHostException {

        final ObserveRequest readRequest = new ObserveRequest(3, 0, 9);
        final LWM2MClientDevice clientUnderTest = new LWM2MClientDevice(client, lwM2mRequestSenderMock);
        clientUnderTest.observe(readRequest);

        verify(lwM2mRequestSenderMock).send(client, readRequest, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    @Test
    public void testDiscover() throws UnsupportedEncodingException, InterruptedException, UnknownHostException {

        final DiscoverRequest readRequest = new DiscoverRequest(3, 0, 9);
        final LWM2MClientDevice clientUnderTest = new LWM2MClientDevice(client, lwM2mRequestSenderMock);
        clientUnderTest.discover(readRequest);

        verify(lwM2mRequestSenderMock).send(client, readRequest, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    private ObserveSpec ObserveSpec() {
        return new ObserveSpec.Builder().build();
    }

    private Client newClient() throws UnknownHostException {
        final String registrationId = RandomStringUtils.random(10, true, true);
        final String endpoint = "test" + registrationId;
        final InetAddress address = InetAddress.getLocalHost();
        final int port = 5683;
        final String lwM2mVersion = "1.0";
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
