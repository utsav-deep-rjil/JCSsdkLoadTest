package com.jcs.sbs.loadTest;

import com.jcs.sbs.exceptions.PropertyNotFoundException;

public class Main {

    

    public static void main(String[] args) throws PropertyNotFoundException{


    	System.out.println(args.toString());
		int threads = 10, volumeSize = 10;
    	if(args.length > 0){
    		threads = Integer.parseInt(args[0]);
    	}
    	if(args.length > 1){
    		volumeSize = Integer.parseInt(args[1]);
    	}
    	for(int threadNo = 1; threadNo <= threads; threadNo++){
    		Thread thread = new Thread(new VolumeOperationsThread("Thread_" + threadNo,volumeSize));
    		thread.start();
    	}
    }
}


