package com.jcs.sbs.serviceTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

public class JCSComputeTestNegativeCases {

    private static final Log log = LogFactory.getLog(JCSComputeTestNegativeCases.class);

    private static JCSCompute jcs;

    private static TestUtils testUtils;
    

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws PropertyNotFoundException {

        JCSCredentialsProvider credentialsProviders = new DefaultJCSCredentialsProviderChain();
        JCSCredentials credentials = credentialsProviders.getCredentials();
        jcs = new JCSComputeClient(credentials);
        testUtils = new TestUtils();
    }



    @Test
    public void createAndDeleteTest() throws Exception{

            // Create empty volume test

            int initialVolumeCount = testUtils.getVolumesCount();
            int initialSnapshotCount = testUtils.getSnapshotsCount();

            CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest().withSize(50);
            CreateVolumeResult createVolumeResult = jcs.createVolume(createVolumeRequest);

            log.info(createVolumeResult.getXml());

            while(!testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("available"));
            int volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("Create volume basic test 1: volumes count: ", volumeCountAfterCreation,
                    equalTo(initialVolumeCount + 1));
            

            CreateSnapshotRequest createSnapshotRequest;
            CreateSnapshotResult createSnapshotResult;

            createSnapshotRequest = new CreateSnapshotRequest()
                    .withVolumeId(createVolumeResult.getVolume().getVolumeId());
            createSnapshotResult = jcs.createSnapshot(createSnapshotRequest);

            log.info(createSnapshotResult.toString());
            int snapshotCountAfterCreation = testUtils.getSnapshotsCount();
            assertThat("Create snapshot negative test 1: snapshots count: ", snapshotCountAfterCreation,
                    equalTo(initialSnapshotCount+1));


            while(!testUtils.getSnapshotStatus(createSnapshotResult.getSnapshot().getSnapshotId()).equals("completed"))
                ;

            DeleteVolumeRequest deleteVolumeRequest = new DeleteVolumeRequest().withVolumeId(createVolumeResult.getVolume().getVolumeId());
            jcs.deleteVolume(deleteVolumeRequest);


            initialVolumeCount = testUtils.getVolumesCount();

            // create volume with snapshotId and size ( < snapshot size) test

            initialVolumeCount = testUtils.getVolumesCount();

            int size = createSnapshotResult.getSnapshot().getSize() - 1;
            createVolumeRequest = new CreateVolumeRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId()).withSize(size);

            expectedException.expect(HttpException.class);
            createVolumeResult = jcs.createVolume(createVolumeRequest);
            

            volumeCountAfterCreation = testUtils.getVolumesCount();
            assertThat("Create volume negative test 2: volumes count: ", initialVolumeCount,
                    equalTo(volumeCountAfterCreation));

            // describe volumes test1 with negative or 0 as maxResults

            List<Volume> expectedVolumes = jcs.describeVolumes(new DescribeVolumesRequest()).getVolumes();
            List<Volume> resultVolumes;
            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest().withMaxResults(-5);
            DescribeVolumesResult describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);

            resultVolumes = describeVolumesResult.getVolumes();

            assertThat("Describe volume negative test 2: with negative maxResults: returns all volumes: ",
                    resultVolumes.size(), equalTo((expectedVolumes.size())));

            // delete volume negative test1: delete same volume twice

            while (!testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("available"))
                ;

            initialVolumeCount = testUtils.getVolumesCount();

             deleteVolumeRequest = new DeleteVolumeRequest()
                    .withVolumeId(createVolumeResult.getVolume().getVolumeId());
            DeleteVolumeResult deleteVolumeResult = jcs.deleteVolume(deleteVolumeRequest);
            log.info(deleteVolumeResult.toString());
            while (testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("deleting"))
                ;
            int finalVolumeCount = testUtils.getVolumesCount();
            assertThat("delete volume negative test 1: first deletion: ", volumeCountAfterCreation - 1,
                    equalTo(finalVolumeCount));

            deleteVolumeRequest = new DeleteVolumeRequest().withVolumeId(createVolumeResult.getVolume().getVolumeId());

            expectedException.expect(HttpException.class);
            deleteVolumeResult = jcs.deleteVolume(deleteVolumeRequest);
            log.info(deleteVolumeResult.toString());

            while (testUtils.getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("deleting"))
                ;
            int finalVolumeCount2 = testUtils.getVolumesCount();

            assertThat("delete volume negative test 1: second deletion: ", finalVolumeCount,
                    equalTo(finalVolumeCount2));

            // delete snapshot negative test1: delete same snapshot twice
                ;
            DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest()
                    .withSnapshotId(createSnapshotResult.getSnapshot().getSnapshotId());

            expectedException.expect(HttpException.class);
            DeleteSnapshotResult deleteSnapshotResult = jcs.deleteSnapshot(deleteSnapshotRequest);
            log.info(deleteSnapshotResult.toString());

            while (testUtils.getSnapshotStatus(createSnapshotResult.getSnapshot().getSnapshotId()).equals("deleting"))
                ;
            int finalSnapshotCount = testUtils.getSnapshotsCount();
            assertThat("delete snapshot test", finalSnapshotCount, equalTo(snapshotCountAfterCreation - 1));

            deleteSnapshotResult = jcs.deleteSnapshot(deleteSnapshotRequest);
            log.info(deleteSnapshotResult.toString());


            int finalSnapshotCount2 = testUtils.getSnapshotsCount();
            assertThat("delete snapshot test", finalSnapshotCount2, equalTo(finalSnapshotCount));

            // describe volume negative test2: random strings

            describeVolumesRequest = new DescribeVolumesRequest().withVolumeIds("abc", "def");
            
            expectedException.expect(HttpException.class);
            expectedException.expectMessage("HTTP/1.1 404 Not Found");
            describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            assertThat("Describe volume negative test 2: random strings: ", describeVolumesResult.getVolumes().size(),
                    equalTo(0));

            // describe snapshots negative test1: 0 or negative as maxResults

            DescribeSnapshotsRequest describeSnapshotsRequest = new DescribeSnapshotsRequest().withMaxResults(-2);

            DescribeSnapshotsResult describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
            List<Snapshot> actualResultSnapshots = describeSnapshotsResult.getSnapshots();

            describeSnapshotsRequest = new DescribeSnapshotsRequest();

            describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
            List<Snapshot> expectedResultSnapshots = describeSnapshotsResult.getSnapshots();

            assertArrayEquals("describe snapshot negative test 1: negative maxResults: ",
                    expectedResultSnapshots.toArray(), actualResultSnapshots.toArray());

            // describe snapshots negative test2: random strings

            describeSnapshotsRequest = new DescribeSnapshotsRequest().withSnapshotIds("abcd", "xyz");

            expectedException.expect(HttpException.class);
            expectedException.expectMessage("HTTP/1.1 404 Not Found");
            describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
            assertThat("describe snapshot negative test 2: random strings: ",
                    describeSnapshotsResult.getSnapshots().size(), equalTo(0));

        

    }

}
