/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.s2jsfplugin.pref;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * S2JSF�̃v���W�F�N�g���Ƃ̐ݒ�����i�[����N���X�B
 */
public class S2JSFProjectParams {
	
	private static final String P_ROOT = "p_root";
	private static final String P_VALIDATE_CLOSE_TAG = "p_validate_close_tag";
	private static final String P_HTML_EXTENSIONS = "p_html_extensions";
	
	private String root;
	private boolean validateCloseTag = true;
	private String extensions;
	
	public S2JSFProjectParams(){
	}
	
	public S2JSFProjectParams(IProject project) throws CoreException {
		load(project);
	}
	
	public String getRoot() {
		return root;
	}
	
	public void setRoot(String root) {
		this.root = root;
	}
	
	public boolean getValidateCloseTag(){
		return this.validateCloseTag;
	}
	
	public void setValidateCloseTag(boolean validateCloseTag){
		this.validateCloseTag = validateCloseTag;
	}
	
	public void setExtensions(String extensions){
		this.extensions = extensions;
	}
	
	public String getExtensions(){
		return this.extensions;
	}
	
	/**
	 * �ݒ���e��ۑ����܂��B
	 * 
	 * @param javaProject Java�v���W�F�N�g
	 * @throws CoreException
	 */
	public void save(IProject project) throws CoreException {
		project.setPersistentProperty(new QualifiedName (
				S2JSFPlugin.S2JSF_PLUGIN_ID, P_ROOT), root);
		project.setPersistentProperty(new QualifiedName (
				S2JSFPlugin.S2JSF_PLUGIN_ID, P_VALIDATE_CLOSE_TAG), String.valueOf(validateCloseTag));
		project.setPersistentProperty(new QualifiedName(
				S2JSFPlugin.S2JSF_PLUGIN_ID, P_HTML_EXTENSIONS), extensions);
	}
	
	/**
	 * �ݒ���e��ǂݍ��݂܂��B
	 * 
	 * @param javaProject Java�v���W�F�N�g
	 * @throws CoreException
	 */
	public void load(IProject project) throws CoreException {
		// Web�A�v���P�[�V�����̃��[�g
		root = project.getPersistentProperty(new QualifiedName(S2JSFPlugin.S2JSF_PLUGIN_ID, P_ROOT));
		if(root==null){
			root = "/";
		}
		// ���^�O�̃o���f�[�V�������s�����ǂ���
		String strValidateCloseTag = project.getPersistentProperty(
				new QualifiedName(S2JSFPlugin.S2JSF_PLUGIN_ID, P_VALIDATE_CLOSE_TAG));
		if(strValidateCloseTag==null || strValidateCloseTag.equals("true")){
			validateCloseTag = true;
		} else {
			validateCloseTag = false;
		}
		// HTML�t�@�C���̊g���q
		extensions = project.getPersistentProperty(
				new QualifiedName(S2JSFPlugin.S2JSF_PLUGIN_ID, P_HTML_EXTENSIONS));
		if(extensions==null){
			extensions = "html";
		}
	}

}
