package no.hvl.dat110.util;

/**
 * dat110
 * @author tdoy
 */

import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hvl.dat110.middleware.Node;
import no.hvl.dat110.rpc.interfaces.NodeInterface;

public class Util {

	public static String activeIP = null;
	public static int numReplicas = 4;

	/**
	 * This method computes (lower <= id <= upper). To use this method to compute
	 * (lower < id <= upper), ensure that the calling method increased the lower
	 * param by 1. To use this method to compute (lower <= id < upper), ensure that
	 * the calling method increased the upper param by 1. To use this method to
	 * compute (lower < id < upper), ensure that the calling method increased both
	 * the lower and upper params by 1.
	 * 
	 * @param id
	 * @param lower
	 * @param upper
	 * @return true if (lower <= id <= upper) or false otherwise
	 */
	public static boolean computeLogic(BigInteger id, BigInteger lower, BigInteger upper) {

		// a formula to check whether an id falls within the set {lower, upper} using
		// the address size as our bound (modulos operation)
		// it modifies 'upper' and 'id' when lower > upper e.g. set (6, 2) in mod 10 =
		// {6, 7, 8, 9, 0, 1, 2}

		// implement: read the descriptions above
		boolean cond = false;

		// 340282366920938463463374607431768211456
		BigInteger addressSize = Hash.addressSize();
		BigInteger temp = upper;

		// lower > upper
		if (lower.compareTo(upper) == 1) {
			temp = upper.add(addressSize);

			// (id >= 0) && (id <= upper)
			if ((id.compareTo(new BigInteger("0")) >= 0) && (id.compareTo(upper) <= 0)) {
				id = id.add(addressSize);
			}

		}

		upper = temp;

		// (id >= lower) && (id <= upper)
		cond = (id.compareTo(lower) >= 0) && (id.compareTo(upper) <= 0);

		return cond;
	}

	public static List<String> toString(List<NodeInterface> list) throws RemoteException {
		List<String> nodestr = new ArrayList<String>();
		list.forEach(node -> {
			nodestr.add(((Node) node).getNodeName());
		});

		return nodestr;
	}

	public static NodeInterface getProcessStub(String name, int port) {

		NodeInterface nodestub = null;
		Registry registry = null;
		try {
			// Get the registry for this worker node
			registry = LocateRegistry.getRegistry(port);

			nodestub = (NodeInterface) registry.lookup(name); // remote stub

		} catch (NotBoundException | RemoteException e) {
			return null; // if this call fails, then treat the node to have left the ring...or
							// unavailable
		}

		return nodestub;
	}

	/**
	 * This method is used when processes are running on a single computer
	 * 
	 * @return the registry for the found ip
	 * @throws RemoteException
	 * @throws NumberFormatException
	 */
	public static Registry tryIPSingleMachine(String nodeip) throws NumberFormatException, RemoteException {

		// try the tracker IP addresses and connect to any one available
		String[] ips = StaticTracker.ACTIVENODES;
		List<String> iplist = Arrays.asList(ips);
		Collections.shuffle(iplist);

		Registry registry = null;
		for (String ip : iplist) {
			String ipaddress = ip.split(":")[0].trim();
			String port = ip.split(":")[1].trim();
			System.out.println(ipaddress + ":" + port);
			if (nodeip.equals(ipaddress))
				continue;
			registry = LocateRegistry.getRegistry(Integer.valueOf(port));
			if (registry != null) {
				activeIP = ipaddress;
				return registry;
			}
		}

		return registry;

	}

	public static Map<String, Integer> getProcesses() {

		Map<String, Integer> processes = new HashMap<>();
		processes.put("process1", 9091);
		processes.put("process2", 9092);
		processes.put("process3", 9093);
		processes.put("process4", 9094);
		processes.put("process5", 9095);

		return processes;
	}

}
