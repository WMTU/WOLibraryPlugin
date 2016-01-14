package edu.mtu.wmtu;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.google.ras.api.core.AsyncCallback;
import com.google.ras.api.core.ErrorInfo;
import com.google.ras.api.core.Platform;
import com.google.ras.api.core.plugin.BasicWidget;
import com.google.ras.api.core.services.media.*;
import com.google.ras.api.core.services.playlist.*;
import com.google.ras.api.core.services.selection.*;
import com.google.ras.api.core.ui.animation.IsAnimated;



public class LibraryII extends BasicWidget implements IsAnimated {
	public static final Logger log = Logger.getLogger(LibraryII.class);
	
	private LibraryService libraryService;
    private SelectionService selectionService;
    private PlaylistEntry selectedEntry;
    
    private boolean init = false;
    
    private DefaultTableModel table_model;
    private JTextField search_field;
    
    private AsyncCallback<LibrarySearchResults, LibraryError> search_callback = new AsyncCallback<LibrarySearchResults, LibraryError>() {
		public void onSuccess(LibrarySearchResults result) {
			//Clear table
			log.error("Table Cleared");
			for (int i = table_model.getRowCount() - 1; i > -1; i--) {
				table_model.removeRow(i);
			}
			
			
			// Display the list of replacement songs.
			MediaAssetInfo[] matches = result.getResults();  
			log.error("Found a total of " + result.getCount() + " songs.");
			for(MediaAssetInfo mai : matches) {
				table_model.addRow(new Object[] { mai.getTitle(), mai.getArtist(), mai.getMetadataField("Album"), mai.getYear(), mai.getId(), mai.getNote() });
			}
			//selectionLabel.setText("Showing " + matches.length + " replacement candidates.");
			//songList.setListData(matches);       
		}
		 
		public void onFailure(ErrorInfo<LibraryError> error) {
			// Display an error message to the user.
			Platform.getMessageHandler().showError(error.message);
		}
	};
	
	public LibraryII() {
		super("LibraryII");
		
		libraryService = Platform.getService(LibraryService.class);
		selectionService = Platform.getService(SelectionService.class);
		
		/*selectionService.addSelectionListener(new SelectionAdapter() {
			@Override
			public void entrySelectionChanged(Selection<PlaylistEntry> newSelection) {
				setSelectedEntry(newSelection);
			}
	    });*/
	}

	@Override
	public void animate(long arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected JComponent buildContentPanel() {
		JPanel layout_container = new JPanel();
		layout_container.setLayout(new GridBagLayout());
		layout_container.setBackground(Color.BLACK);
		
		//for layouts
		GridBagConstraints c = new GridBagConstraints();
		
		//Label
		//resize the icon
		try {
			BufferedImage search_image = ImageIO.read(this.getClass().getResourceAsStream("ic_search_white_48dp.png"));
			int search_icon_width = 50;
			ImageIcon search_icon = new ImageIcon(search_image.getScaledInstance(search_icon_width, -1, Image.SCALE_SMOOTH), "Search");
			
			//finally apply the icon
			JLabel search_label = new JLabel(search_icon);
			//JLabel search_label = new JLabel();
			search_label.setSize(25, 25);
			
			//Label layout
			c.fill = GridBagConstraints.NONE;
			c.ipadx = 5;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0;
			c.weighty = 0;
			layout_container.add(search_label, c);
		} catch (Exception e){
			log.error(ExceptionUtils.getStackTrace(e));
		}
		
		
		//Search Bar
		search_field = new JTextField();
		search_field.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				searchForEntry(search_field.getText());
			}
		});
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
		c.gridx = 1;
		c.weightx = 1;
		layout_container.add(search_field, c);
		
		//Table
		String column_names[]= {"Song","Artist","Album","Year", "ID", "Note"};
		table_model = new DefaultTableModel(column_names, 0);
		JTable library_table = new JTable(table_model);
		JScrollPane table_scroll = new JScrollPane(library_table);
		
		//table layout
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weighty = 1;
		c.weightx = 0;
		layout_container.add(table_scroll, c);
		
		layout_container.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				//The panel has been added!
				
				log.info("Populating Fields");
				//initialize
				if (init == false) {
					//populate the table
					searchForEntry("");
					
					init = true;
				}
				
			}

			@Override
			public void ancestorMoved(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		return (JComponent)layout_container;
	}

	public void searchForEntry(String search_entry) {
		libraryService.search(search_entry, 0, 50,null, null, true, search_callback);	
	}
	
}
