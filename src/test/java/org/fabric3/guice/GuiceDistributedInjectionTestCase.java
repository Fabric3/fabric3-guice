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
package org.fabric3.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.TestCase;
import org.fabric3.api.node.Bootstrap;
import org.fabric3.api.node.Domain;
import org.fabric3.api.node.Fabric;

/**
 *
 */
public class GuiceDistributedInjectionTestCase extends TestCase {
    private Fabric fabric1;
    private Fabric fabric2;

    public void testZMQDeploy() throws Exception {
        Domain domain1 = fabric1.getDomain();
        domain1.deploy("TestZeroMQService", new TestZMQServiceImpl());

        Thread.sleep(1000);

        Domain domain2 = fabric2.getDomain();

        TestZeroMQServiceModule testModule = new TestZeroMQServiceModule(domain2);
        Injector injector = Guice.createInjector(testModule);

        TestServiceClient client = injector.getInstance(TestServiceClient.class);
        assertEquals("test", client.invokeField("test"));

        TestZMQService service = domain2.getService(TestZMQService.class);
        service.invoke("This is a message");
    }

    public void setUp() throws Exception {
        fabric1 = Bootstrap.initialize(getClass().getResource("systemConfig1.xml")).addProfile("zeromq");
        fabric1.start();

        fabric2 = Bootstrap.initialize(getClass().getResource("systemConfig2.xml")).addProfile("zeromq");
        fabric2.start();
        Thread.sleep(1000);
    }

    public void tearDown() throws Exception {
        fabric1.stop();
    }
}
