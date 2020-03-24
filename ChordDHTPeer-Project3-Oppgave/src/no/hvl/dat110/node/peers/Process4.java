package no.hvl.dat110.node.peers;


import no.hvl.dat110.middleware.NodeServer;

public class Process4 {

	public static void main(String[] args) throws Exception {
		Thread.sleep(2000);
		new NodeServer("process4", 9094);
	}
 
}
