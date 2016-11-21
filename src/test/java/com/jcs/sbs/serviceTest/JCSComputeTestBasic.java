/**
 * 
 */
package com.jcs.sbs.serviceTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jcs.sbs.auth.DefaultJCSCredentialsProviderChain;
import com.jcs.sbs.auth.JCSCredentials;
import com.jcs.sbs.auth.JCSCredentialsProvider;
import com.jcs.sbs.exceptions.PropertyNotFoundException;
import com.jcs.sbs.model.CreateSnapshotRequest;
import com.jcs.sbs.model.CreateSnapshotResult;
import com.jcs.sbs.model.CreateVolumeRequest;
import com.jcs.sbs.model.CreateVolumeResult;
import com.jcs.sbs.model.DeleteSnapshotRequest;
import com.jcs.sbs.model.DeleteSnapshotResult;
import com.jcs.sbs.model.DeleteVolumeRequest;
import com.jcs.sbs.model.DeleteVolumeResult;
import com.jcs.sbs.service.JCSCompute;
import com.jcs.sbs.service.impl.JCSComputeClient;

public class JCSComputeTestBasic {

    private static final Log log = LogFactory.getLog(JCSComputeTestBasic.class);
    
    private static TestUtils testUtils;
    private static JCSCompute jcs;

    @BeforeClass
    public static void setUpBeforeClass() throws PropertyNotFoundException {

        JCSCredentialsProvider credentialsProviders = new DefaultJCSCredentialsProviderChain();
        JCSCredentials credentials = credentialsProviders.getCredentials();
        jcs = new JCSComputeClient(credentials);
        testUtils = new TestUtils();
    }

    @Test
    public void createAndDeleteTest() {
        try {
            // deleteAllVolumes();

            // Create empty volume test

            int initialVolumeCount = testUtils.getVolumesCount();

            CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest().withSize(10);
            CreateVolumeResult createVolumeResult = jcs.createVolume(createVolumeRequest);

            while (!testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("available"))
                ;
            log.info(createVolumeResult.toString());
            int volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("create volume basic test 1", initialVolumeCount + 1, equalTo(volumeCountAfterCreation));

            // create snapshot test

            int initialSnapshotCount = testUtils.getSnapshotsCount();

            CreateSnapshotRequest createSnapshotRequest = new CreateSnapshotRequest()
                    .withVolumeId(createVolumeResult.getVolume().getVolumeId());
            CreateSnapshotResult createSnapshotResult = jcs.createSnapshot(createSnapshotRequest);
            while (!testUtils.getSnapshotStatus(createSnapshotResult.getSnapshot().getSnapshotId()).equals("completed"))
                ;
            log.info(createSnapshotResult.toString());
            int snapshotCountAfterCreation = testUtils.getSnapshotsCount();
            assertThat("create snapshot basic test", snapshotCountAfterCreation, equalTo(initialSnapshotCount + 1));

            // delete volume test1

            DeleteVolumeRequest deleteVolumeRequest = new DeleteVolumeRequest()
                    .withVolumeId(createVolumeResult.getVolume().getVolumeId());
            DeleteVolumeResult deleteVolumeResult = jcs.deleteVolume(deleteVolumeRequest);
            assertThat("Delete volume basic test 1: returned value:", deleteVolumeResult.isDeleted(), equalTo(true));

            log.info(deleteVolumeResult.toString());

            while (testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("deleting"))
                ;
            int finalVolumeCount = testUtils.getVolumesCount();
            assertThat("delete volume basic test 1", finalVolumeCount, equalTo(volumeCountAfterCreation - 1));

            // create volume from snapshot test

            initialVolumeCount = testUtils.getVolumesCount();

            createVolumeRequest = new CreateVolumeRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId());
            createVolumeResult = jcs.createVolume(createVolumeRequest);

            log.info(createVolumeResult.toString());

            while (!testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("available"))
                ;
            volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("create volume basic test 2", initialVolumeCount + 1, equalTo(volumeCountAfterCreation));
            assertThat("create volume basic test 2 (snapshotId)", createVolumeResult.getVolume().getSnapshotId(),
                    equalTo(createSnapshotResult.getSnapshot().getSnapshotId()));
            assertThat("create volume basic test 2 (size)", createVolumeResult.getVolume().getSize(),
                    equalTo(createSnapshotResult.getSnapshot().getSize()));

            // delete volume test2

            while (!testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("available"))
                ;
            deleteVolumeRequest = new DeleteVolumeRequest().withVolumeId(createVolumeResult.getVolume().getVolumeId());
            deleteVolumeResult = jcs.deleteVolume(deleteVolumeRequest);
            assertThat("Delete volume basic test 2 returned value:", deleteVolumeResult.isDeleted(), equalTo(true));

            log.info(deleteVolumeResult.toString());
            while (testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("deleting"))
                ;
            finalVolumeCount = testUtils.getVolumesCount();
            assertThat("delete volume basic test 2", volumeCountAfterCreation - 1, equalTo(finalVolumeCount));

            // delete snapshot test

            DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId());
            DeleteSnapshotResult deleteSnapshotResult = jcs.deleteSnapshot(deleteSnapshotRequest);
            log.info(deleteSnapshotResult.toString());
            assertThat("Delete snapshot basic test 1 returned value:", deleteSnapshotResult.isDeleted(), equalTo(true));

            while (testUtils.getSnapshotStatus(createSnapshotResult.getSnapshot().getSnapshotId()).equals("deleting"))
                ;
            int finalSnapshotCount = testUtils.getSnapshotsCount();
            assertThat("delete snapshot basic test", snapshotCountAfterCreation - 1, equalTo(finalSnapshotCount));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }

    }

}