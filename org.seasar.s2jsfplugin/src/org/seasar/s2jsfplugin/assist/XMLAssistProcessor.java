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
package org.seasar.s2jsfplugin.assist;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.AttributeInfo;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.model.TagInfo;

import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDChoice;
import com.wutka.dtd.DTDDecl;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDEmpty;
import com.wutka.dtd.DTDEnumeration;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDMixed;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDParser;
import com.wutka.dtd.DTDSequence;

/**
 * XMLエディタのアシストプロセッサ。
 * 
 * @author Naoki Takezoe
 */
public class XMLAssistProcessor extends HTMLAssistProcessor {

	private FileAssistProcessor fileAssistProcessor = new FileAssistProcessor();
	private List tagList = new ArrayList();
	private TagInfo root = null;
	
	public XMLAssistProcessor(){
		super();
		fileAssistProcessor.setFilter("html");
	}
	
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		// テンプレートは無効
		return null;
	}	
	
	/**
	 * 補完情報を更新します。
	 * 
	 * @param project S2JSFProject
	 * @param file このエディタで編集中のファイル
	 */
	public void update(S2JSFProject project,IFile file){
		super.update(project, file);
		fileAssistProcessor.reload(file);
	}
	
	/**
	 * 補完情報をリフレッシュします。
	 * 
	 * @param in DTDの入力ストリーム
	 */
	public void updateDTDInfo(Reader in){
		// まず既存のリストをクリア
		tagList.clear();
		root = null;
		try {
			// DTDを読み込んで補完情報を作成
			DTDParser parser = new DTDParser(in);
			DTD dtd = parser.parse();
			Object[] obj = dtd.getItems();
			for(int i=0;i<obj.length;i++){
				if(obj[i] instanceof DTDElement){
					DTDElement element = (DTDElement)obj[i];
					String name = element.getName();
					DTDItem item = element.getContent();
					boolean hasBody = true;
					if(item instanceof DTDEmpty){
						hasBody = false;
					}
					TagInfo tagInfo = new TagInfo(name,hasBody);
					Iterator ite = element.attributes.keySet().iterator();
					
					// 子タグの情報をセット
					if(item instanceof DTDSequence){
						DTDSequence seq = (DTDSequence)item;
						setChildTagName(tagInfo,seq.getItem());
					} else if(item instanceof DTDMixed){
						// #PCDATAのときなんもしない
					}
					
					while(ite.hasNext()){
						String attrName = (String)ite.next();
						DTDAttribute attr = element.getAttribute(attrName);
						
						DTDDecl decl = attr.getDecl();
						boolean required = false;
						if(decl == DTDDecl.REQUIRED){
							required = true;
						}
						
						AttributeInfo attrInfo = new AttributeInfo(attrName,true,AttributeInfo.NONE,required);
						tagInfo.addAttributeInfo(attrInfo);
						
						Object attrType = attr.getType();
						if(attrType instanceof DTDEnumeration){
							DTDEnumeration enumDTD = (DTDEnumeration)attrType;
							String[] items = enumDTD.getItems();
							for(int j=0;j<items.length;j++){
								attrInfo.addValue(items[j]);
							}
						}
					}
					tagList.add(tagInfo);
					// TODO とりあえず最初に出てきたのがルートということにしときます。
					// DOCTYPE宣言をパースすれば判別できそうですけどね…
					if(root==null){
						root = tagInfo;
					}
				}
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	/**
	 * TagInfoに子タグの名前をセットします。
	 * 
	 * @param tagInfo タグ情報
	 * @param items   DTDItemの配列
	 */
	private void setChildTagName(TagInfo tagInfo,DTDItem[] items){
		for(int i=0;i<items.length;i++){
			if(items[i] instanceof DTDName){
				DTDName dtdName = (DTDName)items[i];
				tagInfo.addChildTagName(dtdName.getValue());
			} else if(items[i] instanceof DTDChoice){
				DTDChoice dtdChoise = (DTDChoice)items[i];
				setChildTagName(tagInfo,dtdChoise.getItem());
			}
		}
	}
	
	protected boolean supportTagRelation(){
		return true;
	}
	
	protected TagInfo getRootTagInfo() {
		return root;
	}
	
	/**
	 * 属性値を補完するための属性値リストを取得します。
	 * 
	 * @param tagName タグの名前
	 * @param value   入力中の属性
	 * @param info    属性情報
	 * @return 属性値の配列
	 */
	protected AssistInfo[] getAttributeValues(FuzzyXMLElement element,String value,AttributeInfo info){
	    String[] values = info.getValues();
	    AssistInfo[] infos = new AssistInfo[values.length];
	    for(int i=0;i<infos.length;i++){
	    	infos[i] = new AssistInfo(values[i],S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_VALUE));
	    }
	    return infos;
	}
	
	/**
	 * タグを補完するためのTagInfoのリストを取得します。
	 * 
	 * @return TagInfoを格納したList
	 */
	protected List getTagList(){
	    return tagList;
	}
	
	/**
	 * 指定したタグ名のTagInfoを取得します。
	 * 
	 * @param name タグ名
	 * @return TagInfo
	 */
	protected TagInfo getTagInfo(String name){
		for(int i=0;i<tagList.size();i++){
			TagInfo info = (TagInfo)tagList.get(i);
			if(info.getTagName().equals(name)){
				return info;
			}
		}
		return null;
	}

	/**
	 * タグのボディ部分の補完候補を返却します。
	 * <p>
	 * このプロセッサではto-view-id、from-view-idタグの場合に
	 * HTMLファイルの補完を行います。
	 */
	protected AssistInfo[] getTagBody(String tagName, String value) {
		if(tagName.equals("to-view-id") || tagName.equals("from-view-id")){
			AssistInfo[] infos = fileAssistProcessor.getAssistInfo(value, true);
			return infos;
		}
		return super.getTagBody(tagName, value);
	}
	
	
}
