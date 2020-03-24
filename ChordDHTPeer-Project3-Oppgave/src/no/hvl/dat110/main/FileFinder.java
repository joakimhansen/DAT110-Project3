/**
 * 
 */
package no.hvl.dat110.main;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import no.hvl.dat110.middleware.Message;
import no.hvl.dat110.util.FileManager;

/**
 * @author tdoy
 *
 */
public class FileFinder extends SwingWorker<Void, Integer> {

	private JTable table;
	
	private FileManager fm;
	private int counter = 0;  
	 
	public FileFinder(FileManager fm, JTable table) {
		this.fm = fm;
		this.table = table;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		
		// clear all rows
		DefaultTableModel tmodel = (DefaultTableModel) table.getModel();
		tmodel.setRowCount(0);

		Set<Message> activepeers = fm.requestActiveNodesForFile("");
		
		counter = activepeers.size();
		
		for(Message msg : activepeers) {
			double size = (double) msg.getBytesOfFile().length/1000;
			NumberFormat nf = new DecimalFormat();
			nf.setMaximumFractionDigits(2);
			Object[] rdata = {msg.getNameOfFile(), msg.getHashOfFile(), nf.format(size), msg.getNodeIP()};
			updateTableRows(rdata);
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
                    "Search completed with "+counter+" results. See results in the table", "Message",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
	private void updateTableRows(Object[] rdata) {
		
		DefaultTableModel tmodel = (DefaultTableModel) table.getModel();
		tmodel.addRow(rdata);
		
	}

}
