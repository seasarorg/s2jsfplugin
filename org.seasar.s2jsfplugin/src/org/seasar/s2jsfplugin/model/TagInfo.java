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
package org.seasar.s2jsfplugin.model;

import java.util.ArrayList;

/**
 * 
 * @author Naoki Takezoe
 */
public class TagInfo implements Cloneable {

	private String tagName;
	private boolean hasBody;
	private String description;
	private ArrayList attributes = new ArrayList();
	private ArrayList children   = new ArrayList();
	
	public static final int NONE  = 0;
	public static final int EVENT = 1;
	public static final int FORM  = 2;
	
	public Object clone(){
		TagInfo info = new TagInfo(tagName,hasBody);
		for(int i=0;i<attributes.size();i++){
			info.addAttributeInfo((AttributeInfo)attributes.get(i));
		}
		for(int i=0;i<children.size();i++){
			info.addChildTagName((String)children.get(i));
		}
		return info;
	}
	
	/**
	 * �R���X�g���N�^�B
	 * @param tagName �^�O�̖��O
	 * @param hasBody �q�v�f�������邩�ǂ���
	 */
	public TagInfo(String tagName,boolean hasBody){
		this.tagName = tagName;
		this.hasBody = hasBody;
	}
	
	/**
	 * �^�O�̖��O���擾���܂��B
	 * @return
	 */
	public String getTagName(){
		return this.tagName;
	}
	
	/**
	 * �q�v�f�������邩�ǂ������擾���܂��B
	 * @return
	 */
	public boolean hasBody(){
		return this.hasBody;
	}
	
	/**
	 * ��������ǉ����܂��B
	 * @param attribute
	 */
	public void addAttributeInfo(AttributeInfo attribute){
		int i = 0;
		for(;i<attributes.size();i++){
			AttributeInfo info = (AttributeInfo)attributes.get(i);
			if(info.getAttributeName().compareTo(attribute.getAttributeName()) > 0){
				break;
			}
		}
		this.attributes.add(i,attribute);
	}
	
	/**
	 * �S�Ă̑��������擾���܂��B
	 * @param attribute
	 */
	public AttributeInfo[] getAttributeInfo(){
		return (AttributeInfo[])this.attributes.toArray(new AttributeInfo[this.attributes.size()]);
	}
	
	/**
	 * �K�{�̑��������擾���܂��B
	 * @return
	 */
	public AttributeInfo[] getRequiredAttributeInfo(){
		ArrayList list = new ArrayList();
		for(int i=0;i<attributes.size();i++){
			AttributeInfo info = (AttributeInfo)attributes.get(i);
			if(info.isRequired()){
				list.add(info);
			}
		}
		return (AttributeInfo[])list.toArray(new AttributeInfo[list.size()]);
	}
	
	/**
	 * �w�肵�����O�̑��������擾���܂��B
	 * @param name
	 * @return
	 */
	public AttributeInfo getAttributeInfo(String name){
		for(int i=0;i<attributes.size();i++){
			AttributeInfo info = (AttributeInfo)attributes.get(i);
			if(info.getAttributeName().equals(name)){
				return info;
			}
		}
		return null;
	}
	
	/**
	 * �q�^�O�̖��O���Z�b�g���܂��B
	 * 
	 * @param name �q�^�O�̖��O
	 */
	public void addChildTagName(String name){
		children.add(name);
	}
	
	/**
	 * �q�^�O�̖��O���擾���܂��B
	 * 
	 * @return �q�^�O�̖��O�iString�z��j
	 */
	public String[] getChildTagNames(){
		return (String[])children.toArray(new String[children.size()]);
	}
	
	/**
	 * �^�O�̏ڍא������iTLD��description�v�f�ɋL�q���ꂽ���́j���Z�b�g���܂��B
	 * 
	 * @param description �^�O�̏ڍא�����
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * �^�O�̏ڍא������iTLD��description�v�f�ɋL�q���ꂽ���́j���擾���܂��B
	 * 
	 * @return �^�O�̏ڍא�����
	 */
	public String getDescription(){
		return this.description;
	}
}
