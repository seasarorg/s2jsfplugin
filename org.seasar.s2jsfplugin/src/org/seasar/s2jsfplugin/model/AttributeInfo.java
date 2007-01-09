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
package org.seasar.s2jsfplugin.model;

import java.util.ArrayList;

/**
 * 
 * @author Naoki Takezoe
 */
public class AttributeInfo {

	private String attributeName;
	private boolean hasValue;
	private int attributeType;
	private boolean required = false;
	private String description;
	private ArrayList values = new ArrayList();
	
	/** ������ʂȂ� */
	public static final int NONE       = 0;
	/** ALIGN���� */
	public static final int ALIGN      = 1;
	/** VALIGN���� */
	public static final int VALIGN     = 2;
	/** INPUT�^�O��TYPE���� */
	public static final int INPUT_TYPE = 3;
	/** �X�^�C���V�[�g */
	public static final int CSS        = 4;
	/** �t�@�C���A�f�B���N�g���̕⊮ */
	public static final int FILE       = 5;

	
	/**
	 * �R���X�g���N�^�B
	 * @param attributeName �����̖��O
	 * @param hasValue      �l�������ǂ���
	 */
	public AttributeInfo(String attributeName,boolean hasValue){
		this(attributeName,hasValue,NONE);
	}
	
	/**
	 * �R���X�g���N�^�B
	 * @param attributeName �����̖��O
	 * @param hasValue      �l�������ǂ���
	 * @param attributeType �����̎��
	 */
	public AttributeInfo(String attributeName,boolean hasValue,int attributeType){
		this(attributeName,hasValue,attributeType,false);
	}
	
	/**
	 * �R���X�g���N�^�B
	 * @param attributeName �����̖��O
	 * @param hasValue      �l�������ǂ���
	 * @param attributeType �����̎��
	 * @param required      �K�{�������ǂ���
	 */
	public AttributeInfo(String attributeName,boolean hasValue,int attributeType,boolean required){
		this.attributeName = attributeName;
		this.hasValue      = hasValue;
		this.attributeType = attributeType;
		this.required      = required;
	}
	
	/**
	 * �����̎�ʂ��擾���܂��B
	 * @return
	 */
	public int getAttributeType(){
		return this.attributeType;
	}
	
	/**
	 * �����̖��O���擾���܂��B
	 * @return
	 */
	public String getAttributeName(){
		return this.attributeName;
	}
	
	/**
	 * �������l�������ǂ������擾���܂��B
	 * @return
	 */
	public boolean hasValue(){
		return this.hasValue;
	}
	
	/**
	 * �������K�{���ǂ������擾���܂��B
	 * @return
	 */
	public boolean isRequired(){
		return this.required;
	}
	
	/**
	 * �����l��ǉ����܂��B
	 * @param value �l
	 */
	public void addValue(String value){
	    this.values.add(value);
	}
	
	/**
	 * �����l���擾���܂��B
	 * @return �����l�̔z��
	 */
	public String[] getValues(){
	    return (String[])this.values.toArray(new String[this.values.size()]);
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
		if(this.description==null){
			return "";
		}
		return this.description;
	}
}
