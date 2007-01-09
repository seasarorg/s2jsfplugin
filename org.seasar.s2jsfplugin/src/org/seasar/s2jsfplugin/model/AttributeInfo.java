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
	
	/** 属性種別なし */
	public static final int NONE       = 0;
	/** ALIGN属性 */
	public static final int ALIGN      = 1;
	/** VALIGN属性 */
	public static final int VALIGN     = 2;
	/** INPUTタグのTYPE属性 */
	public static final int INPUT_TYPE = 3;
	/** スタイルシート */
	public static final int CSS        = 4;
	/** ファイル、ディレクトリの補完 */
	public static final int FILE       = 5;

	
	/**
	 * コンストラクタ。
	 * @param attributeName 属性の名前
	 * @param hasValue      値を持つかどうか
	 */
	public AttributeInfo(String attributeName,boolean hasValue){
		this(attributeName,hasValue,NONE);
	}
	
	/**
	 * コンストラクタ。
	 * @param attributeName 属性の名前
	 * @param hasValue      値を持つかどうか
	 * @param attributeType 属性の種別
	 */
	public AttributeInfo(String attributeName,boolean hasValue,int attributeType){
		this(attributeName,hasValue,attributeType,false);
	}
	
	/**
	 * コンストラクタ。
	 * @param attributeName 属性の名前
	 * @param hasValue      値を持つかどうか
	 * @param attributeType 属性の種別
	 * @param required      必須属性かどうか
	 */
	public AttributeInfo(String attributeName,boolean hasValue,int attributeType,boolean required){
		this.attributeName = attributeName;
		this.hasValue      = hasValue;
		this.attributeType = attributeType;
		this.required      = required;
	}
	
	/**
	 * 属性の種別を取得します。
	 * @return
	 */
	public int getAttributeType(){
		return this.attributeType;
	}
	
	/**
	 * 属性の名前を取得します。
	 * @return
	 */
	public String getAttributeName(){
		return this.attributeName;
	}
	
	/**
	 * 属性が値を持つかどうかを取得します。
	 * @return
	 */
	public boolean hasValue(){
		return this.hasValue;
	}
	
	/**
	 * 属性が必須かどうかを取得します。
	 * @return
	 */
	public boolean isRequired(){
		return this.required;
	}
	
	/**
	 * 属性値を追加します。
	 * @param value 値
	 */
	public void addValue(String value){
	    this.values.add(value);
	}
	
	/**
	 * 属性値を取得します。
	 * @return 属性値の配列
	 */
	public String[] getValues(){
	    return (String[])this.values.toArray(new String[this.values.size()]);
	}
	
	/**
	 * タグの詳細説明文（TLDのdescription要素に記述されたもの）をセットします。
	 * 
	 * @param description タグの詳細説明文
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * タグの詳細説明文（TLDのdescription要素に記述されたもの）を取得します。
	 * 
	 * @return タグの詳細説明文
	 */
	public String getDescription(){
		if(this.description==null){
			return "";
		}
		return this.description;
	}
}
