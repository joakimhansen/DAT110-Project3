package no.hvl.dat110.node.peers;

import no.hvl.dat110.middleware.NodeServer;

public class Process1 {

	public static void main(String[] args) throws Exception {
		Thread.sleep(1000);     
		new NodeServer("process1", 9091);   
	}

} 