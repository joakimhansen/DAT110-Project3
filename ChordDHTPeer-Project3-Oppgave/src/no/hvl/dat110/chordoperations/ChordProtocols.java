/**
 * 
 */
package no.hvl.dat110.chordoperations;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.List;

import no.hvl.dat110.middleware.Node;
import no.hvl.dat110.rpc.interfaces.NodeInterface;
import no.hvl.dat110.util.Hash;
import no.hvl.dat110.util.Util;

/**
 * @author tdoy
 *
 */
public class ChordProtocols {
	
	/**
	 * UpdateSuccessor
	 * StabilizeRing
	 * FixFingerTable
	 * CheckPredecessor
	 */
	
	private NodeInterface chordnode;
	//private Node node;
	
	public ChordProtocols(NodeInterface chordnode) {
		this.chordnode = chordnode;
		//this.node = (Node) chordnode;
	}
	
	/**
	 * 
	 * @throws RemoteException
	 */ 
	public void checkPredecessor() {
		
		try {
			System.out.println("Checking the predecessor for Node: "+chordnode.getNodeName());
			
			NodeInterface predecessor = chordnode.getPredecessor();
			String name = predecessor.getNodeName();
			int port = predecessor.getPort();
			NodeInterface predNode = Util.getProcessStub(name, port);
			if(predNode == null) {
				chordnode.setPredecessor(null);		// object not available remove predecessor
				return;
			}

		} catch (NullPointerException | RemoteException e) {		
			try {
				chordnode.setPredecessor(null);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}		// if error occurs - predecessor can't be reached.. set the reference to null
		}

	}
	
	public void fixFingerTable() {
		
		try {
			System.out.println("Fixing the FingerTable for the Node: "+ chordnode.getNodeName());
			int s = Hash.bitSize();
	
			List<NodeInterface> fingers = ((Node) chordnode).getFingerTable();
	
			BigInteger modulos = Hash.addressSize();			// we can't go beyond our address space 2^mbit
		
			for(int i=0; i<s; i++) {
	
				BigInteger nextsuccID = new BigInteger("2");
				nextsuccID = nextsuccID.pow(i);
				//System.out.println("nextsuccID: "+nextsuccID);
				
				BigInteger succnodeID = chordnode.getNodeID().add(nextsuccID);
				succnodeID = succnodeID.mod(modulos);								// do succ(n + 2^(i-1)) mod 2^mbit
				
				//System.out.println("nodeID: "+chordnode.getNodeID()+" | succID: "+succnodeID);
				
				NodeInterface succnode = null;
				try {
					succnode = chordnode.findSuccessor(succnodeID);
				} catch (RemoteException e) {
					//e.printStackTrace();
				}
	
				if(succnode != null) {
					try {
						fingers.set(i, succnode);
					}catch(IndexOutOfBoundsException e) {
						fingers.add(i, succnode);			// first time initialization
					}					
				}
			}
		} catch (RemoteException e) {
			//e.printStackTrace();
		}
	}
	
	public void stabilizeRing() {
		
		try {
			System.out.println("Stabilizing ring from "+chordnode.getNodeName()+"...");
			
			NodeInterface succ = chordnode.getSuccessor();						// get the successor of node
					
			NodeInterface succnode = null;
			NodeInterface predsucc = null;
			//Registry registry = null;
			
			System.out.println("Stabilize ring: succnode = "+succ.getNodeName());

			succnode = Util.getProcessStub(succ.getNodeName(), succ.getPort());		// confirm the successor is alive
			predsucc = succnode.getPredecessor(); 													// get the predecessor of the successor of this node
			
			BigInteger nodeID = chordnode.getNodeID();
			BigInteger succID = succnode.getNodeID();
	
			BigInteger predsuccID = null;
			
			if(predsucc != null) {
				predsuccID = predsucc.getNodeID();

				boolean cond = Util.computeLogic(predsuccID, nodeID.add(new BigInteger("1")), succID.add(new BigInteger("1")));
				//System.out.println(cond+" = "+predsuccID+" is btw "+nodeID.add(new BigInteger("1"))+" and "+succID.add(new BigInteger("1")));
				if(cond) {
					chordnode.setSuccessor(predsucc);
					((Node)chordnode).copyKeysFromSuccessor(succ); 						// copy keys from successor
					predsucc.notify(chordnode);											// notify successor (predsucc) that it has a new predecessor (node)	
				}
			}
			
			System.out.println("Finished stabilizing chordring from "+chordnode.getNodeName());
		} catch (RemoteException e) {
			System.out.println("Error stabilizing chordring ...");
		}
		
	}

	
	/**
	 * This protocol is not in the original paper but it is crucial for stabilizing the chord ring
	 * periodically, a node n tries to resolve the next key (i.e. succ id which is nodeid + 1) from itself 
	 * node.findsuccessor(node+1). The result is the node ns that's still active and responsible for nodeid+1
	 * which is then the successor of n.
	 */
	public void updateSuccessor() {
		try {
			System.out.println("Updating the successor for Node: "+ chordnode.getNodeName());
			BigInteger succid = chordnode.getNodeID().add(new BigInteger("1")); 					// get the succid of (nodestub+1)	
			
			NodeInterface succnodestub = chordnode.findSuccessor(succid);						// finds the successor (succ(nodestub+1) of this node(remote call)
			
			if (succnodestub == null)
				return;
			
			NodeInterface predsucc = succnodestub.getPredecessor();							// get the predecessor of the successor of this node
			
			try {
				if(chordnode.getNodeName().equals(predsucc.getNodeName())){
					return;
				} else {
					((Node) chordnode).getFingerTable().set(0, predsucc);									// update the first successor (entry) of the finger table				
					chordnode.setSuccessor(predsucc); 												// update the immediate successor (same as FT[0]
					((Node) chordnode).copyKeysFromSuccessor((Node)predsucc); 										// copy keys from successor
					predsucc.notify(chordnode); 													// notify succnodestub of this node as its predecessor
					updateSuccessor();
				}
				
			} catch(Exception e) {
				// in the case of a two ring node: we need this mechanism to eventually update the predecessors
				// notify succ that its pred may be this node (chordnode)
				try {
					if (predsucc == null) {
						if(!succnodestub.getNodeName().equals(chordnode.getNodeName()))
							succnodestub.notify(chordnode); 				// notify succ that its pred may be this node (chordnode)
					}
				}catch(Exception e1) {
					//
				}
				//e.printStackTrace();
			}
			
			System.out.println("Finished updating the successor for Node: "+ chordnode.getNodeName());
			
		} catch(RemoteException e) 
		{
			//
		}		
	}
	
	public void printInfo() {
		try {
			System.out.println("==================================");
			System.out.println("Node Identifier = "+chordnode.getNodeID());
			System.out.println("Node IP address = "+chordnode.getNodeName());
			System.out.println("successor("+ chordnode.getNodeName()+") = "+chordnode.getSuccessor().getNodeName());
			if(chordnode.getPredecessor() == null)
				System.out.println("predecessor("+ chordnode.getNodeName()+") = "+chordnode.getNodeName());
			else
				System.out.println("predecessor("+ chordnode.getNodeName()+") = "+chordnode.getPredecessor().getNodeName());
			//System.out.println("Current FingerTable for "+chordnode.getNodeIP()+" => "+Util.toString(chordnode.getFingerTable()));
			System.out.println("Current File keys for "+chordnode.getNodeName()+" => "+chordnode.getNodeKeys());
			System.out.println("==================================");
		}catch(RemoteException e) {
			//
		}
	}

}
