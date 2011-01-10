package ui;

import html.AbstractHtmlPage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.IChangeListener;
import util.SystemTools;
import util.UiTools;


import core.Configuration;
import core.Constants;
import core.FileManager;
import core.HeapManager;
import core.ISystemComponent;
import core.TaskManager;
import core.TaskManager.TaskInfo;

public class ToolsMenu extends JMenu implements ISystemComponent, IChangeListener {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;
	private TaskManager taskManager;
	private FileManager fileManager;
	private HeapManager heapManager;
	
	private JMenuItem exportConfig;
	private JMenuItem garbageCollector;
	private JMenuItem taskMonitor;
	
	public ToolsMenu(
			Configuration configuration,
			TaskManager taskManager,
			FileManager fileManager,
			HeapManager heapManager)
	{
		super("Tools");
		
		this.configuration = configuration;
		this.taskManager = taskManager;
		this.fileManager = fileManager;
		this.heapManager = heapManager;
		
		JMenu configMenu = new JMenu("Configuration");
		add(configMenu);
		
		exportConfig = new JMenuItem("Export");
		exportConfig.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ exportConfiguration(); }
		});
		configMenu.add(exportConfig);
		
		garbageCollector = new JMenuItem("Garbage Collector");
		garbageCollector.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ garbageCollector(); }
		});
		add(garbageCollector);
		
		taskMonitor = new JMenuItem("Task Monitor");
		taskMonitor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ taskMonitor(); }
		});
		add(taskMonitor);
		
		configuration.addListener(this);
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	private void exportConfiguration(){
		
		String path = 
			fileManager.getTempFolderPath()+
			File.separator+"Configuration.htm";
		ConfigPage page = new ConfigPage(path);
		try{
			page.create();
			SystemTools.openBrowser(path);
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
	
	private class ConfigPage extends AbstractHtmlPage {
		public ConfigPage(String path) {
			super(Constants.APP_NAME+" [ Configuration ]", path, null);
		}
		@Override
		public String getBody() {
			return configuration.toHtml();
		}
	}
	
	private void garbageCollector(){
		heapManager.cleanup();
	}
	
	private void taskMonitor(){
		
		TaskMonitor monitor = new TaskMonitor(taskManager);
		monitor.setVisible(true);
	}
	
	private class TaskMonitor extends JDialog implements IChangeListener {
		
		private static final long serialVersionUID = 1L;
		
		private JTextArea infoPanel;
		private TaskManager taskManager;
		
		public TaskMonitor(TaskManager taskManager){
			this.taskManager = taskManager;
			
			infoPanel = new JTextArea();
			infoPanel.setEditable(false);
			add(new JScrollPane(infoPanel));
						
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e){ 
					dispose(); 
				}
			});
			
			setSize(400, 150); 
			initUI();
			taskManager.addListener(this);
		}
		
		public void dispose(){
			
			taskManager.removeListener(this);
			super.dispose();
		}
		
		private void initUI() {
			ArrayList<TaskInfo> tasks = taskManager.getInfo();
			setTitle("TaskMonitor ("+tasks.size()+")");
			StringBuilder info = new StringBuilder();
			for(int i=0; i<tasks.size(); i++){
				TaskInfo task = tasks.get(i);
				if(task.running){
					info.append((i+1)+". "+task.name+"\n");
				}else{
					info.append((i+1)+". "+task.name+" <idle>\n");
				}
			}
			infoPanel.setText(info.toString());
		}

		@Override
		public void changed(Object object) {
			initUI();
		}
	}

	@Override
	public void changed(Object object) {}
}
