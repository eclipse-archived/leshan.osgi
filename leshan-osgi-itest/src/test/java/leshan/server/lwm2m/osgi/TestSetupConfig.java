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

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.net.InetAddress;

import javax.inject.Inject;

import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.osgi.framework.BundleContext;

public class TestSetupConfig {

    OsgiBasedClientRegistry osgiRegistry;
    InetAddress address;

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        Option[] options = new Option[] {
                systemProperty("pax.exam.logging").value("none"),
                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject().startLevel(
                        Constants.START_LEVEL_SYSTEM_BUNDLES),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject().startLevel(
                        Constants.START_LEVEL_SYSTEM_BUNDLES),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject().startLevel(
                        Constants.START_LEVEL_SYSTEM_BUNDLES),
                mavenBundle("com.google.code.gson", "gson").versionAsInProject(),
                mavenBundle("org.osgi", "org.osgi.compendium").versionAsInProject(),
                mavenBundle("org.eclipse.leshan", "leshan-core").versionAsInProject(),
                mavenBundle("org.eclipse.leshan", "leshan-client-core").versionAsInProject(),
                mavenBundle("org.eclipse.leshan", "leshan-client-cf").versionAsInProject(),
                mavenBundle("org.eclipse.leshan", "leshan-server-core").versionAsInProject(),
                mavenBundle("org.eclipse.leshan", "leshan-server-cf").versionAsInProject(),
                mavenBundle("org.eclipse.leshan", "leshan-osgi").versionAsInProject(),
                mavenBundle("org.eclipse.californium", "californium-osgi").versionAsInProject(),
                mavenBundle("org.eclipse.californium", "scandium").versionAsInProject(),
                junitBundles()
        };

        // workaround to determine if we are running on jenkins
        if ("jenkins".equals(System.getProperty("user.name"))) {
            options = OptionUtils.combine(
                    options,
                    systemProperty("org.ops4j.pax.url.mvn.settings").value(
                            "/home/jenkins-user/settings-files/settings-edc.xml"));
        }

        return options;
    }

    @Before
    public void setUp() throws Exception {
        osgiRegistry = new OsgiBasedClientRegistry(context, null);
        address = InetAddress.getLocalHost();
    }

}
