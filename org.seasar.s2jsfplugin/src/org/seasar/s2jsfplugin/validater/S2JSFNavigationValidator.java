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
package org.seasar.s2jsfplugin.validater;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.editor.FacesConfigDTDResolver;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * S2JSF��faces-config.xml���o���f�[�V�������܂��B
 * 
 * @author Naoki Takezoe
 */
public class S2JSFNavigationValidator extends AbstractS2JSFValidator {

	/**
	 * �R���X�g���N�^�B
	 * 
	 * @param project S2JSF�v���W�F�N�g
	 * @param file ���ؑΏۂ̃t�@�C���I�u�W�F�N�g
	 */
	public S2JSFNavigationValidator(S2JSFProject project,IFile file){
		super(project,file);
	}
	
	public void doValidate(){
		try {
			Util.removeMakers(getFile());
			
			if(validateDTD()){
				validateContents();
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	/** DTD�x�[�X�̌��؂��s���܂��B */
	private boolean validateDTD(){
		try {
			SAXParserFactory spfactory = SAXParserFactory.newInstance();
			spfactory.setValidating(true);
			SAXParser parser = spfactory.newSAXParser();
			XMLReader reader = parser.getXMLReader(); 
			
			reader.setEntityResolver(new FacesConfigDTDResolver());
			XMLValidationHandler handler = new XMLValidationHandler(getFile());
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(getFile().getContents()));
			
			return handler.getResult();
		} catch(Exception ex){
		}
		return false;
	}
	
	/** XML�t�@�C���̓��e�����؂��܂��B */
	private void validateContents() throws Exception {
		FuzzyXMLDocument doc = new FuzzyXMLParser().parse(getFile().getContents());
		FuzzyXMLElement root = doc.getDocumentElement();
		FuzzyXMLNode[] nodes = XPath.selectNodes(root,"/faces-config/navigation-rule");
		for(int i=0;i<nodes.length;i++){
			validateNavigation((FuzzyXMLElement)nodes[i]);
		}
	}
	
	/** navigation-rule�G�������g�̌��؂��s���܂��B */
	private void validateNavigation(FuzzyXMLElement element) throws Exception {
		FuzzyXMLNode[] cases = XPath.selectNodes(element,"/navigation-case");
		for(int i=0;i<cases.length;i++){
			FuzzyXMLNode toViewId = XPath.selectSingleNode((FuzzyXMLElement)cases[i],"/to-view-id");
			if(toViewId!=null){
				String viewId = ((FuzzyXMLElement)toViewId).getValue().trim();
				if(!existsHTML(viewId)){
					// �G���[
					Util.createErrorMarker(
							getFile(),
							toViewId.getOffset(),
							toViewId.getOffset()+toViewId.getLength(),
							getLineAtOffset(toViewId.getOffset()),
							createMessage(ValidationMessages.NOT_EXISTS,viewId));
				}
			}
		}
	}
	
	/** �w�肳�ꂽ�p�X��HTML�����݂��邩�ǂ������`�F�b�N���܂��B */
	private boolean existsHTML(String viewID){
		IFile html = getFile().getProject().getFile(new Path(getS2JSFProjectParams().getRoot()).append(viewID));
		if(html.exists()){
			return true;
		}
		return false;
	}
	
	/** XML�t�@�C���̌��؂��s��SAX�n���h�� */
	private class XMLValidationHandler implements ErrorHandler {

		private IResource resource;
		private boolean result;
		
		public XMLValidationHandler(IResource resource) {
			this.resource = resource;
			this.result = true;
		}
		
		public boolean getResult(){
			return this.result;
		}
		
		private void addMarker(int line,String message,int type){
			try {
				IMarker marker = resource.createMarker(IMarker.PROBLEM);
				Map map = new HashMap();
				map.put(IMarker.SEVERITY, new Integer(type));
				map.put(IMarker.MESSAGE, message);
				map.put(IMarker.LINE_NUMBER,new Integer(line));
				marker.setAttributes(map);
				result = false;
			} catch(Exception ex){
			    Util.logException(ex);
			}
		}
		
		public void error(SAXParseException exception) throws SAXException {
			int line = exception.getLineNumber();
			String message = exception.getMessage();
			addMarker(line,message,IMarker.SEVERITY_ERROR);
		}
		
		public void fatalError(SAXParseException exception) throws SAXException {
			int line = exception.getLineNumber();
			String message = exception.getMessage();
			addMarker(line,message,IMarker.SEVERITY_ERROR);
		}
		
		public void warning(SAXParseException exception) throws SAXException {
			int line = exception.getLineNumber();
			String message = exception.getMessage();
			addMarker(line,message,IMarker.SEVERITY_WARNING);
		}
	}
	
}
