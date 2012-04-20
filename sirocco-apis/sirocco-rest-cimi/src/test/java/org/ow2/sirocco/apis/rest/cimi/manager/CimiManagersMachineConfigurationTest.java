/**
 *
 * SIROCCO
 * Copyright (C) 2011 France Telecom
 * Contact: sirocco@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * $Id$
 *
 */
package org.ow2.sirocco.apis.rest.cimi.manager;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.sirocco.apis.rest.cimi.domain.CimiCpu;
import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineConfiguration;
import org.ow2.sirocco.apis.rest.cimi.domain.CimiMemory;
import org.ow2.sirocco.apis.rest.cimi.request.CimiContextImpl;
import org.ow2.sirocco.apis.rest.cimi.request.CimiRequest;
import org.ow2.sirocco.apis.rest.cimi.request.CimiResponse;
import org.ow2.sirocco.apis.rest.cimi.request.CimiSelect;
import org.ow2.sirocco.apis.rest.cimi.request.RequestHeader;
import org.ow2.sirocco.apis.rest.cimi.utils.ConstantsPath;
import org.ow2.sirocco.cloudmanager.core.api.IMachineManager;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Basic tests "end to end" for managers MachineConfiguration.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/context/managerContext.xml"})
public class CimiManagersMachineConfigurationTest {

    @Autowired
    @Qualifier("IMachineManager")
    private IMachineManager service;

    @Autowired
    @Qualifier("CimiManagerCreateMachineConfiguration")
    private CimiManager managerCreate;

    @Autowired
    @Qualifier("CimiManagerDeleteMachineConfiguration")
    private CimiManager managerDelete;

    @Autowired
    @Qualifier("CimiManagerReadMachineConfiguration")
    private CimiManager managerRead;

    @Autowired
    @Qualifier("CimiManagerUpdateMachineConfiguration")
    private CimiManager managerUpdate;

    private CimiRequest request;

    private CimiResponse response;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.request = new CimiRequest();
        this.response = new CimiResponse();

        this.request.setBaseUri("/");
        this.request.setContext(new CimiContextImpl(this.request));
        RequestHeader header = new RequestHeader();
        header.setCimiSelect(new CimiSelect());
        this.request.setHeader(header);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        EasyMock.reset(this.service);
    }

    @Test
    public void testCreate() throws Exception {
        MachineConfiguration create = new MachineConfiguration();
        create.setId(456);

        EasyMock.expect(this.service.createMachineConfiguration(EasyMock.anyObject(MachineConfiguration.class)))
            .andReturn(create);
        EasyMock.replay(this.service);

        CimiMachineConfiguration cimi = new CimiMachineConfiguration();
        cimi.setCpu(new CimiCpu(1f, "gigaHertz", 1));
        cimi.setMemory(new CimiMemory(1, "MiB"));

        this.request.setCimiData(cimi);
        this.managerCreate.execute(this.request, this.response);

        Assert.assertEquals(201, this.response.getStatus());
        Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH + "/456",
            ((CimiMachineConfiguration) this.response.getCimiData()).getId());
        EasyMock.verify(this.service);
    }

    @Test
    public void testRead() throws Exception {

        MachineConfiguration machine = new MachineConfiguration();
        machine.setId(1);
        EasyMock.expect(this.service.getMachineConfigurationById("1")).andReturn(machine);
        EasyMock.replay(this.service);

        this.request.setId("1");
        this.managerRead.execute(this.request, this.response);

        Assert.assertEquals(200, this.response.getStatus());
        Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH + "/1",
            ((CimiMachineConfiguration) this.response.getCimiData()).getId());
        EasyMock.verify(this.service);
    }

    @Test
    public void testDelete() throws Exception {

        this.service.deleteMachineConfiguration("1");
        EasyMock.replay(this.service);

        this.request.setId("1");
        this.managerDelete.execute(this.request, this.response);

        Assert.assertEquals(200, this.response.getStatus());
        EasyMock.verify(this.service);
    }

    @Test
    public void testUpdate() throws Exception {

        this.service.updateMachineConfiguration(EasyMock.anyObject(MachineConfiguration.class));
        EasyMock.replay(this.service);

        CimiMachineConfiguration cimi = new CimiMachineConfiguration();
        cimi.setName("foo");
        this.request.setId("1");
        this.request.setCimiData(cimi);

        this.managerUpdate.execute(this.request, this.response);

        Assert.assertEquals(200, this.response.getStatus());
        EasyMock.verify(this.service);
    }

    @Test
    public void testUpdateWithCimiSelect() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "fooName");
        map.put("description", "fooDescription");

        this.service.updateMachineConfigurationAttributes(EasyMock.eq("1"), EasyMock.eq(map));
        EasyMock.replay(this.service);

        CimiMachineConfiguration cimi = new CimiMachineConfiguration();
        cimi.setName("fooName");
        cimi.setDescription("fooDescription");
        this.request.setId("1");
        this.request.setCimiData(cimi);
        this.request.getHeader().getCimiSelect().setSelects(new String[] {"name", "description"});

        this.managerUpdate.execute(this.request, this.response);

        Assert.assertEquals(200, this.response.getStatus());
        EasyMock.verify(this.service);
    }

}
