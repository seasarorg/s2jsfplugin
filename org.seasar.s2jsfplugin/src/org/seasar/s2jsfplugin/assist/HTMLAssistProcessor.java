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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.AttributeInfo;
import org.seasar.s2jsfplugin.model.ManagedBean;
import org.seasar.s2jsfplugin.model.ManagedBeanProperty;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.model.TLDInfo;
import org.seasar.s2jsfplugin.model.TagInfo;
import org.seasar.s2jsfplugin.model.Taglib;
import org.seasar.s2jsfplugin.template.HTMLTemplateAssistProcessor;

/**
 * HTML�̓��͕⊮���s��IContentAssistProcessor�̎����B
 * 
 * @author Naoki Takezoe
 */
public class HTMLAssistProcessor extends HTMLTemplateAssistProcessor /*implements IContentAssistProcessor*/ {
	
	/** JSF�̈ÖكI�u�W�F�N�g */
	public static final String[] IMPLICIT_OBJECTS = {
			"applicationScope",
			"cookie",
			"facesContext",
			"header",
			"headerValues",
			"initParam",
			"param",
			"paramValues",
			"requestScope",
			"sessionScope"
	};
	
	/** �⊮�̃g���K�ƂȂ镶�� */
	private char[] chars = {};
	/** �C���[�W */
	private Image tagImage;
	private Image attrImage;
	private Image valueImage;
	private Image fieldImage;
	private Image classImage;
//	private Image ifImage;
	private Image methodImage;
	/** ���^�O�������I�ɕ⊮���邩�ǂ��� */
	private boolean assistCloseTag = true;
	
	/** href�����Ȃǂ̕⊮���s���N���X */
	private FileAssistProcessor fileAssistProcessor = new FileAssistProcessor();
	/** class�����̕⊮���s���N���X */
	private CSSAssistProcessor cssAssistProcessor = new CSSAssistProcessor();
	
	private S2JSFProject project = null;
	private ManagedBean initBean = null;
	
	public HTMLAssistProcessor(){
		tagImage    = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TAG);
		attrImage   = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_ATTR);
		valueImage  = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_VALUE);
		fieldImage  = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_FIELD);
		classImage  = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_CLASS);
//		ifImage     = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_IF);
		methodImage = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_METHOD);
	}
	
	public void setAutoAssistChars(char[] chars){
		if(chars!=null){
			this.chars = chars;
		}
	}
	
	public void setAssistCloseTag(boolean assistCloseTag){
		this.assistCloseTag = assistCloseTag;
	}
	
	/**
	 * �^�O�̃{�f�B������⊮���邽�߂̃��X�g���擾���܂��B
	 * 
	 * @param tagName �^�O�̖��O
	 * @param value   �⊮�Ώۂ̕�����
	 * @return �⊮���̔z��
	 */
	protected AssistInfo[] getTagBody(String tagName, String value){
		return new AssistInfo[0];
	}
	
	/**
	 * �����l��⊮���邽�߂̑����l���X�g���擾���܂��B
	 * 
	 * @param element �^�O��FuzzyXMLElement�I�u�W�F�N�g
	 * @param value   ���͒��̑���
	 * @param info    �������
	 * @return �����l�̔z��
	 */
	protected AssistInfo[] getAttributeValues(FuzzyXMLElement element,String value,AttributeInfo info){
		
		String mayaPrefix = Util.getMayaPrefix(element);
		String attrName = info.getAttributeName();
		
		if(mayaPrefix!=null){
			// html�^�O�̏ꍇ
			if(element.getName().equalsIgnoreCase("html")){
				if(attrName.startsWith(mayaPrefix + ":action")){
					return getManagedBeanValues(JSFTagDefinition.ACTION,value);
				}
				if(attrName.startsWith(mayaPrefix + ":extends")){
					return fileAssistProcessor.getAssistInfo(value,true);
				}
			}
			// m:class�����̕⊮
			if(info.getAttributeName().equals(mayaPrefix + ":class")){
				return cssAssistProcessor.getAssistInfo(element.getName(),value);
			}
		}
		
		// m:inject�̑����l�������̓}�l�[�W�h�E�r�[���̕⊮
		if(mayaPrefix!=null && element!=null && attrName.startsWith(mayaPrefix + ":")){
			if(attrName.startsWith(mayaPrefix + ":inject")){
				return getInjectTaglibValues();
				
			} else if(attrName.equals(mayaPrefix + ":dir") && !value.startsWith("#{")){
				return new AssistInfo[]{
						new AssistInfo("LTR", "LTR (left-to-right)", valueImage),
						new AssistInfo("RTL", "RTL (right-to-left)", valueImage)
				};
				
			} else {
				String[] jsfTagName = S2JSFSelecterMap.getTagName(project,element,mayaPrefix);
				if(jsfTagName!=null){
					if((jsfTagName[1].equals("selectOneRadio") || jsfTagName[1].equals("selectManyCheckbox")) &&
							attrName.equals(mayaPrefix + ":layout") && !value.startsWith("#{")){
						return new AssistInfo[]{
								new AssistInfo("pageDirection", "pageDirection (vertical)", valueImage),
								new AssistInfo("lineDirection", "lineDirection (horizontal)", valueImage)
						};
					}
					String type = JSFTagDefinition.getAttributeInfo(jsfTagName[1],attrName.split(":")[1]);
					return getManagedBeanValues(type,value);
				}
			}
		}
		
		// CSS
		if(info.getAttributeType()==AttributeInfo.CSS){
			return cssAssistProcessor.getAssistInfo(element.getName(),value);
		}
		// FILE
		if(info.getAttributeType()==AttributeInfo.FILE){
			return fileAssistProcessor.getAssistInfo(value,false);
		}
		
		// �^�C�v��NONE�̏ꍇ��AttributeInfo�ɒǉ�����Ă��鑮���l�����g�p
		if(info.getAttributeType()==AttributeInfo.NONE){
			String[] values = info.getValues();
			if(values!=null){
				AssistInfo[] infos = new AssistInfo[values.length];
				for(int i=0;i<infos.length;i++){
					infos[i] = new AssistInfo(values[i],valueImage);
				}
				return infos;
			}
		}
		
		// ����ȊO�̏ꍇ�͋K��̑����l�����g�p
		String[] values = AttributeValueDefinition.getAttributeValues(info.getAttributeType());
		AssistInfo[] infos = new AssistInfo[values.length];
		for(int i=0;i<infos.length;i++){
			infos[i] = new AssistInfo(values[i],valueImage);
		}
		return infos;
	}
	
	/**
	 * m:inject�Ŏw��\�ȃJ�X�^���^�O���̕⊮�����擾���܂��B
	 * <p>
	 * 
	 */
	private AssistInfo[] getInjectTaglibValues(){
		// m:inject�����̕⊮�����쐬
		ArrayList list = new ArrayList();
		Taglib[] taglibs = project.getTaglibs();
		for(int j=0;j<taglibs.length;j++){
			String prefix = taglibs[j].getPrefix();
			TLDInfo tld = project.getTLDInfo(taglibs[j].getUri());
			if(tld!=null){
				List customTagList = tld.getTagInfo();
				for(int k=0;k<customTagList.size();k++){
					TagInfo customTagInfo = (TagInfo)customTagList.get(k);
					list.add(new AssistInfo(
							prefix + ":" + customTagInfo.getTagName(),
							prefix + ":" + customTagInfo.getTagName(),
							valueImage, customTagInfo.getDescription()));
				}
			}
		}
		return (AssistInfo[])list.toArray(new AssistInfo[list.size()]);
	}
	
	/**
	 * �}�l�[�W�h�E�r�[���̕⊮�����擾���܂��B
	 */
	private AssistInfo[] getManagedBeanValues(String type,String value){
		if(value.startsWith("#{")){
			value = value.replaceFirst("^#\\{","");
			value = value.replaceFirst("\\}$" ,"");
			
			String lastWord = value;
			String preWord  = "";
			
//			if(type==JSFTagDefinition.VALUE){
				// �Ō�̒P���؂�o��
				StringBuffer sb = new StringBuffer();
				for(int i=0;i<value.length();i++){
					char c = value.charAt(i);
					if(Character.isJavaIdentifierPart(c) || c=='.'){
						sb.append(c);
					} else {
						sb.setLength(0);
					}
				}
				lastWord = sb.toString();
				preWord  = value.substring(0,value.length()-lastWord.length());
//			}
			
			if(lastWord.indexOf(".") == -1){
				ManagedBean[] beans = project.getManagedBeans();
				ArrayList assists = new ArrayList();
				for(int i=0;i<beans.length;i++){
//					Image image = null;
					assists.add( new AssistInfo(
							"#{" + preWord + beans[i].getBeanName(),
							beans[i].getBeanName() + " - " + beans[i].getClassName(),
							classImage, beans[i].getJavadoc()));
				}
				for(int i=0;i<IMPLICIT_OBJECTS.length;i++){
					assists.add( new AssistInfo(
							"#{" + preWord + IMPLICIT_OBJECTS[i],
							IMPLICIT_OBJECTS[i] + " - " + S2JSFPlugin.getResourceString("ContentAssistProposal.implicitObject"),
							classImage));
				}
				if(initBean!=null){
					ManagedBeanProperty[] props = initBean.getProperties();
					for(int i=0;i<props.length;i++){
						ManagedBean propBean = props[i].toManagedBean();
						assists.add( new AssistInfo(
								"#{" + preWord + propBean.getBeanName(),
								propBean.getBeanName() + " - " + propBean.getClassName(),
								classImage,propBean.getJavadoc()));
					}
				}
				return (AssistInfo[])assists.toArray(new AssistInfo[assists.size()]);
				
			} else {
				String[] dim = Util.splitManagedBean(lastWord);
				preWord = preWord + Util.joinManagedBean(dim);
				
				ManagedBean bean = Util.evalPropertyBinding(dim,project,initBean);
				if(bean!=null){
					return getManagedBeanProperties(bean,type,preWord);
				}
			}
		}
		return new AssistInfo[0];
	}
	
	/**
	 * �����l�̃^�C�v�ɉ����ă}�l�[�W�h�E�r�[���̃v���p�e�B�܂��̓��\�b�h�̕ۊǏ����擾���܂��B
	 * 
	 * @param bean �⊮�Ώۂ̃}�l�[�W�h�E�r�[��
	 * @param type �����l�̃^�C�v�iJSFTagDefinition�Œ�`����Ă���l�j
	 * @param pre �⊮������̑O�̕���
	 * @return �A�V�X�g���
	 */
	private AssistInfo[] getManagedBeanProperties(ManagedBean bean,String type,String pre){
		if(type==JSFTagDefinition.PROPERTY || type==JSFTagDefinition.VALUE){
			ManagedBeanProperty[] beanProps = bean.getProperties();
			AssistInfo[] info = new AssistInfo[beanProps.length];
			for(int j=0;j<beanProps.length;j++){
				info[j] = new AssistInfo(
						"#{" + pre + "." + beanProps[j].getPropertyName(),
						beanProps[j].getPropertyName() + " - " + beanProps[j].getPropertyType(),
						fieldImage, beanProps[j].getJavadoc());
			}
			return info;
		} else {
			try {
				ArrayList list =  new ArrayList();
				ManagedBeanProperty[] beanProps = bean.getProperties();
				for(int j=0;j<beanProps.length;j++){
					AssistInfo info = new AssistInfo(
							"#{" + pre + "." + beanProps[j].getPropertyName(),
							beanProps[j].getPropertyName() + " - " + beanProps[j].getPropertyType(),
							fieldImage, beanProps[j].getJavadoc());
					list.add(info);
				}
				IMethod[] methods = new IMethod[0];
				if(type==JSFTagDefinition.ACTION){
					methods = bean.getActionMethods();
				} else if(type==JSFTagDefinition.ACTION_LISTENER){
					methods = bean.getActionListenerMethods();
				} else if(type==JSFTagDefinition.CHANGE_LISTENER){
					methods = bean.getValueChangeListenerMethods();
				} else if(type==JSFTagDefinition.VALIDATER){
					methods = bean.getValidaterMethods();
				}
				for(int i=0;i<methods.length;i++){
					AssistInfo info = new AssistInfo("#{" + pre + "." + methods[i].getElementName(),
							methods[i].getElementName(),
							methodImage, Util.extractJavadoc(methods[i], null));
					list.add(info);
				}
				return (AssistInfo[])list.toArray(new AssistInfo[list.size()]);
			} catch(Exception ex){
			}
		}
		return new AssistInfo[0];
	}
	
	/**
	 * �^�O��⊮���邽�߂�TagInfo�̃��X�g���擾���܂��B
	 * 
	 * @return TagInfo���i�[����List
	 */
	protected List getTagList(){
		return TagDefinition.getTagInfoAsList();
	}
	
	/**
	 * �w�肵���^�O����TagInfo���擾���܂��B
	 * 
	 * @param name �^�O��
	 * @return TagInfo
	 */
	protected TagInfo getTagInfo(String name){
		List tagList = getTagList();
		for(int i=0;i<tagList.size();i++){
			TagInfo info = (TagInfo)tagList.get(i);
			if(info.getTagName().equals(name)){
				return info;
			}
		}
		return null;
	}
	
	/**
	 * S2JSF�Ǝ������̕⊮�����擾���܂��B
	 * <p>
	 * �ڍ׏�񂪂���ꍇ��&quot;������|�ڍ׃��b�Z�[�W&quot;�Ƃ����`���ŕԋp���܂��B
	 */
	private AssistInfo[] getS2JSFAttributes(FuzzyXMLElement element,int documentOffset){
		String mayaPrefix = Util.getMayaPrefix(element);
		ArrayList list = new ArrayList();
		if(mayaPrefix!=null){
			list.add(new AssistInfo(mayaPrefix + ":inject", attrImage));
			list.add(new AssistInfo(mayaPrefix + ":passthrough", attrImage));
			list.add(new AssistInfo(mayaPrefix + ":rendered", attrImage));
//			list.add(mayaPrefix + ":binding");
//			list.add(mayaPrefix + ":id");
			// html�^�O�̏ꍇ
			if(element.getName().equalsIgnoreCase("html")){
				list.add(new AssistInfo(mayaPrefix + ":extends", attrImage));
				list.add(new AssistInfo(mayaPrefix + ":action", attrImage));
			}
			// title�^�O�̏ꍇ
			if(element.getName().equalsIgnoreCase("title") && element.getAttributeNode(mayaPrefix + ":inject")==null){
				list.add(new AssistInfo(mayaPrefix + ":value", attrImage));
			}
			// span�^�O�̏ꍇ
			if(element.getName().equalsIgnoreCase("span") && element.getAttributeNode(mayaPrefix + ":inject")==null){
				list.add(new AssistInfo(mayaPrefix + ":value", attrImage));
			}
			// input type="submit"�̏ꍇ
			if(element.getName().equalsIgnoreCase("input") && element.getAttributeNode(mayaPrefix + ":inject")==null){
				FuzzyXMLAttribute attr = element.getAttributeNode("type");
				if(attr!=null && attr.getValue().equals("submit")){
					list.add(new AssistInfo("m:action", attrImage));
				}
			}
		}
		if(element!=null && mayaPrefix!=null){
			String inject = Util.getXPathValue(element, "@" + mayaPrefix + ":inject");
			if(inject!=null && inject.indexOf(":") > 0){
				String[] dim = inject.split(":");
				String uri = null;
				Taglib taglib = project.getTaglib(dim[0]);
				if(taglib!=null){
					uri = taglib.getUri();
				}
				TLDInfo info = project.getTLDInfo(uri);
				TagInfo tag = info.getTagInfo(dim[1]);
				if(tag!=null){
					AttributeInfo[] attrInfo = tag.getAttributeInfo();
					for(int j=0;j<attrInfo.length;j++){
						list.add(new AssistInfo(
								mayaPrefix + ":" + attrInfo[j].getAttributeName(),
								mayaPrefix + ":" + attrInfo[j].getAttributeName(),
								attrImage, attrInfo[j].getDescription()));
					}
					return removeDupulicate((AssistInfo[])list.toArray(new AssistInfo[list.size()]));
				}
			}
			
			// �^�O�ɉ����������̕⊮�����쐬
			String[] jsfTagName = S2JSFSelecterMap.getTagName(project,element,mayaPrefix);
			if(jsfTagName!=null){
				TLDInfo tld = project.getTLDInfo(jsfTagName[0]);
				TagInfo tag = tld.getTagInfo(jsfTagName[1]);
				if(tag!=null){
					AttributeInfo[] attrs = tag.getAttributeInfo();
					for(int k=0;k<attrs.length;k++){
						String name = attrs[k].getAttributeName();
						list.add(new AssistInfo(
								mayaPrefix + ":" + name,
								mayaPrefix + ":" + name,
								attrImage, attrs[k].getDescription()));
					}
					return removeDupulicate((AssistInfo[])list.toArray(new AssistInfo[list.size()]));
				}
			}
		}
		return removeDupulicate((AssistInfo[])list.toArray(new AssistInfo[list.size()]));
	}
	
	/**
	 * �⊮�����쐬���܂��B
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,int documentOffset) {
		
		String   text = viewer.getDocument().get();
		String[] dim  = Util.getWordsForCompletion(text,documentOffset);
		String   word = dim[0].toLowerCase();
		String   prev = dim[1].toLowerCase();
		String   last = dim[2];
		String   attr = dim[3];
		
		FuzzyXMLDocument doc = new FuzzyXMLParser().parse(text);
		FuzzyXMLElement element = null;
		if(doc!=null){
			// �I�t�Z�b�g�ʒu�̃G�������g���擾
			element = doc.getElementByOffset(documentOffset);
			// html��m:action�ŌĂяo���Ă���}�l�[�W�h�E�r�[�����擾
			if(doc.getDocumentElement()!=null){
				initBean = Util.getInitBean(project,
						(FuzzyXMLElement)XPath.selectSingleNode(doc.getDocumentElement(),"*"));
			}
		}
		
		List list    = new ArrayList();
		List tagList = getTagList();
		
		// �����l�̕⊮
		if((word.startsWith("\"") && (word.length()==1 || !word.endsWith("\""))) || 
		   (word.startsWith("'")  && (word.length()==1 || !word.endsWith("\'")))){
			String value = dim[0].substring(1);
			TagInfo tagInfo = getTagInfo(last.toLowerCase());
			if(tagInfo!=null){
				AttributeInfo attrInfo = tagInfo.getAttributeInfo(attr);
				if(attrInfo==null){
					attrInfo = new AttributeInfo(attr,true);
				}
				AssistInfo[] keywords = getAttributeValues(element,dim[0].substring(1),attrInfo);
				for(int i=0;i<keywords.length;i++){
					if(keywords[i].getReplaceString().toLowerCase().startsWith(value.toLowerCase())){
						list.add(new CompletionProposal(
								keywords[i].getReplaceString(), documentOffset - value.length(), value.length(),
								keywords[i].getReplaceString().length(), keywords[i].getImage(),
								keywords[i].getDisplayString(), null,keywords[i].getDescription()));
					}
				}
			}
		// �^�O�̕⊮
		} else if(word.startsWith("<") && !word.equals("</")) {
			if(supportTagRelation()){
				TagInfo parent = getTagInfo(last);
				tagList = new ArrayList();
				if(parent!=null){
					String[] childNames = parent.getChildTagNames();
					for(int i=0;i<childNames.length;i++){
						tagList.add(getTagInfo(childNames[i]));
					}
				} else {
					TagInfo root = getRootTagInfo();
					if(root!=null){
						tagList.add(root);
					}
				}
			}
			for(int i=0;i<tagList.size();i++){
				TagInfo tagInfo = (TagInfo)tagList.get(i);
				String tagName = tagInfo.getTagName();
				if (("<" + tagInfo.getTagName().toLowerCase()).indexOf(word) == 0) {
					String assistKeyword = tagName;
					int position = 0;
					// �K�{������⊮
					AttributeInfo[] requierAttrs = tagInfo.getRequiredAttributeInfo();
					for(int j=0;j<requierAttrs.length;j++){
						assistKeyword = assistKeyword + " " + requierAttrs[j].getAttributeName();
						if(requierAttrs[j].hasValue()){
							assistKeyword = assistKeyword + "=\"\"";
							if(j==0){
								position = tagName.length() + requierAttrs[j].getAttributeName().length() + 3;
							}
						}
					}
					// html�^�O�̏ꍇ��Maya�̖��O��Ԃ���������
					if(tagName.equalsIgnoreCase("html")){
						assistKeyword = assistKeyword + " xmlns:m=\"" + S2JSFPlugin.MAYA_URI + "\"";
					}
					if(tagInfo.hasBody()){
						assistKeyword = assistKeyword + ">";
						if(assistCloseTag){
							if(position==0){
								position = assistKeyword.length();
							}
							assistKeyword = assistKeyword + "</" + tagName + ">";
						}
					} else {
						assistKeyword = assistKeyword + "/>";
					}
					if(position==0){
						position = assistKeyword.length();
					}
					list.add(new CompletionProposal(
							assistKeyword, documentOffset - word.length() + 1, word.length() - 1,
							position, tagImage, tagName, null, tagInfo.getDescription()));
				}
			}
		// �����̕⊮
		} else if(!prev.equals("")){
			String tagName = prev;
			TagInfo tagInfo = getTagInfo(tagName);
			if(tagInfo!=null){
				AttributeInfo[] attrList = tagInfo.getAttributeInfo();
				for(int j=0;j<attrList.length;j++){
					if (attrList[j].getAttributeName().toLowerCase().indexOf(word) == 0) {
						String assistKeyword = null;
						int position = 0;
						if(attrList[j].hasValue()){
							assistKeyword = attrList[j].getAttributeName() + "=\"\"";
							position = 2;
						} else {
							assistKeyword = attrList[j].getAttributeName();
							position = 0;
						}
						list.add(new CompletionProposal(
								assistKeyword, documentOffset - word.length(), word.length(),
								attrList[j].getAttributeName().length() + position, attrImage, 
								attrList[j].getAttributeName(), null, attrList[j].getDescription()));
					}
				}
				AssistInfo[] s2jsfAttrs = getS2JSFAttributes(element,documentOffset);
				for(int i=0;i<s2jsfAttrs.length;i++){
					if (s2jsfAttrs[i].getReplaceString().indexOf(word) == 0) {
						String assistKeyword = s2jsfAttrs[i].getReplaceString() + "=\"\"";
						list.add(new CompletionProposal(
								assistKeyword, documentOffset - word.length(), word.length(),
								s2jsfAttrs[i].getReplaceString().length() + 2, 
								s2jsfAttrs[i].getImage(), s2jsfAttrs[i].getDisplayString(), 
								null, s2jsfAttrs[i].getDescription()));
					}
				}
			}
		// ���^�O�̕⊮
		} else if(!last.equals("")){
			AssistInfo[] infos = getTagBody(last, word);
			for(int i=0;i<infos.length;i++){
				if(infos[i].getReplaceString().toLowerCase().startsWith(word.toLowerCase())){
					list.add(new CompletionProposal(
							infos[i].getReplaceString(), documentOffset - word.length(), word.length(),
							infos[i].getReplaceString().length(), infos[i].getImage(),
							infos[i].getDisplayString(), null, null));
				}
			}
			String assistKeyword = "</" + last + ">";
			int length = 0;
			if(word.equals("</")){
				length = 2;
			}
			list.add(new CompletionProposal(
					assistKeyword, documentOffset - length, length,
					assistKeyword.length(), tagImage, assistKeyword, null, null));
		}
		
		ICompletionProposal[] prop = (CompletionProposal[])list.toArray(new CompletionProposal[list.size()]);
		sortCompilationProposal(prop);
		
		ICompletionProposal[] templates = super.computeCompletionProposals(viewer, documentOffset);
		return joinProposals(prop, templates);
	}
	
	/**
	 * �d�������G���g����z�񂩂�폜���܂��B
	 * 
	 * @param array �z��
	 * @return �폜��̔z��
	 */
	private AssistInfo[] removeDupulicate(AssistInfo[] array){
		List result = new ArrayList();
		for(int i=0;i<array.length;i++){
			if(!result.contains(array[i])){
				result.add(array[i]);
			}
		}
		return (AssistInfo[])result.toArray(new AssistInfo[result.size()]);
	}
	
	/**
	 * �⊮�����A���t�@�x�b�g���Ƀ\�[�g���܂��B
	 * 
	 * @param prop CompletionProposal[]
	 */
	private void sortCompilationProposal(ICompletionProposal[] prop){
		Arrays.sort(prop,new Comparator(){
			public int compare(Object o1,Object o2){
				CompletionProposal c1 = (CompletionProposal)o1;
				CompletionProposal c2 = (CompletionProposal)o2;
				return c1.getDisplayString().compareTo(c2.getDisplayString());
			}
		});
	}
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer,int documentOffset) {
		ContextInformation[] info = new ContextInformation[0];
		return info;
	}
	
	public char[] getCompletionProposalAutoActivationCharacters() {
		return chars;
	}
	
	public char[] getContextInformationAutoActivationCharacters() {
		return chars;
	}
	
	public IContextInformationValidator getContextInformationValidator() {
		return new ContextInformationValidator(this);
	}
	
	public String getErrorMessage() {
		return "Error";
	}
	
	/**
	 * �^�O�̐e�q�֌W���T�|�[�g����ꍇtrue��Ԃ��悤�I�[�o�[���C�h���܂��B
	 * 
	 * @return �^�O�̐e�q�֌W���T�|�[�g����ꍇtrue�A�T�|�[�g���Ȃ��ꍇfalse
	 */
	protected boolean supportTagRelation(){
		return false;
	}
	
	/**
	 * �^�O�̐e�q�֌W���T�|�[�g����ꍇ�A���[�g�̃^�O����Ԃ��悤�I�[�o�[���C�h���܂��B
	 * 
	 * @return ���[�g�ƂȂ�^�O���
	 */
	protected TagInfo getRootTagInfo() {
		return null;
	}
	
	/**
	 * �⊮�����X�V���܂��B
	 * 
	 * @param project S2JSFProject
	 * @param file ���̃G�f�B�^�ŕҏW���̃t�@�C��
	 */
	public void update(S2JSFProject project,IFile file){
		this.project = project;
		this.fileAssistProcessor.reload(file);
		this.cssAssistProcessor.reload(file);
	}
}