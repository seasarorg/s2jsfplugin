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
	 * コンストラクタ。
	 * @param tagName タグの名前
	 * @param hasBody 子要素を許可するかどうか
	 */
	public TagInfo(String tagName,boolean hasBody){
		this.tagName = tagName;
		this.hasBody = hasBody;
	}
	
	/**
	 * タグの名前を取得します。
	 * @return
	 */
	public String getTagName(){
		return this.tagName;
	}
	
	/**
	 * 子要素を許可するかどうかを取得します。
	 * @return
	 */
	public boolean hasBody(){
		return this.hasBody;
	}
	
	/**
	 * 属性情報を追加します。
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
	 * 全ての属性情報を取得します。
	 * @param attribute
	 */
	public AttributeInfo[] getAttributeInfo(){
		return (AttributeInfo[])this.attributes.toArray(new AttributeInfo[this.attributes.size()]);
	}
	
	/**
	 * 必須の属性情報を取得します。
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
	 * 指定した名前の属性情報を取得します。
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
	 * 子タグの名前をセットします。
	 * 
	 * @param name 子タグの名前
	 */
	public void addChildTagName(String name){
		children.add(name);
	}
	
	/**
	 * 子タグの名前を取得します。
	 * 
	 * @return 子タグの名前（String配列）
	 */
	public String[] getChildTagNames(){
		return (String[])children.toArray(new String[children.size()]);
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
		return this.description;
	}
}
