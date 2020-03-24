package no.hvl.dat110.middleware;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import no.hvl.dat110.chordoperations.ChordProtocols;
import no.hvl.dat110.chordoperations.JoinRing;
import no.hvl.dat110.rpc.interfaces.NodeInterface;

/**
 * @author tdoy
 * dat110: Project 3
 */


public class NodeServer {
	
	private String nodename;
	private int port;
	private NodeInterface chordnode;
	
	public NodeServer(String nodename, int port){
		this.nodename = nodename;
		this.port = port;
		start();										// start the server aka registry
		
		joinRing();										// attempt to join an existing ring or create a new one
		stabilizationProtocols();						// start the stabilization protocols
	}
	
	public NodeServer(String nodename, int port, boolean gui){
		this.nodename = nodename;
		this.port = port;
		start();										// start the server aka registry
	}
	
	public void start() {
		
		try {		
			// create registry and start it on port 9091
			Registry registry = LocateRegistry.createRegistry(port);
			
			// Make a new instance (stub) of the implementation class
			chordnode = new Node(nodename, port);
			
			// Bind the remote object (stub) in the registry
			registry.bind(nodename, chordnode);
			
			System.out.println(nodename+" server is running... ");
		}catch(Exception e) {
			System.err.println("Node Server: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void joinRing() {
		// attempt to join the ring
		JoinRing join;
		try {
			join = new JoinRing(chordnode);
			join.join();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void stabilizationProtocols() {
		
		ChordProtocols protocols = new ChordProtocols(chordnode);
		
		Runnable runnable = new Runnable() {

			@Override 
			public void run() {
				while(true) {
					protocols.updateSuccessor();
					protocols.stabilizeRing();
					protocols.fixFingerTable();
					protocols.checkPredecessor();
					protocols.printInfo();
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}		
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void leaveRing() {
		
		try {
			new ChordLookup((Node) chordnode).leaveRing();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}

	public NodeInterface getNode() {
		
		return chordnode;
	}
	
	public static void main(String args[]) throws RemoteException {
		
		NodeServer server = new NodeServer("process0", 9090); 
		server.start();
	}

}
