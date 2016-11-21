package com.jcs.sbs.serviceTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
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
import com.jcs.sbs.model.DescribeSnapshotsRequest;
import com.jcs.sbs.model.DescribeSnapshotsResult;
import com.jcs.sbs.model.DescribeVolumesRequest;
import com.jcs.sbs.model.DescribeVolumesResult;
import com.jcs.sbs.model.Snapshot;
import com.jcs.sbs.model.Volume;
import com.jcs.sbs.service.JCSCompute;
import com.jcs.sbs.service.impl.JCSComputeClient;

public class JCSComputeTestVariableArguments {

    private static final Log log = LogFactory.getLog(JCSComputeTestVariableArguments.class);

    private static JCSCompute jcs;

    private static TestUtils testUtils;

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

            /*
             * volumes and volumeIds will be used later for describe volumes
             * test operations with specified volume IDs
             */
            List<Volume> volumes = new ArrayList<Volume>();
            List<String> volumeIds = new ArrayList<String>();

            // Create empty encrypted volume test

            int initialVolumeCount = testUtils.getVolumesCount();

            CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest().withSize(10).withEncryption();
            CreateVolumeResult createVolumeResult = jcs.createVolume(createVolumeRequest);

            log.info(createVolumeResult.toString());
            int volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("Create volume var-args test 1: volumes count: ", initialVolumeCount + 1,
                    equalTo(volumeCountAfterCreation));
            assertThat("Create volume var-args test 1: Size of created volume: ",
                    createVolumeResult.getVolume().getSize(), equalTo(createVolumeRequest.getSize()));
            assertThat("Create volume var-args test 1: isEncryped: ", createVolumeResult.getVolume().getEncrypted(),
                    equalTo(true));

            volumes.add(createVolumeResult.getVolume());
            volumeIds.add(createVolumeResult.getVolume().getVolumeId());

            // create snapshot from encrypted volume test

            int initialSnapshotCount = testUtils.getSnapshotsCount();

            CreateSnapshotRequest createSnapshotRequest = new CreateSnapshotRequest()
                    .withVolumeId(createVolumeResult.getVolume().getVolumeId());
            CreateSnapshotResult createSnapshotResult = jcs.createSnapshot(createSnapshotRequest);

            log.info(createSnapshotResult.toString());

            int snapshotCountAfterCreation = testUtils.getSnapshotsCount();
            assertThat("Create snapshot var-args test: snapshots count: ", initialSnapshotCount + 1,
                    equalTo(snapshotCountAfterCreation));
            assertThat("Create snapshot var-args test: isEncrypted: ",
                    createSnapshotResult.getSnapshot().getEncrypted(), equalTo(true));
            assertThat("Create snapshot var-args test: size: ", createSnapshotResult.getSnapshot().getSize(),
                    equalTo(createVolumeResult.getVolume().getSize()));

            while (!testUtils.getSnapshotStatus(createSnapshotResult.getSnapshot().getSnapshotId()).equals("completed"))
                ;

            // create volume with snapshotId and size (>= snapshot size) test

            initialVolumeCount = testUtils.getVolumesCount();

            int size = createSnapshotResult.getSnapshot().getSize() + 10;
            createVolumeRequest = new CreateVolumeRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId()).withSize(size);
            createVolumeResult = jcs.createVolume(createVolumeRequest);

            log.info(createVolumeResult.toString());

            volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("Create volume var-args test 2: volumes count: ", initialVolumeCount + 1,
                    equalTo(volumeCountAfterCreation));
            assertThat("Create volume var-args test 2: snapshotId: ", createVolumeResult.getVolume().getSnapshotId(),
                    equalTo(createSnapshotResult.getSnapshot().getSnapshotId()));
            assertThat("Create volume var-args test 2: size:", createVolumeResult.getVolume().getSize(), equalTo(size));
            assertThat("Create volume var-args test 2: encryption: ", createVolumeResult.getVolume().getEncrypted(),
                    equalTo(createSnapshotResult.getSnapshot().getEncrypted()));

            volumes.add(createVolumeResult.getVolume());
            volumeIds.add(createVolumeResult.getVolume().getVolumeId());

            // create volume with snapshotId, size (>= snapshot size) and
            // volumeType test

            initialVolumeCount = testUtils.getVolumesCount();
            size = createSnapshotResult.getSnapshot().getSize() + 10;

            createVolumeRequest = new CreateVolumeRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId()).withSize(size)
                    .withVolumeType("standard");

            log.info(createVolumeRequest.toString());

            createVolumeResult = jcs.createVolume(createVolumeRequest);

            log.info(createVolumeResult.toString());

            volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("Create volume var-args test 3: volumes count: ", initialVolumeCount + 1,
                    equalTo(volumeCountAfterCreation));
            assertThat("Create volume var-args test 3: snapshotId: ", createVolumeResult.getVolume().getSnapshotId(),
                    equalTo(createSnapshotResult.getSnapshot().getSnapshotId()));
            assertThat("Create volume var-args test 3: size:", createVolumeResult.getVolume().getSize(), equalTo(size));
            assertThat("Create volume var-args test 3: encryption: ", createVolumeResult.getVolume().getEncrypted(),
                    equalTo(createSnapshotResult.getSnapshot().getEncrypted()));
            assertThat("Create volume var-args test 3: volumeType: ", createVolumeResult.getVolume().getVolumeType(),
                    equalTo("standard"));

            volumes.add(createVolumeResult.getVolume());
            volumeIds.add(createVolumeResult.getVolume().getVolumeId());

            // create volume test with fields: snapshotId (of encrypted
            // snapshot), size (>= snapshot size),
            // volumeType and encryption (= false) (if either source snapshot is
            // encrypted or encrypted field of request is true, the created
            // volume will be encrypted)

            initialVolumeCount = testUtils.getVolumesCount();
            size = createSnapshotResult.getSnapshot().getSize() + 10;

            createVolumeRequest = new CreateVolumeRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId()).withSize(size)
                    .withVolumeType("standard");
            createVolumeRequest.setEncrypted(false);

            log.info(createVolumeRequest.toString());

            createVolumeResult = jcs.createVolume(createVolumeRequest);

            log.info(createVolumeResult.toString());

            volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("Create volume var-args test 4: volumes count: ", initialVolumeCount + 1,
                    equalTo(volumeCountAfterCreation));
            assertThat("Create volume var-args test 4: snapshotId: ", createVolumeResult.getVolume().getSnapshotId(),
                    equalTo(createSnapshotResult.getSnapshot().getSnapshotId()));
            assertThat("Create volume var-args test 4: size:", createVolumeResult.getVolume().getSize(), equalTo(size));
            assertThat("Create volume var-args test 4: encryption: ", createVolumeResult.getVolume().getEncrypted(),
                    equalTo(createSnapshotResult.getSnapshot().getEncrypted() || createVolumeRequest.getEncrypted()));
            assertThat("Create volume var-args test 4: volumeType: ", createVolumeResult.getVolume().getVolumeType(),
                    equalTo("standard"));

            volumes.add(createVolumeResult.getVolume());
            volumeIds.add(createVolumeResult.getVolume().getVolumeId());

            Collections.sort(volumeIds);

            // describe volumes test with specific volume IDs

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest().withVolumeIds(volumeIds);
            DescribeVolumesResult describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            List<String> resultVolumeIds = new ArrayList<String>();
            for (Volume volume : describeVolumesResult.getVolumes()) {
                resultVolumeIds.add(volume.getVolumeId());
            }

            Collections.sort(resultVolumeIds);
            assertArrayEquals("Describe volume var-args test 1: no. of volumes: ", volumeIds.toArray(),
                    resultVolumeIds.toArray());

            // describe volumes test with maxResults

            List<Volume> expectedVolumes = jcs.describeVolumes(new DescribeVolumesRequest()).getVolumes();

            List<Volume> resultVolumes = new ArrayList<Volume>();
            List<Volume> currentResult;
            int currentResultSize;
            describeVolumesRequest = new DescribeVolumesRequest().withMaxResults(5);
            describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            log.info(describeVolumesResult.toString());
            currentResult = describeVolumesResult.getVolumes();
            currentResultSize = currentResult.size();

            resultVolumes.addAll(currentResult);
            
            assertTrue("Describe volume var-args test 2: maxResults >= actual no of volumes returned: ",
                    currentResultSize <= describeVolumesRequest.getMaxResults());
            
            while (currentResultSize > 0) {
                describeVolumesRequest = new DescribeVolumesRequest().withMaxResults(5)
                        .withNextToken(currentResult.get(currentResultSize-1).getVolumeId());
                describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
                

                currentResult = describeVolumesResult.getVolumes();
                currentResultSize = currentResult.size();

                resultVolumes.addAll(currentResult);
                
                assertTrue("Describe volume var-args test 2: maxResults >= actual no of volumes returned: ",
                        currentResultSize <= describeVolumesRequest.getMaxResults());

                log.info(new Gson().toJson(currentResult));

            }
            
            List<String> expectedVolumeIds = new ArrayList<String>();
            resultVolumeIds = new ArrayList<String>();

            for (Volume volume : expectedVolumes) {
                expectedVolumeIds.add(volume.getVolumeId());
            }

            for (Volume volume : resultVolumes) {
                resultVolumeIds.add(volume.getVolumeId());
            }

            Collections.sort(expectedVolumeIds);
            Collections.sort(resultVolumeIds);

            assertArrayEquals("Describe volume var-args test 2: with nextToken and maxResults: ",
                    expectedVolumeIds.toArray(), resultVolumeIds.toArray());

            // describe volume test with details

            describeVolumesRequest = new DescribeVolumesRequest().withDetail(true);
            describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            log.info(describeVolumesResult.toString());

            for (Volume volume : describeVolumesResult.getVolumes()) {
                assertTrue("Describe volumes var-args test 3: with detail: attachmentSet not null",
                        volume.getAttachmentSet() != null);
                assertTrue("Describe volumes var-args test 3: with detail: createTime not null",
                        volume.getCreateTime() != null);
                assertTrue("Describe volumes var-args test 3: with detail: snapshotId not null",
                        volume.getSnapshotId() != null);
                assertTrue("Describe volumes var-args test 3: with detail: size not null and not 0",
                        volume.getSize() != null && volume.getSize() != 0);
                assertTrue("Describe volumes var-args test 3: with detail: encrypted not null",
                        volume.getEncrypted() != null);
            }

            // describe Snapshots Test1: with details

            DescribeSnapshotsRequest describeSnapshotsRequest = new DescribeSnapshotsRequest().withDetail(true);
            DescribeSnapshotsResult describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
            log.info(describeSnapshotsResult.toString());

            for (Snapshot snapshot : describeSnapshotsResult.getSnapshots()) {
                assertTrue("Describe snapshots var-args test 1: with detail: size not null",
                        snapshot.getSize() != null);
                assertTrue("Describe snapshots var-args test 1: with detail: startTime not null",
                        snapshot.getStartTime() != null);
                assertTrue("Describe snapshots var-args test 1: with detail: encrypted not null",
                        snapshot.getEncrypted() != null);
            }
            
            // describe snapshots test2: with maxResults
            
            
            
            List<Snapshot> expectedSnapshots = jcs.describeSnapshots(new DescribeSnapshotsRequest()).getSnapshots();

            List<Snapshot> resultSnapshots = new ArrayList<Snapshot>();
            List<Snapshot> currentSnapshotResult;
            describeSnapshotsRequest = new DescribeSnapshotsRequest().withMaxResults(5);
            describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
            log.info(describeSnapshotsResult.toString());
            currentSnapshotResult = describeSnapshotsResult.getSnapshots();
            currentResultSize = currentSnapshotResult.size();

            resultSnapshots.addAll(currentSnapshotResult);
            
            assertTrue("Describe Snapshot var-args test 2: maxResults >= actual no of Snapshots returned: ",
                    currentResultSize <= describeSnapshotsRequest.getMaxResults());
            
            while (currentResultSize > 0) {
                describeSnapshotsRequest = new DescribeSnapshotsRequest().withMaxResults(5)
                        .withNextToken(currentSnapshotResult.get(currentResultSize-1).getSnapshotId());
                describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
                

                currentSnapshotResult = describeSnapshotsResult.getSnapshots();
                currentResultSize = currentSnapshotResult.size();

                resultSnapshots.addAll(currentSnapshotResult);
                
                assertTrue("Describe Snapshot var-args test 2: maxResults >= actual no of Snapshots returned: ",
                        currentResultSize <= describeSnapshotsRequest.getMaxResults());
                
                //System.out.println(new Gson().toJson(currentSnapshotResult));

            }
            
            List<String> expectedSnapshotIds = new ArrayList<String>();
            List<String> resultSnapshotIds = new ArrayList<String>();

            for (Snapshot Snapshot : expectedSnapshots) {
                expectedSnapshotIds.add(Snapshot.getSnapshotId());
            }

            for (Snapshot Snapshot : resultSnapshots) {
                resultSnapshotIds.add(Snapshot.getSnapshotId());
            }

            Collections.sort(expectedSnapshotIds);
            Collections.sort(resultSnapshotIds);

            log.info(new Gson().toJson(expectedSnapshotIds));
            log.info(new Gson().toJson(resultSnapshotIds));
            
            assertArrayEquals("Describe Snapshot var-args test 2: with nextToken and maxResults: ",
                    expectedSnapshotIds.toArray(), resultSnapshotIds.toArray());


            
            
            
            

            // delete volume test1

            DeleteVolumeRequest deleteVolumeRequest = new DeleteVolumeRequest()
                    .withVolumeId(createVolumeResult.getVolume().getVolumeId());
            DeleteVolumeResult deleteVolumeResult = jcs.deleteVolume(deleteVolumeRequest);
            log.info(deleteVolumeResult.toString());

            while (testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("deleting"))
                ;
            int finalVolumeCount = testUtils.getVolumesCount();
            assertThat("delete volume test 1", volumeCountAfterCreation - 1, equalTo(finalVolumeCount));

            // delete snapshot test

            DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId());
            DeleteSnapshotResult deleteSnapshotResult = jcs.deleteSnapshot(deleteSnapshotRequest);
            log.info(deleteSnapshotResult.toString());

            while (testUtils.getSnapshotStatus(createSnapshotResult.getSnapshot().getSnapshotId()).equals("deleting"))
                ;
            int finalSnapshotCount = testUtils.getSnapshotsCount();
            assertThat("delete snapshot test", snapshotCountAfterCreation - 1, equalTo(finalSnapshotCount));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }

    }
}
