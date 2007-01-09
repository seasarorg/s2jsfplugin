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
package org.seasar.s2jsfplugin.model.autoregister;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.seasar.s2jsfplugin.Util;

/**
 * <code>FileSystemComponentAutoRegister</code>の動作をエミュレートするIAutoRegisterの実装。
 * 
 * @author Naoki Takezoe
 */
public class FileSystemAutoRegister extends AbstractAutoRegister {

	public void registerAll() {
		try {
			IPackageFragmentRoot[] roots = getProject().getPackageFragmentRoots();
			for(int i=0;i<roots.length;i++){
				if(roots[i].getResource() instanceof IFolder){
					List classPatterns = getClassPatterns();
					for(int j=0;j<classPatterns.size();j++){
						ClassPattern pattern = (ClassPattern)classPatterns.get(j);
						register(roots[i], pattern);
					}
				}
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	private void register(IPackageFragmentRoot root, 
			ClassPattern pattern) throws Exception {
		
		String packageName = pattern.getPackageName();
		IPackageFragment fragment = root.getPackageFragment(packageName);
		if(fragment.exists()){
			IJavaElement[] elements = fragment.getChildren();
			for(int i=0;i<elements.length;i++){
				if(elements[i] instanceof ICompilationUnit){
					String sourceName = elements[i].getElementName();
					String className = sourceName.replaceFirst("\\.java$","");
					processClass(packageName, className);
				}
			}
		}
	}		
}
