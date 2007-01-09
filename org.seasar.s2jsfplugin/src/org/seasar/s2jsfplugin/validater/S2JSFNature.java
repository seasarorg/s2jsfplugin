/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2jsfplugin.validater;

import java.util.ArrayList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * S2JSFプロジェクトのネイチャ。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFNature implements IProjectNature {
	
	private IProject project;
	
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		for(int i=0;i<commands.length;i++){
			if(commands[i].getBuilderName().equals(S2JSFPlugin.S2JSF_BUILDER)){
				return;
			}
		}
		ICommand command = desc.newCommand();
		command.setBuilderName(S2JSFPlugin.S2JSF_BUILDER);
		ICommand[] newCommands = new ICommand[commands.length + 1];
		for(int i=0;i<commands.length;i++){
			newCommands[i] = commands[i];
		}
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc,null);
	}

	public void deconfigure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		ArrayList list = new ArrayList();
		for(int i=0;i<commands.length;i++){
			if(!commands[i].getBuilderName().equals(S2JSFPlugin.S2JSF_BUILDER)){
				list.add(commands[i]);
			}
		}
		desc.setBuildSpec((ICommand[])list.toArray(new ICommand[list.size()]));
		project.setDescription(desc,null);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
