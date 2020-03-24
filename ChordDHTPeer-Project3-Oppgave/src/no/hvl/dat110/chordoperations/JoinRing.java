package no.hvl.dat110.chordoperations;

/**
 * @author tdoy
 * dat110 - demo/exercise
 */

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import no.hvl.dat110.middleware.Node;
import no.hvl.dat110.rpc.interfaces.NodeInterface;
import no.hvl.dat110.util.Util;

public class JoinRing {
	
	private Node chordnode;

	public JoinRing(NodeInterface chordnode) throws RemoteException {
		this.chordnode = (Node) chordnode;
	}
	
	public void join() throws RemoteException {
				
		//Registry registry = Util.tryIPs();											// try the trackers IP addresses
		Registry registry = Util.tryIPSingleMachine(chordnode.getNodeName());			// try the ports

		if(registry != null) {
			try {
				//String haship = Hash.hashOf(Util.activeIP).toString();
				String foundNode = Util.activeIP;

				NodeInterface randomNode = (NodeInterface) registry.lookup(foundNode);
				
				System.out.println("JoinRing-randomNode = "+randomNode.getNodeName());
				// call remote findSuccessor function. The result will be the successor of this randomNode
				NodeInterface chordnodeSuccessor = randomNode.findSuccessor(chordnode.getNodeID());

				// set the successor of this node to chordnodeSuccessor and its predecessor to null
				chordnode.setSuccessor(chordnodeSuccessor);	
				chordnode.setPredecessor(null);
				
				// notify chordnodeSuccessor of a new predecessor
				chordnodeSuccessor.notify(chordnode);
				
				// fix the finger table
				ChordProtocols cp = new ChordProtocols(chordnode);
				cp.fixFingerTable();
				
				// copy all keys that are less or equal (<=) to chordnode ID to chordnode
				chordnode.copyKeysFromSuccessor(chordnodeSuccessor);
				
				System.out.println(chordnode.getNodeName()+" is between null | "+chordnodeSuccessor.getNodeName());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			
			createRing(chordnode);		// no node is available, create a new ring
		}
	}
	
	private void createRing(Node node) throws RemoteException {
		
		// set predecessor to nil - No predecessor for now
		node.setPredecessor(null);
		
		// set the successor to itself
		node.setSuccessor(node);
		
		System.out.println("New ring created. Node = "+node.getNodeName()+" | Successor = "+node.getSuccessor().getNodeName()+
				" | Predecessor = "+node.getPredecessor());
		
	}

}
