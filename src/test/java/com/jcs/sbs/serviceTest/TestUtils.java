package com.jcs.sbs.serviceTest;

import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcs.sbs.auth.DefaultJCSCredentialsProviderChain;
import com.jcs.sbs.auth.JCSCredentials;
import com.jcs.sbs.auth.JCSCredentialsProvider;
import com.jcs.sbs.exceptions.PropertyNotFoundException;
import com.jcs.sbs.model.DeleteVolumeRequest;
import com.jcs.sbs.model.DescribeSnapshotsRequest;
import com.jcs.sbs.model.DescribeSnapshotsResult;
import com.jcs.sbs.model.DescribeVolumesRequest;
import com.jcs.sbs.model.DescribeVolumesResult;
import com.jcs.sbs.model.Snapshot;
import com.jcs.sbs.model.Volume;
import com.jcs.sbs.service.JCSCompute;
import com.jcs.sbs.service.impl.JCSComputeClient;

public class TestUtils {

    private static final Log log = LogFactory.getLog(TestUtils.class);
    
    private JCSCompute jcs;
    
    public TestUtils() throws PropertyNotFoundException{

        JCSCredentialsProvider credentialsProviders = new DefaultJCSCredentialsProviderChain();
        JCSCredentials credentials = credentialsProviders.getCredentials();
        jcs = new JCSComputeClient(credentials);
    }
    
    public int getSnapshotsCount() {
        try {
            DescribeSnapshotsRequest describeSnapshotsRequest = new DescribeSnapshotsRequest();
            DescribeSnapshotsResult describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
            log.info("Snapshots count: "+describeSnapshotsResult.getSnapshots().size());
            return describeSnapshotsResult.getSnapshots().size();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
            return 0;
        }
    }

    public int getVolumesCount() {
        try {

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
            DescribeVolumesResult describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            log.info("Volumes count: "+describeVolumesResult.getVolumes().size());
            return describeVolumesResult.getVolumes().size();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
            return 0;
        }
    }

    // Covers basic describe volume test
    public String getVolumeStatus(String volumeId) {
        try {

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
            DescribeVolumesResult describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            for (Volume volume : describeVolumesResult.getVolumes()) {
                if (volume.getVolumeId().equals(volumeId)) {
                    log.info(volume.getStatus());
                    return volume.getStatus();
                }
            }
            log.info("doesn't exist");
            return "doesn't exist";

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
            log.info("Error");
            return "Error";
        }
    }

    // Covers basic describe snapshot test
    public String getSnapshotStatus(String snapshotId) {
        try {

            DescribeSnapshotsRequest describeSnapshotsRequest = new DescribeSnapshotsRequest();
            DescribeSnapshotsResult describeSnapshotsResult = jcs.describeSnapshots(describeSnapshotsRequest);
            for (Snapshot snapshot : describeSnapshotsResult.getSnapshots()) {
                if (snapshot.getSnapshotId().equals(snapshotId)) {
                    log.info(snapshot.getStatus());
                    return snapshot.getStatus();
                }
            }
            log.info("doesn't exist");
            return "doesn't exist";

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
            log.info("Error");
            return "Error";
        }
    }

    public void deleteAllVolumes() {
        try {

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
            DescribeVolumesResult describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            for (Volume volume : describeVolumesResult.getVolumes()) {
                if (volume.getStatus().equals("available")) {
                    jcs.deleteVolume(new DeleteVolumeRequest(volume.getVolumeId()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
