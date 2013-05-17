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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  $Id$
 *
 */

package org.ow2.sirocco.cloudmanager.connector.itests;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.sirocco.cloudmanager.connector.api.ConnectorException;
import org.ow2.sirocco.cloudmanager.connector.api.ICloudProviderConnector;
import org.ow2.sirocco.cloudmanager.connector.api.IComputeService;
import org.ow2.sirocco.cloudmanager.connector.api.IVolumeService;
import org.ow2.sirocco.cloudmanager.connector.api.ProviderTarget;
import org.ow2.sirocco.cloudmanager.model.cimi.Address;
import org.ow2.sirocco.cloudmanager.model.cimi.Credentials;
import org.ow2.sirocco.cloudmanager.model.cimi.DiskTemplate;
import org.ow2.sirocco.cloudmanager.model.cimi.Machine;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineCreate;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterface;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterfaceAddress;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplate;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplateNetworkInterface;
import org.ow2.sirocco.cloudmanager.model.cimi.MachineVolume;
import org.ow2.sirocco.cloudmanager.model.cimi.Network;
import org.ow2.sirocco.cloudmanager.model.cimi.Volume;
import org.ow2.sirocco.cloudmanager.model.cimi.VolumeConfiguration;
import org.ow2.sirocco.cloudmanager.model.cimi.VolumeCreate;
import org.ow2.sirocco.cloudmanager.model.cimi.VolumeTemplate;
import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProvider;
import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProviderAccount;
import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProviderLocation;

public class CloudProviderConnectorTest {
    private static final String MACHINE_CONFIG_CPU_PROP = "machineconfig.cpu";

    private static final String MACHINE_CONFIG_MEMORY_PROP = "machineconfig.memory";

    private static final String MACHINE_CONFIG_DISKS_PROP = "machineconfig.disks";

    private static final String MACHINE_IMAGE_ID_PROP = "machineimage.id";

    private static final String MACHINE_IMAGE_KEY_PROP = "machineimage.key";

    private static final String MACHINE_STOP_PROP = "machine.stop";

    private static final String VOLUME_ATTACH_PROP = "volume.attach";

    private static final String VOLUME_CONFIG_SIZE_PROP = "volumeconfig.size";

    private static final String VOLUME_DEVICE_PROP = "volume.device";

    private static final String PUBLIC_KEY_PROP = "credentials.publickey";

    private static final String LOCATION_COUNTRY_PROP = "location.country";

    private static final int ASYNC_OPERATION_WAIT_TIME_IN_SECONDS = 240;

    private ICloudProviderConnector connector;

    private CloudProviderAccount cloudProviderAccount;

    private CloudProviderLocation location;

    private String providerName;

    private int machineConfigCpu;

    private int machineConfigMemory;

    private int[] machineConfigDiskSizes;

    private int volumeConfigSizeGB;

    private String volumeDevice;

    private String imageId;

    private String imageKey;

    private String key;

    private boolean testStopMachine, testVolumeAttach;

    @Before
    public void setUp() throws Exception {
        this.providerName = System.getProperty("test.provider");
        if (this.providerName == null) {
            throw new Exception("Missing test.provider property");
        }
        Properties prop = new Properties();
        InputStream in = this.getClass().getResourceAsStream(this.providerName.toLowerCase() + ".properties");
        prop.load(in);
        in.close();

        this.machineConfigCpu = Integer.valueOf(prop.getProperty(CloudProviderConnectorTest.MACHINE_CONFIG_CPU_PROP));
        this.machineConfigMemory = Integer.valueOf(prop.getProperty(CloudProviderConnectorTest.MACHINE_CONFIG_MEMORY_PROP));
        this.imageId = prop.getProperty(CloudProviderConnectorTest.MACHINE_IMAGE_ID_PROP);
        this.imageKey = prop.getProperty(CloudProviderConnectorTest.MACHINE_IMAGE_KEY_PROP);
        String diskSizes[] = prop.getProperty(CloudProviderConnectorTest.MACHINE_CONFIG_DISKS_PROP).split(", ");
        this.machineConfigDiskSizes = new int[diskSizes.length];
        for (int i = 0; i < diskSizes.length; i++) {
            this.machineConfigDiskSizes[i] = Integer.valueOf(diskSizes[i]);
        }
        this.volumeConfigSizeGB = Integer.valueOf(prop.getProperty(CloudProviderConnectorTest.VOLUME_CONFIG_SIZE_PROP));
        this.volumeDevice = prop.getProperty(CloudProviderConnectorTest.VOLUME_DEVICE_PROP);

        String login = System.getProperty("test.login");
        String password = System.getProperty("test.password");
        String endpoint = System.getProperty("test.endpoint");

        this.key = prop.getProperty(CloudProviderConnectorTest.PUBLIC_KEY_PROP);

        this.testStopMachine = Boolean.valueOf(prop.getProperty(CloudProviderConnectorTest.MACHINE_STOP_PROP));
        this.testVolumeAttach = Boolean.valueOf(prop.getProperty(CloudProviderConnectorTest.VOLUME_ATTACH_PROP));

        String className = "org.ow2.sirocco.cloudmanager.connector." + this.providerName.toLowerCase() + "."
            + this.providerName + "CloudProviderConnector";
        Class<?> connectorClass = Class.forName(className);

        this.connector = (ICloudProviderConnector) connectorClass.newInstance();

        this.location = null;
        String country = prop.getProperty(CloudProviderConnectorTest.LOCATION_COUNTRY_PROP);
        if (country == null) {
            this.location = this.connector.getLocations().iterator().next();
        } else {
            for (CloudProviderLocation loc : this.connector.getLocations()) {
                if (loc.getCountryName().equals(country)) {
                    this.location = loc;
                    break;
                }
            }
        }
        if (this.location == null) {
            throw new Exception("Cannot find suitable provider location for country " + country);
        }
        this.cloudProviderAccount = new CloudProviderAccount();
        this.cloudProviderAccount.setLogin(login);
        this.cloudProviderAccount.setPassword(password);

        CloudProvider cloudProvider = new CloudProvider();
        cloudProvider.setEndpoint(endpoint);

        this.cloudProviderAccount.setCloudProvider(cloudProvider);

    }

    private Machine.State waitForMachineState(final IComputeService computeService, final ProviderTarget target,
        final String machineId, final int seconds, final Machine.State... expectedStates) throws Exception {
        int tries = seconds;
        while (tries-- > 0) {
            Machine.State machineState = computeService.getMachineState(machineId, target);
            if (machineState == Machine.State.ERROR) {
                throw new Exception("Machine state ERROR");
            }
            for (Machine.State expectedFinalState : expectedStates) {
                if (machineState == expectedFinalState) {
                    return machineState;
                }
            }
            Thread.sleep(1000);
        }
        throw new Exception("Timeout waiting for Machine state transition");
    }

    private Volume.State waitForVolumeState(final IVolumeService volumeService, final ProviderTarget target,
        final String volumeId, final int seconds, final Volume.State... expectedStates) throws Exception {
        int tries = seconds;
        while (tries-- > 0) {
            Volume.State volumeState = volumeService.getVolumeState(volumeId, target);
            if (volumeState == Volume.State.ERROR) {
                throw new Exception("Volume state ERROR");
            }
            for (Volume.State expectedFinalState : expectedStates) {
                if (volumeState == expectedFinalState) {
                    return volumeState;
                }
            }
            Thread.sleep(1000);
        }
        throw new Exception("Timeout waiting for Volume state transition");
    }

    @Test
    public void computeAndVolumeServiceTest() throws Exception {
        ProviderTarget target = new ProviderTarget().account(this.cloudProviderAccount).location(this.location);
        IComputeService computeService = this.connector.getComputeService();
        IVolumeService volumeService = this.connector.getVolumeService();

        MachineCreate machineCreate = new MachineCreate();
        MachineTemplate machineTemplate = new MachineTemplate();
        MachineConfiguration machineConfiguration = new MachineConfiguration();
        machineConfiguration.setCpu(this.machineConfigCpu);
        machineConfiguration.setMemory(this.machineConfigMemory * 1024);
        List<DiskTemplate> disks = new ArrayList<DiskTemplate>();
        for (int diskSizeGB : this.machineConfigDiskSizes) {
            DiskTemplate disk = new DiskTemplate();
            disk.setCapacity(diskSizeGB * 1000 * 1000);
            disks.add(disk);
        }
        machineConfiguration.setDisks(disks);
        machineTemplate.setMachineConfig(machineConfiguration);
        MachineImage machineImage = new MachineImage();
        machineImage.setProviderAssignedId(this.imageId);
        Map<String, String> imageProps = new HashMap<String, String>();
        if (this.imageKey != null) {
            imageProps.put(this.imageKey, this.imageId);
        }
        machineImage.setProperties(imageProps);
        machineTemplate.setMachineImage(machineImage);
        List<MachineTemplateNetworkInterface> nics = new ArrayList<MachineTemplateNetworkInterface>();
        MachineTemplateNetworkInterface nic = new MachineTemplateNetworkInterface();
        nic.setNetworkType(Network.Type.PRIVATE);
        nics.add(nic);
        nic = new MachineTemplateNetworkInterface();
        nic.setNetworkType(Network.Type.PUBLIC);
        nics.add(nic);
        machineTemplate.setNetworkInterfaces(nics);
        if (this.key != null) {
            Credentials credentials = new Credentials();
            credentials.setPublicKey(this.key);
            machineTemplate.setCredential(credentials);
        }
        machineTemplate.setUserData("color=blue\nip=1.2.3.4\n");
        machineCreate.setMachineTemplate(machineTemplate);
        machineCreate.setName("test");

        System.out.println("Creating machine...");
        Machine machine = computeService.createMachine(machineCreate, target);
        String machineId = machine.getProviderAssignedId();
        this.waitForMachineState(computeService, target, machine.getProviderAssignedId(),
            CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS, Machine.State.STARTED, Machine.State.STOPPED);

        machine = computeService.getMachine(machine.getProviderAssignedId(), target);
        System.out.println("Machine id=" + machine.getProviderAssignedId() + " state=" + machine.getState());
        for (MachineNetworkInterface netInterface : machine.getNetworkInterfaces()) {
            System.out.print("\t Network " + netInterface.getNetworkType() + " addresses=");
            if (netInterface.getAddresses() != null) {
                for (MachineNetworkInterfaceAddress addr : netInterface.getAddresses()) {
                    Address address = addr.getAddress();
                    if (address != null) {
                        System.out.print(address.getIp() + " ");
                    }
                }
            }
            System.out.println();
        }
        if (this.testStopMachine) {
            if (machine.getState() == Machine.State.STOPPED) {
                System.out.println("Starting machine " + machineId);
                computeService.startMachine(machineId, target);
                this.waitForMachineState(computeService, target, machine.getProviderAssignedId(),
                    CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS, Machine.State.STARTED);
            } else {
                System.out.println("Stopping machine " + machineId);
                computeService.stopMachine(machineId, false, target);
                this.waitForMachineState(computeService, target, machine.getProviderAssignedId(),
                    CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS, Machine.State.STOPPED);
                System.out.println("Starting machine " + machineId);
                computeService.startMachine(machineId, target);
                this.waitForMachineState(computeService, target, machine.getProviderAssignedId(),
                    CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS, Machine.State.STARTED);
            }
        }

        VolumeCreate volumeCreate = new VolumeCreate();
        VolumeTemplate volumeTemplate = new VolumeTemplate();
        VolumeConfiguration volumeConfig = new VolumeConfiguration();
        volumeConfig.setCapacity(this.volumeConfigSizeGB * 1000 * 1000);
        volumeTemplate.setVolumeConfig(volumeConfig);
        volumeCreate.setVolumeTemplate(volumeTemplate);
        volumeCreate.setName("test");
        volumeCreate.setDescription("a test volume");

        System.out.println("Creating Volume size=" + this.volumeConfigSizeGB + "GB");
        Volume volume = volumeService.createVolume(volumeCreate, target);
        String volumeId = volume.getProviderAssignedId();
        this.waitForVolumeState(volumeService, target, volumeId,
            CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS, Volume.State.AVAILABLE);
        volume = volumeService.getVolume(volumeId, target);
        System.out.println("Volume id=" + volume.getProviderAssignedId() + " size=" + volume.getCapacity() + " KB");

        if (this.testVolumeAttach) {
            MachineVolume machineVolume = new MachineVolume();
            machineVolume.setVolume(volume);
            machineVolume.setInitialLocation(this.volumeDevice);
            System.out.println("Attaching volume " + volume.getProviderAssignedId() + " to machine " + machineId);
            computeService.addVolumeToMachine(machineId, machineVolume, target);

            int seconds = CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS;
            while (seconds-- > 0) {
                machine = computeService.getMachine(machineId, target);
                if (machine.getVolumes().get(0).getState() != MachineVolume.State.ATTACHING) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
            Assert.assertEquals(MachineVolume.State.ATTACHED, machine.getVolumes().get(0).getState());

            System.out.println("Detaching volume " + volume.getProviderAssignedId() + " from machine " + machineId);
            computeService.removeVolumeFromMachine(machineId, machineVolume, target);
            seconds = CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS;
            while (seconds-- > 0) {
                machine = computeService.getMachine(machineId, target);
                if (machine.getVolumes().isEmpty()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
            Assert.assertTrue(machine.getVolumes().isEmpty());
        }

        System.out.println("Deleting volume " + volumeId);
        volumeService.deleteVolume(volumeId, target);
        try {
            this.waitForVolumeState(volumeService, target, volumeId,
                CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS, Volume.State.DELETED);
        } catch (ConnectorException ex) {
            // OK
        }
        try {
            volume = volumeService.getVolume(volumeId, target);
            Assert.fail("Volume still exists after deletion");
        } catch (ConnectorException ex) {
            // OK
        }

        System.out.println("Deleting machine " + machineId);
        computeService.deleteMachine(machineId, target);
        try {
            this.waitForMachineState(computeService, target, machine.getProviderAssignedId(),
                CloudProviderConnectorTest.ASYNC_OPERATION_WAIT_TIME_IN_SECONDS, Machine.State.DELETED);
        } catch (ConnectorException ex) {
            // OK
        }
        try {
            machine = computeService.getMachine(machineId, target);
            Assert.fail("Volume still exists after deletion");
        } catch (ConnectorException ex) {
            // OK
        }

    }
}
