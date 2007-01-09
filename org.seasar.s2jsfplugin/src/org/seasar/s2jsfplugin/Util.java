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
package org.seasar.s2jsfplugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.XPath;
import jp.aonir.fuzzyxml.internal.FuzzyXMLUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.seasar.s2jsfplugin.model.ManagedBean;
import org.seasar.s2jsfplugin.model.ManagedBeanProperty;
import org.seasar.s2jsfplugin.model.S2JSFProject;


/**
 * ���[�e�B���e�B���\�b�h��񋟂��܂��B
 * 
 * @author Naoki Takezoe
 */
public class Util {
	
	/**
	 * HTML�^�O�̃G�X�P�[�v���s���܂��B�ȉ��̕ϊ����s���܂��B
	 * <p>
	 * �܂��A�����Ƃ���null���n���ꂽ�ꍇ�͋󕶎���ɕϊ����ĕԋp���܂��B
	 * </p>
	 * @param str ������
	 * @return �ϊ���̕�����
	 */
	public static String escapeHTML(String str){
		if(str==null){
			return "";
		}
		return FuzzyXMLUtil.escape(str, true);
	}
	
	/**
	 * IFile����e�L�X�g�t�@�C���̓��e��ǂݍ��݁A������Ƃ��ĕԋp���܂��B
	 * 
	 * @param file �ǂݍ���IFile�I�u�W�F�N�g
	 * @return �����Ŏw�肵��IFile�I�u�W�F�N�g�̓��e
	 * @throws IOException �t�@�C�����o�͗�O
	 * @throws CoreException IFile������̓X�g���[���╶���R�[�h�̎擾�Ɏ��s�����ꍇ
	 */
	public static String readFile(IFile file) throws IOException, CoreException {
		InputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			in = file.getContents();
			int len = 0;
			byte[] buf = new byte[1024 * 8];
			while((len = in.read(buf))!=-1){
				out.write(buf,0,len);
			}
			byte[] result = out.toByteArray();
			return new String(result, file.getCharset());
			
		} finally {
			if(in!=null){
				in.close();
			}
		}
	}
	
	/**
	 * FuzzyXML��XPath#getValue�����b�v���܂��BFuzzyXML��XPath#getValue���\�b�h�Ƃ͈ȉ��̓_���قȂ�܂��B
	 * 
	 * <ul>
	 *   <li>��O��������null��Ԃ��܂��B</li>
	 *   <li>��������g�������ĕԂ��܂��B</li>
	 * </ul>
	 * 
	 * @param element ���[�g�Ƃ���FuzzyXML�̃G�������g�I�u�W�F�N�g
	 * @param xpath XPath
	 * @return XPath�̌��ʒl
	 */
	public static String getXPathValue(FuzzyXMLElement element,String xpath){
		try {
			String value = (String)XPath.getValue(element,xpath);
			return value.trim();
		} catch(Exception ex){
			return null;
		}
	}
	
	/**
	 * FuzzyXML��XPath#selectSingleNode�����b�v���܂��B��O�������ɂ�throw�����Anull��ԋp���܂��B
	 * 
	 * @param element ���[�g�Ƃ���FuzzyXML�̃G�������g�I�u�W�F�N�g
	 * @param xpath XPath
	 * @return XPath�őI�����ꂽ�m�[�h
	 */
	public static FuzzyXMLNode selectXPathNode(FuzzyXMLElement element,String xpath){
		try {
			return XPath.selectSingleNode(element,xpath);
		} catch(Exception ex){
			return null;
		}
	}
	
	/**
	 * �����񃊃e�������f�R�[�h���܂��B
	 * 
	 * @param value �����񃊃e����
	 * @return �f�R�[�h��̕�����
	 */
	public static String decodeString(String value){
		value = value.replaceAll("(^\"|\"$)","");
		value = value.replaceAll("\\\"","\"");
		return value;
	}
	
	/**
	 * �����œn���������񂪕����񃊃e�������ǂ������`�F�b�N���܂��B
	 * �����񂪃_�u���N�H�[�g�ŊJ�n���A�_�u���N�H�[�g�ŏI�����Ă���ꍇ��true��ԋp���܂��B
	 * 
	 * @param value �`�F�b�N�Ώۂ̕�����
	 * @return �����񃊃e�����̏ꍇtrue�A�����łȂ��ꍇfalse
	 */
	public static boolean isString(String value){
		if(value==null){
			return false;
		}
		if(value.startsWith("\"") && value.endsWith("\"")){
			return true;
		}
		return false;
	}
	
	/**
	 * Eclipse��IFile�I�u�W�F�N�g����java.io.File�I�u�W�F�N�g���擾���܂��B
	 * 
	 * @param file IFile�I�u�W�F�N�g
	 * @return java.io.File�I�u�W�F�N�g
	 */
	public static File getFile(IFile file){
		return file.getLocation().makeAbsolute().toFile();
	}
	
	/**
	 * �e���v���[�g�ƃp�����[�^���烁�b�Z�[�W���쐬���܂��B
	 * ���b�Z�[�W�Ɋ܂܂��{0}{1}�c���p�����[�^�Œu�������������ԋp���܂��B
	 * 
	 * @param message ���b�Z�[�W
	 * @param params  �p�����[�^
	 * @return �쐬���ꂽ���b�Z�[�W
	 */
	public static String createMessage(String message,String[] params){
		for(int i=0;i<params.length;i++){
			message = message.replaceAll("\\{"+i+"\\}",params[i]);
		}
		return message;
	}
	
	/**
	 * FuzzyXMLElement�I�u�W�F�N�g����v���t�B�b�N�X��URI���擾���܂��B
	 * 
	 * @param element FuzzyXMLElement�I�u�W�F�N�g
	 * @param prefix �v���t�B�b�N�X
	 * @return �v���t�B�b�N�X�ɑΉ�����URI�B������Ȃ��ꍇ��null��Ԃ��܂��B
	 */
	public static String getPrefixURI(FuzzyXMLElement element,String prefix){
		try {
			FuzzyXMLAttribute[] attrs = element.getAttributes();
			for(int i=0;i<attrs.length;i++){
				if(attrs[i].getName().startsWith("xmlns:" + prefix)){
					return attrs[i].getValue();
				}
			}
			FuzzyXMLElement parent = (FuzzyXMLElement)element.getParentNode();
			if(parent!=null){
				return getPrefixURI(parent,prefix);
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
		return null;
	}
	
	/**
	 * HTML����Maya�̃v���t�B�b�N�X���擾���܂��B
	 * 
	 * @param element FuzzyXMLElement�I�u�W�F�N�g
	 * @return Maya�̃v���t�B�b�N�X�B������Ȃ������ꍇ��null��Ԃ��܂��B
	 */
	public static String getMayaPrefix(FuzzyXMLElement element){
		try {
			FuzzyXMLAttribute[] attrs = element.getAttributes();
			for(int i=0;i<attrs.length;i++){
				if(attrs[i].getName().startsWith("xmlns:")){
					if(attrs[i].getValue().equals(S2JSFPlugin.MAYA_URI)){
						String[] dim = attrs[i].getName().split(":");
						if(dim.length > 1){
							return dim[1];
						}
					}
				}
			}
			FuzzyXMLElement parent = (FuzzyXMLElement)element.getParentNode();
			if(parent!=null){
				return getMayaPrefix(parent);
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
		return null;
	}
	
	/**
	 * CoreException�𓊂��邽�߂̃��[�e�B���e�B���\�b�h
	 * 
	 * @param message ���b�Z�[�W
	 * @throws CoreException �����̃��b�Z�[�W���g�p���Đ������ꂽCoreException�B
	 */
	public static void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR,
				S2JSFPlugin.S2JSF_PLUGIN_ID,
				IStatus.OK,
				message, null);
		throw new CoreException(status);
	}
	
	/**
	 * ��O���O���o�͂��܂��B
	 * 
	 * @param ex ���O�ɏo�͂����O
	 */
	public static void logException(Exception ex){
		IStatus status = null;
		if(ex instanceof CoreException){
			status = ((CoreException)ex).getStatus();
		} else {
			status = new Status(IStatus.ERROR,S2JSFPlugin.S2JSF_PLUGIN_ID,0,ex.toString(),ex);
		}
		S2JSFPlugin.getDefault().getLog().log(status);
		// TODO �f�o�b�O�o��
		ex.printStackTrace();
	}
	
	////////////////////////////////////////////////////////////////////////
	// �}�[�J�[�֌W�̃��[�e�B���e�B���\�b�h
	////////////////////////////////////////////////////////////////////////
	
	/**
	 * ���\�[�X�̑S�Ẵ}�[�J���폜���܂��B
	 * 
	 * @param resouce ���\�[�X
	 */
	public static void removeMakers(IResource resouce) throws CoreException {
		resouce.deleteMarkers(IMarker.PROBLEM,false,0);
	}
	
	/**
	 * ���\�[�X�ɃG���[�}�[�J���쐬���܂��B
	 * 
	 * @param resource ���\�[�Xe
	 * @param start    �J�n�I�t�Z�b�g
	 * @param end      �I���I�t�Z�b�g
	 * @param line     �s�ԍ�
	 * @param message  ���b�Z�[�W
	 */
	public static void createErrorMarker(IResource resource,int start,int end,int line,String message) throws CoreException {
		IMarker marker = resource.createMarker(IMarker.PROBLEM);
		Map map = new HashMap();
		map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
		map.put(IMarker.MESSAGE, message);
		map.put(IMarker.LINE_NUMBER,new Integer(line));
		map.put(IMarker.CHAR_START,new Integer(start));
		map.put(IMarker.CHAR_END,new Integer(end));
		marker.setAttributes(map);
	}
	
	/**
	 * ���\�[�X�Ɍx���}�[�J���쐬���܂��B
	 * 
	 * @param resource ���\�[�Xe
	 * @param start    �J�n�I�t�Z�b�g
	 * @param end      �I���I�t�Z�b�g
	 * @param line     �s�ԍ�
	 * @param message  ���b�Z�[�W
	 */
	public static void createWarnMarker(IResource resource,int start,int end,int line,String message) throws CoreException {
		IMarker marker = resource.createMarker(IMarker.PROBLEM);
		Map map = new HashMap();
		map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_WARNING));
		map.put(IMarker.MESSAGE, message);
		map.put(IMarker.LINE_NUMBER,new Integer(line));
		map.put(IMarker.CHAR_START,new Integer(start));
		map.put(IMarker.CHAR_END,new Integer(end));
		marker.setAttributes(map);
	}
	
	////////////////////////////////////////////////////////////////////////
	// �⊮�֌W�̃��[�e�B���e�B���\�b�h
	////////////////////////////////////////////////////////////////////////
	
	/**
	 * �L�����b�g�ʒu�̒P��ȂǁA�⊮�ɕK�v�ȏ����擾���܂��B
	 * 
	 * @param text �e�L�X�g
	 * @param offset �L�����b�g�̃I�t�Z�b�g
	 * @return
	 * <ul>
	 *   <li>0 - �J�[�\���ʒu�̒��߂̒P��i�^�O�̏ꍇ��&lt;���܂ށj</li>
	 *   <li>1 - �����⊮�̃^�[�Q�b�g�i&lt;���܂܂Ȃ��^�O���̂݁j</li>
	 *   <li>2 - ���^�O�⊮�̃^�[�Q�b�g�i&lt;���܂܂Ȃ��^�O���̂݁j</li>
	 *   <li>3 - ���O�̑�����</li>
	 * </ul>
	 */
	public static String[] getWordsForCompletion(String text,int offset) {
		text = text.substring(0,offset);
		
		StringBuffer sb = new StringBuffer();
		Stack  stack   = new Stack();
		String word    = "";
		String prevTag = "";
		String lastTag = "";
		String attr    = "";
		String temp1   = ""; // �e���|����
		String temp2   = ""; // �e���|����
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			// XML�錾�͔�΂�
			if(c=='<' && text.length()>i+1 && text.charAt(i+1)=='?'){
				i = text.indexOf("?>",i+2);
				if(i==-1){
					i = text.length();
				}
				continue;
			}
			if (isDelimiter(c)) {
				temp1 = sb.toString();
				// �����l�̃p�[�X���̋󔒕�����͋�؂�Ƃ݂Ȃ��Ȃ�
				if(temp1.length()>1 && 
						((temp1.startsWith("\"") && !temp1.endsWith("\"") && c!='"') || 
								(temp1.startsWith("'") && !temp1.endsWith("'") && c!='\''))){
					sb.append(c);
					continue;
				}
				
				if(!temp1.equals("")){
					temp2 = temp1;
					if(temp2.endsWith("=") && !prevTag.equals("") && !temp2.equals("=")){
						attr = temp2.substring(0,temp2.length()-1);
					}
				}
				if(temp1.startsWith("<") && !temp1.startsWith("</") && !temp1.startsWith("<!")){
					prevTag = temp1.substring(1);
					if(!temp1.endsWith("/")){
						stack.push(prevTag);
					}
				} else if(temp1.startsWith("</") && stack.size()!=0){
					stack.pop();
				} else if(temp1.endsWith("/") && stack.size()!=0){
					stack.pop();
				}
				sb.setLength(0);
				
				if(c=='<'){
					sb.append(c);
				} else if(c=='"' || c=='\''){
					if(temp1.startsWith("\"") || temp1.startsWith("'")){
						sb.append(temp1);
					}
					sb.append(c);
				} else if(c=='>'){
					prevTag = "";
					attr    = "";
				}
			} else {
				if(c=='=' && !prevTag.equals("")){
					attr = temp2;
				}
				temp1 = sb.toString();
				if(temp1.length()>1 &&
						(temp1.startsWith("\"") && temp1.endsWith("\"")) || 
						(temp1.startsWith("'") && temp1.endsWith("'"))){
					sb.setLength(0);
				}
				sb.append(c);
			}
		}
		
		if(stack.size()!=0){
			lastTag = (String)stack.pop();
		}
		
		word = sb.toString();
		
		return new String[]{word,prevTag,lastTag,attr};
	}
	
	/**
	 * �������P��̋�؂蕶���ł��邩�ǂ����𔻒肵�܂��B
	 * <ul>
	 *   <li>���p�X�y�[�X, �^�u, ���s(\r��������\n)</li>
	 *   <li>�_�u���N�I�[�g,  �V���O���N�H�[�g, �J���}, �h�b�g, �Z�~�R����</li>
	 *   <li>(, ), [, ], &lt;, &gt;, +, *</li>
	 * </ul>
	 * 
	 * @param c ����
	 * @return ��؂蕶���̏ꍇtrue�A��؂蕶���ł͂Ȃ��ꍇfalse
	 */
	private static boolean isDelimiter(char c) {
		if (c == ' ' || c == '(' || c == ')' || c == ',' //|| c == '.' 
		 || c == ';' || c == '\n' || c == '\r' || c == '\t' || c == '+'
		 || c == '>' || c == '<' || c == '*' || c == '^' //|| c == '{'
			//|| c == '}' 
		 || c == '[' || c == ']' || c == '"' || c == '\'') {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * �v���p�e�B�o�C���f�B���O����EL��]�����ĕ⊮�Ώۂ�ManagedBean���擾���܂��B
	 * 
	 * @param dim EL���f���~�^�ŕ����������́B
	 * @param project S2JSFProject�I�u�W�F�N�g�B
	 * @param initBean
	 * @return EL��]���������ʂƂ��ē���ꂽ�}�l�[�W�h�E�r�[��
	 */
	public static ManagedBean evalPropertyBinding(String[] dim,S2JSFProject project,ManagedBean initBean){
		ManagedBean result = getFirstBean(dim[0],project,initBean);
		if(result==null){
			return null;
		}
		try {
			for(int i=1;i<dim.length-1;i++){
				ManagedBeanProperty[] props = result.getProperties();
				boolean flag = false;
				for(int j=0;j<props.length;j++){
					if(props[j].getPropertyName().equals(dim[i])){
						result = props[j].toManagedBean();
						flag = true;
						break;
					}
				}
				if(flag==false){
					return null;
				}
			}
		} catch(Exception ex){
		}
		return result;
	}
	
	/**
	 * �}�l�[�W�h�E�r�[���̖��O������ۂ̃}�l�[�W�h�E�r�[�����������܂��B
	 * <p>
	 * �ȉ��̏��ŉ������s���܂��B
	 * </p>
	 * <ol>
	 *   <li>beanName�Ŏw�肵���}�l�[�W�h�E�r�[�����R���e�i�ɓo�^����Ă���ꍇ�A���̃r�[����ԋp�B</li>
	 *   <li>initBean�Ŏw�肵���r�[����beanName�Ŏw�肵�����̂̃v���p�e�B�����ꍇ�A���̃v���p�e�B�̖߂�l��ԋp�B</li>
	 *   <li>��L�̂�����ɂ��Y�����Ȃ��ꍇ�Anull��ԋp</li>
	 * </ol>
	 * 
	 * @param beanName �}�l�[�W�h�E�r�[���̖��O
	 * @param project S2JSFProject�I�u�W�F�N�g
	 * @param initBean �������A�N�V�����ŌĂяo�����r�[���i���݂��Ȃ��ꍇ��null���w��j
	 * @return ������̃}�l�[�W�h�E�r�[��
	 */
	private static ManagedBean getFirstBean(String beanName,S2JSFProject project,ManagedBean initBean){
		ManagedBean result = null;
		ManagedBean[] beans = project.getManagedBeans();
		for(int i=0;i<beans.length;i++){
			if(beans[i].getBeanName().equalsIgnoreCase(beanName)){
				result = beans[i];
				break;
			}
		}
		if(result==null && initBean!=null){
			ManagedBeanProperty[] initProps = initBean.getProperties();
			for(int i=0;i<initProps.length;i++){
				if(initProps[i].getPropertyName().equalsIgnoreCase(beanName)){
					result = initProps[i].toManagedBean();
				}
			}
		}
		return result;
	}
	
	/**
	 * �}�l�[�W�h�E�r�[���̋L�q��z��ɕ������܂��B
	 * 
	 * @param el �}�l�[�W�h�E�r�[���̋L�q���܂�EL
	 * @return EL���f���~�^�ŕ��������z��
	 */
	public static String[] splitManagedBean(String el){
		String[] dim = el.trim().split("\\.");
		if(!el.endsWith(".")){
			return dim;
		}
		ArrayList list = new ArrayList();
		for(int i=0;i<dim.length;i++){
			list.add(dim[i]);
		}
		list.add("");
		return (String[])list.toArray(new String[list.size()]);
	}
	
	/**
	 * �}�l�[�W�h�E�r�[���𕪊������z����������܂��B
	 * 
	 * @param dim EL���f���~�^�ŕ��������z��
	 * @return �����̔z����f���~�^�Ō�������������
	 */
	public static String joinManagedBean(String[] dim){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<dim.length-1;i++){
			if(i!=0){
				sb.append('.');
			}
			sb.append(dim[i]);
		}
		return sb.toString();
	}
	
	/**
	 * �^���v���~�e�B�u�^���ǂ����𔻒肵�܂��B
	 * 
	 * @param type �^
	 * @return �v���~�e�B�u�^�̏ꍇtrue�A�����łȂ��ꍇfalse
	 */
	public static boolean isPrimitive(String type){
		// ���̕����̓��[�e�B���e�B�Ƃ��Đ؂�o�����ق�����������
		if(type.equals("int") || type.equals("long") || type.equals("double") || type.equals("float") || 
				type.equals("char") || type.equals("boolean") || type.equals("byte")){
			return true;
		}
		return false;
	}
	
	/**
	 * �p�b�P�[�W���Ȃ��̃N���X������p�b�P�[�W�t���̃t���N���X�����쐬���܂��B
	 * 
	 * @param parent ���̕ϐ����g���Ă���N���X�̌^
	 * @param type   �p�b�P�[�W�Ȃ��̃N���X��
	 * @return �p�b�P�[�W�t���̃N���X��
	 */
	public static String getFullQName(IType parent,String type){
		if(type.indexOf('.') >= 0){
			return type;
		}
		if(isPrimitive(type)){
			return type;
		}
		IJavaProject project = parent.getJavaProject();
		try {
			IType javaType = project.findType("java.lang." + type);
			if(javaType!=null && javaType.exists()){
				return javaType.getFullyQualifiedName();
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		try {
			IType javaType = project.findType(parent.getPackageFragment().getElementName() + "." + type);
			if(javaType!=null && javaType.exists()){
				return javaType.getFullyQualifiedName();
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		try {
			IImportDeclaration[] imports = parent.getCompilationUnit().getImports();
			for(int i=0;i<imports.length;i++){
				String importName = imports[i].getElementName();
				if(importName.endsWith("." + type)){
					return importName;
				}
				if(importName.endsWith(".*")){
					try {
						IType javaType = project.findType(importName.replaceFirst("\\*$",type));
						if(javaType!=null && javaType.exists()){
							return javaType.getFullyQualifiedName();
						}
					} catch(Exception ex){
					}
				}
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return type;
	}
	
	/**
	 * HTML�^�O��m:action�ŌĂяo����鏉�����r�[�����擾���܂��B
	 * 
	 * @param project S2JSFProject�I�u�W�F�N�g�B
	 * @param root HTML�t�@�C���̃��[�g�v�f�B
	 * @return HTML�^�O��m:action�ŌĂяo����鏉�����r�[���B���݂��Ȃ��ꍇ��null��Ԃ��܂��B
	 */
	public static ManagedBean getInitBean(S2JSFProject project,FuzzyXMLElement root){
		if(root==null){
			return null;
		}
		String mayaPrefix = Util.getMayaPrefix(root);
		if(mayaPrefix!=null){
			String invoke = Util.getXPathValue(root, "@" + mayaPrefix + ":action");
			if(invoke!=null){
				if(invoke.startsWith("#{") && invoke.endsWith("}")){
					invoke = invoke.replaceFirst("^#\\{","");
					invoke = invoke.replaceFirst("\\}$" ,"");
					String[] dim = invoke.split("\\.");
					if(dim.length < 2){
						// TODO �G���[
					}
					return project.getManagedBean(dim[0]);
				} else {
					// TODO �G���[
				}
			}
		}
		return null;
	}
}
