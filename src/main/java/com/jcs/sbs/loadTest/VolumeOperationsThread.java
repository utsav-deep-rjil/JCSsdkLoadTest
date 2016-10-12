package com.jcs.sbs.loadTest;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcs.sbs.auth.JCSCredentials;
import com.jcs.sbs.common.JCSProperties;
import com.jcs.sbs.model.CreateVolumeRequest;
import com.jcs.sbs.model.CreateVolumeResult;
import com.jcs.sbs.model.DeleteVolumeRequest;
import com.jcs.sbs.model.DeleteVolumeResult;
import com.jcs.sbs.model.DescribeVolumesRequest;
import com.jcs.sbs.model.DescribeVolumesResult;
import com.jcs.sbs.service.JCSCompute;
import com.jcs.sbs.service.impl.JCSComputeClient;

public class VolumeOperationsThread implements Runnable{
	
	private String threadName;
	
	private static JCSCompute jcs;
	
	private int volumeSize;

	/**
	 * @throws java.lang.Exception
	 */
	public static void init() {

		JCSCredentials credentials = new JCSCredentials() {

			public String getJCSAccessKey() {
				return JCSProperties.getProperty("ACCESS_KEY");

			}

			public String getJCSSecretKey() {
				return JCSProperties.getProperty("SECRET_KEY");
			}

		};

		jcs = new JCSComputeClient(credentials);



	}

	public VolumeOperationsThread(String threadName,int volumeSize) {
		super();
		init();
		this.volumeSize = volumeSize;
		this.threadName = threadName;
	}

	private String getVolumeStatus(String volumeId){
		try {
			
			DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
			DescribeVolumesResult describeVolumesResult = jcs.describeVolumes(describeVolumesRequest);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(describeVolumesResult.toString().getBytes("UTF8")));
			Element root = document.getDocumentElement();
			NodeList sublist = root.getElementsByTagName("volumeSet");
			if(sublist.getLength() == 0){

				return "doesn't exist";
			}
			Element volumeSet = (Element)sublist.item(0);
			int itemsCount = volumeSet.getElementsByTagName("item").getLength();
			
			NodeList items = volumeSet.getElementsByTagName("item");
			for(int itr = 0; itr < itemsCount; itr++){
				Element item = (Element)items.item(itr);
				String currVolId = item.getElementsByTagName("volumeId").item(0).getTextContent();
				if(currVolId.equals(volumeId)){
					System.out.println(item.getElementsByTagName("status").item(0).getTextContent());
					return item.getElementsByTagName("status").item(0).getTextContent();
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

			//Create empty volume
			
			CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest().withSize(volumeSize);
			CreateVolumeResult createVolumeResult = jcs.createVolume(createVolumeRequest);
			System.out.println("Create Volume result from Thread : " + threadName + ":\n" + createVolumeResult.toString());

			while(!getVolumeStatus(createVolumeResult.getVolume().getVolumeId()).equals("available")){
				Thread.yield();	
			}
			
			
			//Delete volume
			
			DeleteVolumeRequest deleteVolumeRequest = new DeleteVolumeRequest()
					.withVolumeId(createVolumeResult.getVolume().getVolumeId());
			DeleteVolumeResult deleteVolumeResult = jcs.deleteVolume(deleteVolumeRequest);
			System.out.println("Delete Volume result from Thread : " + threadName + ":\n" + deleteVolumeResult.toString());
			
		}
		catch (InterruptedException e) {
			System.out.println("Thread " +  threadName + " interrupted.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void run() {
		createAndDeleteVolume();
		
	}

}
