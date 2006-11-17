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
package org.seasar.s2jsfplugin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * �J�X�^���^�O���C�u�����̕⊮�����i�[����N���X�B
 * 
 * @author Naoki Takezoe
 */
public class TLDInfo {
	
	private String uri;
	private List tagInfoList = new ArrayList();
	
	public TLDInfo(String uri,List tagInfoList){
		this.uri = uri;
		this.tagInfoList = tagInfoList;
	}
	
	/**
	 * �J�X�^���^�O��URI�iJSP��taglib�f�B���N�e�B�u�Ŏw�肳�ꂽ���́j���擾���܂��B
	 * 
	 * @return URI
	 */
	public String getUri(){
		return uri;
	}
	
	/**
	 * TLD�Œ�`����Ă���J�X�^���^�O��TagInfo�����X�g�Ŏ擾���܂��B
	 * 
	 * @return �J�X�^���^�O��TagInfo���i�[����List
	 */
	public List getTagInfo(){
		return tagInfoList;
	}
	
	/**
	 * �^�O�̖��O���w�肵��TagInfo���擾���܂��B
	 * �Y������^�O�����݂��Ȃ��ꍇ�Anull��Ԃ��܂��B
	 * 
	 * @param tagName
	 * @return
	 */
	public TagInfo getTagInfo(String tagName){
		for(int i=0;i<tagInfoList.size();i++){
			TagInfo info = (TagInfo)tagInfoList.get(i);
			if(info.getTagName().equals(tagName)){
				return info;
			}
		}
		return null;
	}

}
