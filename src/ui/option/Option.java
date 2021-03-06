package ui.option;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * option of an item
 */
public class Option {

	public enum Type { TEXT, TEXT_SMALL, TEXT_AREA, TEXT_LIST, INTEGER, BOOLEAN, TIME }
	
	public enum Properties {
		INTEGER_MIN, INTEGER_MAX, LIST_SIZE, LIST_ITEM
	}

	public transient JPanel parent;
	public transient JComponent component;
	public transient JPopupMenu popup;
	
	private String group;
	private String name;
	private String description;
	private Type type;
	private String value;
	private HashMap<String, String> properties;
	
	public Option(String group, String name, String description, Type type, String value){
		
		init(group, name, description, type);
		setStringValue(value);
	}
	
	public Option(String group, String name, String description, Type type, Date value){
		
		init(group, name, description, type);
		setDateValue(value);
	}
	
	public Option(String group, String name, String description, Type type, int value, int min, int max){
		
		init(group, name, description, type);
		setIntegerValue(value);
		properties.put(Properties.INTEGER_MIN.toString(), ""+min);
		properties.put(Properties.INTEGER_MAX.toString(), ""+max);
	}
	
	public Option(String group, String name, String description, Type type, boolean value){
		
		init(group, name, description, type);
		setBooleanValue(value);
	}
	
	public Option(String group, String name, String description, Type type, ArrayList<String> values, String value){
		
		init(group, name, description, type);
		properties.put(Properties.LIST_SIZE.toString(), ""+values.size());
		for(int i=0; i<values.size(); i++){
			properties.put(Properties.LIST_ITEM.toString()+"_"+i, values.get(i));
		}
		setStringValue(value);
	}

	private void init(String group, String name, String description, Type type) {
		
		this.group = group;
		this.name = name;
		this.description = description;
		this.type = type;
		properties = new HashMap<String, String>();
	}
	
	public String getGroup(){ return group; }
	public String getName(){ return name; }
	public String getDescription(){ return description; }
	public Type getType(){ return type; }
	public HashMap<String, String> getProperties(){ return properties; }
	
	public void setStringValue(String value){ this.value = value; }
	public String getStringValue(){ return value; }
	
	public void setDateValue(Date value){ this.value = ""+value.getTime(); }
	public Date getDateValue(){ return new Date(new Long(value).longValue()); }
	
	public void setIntegerValue(int value){ this.value = ""+value; }
	public int getIntegerValue(){ return new Integer(value).intValue(); }
	public int getIntegerMinimum(){
		String property = properties.get(Properties.INTEGER_MIN.toString());
		return property != null ? new Integer(property).intValue() : Integer.MIN_VALUE;
	}
	public int getIntegerMaximum(){
		String property = properties.get(Properties.INTEGER_MAX.toString());
		return property != null ? new Integer(property).intValue() : Integer.MAX_VALUE;
	}
	
	public void setBooleanValue(boolean value){ this.value = ""+value; }
	public boolean getBooleanValue(){ return new Boolean(value).booleanValue(); }
	
	public int getListSize() {
		String property = properties.get(Properties.LIST_SIZE.toString());
		return property != null ? new Integer(property).intValue() : 0;
	}
	public String getListItem(int index) {
		String property = properties.get(Properties.LIST_ITEM.toString()+"_"+index);
		return property;
	}

	public String toString(){
		return name+" ("+type+") = "+value;
	}
	
	public String getUIName(){
		
		String converted = "";
		char[] chars = name.toLowerCase().replaceAll("_", " ").toCharArray();
		boolean word = true;
		for(char c : chars){
			if(word){
				converted += Character.toUpperCase(c);
				word = false;
			}else{
				converted += c;
			}
			if(c == ' '){
				word = true;
			}
		}
		return converted;
	}
	
	public void addPopup(JMenuItem item) {
		
		if(popup == null){
			popup = new JPopupMenu();
			component.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e){
					if(e.getButton() == MouseEvent.BUTTON3){
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
		popup.add(item);
	}
}
