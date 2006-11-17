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
 * カスタムタグライブラリの補完情報を格納するクラス。
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
	 * カスタムタグのURI（JSPのtaglibディレクティブで指定されたもの）を取得します。
	 * 
	 * @return URI
	 */
	public String getUri(){
		return uri;
	}
	
	/**
	 * TLDで定義されているカスタムタグのTagInfoをリストで取得します。
	 * 
	 * @return カスタムタグのTagInfoを格納したList
	 */
	public List getTagInfo(){
		return tagInfoList;
	}
	
	/**
	 * タグの名前を指定してTagInfoを取得します。
	 * 該当するタグが存在しない場合、nullを返します。
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
