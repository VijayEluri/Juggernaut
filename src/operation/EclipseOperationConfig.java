package operation;

import java.util.ArrayList;

import util.StringTools;

import core.Cache;
import core.Configuration;
import core.TaskManager;

import launch.LaunchAgent;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.Option;
import data.Option.Type;

public class EclipseOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Eclipse";

	public enum OPTIONS {
		ECLIPSE, BUILD, EXCLUDE, CLEAN, TOLERANT
	}
	
	public EclipseOperationConfig(){
		
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.ECLIPSE.toString(), "The path to Eclipse", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.BUILD.toString(), "[project|configuration] pattern to include in build (linewise, commented with '//')", 
				Type.TEXT_AREA, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.EXCLUDE.toString(), "[project|configuration] pattern to exclude from build (comma seperated)",
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.CLEAN.toString(), "Perform a clean build", 
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.TOLERANT.toString(), "Build in non-strict mode", 
				Type.BOOLEAN, false
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to run Eclipse with CDT-Builder Plugin";
	}
	
	public String getEclipsePath(){ 
		return optionContainer.getOption(OPTIONS.ECLIPSE.toString()).getStringValue(); 
	}
	
	public ArrayList<String> getBuildPattern(){ 
		
		String value = optionContainer.getOption(OPTIONS.BUILD.toString()).getStringValue();
		return StringTools.split(value, "\\n", "//");
	}
	
	public ArrayList<String> getExcludePattern(){ 
		
		String value = optionContainer.getOption(OPTIONS.EXCLUDE.toString()).getStringValue();
		return StringTools.split(value, ", ");
	}
	
	public boolean isCleanBuild(){ 
		return optionContainer.getOption(OPTIONS.CLEAN.toString()).getBooleanValue(); 
	}
	
	public boolean isTolerantBuild(){ 
		return optionContainer.getOption(OPTIONS.TOLERANT.toString()).getBooleanValue(); 
	}
	
	@Override
	public boolean isValid(){
		return !getEclipsePath().isEmpty();
	}
	
	@Override
	public AbstractOperation createOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent)
	{
		return new EclipseOperation(configuration, cache, taskManager, parent, this);
	}
}
