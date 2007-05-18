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
package org.seasar.s2jsfplugin.validater;

import java.io.InputStream;
import java.io.StringReader;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;
import jp.aonir.fuzzyxml.event.FuzzyXMLErrorEvent;
import jp.aonir.fuzzyxml.event.FuzzyXMLErrorListener;

import org.apache.commons.el.parser.ELParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.assist.HTMLAssistProcessor;
import org.seasar.s2jsfplugin.assist.JSFTagDefinition;
import org.seasar.s2jsfplugin.assist.S2JSFSelecterMap;
import org.seasar.s2jsfplugin.model.AttributeInfo;
import org.seasar.s2jsfplugin.model.ManagedBean;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.model.TLDInfo;
import org.seasar.s2jsfplugin.model.TagInfo;
import org.seasar.s2jsfplugin.model.Taglib;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * S2JSFのHTMLのバリデーションを行います。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFHTMLValidator extends AbstractS2JSFValidator {
	
	private ManagedBean initBean = null;
	
	/**
	 * コンストラクタ
	 * 
	 * @param project S2JSFプロジェクト
	 * @param file 検証対象のファイルオブジェクト
	 */
	public S2JSFHTMLValidator(S2JSFProject project,IFile file){
		super(project,file);
	}
	
	/**
	 * バリデーションを実行します。
	 */
	public void doValidate(){
		try {
			Util.removeMakers(getFile());
			
			S2JSFProjectParams params = getS2JSFProjectParams();
			
			InputStream in = getFile().getContents();
			FuzzyXMLParser parser = new FuzzyXMLParser(true);
			if(params.getValidateCloseTag()){
				parser.addErrorListener(new FuzzyXMLErrorListenerImpl());
			}
			FuzzyXMLDocument doc = parser.parse(in);
			if(doc==null){
				return;
			}
			FuzzyXMLElement root = doc.getDocumentElement();
			FuzzyXMLElement html = (FuzzyXMLElement)XPath.selectSingleNode(root,"*");
			if(html!=null){
				initBean = Util.getInitBean(getS2JSFProject(),html);
				validateElement(html);
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	/** エレメントのバリデーションを行います。 */
	private void validateElement(FuzzyXMLElement element) throws CoreException {
		// 名前空間のバリデーション
		if(element.getName().indexOf(":") > 0){
			// タグに対してバリデーション
			String[] dim = element.getName().split(":");
			if(dim.length > 1 && Util.getPrefixURI(element,dim[0])==null){
				Util.createErrorMarker(getFile(),
						element.getOffset()+1,
						element.getOffset()+dim[0].length()+2,
						getLineAtOffset(element.getOffset()),
						createMessage(ValidationMessages.NS_NOT_DEFINED,dim[0]));
				return;
			}
		}
		// 属性に対してもバリデーション
		FuzzyXMLAttribute[] attrs = element.getAttributes();
		for(int i=0;i<attrs.length;i++){
			if(attrs[i].getName().indexOf(":") > 0 && !attrs[i].getName().startsWith("xmlns:")){
				String[] dim = attrs[i].getName().split(":");
				if(dim.length > 1 && !dim[0].equals("xml") && Util.getPrefixURI(element,dim[0])==null){
					Util.createErrorMarker(getFile(),
							attrs[i].getOffset()+1,
							attrs[i].getOffset()+dim[0].length()+2,
							getLineAtOffset(attrs[i].getOffset()+1),
							createMessage(ValidationMessages.NS_NOT_DEFINED,dim[0]));
				}
			}
		}
		
		String mayaPrefix = Util.getMayaPrefix(element);
		if(mayaPrefix!=null){
			FuzzyXMLAttribute attr = element.getAttributeNode(mayaPrefix + ":inject");
			if(attr!=null){
				// m:injectでインジェクションされたカスタムタグのバリデーション
				validateTaglib(attr,element);
			} else {
				// それ以外
				String[] jsfTagName = S2JSFSelecterMap.getTagName(getS2JSFProject(),element, mayaPrefix);
				if(jsfTagName!=null){
					// JSFカスタムタグのバリデーション
					validateJSFTaglib(jsfTagName[1],element);
				} else if(element.getName().equalsIgnoreCase("html")){
					// HTMLタグのバリデーション
					validateHTMLTag(element);
				}
			}
		}
		// 子要素をバリデーション
		FuzzyXMLNode[] children = element.getChildren();
		for(int i=0;i<children.length;i++){
			if(children[i] instanceof FuzzyXMLElement){
				validateElement((FuzzyXMLElement)children[i]);
			}
		}
	}
	
	/** m:injectで指定されたカスタムタグのバリデーションを行います。 */
	private void validateTaglib(FuzzyXMLAttribute injectAttr,FuzzyXMLElement element) throws CoreException {
		String mayaPrefix = Util.getMayaPrefix(element);
		if(mayaPrefix==null){
			return;
		}
		String inject = injectAttr.getValue();
		// m:injectの属性値が空
		if(inject.equals("")){
			createAttributeValueMarker(injectAttr,createMessage(ValidationMessages.INJECT_EMPTY,mayaPrefix));
			return;
		}
		// プレフィックスが指定されていない
		if(inject.indexOf(":") <= 0){
			createAttributeValueMarker(injectAttr,createMessage(ValidationMessages.INJECT_PREFIX,mayaPrefix));
			return;
		}
		// カスタムタグを検索
		String[] dim = inject.split(":");
		Taglib taglib = getS2JSFProject().getTaglib(dim[0]);
		if(taglib==null){
			createAttributeValueMarker(injectAttr,createMessage(ValidationMessages.PREFIX_NOT_EXIST,dim[0]));
			return;
		}
		TLDInfo tld = getS2JSFProject().getTLDInfo(taglib.getUri());
		if(tld==null){
			createAttributeValueMarker(injectAttr,createMessage(ValidationMessages.TLD_NOT_FOUND,taglib.getUri()));
			return;
		}
		TagInfo tag = tld.getTagInfo(dim[1]);
		if(tag==null){
			createAttributeValueMarker(injectAttr,createMessage(ValidationMessages.TAGLIB_NOT_EXIST,inject));
			return;
		}
		
		// JSFタグと関連づいているタグの場合
		if(JSFTagDefinition.hasTagInfo(dim[1]) && taglib.getUri().equals(S2JSFPlugin.S2JSF_URI)){
			validateJSFTaglib(dim[1],element);
		}
		
		// 必須属性のチェック
		AttributeInfo[] attrs = tag.getAttributeInfo();
		for(int i=0;i<attrs.length;i++){
			if(attrs[i].isRequired() && element.getAttributeNode(mayaPrefix + ":" + attrs[i].getAttributeName())==null){
				Util.createErrorMarker(getFile(),
						element.getOffset(),
						element.getOffset()+element.getLength(),
						getLineAtOffset(element.getOffset()),
						createMessage(ValidationMessages.ATTR_REQUIRED,mayaPrefix + ":" + attrs[i].getAttributeName()));
				return;
			}
		}
	}
	
	/** HTMLタグのバリデーション */
	private void validateHTMLTag(FuzzyXMLElement element) throws CoreException {
		String mayaPrefix = Util.getMayaPrefix(element);
		if(mayaPrefix==null){
			return;
		}
		FuzzyXMLAttribute[] attrs = element.getAttributes();
		for(int i=0;i<attrs.length;i++){
			if(attrs[i].getName().startsWith(mayaPrefix + ":action")){
				String value = processExpression(attrs[i],attrs[i].getValue());
				if(value==null){
					continue;
				}
				String[] dim = value.split("\\.");
				ManagedBean bean = getS2JSFProject().getManagedBean(dim[0]);
				
				if(attrs[i].getValue().indexOf('.')>0 && dim.length==1){
					createAttributeValueMarker(attrs[i],S2JSFPlugin.getResourceString(ValidationMessages.NOT_SPECIFIED));
					continue;
				}
				if(dim.length==1 || bean==null){
					continue;
				}
				if(!bean.hasActionMethod(dim[1])){
					createAttributeValueMarker(attrs[i],createMessage(ValidationMessages.NOT_DEFINED,value));
					continue;
				}
			}
			if(attrs[i].getName().startsWith(mayaPrefix + ":extends")){
//				String value = attrs[i].getValue();
				// TODO extends属性のバリデーション
//				if(project.existsFile(value)){
//					
//				}
			}
		}
	}
	
	/** JSFのカスタムタグのバリデーションを行います。 */
	private void validateJSFTaglib(String jsfTagName,FuzzyXMLElement element) throws CoreException {
		String mayaPrefix = Util.getMayaPrefix(element);
		if(mayaPrefix==null){
			return;
		}
		
		// UIコンポーネントは必須属性がほとんどないのでやるだけ無駄かも？
//		// 必須属性のチェック
//		TLDInfo tld = project.getTLDInfo(S2JSFPlugin.HTML_URI);
//		TagInfo tag = tld.getTagInfo(jsfTagName);
//		if(tld!=null){
//			AttributeInfo[] attrs = tag.getAttributeInfo();
//			for(int i=0;i<attrs.length;i++){
//				if(attrs[i].isRequired() && element.getAttributeNode(mayaPrefix + ":" + attrs[i].getAttributeName())==null){
//					createMarker(file,element.getOffset(),element.getOffset()+element.getLength(),
//							widget.getLineAtOffset(element.getOffset()),
//							mayaPrefix + ":" + attrs[i].getAttributeName() + "属性は必須です。");
//					return;
//				}
//			}
//		}
		// 属性値のチェック
		FuzzyXMLAttribute[] attrs = element.getAttributes();
		for(int i=0;i<attrs.length;i++){
			String attrName  = attrs[i].getName();
			String attrValue = attrs[i].getValue();
			if(attrName.indexOf(":")<=0){
				continue;
			}
			String[] attrDim = attrName.split(":");
			if(!attrDim[0].equals(mayaPrefix)){
				continue;
			}
			String type = JSFTagDefinition.getAttributeInfo(jsfTagName,attrDim[1]);
			if(type==null){
				continue;
			}
			attrValue = processExpression(attrs[i],attrValue);
			if(attrValue==null){
				continue;
			}
			
			if(type==JSFTagDefinition.VALUE){
				type = JSFTagDefinition.PROPERTY;
			}
			// まずはELのシンタックスエラーがないか確かめる
			String el = attrs[i].getValue();
			el = el.replaceFirst("^#","\\$");
			try {
				ELParser parser = new ELParser(new StringReader(el));
				parser.ExpressionString();
			} catch(Exception ex){
				createAttributeValueMarker(attrs[i],createMessage(ValidationMessages.INVALID_EL,attrValue));
				continue;
			}
			// シンタックスエラーがなかったらBean記述部分の妥当性を検証する
			StringBuffer sb = new StringBuffer();
			boolean errorFlag = true;
			for(int j=0;j<attrValue.length();j++){
				char c = attrValue.charAt(j);
				if(Character.isJavaIdentifierPart(c) || c=='.'){
					sb.append(c);
				} else {
					errorFlag = validateBinding(element,attrs[i],sb.toString(),type);
					sb.setLength(0);
					if(errorFlag==false){
						break;
					}
					if(c=='['){
						errorFlag = false;
						break;
					}
				}
			}
			if(errorFlag==true && sb.length()>0){
				validateBinding(element,attrs[i],sb.toString(),type);
			}
		}
	}
	
	/**
	 * Bean記述部分のバリデーションを行います。
	 * エラーがなかった場合true、あった場合falseを返します。
	 */
	private boolean validateBinding(FuzzyXMLElement element,FuzzyXMLAttribute attr,String value,String type) throws CoreException {
		String[] dim = Util.splitManagedBean(value);
		ManagedBean bean = Util.evalPropertyBinding(dim,getS2JSFProject(),initBean);
		
		if(value.indexOf('.')>0 && dim.length==1){
			createAttributeValueMarker(attr,S2JSFPlugin.getResourceString(ValidationMessages.NOT_SPECIFIED));
			return false;
		}
		if(bean==null && !hasVariable(dim[0],element) && value.indexOf('.')>0){
			createAttributeValueMarker(attr,createMessage(ValidationMessages.NOT_DEFINED,value));
			return false;
		}
		if(dim.length==1 || bean==null){
			return true;
		}
		
		if(type==JSFTagDefinition.ACTION){
			if(!bean.hasActionMethod(dim[dim.length-1])){
				createAttributeValueMarker(attr,createMessage(ValidationMessages.NOT_DEFINED,value));
				return false;
			}
		} else if(type==JSFTagDefinition.ACTION_LISTENER){
			if(!bean.hasActionListenerMethod(dim[dim.length-1])){
				createAttributeValueMarker(attr,createMessage(ValidationMessages.NOT_DEFINED,value));
				return false;
			}
		} else if(type==JSFTagDefinition.CHANGE_LISTENER){
			if(!bean.hasValueChangeListenerMethod(dim[dim.length-1])){
				createAttributeValueMarker(attr,createMessage(ValidationMessages.NOT_DEFINED,value));
				return false;
			}
		} else if(type==JSFTagDefinition.VALIDATER){
			if(!bean.hasValidaterMethod(dim[dim.length-1])){
				createAttributeValueMarker(attr,createMessage(ValidationMessages.NOT_DEFINED,value));
				return false;
			}
		} else if(type==JSFTagDefinition.PROPERTY){
			if(!bean.hasProperty(dim[dim.length-1])){
				createAttributeValueMarker(attr,createMessage(ValidationMessages.NOT_DEFINED,value));
				return false;
			}
		}
		return true;
	}
	
	/**
	 * EL部分から #{ と } を除去して式の部分を取り出します。
	 */
	private String processExpression(FuzzyXMLAttribute attr,String value) throws CoreException {
		if(!value.startsWith("#{")){
			return null;
		}
		value = value.replaceFirst("^#\\{","");
		if(!value.endsWith("}")){
			createAttributeValueMarker(attr,createMessage(ValidationMessages.REQUIRE_CLOSE,attr.getName()));
			return null;
		}
		value = value.replaceFirst("\\}$" ,"");
		value = value.trim();
		
		// 暗黙オブジェクトの場合はとりあえず飛ばしておく
		for(int i=0;i<HTMLAssistProcessor.IMPLICIT_OBJECTS.length;i++){
			if(value.startsWith(HTMLAssistProcessor.IMPLICIT_OBJECTS[i])){
				return null;
			}
		}
		
		return value;
	}
	
	/** 属性値部分にエラーマーカを作成します。 */
	private void createAttributeValueMarker(FuzzyXMLAttribute attr,String message) throws CoreException {
		Util.createErrorMarker(getFile(),
				getAttrValueOffset(attr.getOffset()+1),
				attr.getOffset()+attr.getLength(),
				getLineAtOffset(attr.getOffset()+1),
				message);
	}
	
	/**
	 * このタグのコンテキストで変数が存在するかどうかを調べます（適当）。
	 */
	private boolean hasVariable(String var, FuzzyXMLElement element){
		if(element.getParentNode()==null){
			return false;
		}
		String mayaPrefix = Util.getMayaPrefix(element);
		if(mayaPrefix==null){
			return false;
		}
		FuzzyXMLElement parent = (FuzzyXMLElement)element.getParentNode();
		FuzzyXMLAttribute attr = parent.getAttributeNode(mayaPrefix + ":var");
		if(attr!=null && attr.getValue().equals(var)){
			return true;
		}
		return hasVariable(var,parent);
	}
	
	/** FuzzyXMLのパースエラーを処理するリスナ */
	private class FuzzyXMLErrorListenerImpl implements FuzzyXMLErrorListener {
		public void error(FuzzyXMLErrorEvent evt) {
			try {
				FuzzyXMLNode node = evt.getNode();
				if(node instanceof FuzzyXMLElement){
					FuzzyXMLElement element = (FuzzyXMLElement)node;
					String name = element.getName();
					// 閉じタグがなくてもOKなタグは警告しない
					if(name.equalsIgnoreCase("p") || name.equalsIgnoreCase("br") || name.equalsIgnoreCase("img") ||
					   name.equalsIgnoreCase("link") || name.equalsIgnoreCase("meta") || name.equalsIgnoreCase("input") ||
					   name.equalsIgnoreCase("hr")){
						return;
					}
				}
				
				Util.createWarnMarker(getFile(),evt.getOffset(),evt.getOffset()+evt.getLength(),
						getLineAtOffset(evt.getOffset()),evt.getMessage());
			} catch(Exception ex){
				Util.logException(ex);
			}
		}
	}
}
