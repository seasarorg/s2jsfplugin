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
package org.seasar.s2jsfplugin.assist;

import org.eclipse.swt.graphics.Image;

/**
 * アシスト情報を格納するクラス。
 * 
 * @author Naoki Takezoe
 */
public class AssistInfo {
	
	private String displayString;
	private String replaceString;
	private Image image;
	private String description;
	
	public AssistInfo(String displayString,Image image){
		this.displayString = displayString;
		this.replaceString = displayString;
		this.image = image;
	}
	
	public AssistInfo(String replaceString,String displayString,Image image){
		this.displayString = displayString;
		this.replaceString = replaceString;
		this.image = image;
	}
	
	public AssistInfo(String replaceString,String displayString,Image image, String description){
		this.displayString = displayString;
		this.replaceString = replaceString;
		this.image = image;
		this.description = description;
	}
	
	/** 表示用の文字列を取得します。 */
	public String getDisplayString() {
		return displayString;
	}
	
	/** 実際にアシストする文字列を取得します。 */
	public String getReplaceString() {
		return replaceString;
	}
	
	public Image getImage(){
		return image;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public int hashCode() {
		return this.replaceString.hashCode();
	}

	public boolean equals(Object obj){
		if(obj instanceof AssistInfo){
			return ((AssistInfo)obj).getReplaceString().equals(this.replaceString);
		}
		return false;
	}
}
