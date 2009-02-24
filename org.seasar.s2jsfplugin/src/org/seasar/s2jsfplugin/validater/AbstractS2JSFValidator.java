/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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

import org.eclipse.core.resources.IFile;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * S2JSF�v���O�C���Ŏg�p����o���f�[�^�̒��ۊ��N���X�B
 * 
 * @author Naoki Takezoe
 */
public abstract class AbstractS2JSFValidator {
	
	private S2JSFProject project;
	private IFile file;
	private String contents;
	private S2JSFProjectParams params;
	
	public AbstractS2JSFValidator(S2JSFProject project,IFile file){
		this.project = project;
		this.file    = file;
		
		try {
			this.contents = Util.readFile(file);
			this.contents = contents.replaceAll("\r\n"," \n");
			this.contents = contents.replaceAll("\r"  ,"\n");
			
			this.params = new S2JSFProjectParams(file.getProject());
			
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	/** ���؏������������܂��B */
	public abstract void doValidate();
	
	/**
	 * ���b�Z�[�W���쐬���܂��B
	 * 
	 * @param key ���b�Z�[�W�̃L�[
	 * @param param ���b�Z�[�W�ɖ��ߍ��ރp�����[�^
	 * @return �쐬���ꂽ���b�Z�[�W
	 */
	protected String createMessage(String key,String param){
		String message = S2JSFPlugin.getResourceString(key);
		return Util.createMessage(message,new String[]{param});
	}
	
	/** �I�t�Z�b�g����s�ԍ����擾���܂��B */
	protected int getLineAtOffset(int offset){
		String text = contents.substring(0,offset);
		return text.split("\n").length;
	}
	
	/** �����̃I�t�Z�b�g���瑮���l�����̃I�t�Z�b�g���擾���܂��B */
	protected int getAttrValueOffset(int offset){
		int valueStart = 0;
		
		valueStart = contents.indexOf("\"",offset);
		if(valueStart==-1){
			valueStart = contents.indexOf("=")+1;
		}
		
		return valueStart;
	}
	
	/** S2JSFProject���擾���܂��B */
	protected S2JSFProject getS2JSFProject(){
		return this.project;
	}
	
	/** ���ؑΏۂ̃t�@�C���I�u�W�F�N�g���擾���܂��B */
	protected IFile getFile(){
		return this.file;
	}
	
	/** S2JSFProjectParams���擾���܂��B */
	protected S2JSFProjectParams getS2JSFProjectParams(){
		return this.params;
	}
}
