/**
 * 
 */
package no.hvl.dat110.main;

import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import no.hvl.dat110.util.FileManager;


/**
 * @author tdoy
 *
 */
public class FileReplicator extends SwingWorker<Void, Integer> {

	private FileManager filemanager;
	private int counter = 0;
	
	public FileReplicator(FileManager fm) {

		this.filemanager = fm;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		
		try {			
			filemanager.readFile();
			counter = filemanager.distributeReplicastoPeers();			
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Error uploading file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);           
            e.printStackTrace();
            setProgress(0);
            cancel(true);
		} 
				
		return null;
	} 
	
	/**
     * Executed in Swing's event dispatching thread
     */
    @Override
    protected void done() {
        if (!isCancelled()) {
            JOptionPane.showMessageDialog(null,
                    "Distribution of "+counter+" replicas to active peers completed!", "Message",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
