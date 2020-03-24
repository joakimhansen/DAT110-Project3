package no.hvl.dat110.node.peers;

import no.hvl.dat110.middleware.NodeServer;

public class Process5 {

	public static void main(String[] args) throws Exception {
		Thread.sleep(1000);     
		new NodeServer("process5", 9095);   
	}

} 