package com.jcs.sbs.loadTest;

import com.jcs.sbs.auth.DefaultJCSCredentialsProviderChain;
import com.jcs.sbs.auth.JCSCredentials;
import com.jcs.sbs.auth.JCSCredentialsProvider;
import com.jcs.sbs.exceptions.PropertyNotFoundException;
import com.jcs.sbs.model.CreateVolumeRequest;
import com.jcs.sbs.model.CreateVolumeResult;
import com.jcs.sbs.model.DeleteVolumeRequest;
import com.jcs.sbs.model.DeleteVolumeResult;
import com.jcs.sbs.model.DescribeVolumesRequest;
import com.jcs.sbs.model.DescribeVolumesResult;
import com.jcs.sbs.model.Volume;
import com.jcs.sbs.service.JCSCompute;
import com.jcs.sbs.service.impl.JCSComputeClient;

public class VolumeOperationsThread implements Runnable {

    private String threadName;

    private static JCSCompute jcs;

    private int volumeSize;

    /**
     * @throws PropertyNotFoundException
     * @throws java.lang.Exception
     */
    public static void init() throws PropertyNotFoundException {

        JCSCredentialsProvider credentialsProviders = new DefaultJCSCredentialsProviderChain();
        JCSCredentials credentials = credentialsProviders.getCredentials();
        jcs = new JCSComputeClient(credentials);

    }

    public VolumeOperationsThread(String threadName, int volumeSize) throws PropertyNotFoundException {
        super();
        init();
        this.volumeSize = volumeSize;
        this.threadName = threadName;
    }

    private String getVolumeStatus(String volumeId) {
        try {

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
            DescribeVolumesResult describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
            for (Volume volume : describeVolumesResult.getVolumes()) {
                if (volume.getVolumeId().equals(volumeId)) {
                    return volume.getStatus();
                }
            }
            return "doesn't exist";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public void createAndDeleteVolume() {
        try {
            System.out.println("From thread : " + threadName);

            // Create empty volume

            CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest().withSize(volumeSize);
            CreateVolumeResult createVolumeResult = jcs.createVolume(createVolumeRequest);
            System.out.println(
                    "Create Volume result from Thread : " + threadName + ":\n" + createVolumeResult.toString());

            while (!getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("available")) {
                Thread.yield();
            }

            // Delete volume

            DeleteVolumeRequest deleteVolumeRequest = new DeleteVolumeRequest()
                    .withVolumeId(createVolumeResult.getVolume().getVolumeId());
            DeleteVolumeResult deleteVolumeResult = jcs.deleteVolume(deleteVolumeRequest);
            System.out.println(
                    "Delete Volume result from Thread : " + threadName + ":\n" + deleteVolumeResult.toString());

        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        createAndDeleteVolume();

    }

}
