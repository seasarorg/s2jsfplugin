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

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * a�^�O��href������img�^�O��src�����Ȃǂ̂悤�ɁA
 * �T�[�o��̕ʃt�@�C�����w�肷�鑮���l���A�V�X�g���邽�߂̏����Ǘ�����N���X�ł��B
 * 
 * @author Naoki Takezoe
 */
public class FileAssistProcessor {
	
	private IFile file;
	private String filter;
	
	public void reload(IFile file){
		this.file = file;
	}
	
	/**
	 * �⊮���Ƃ��ċ�����t�@�C�����̊g���q���w�肵�܂��B
	 * �����̊g���q���J���}�ŋ�؂��Ďw�肷�邱�Ƃ��\�ł��B
	 * <pre>
	 * FileAssistProcessor processor = new FileAssistProcessor();
	 * prpcessor.setFilter("html,htm");
	 * </pre>
	 * �Ȃ��A�t�B���^�𖳌��ɂ���ꍇ��null���Z�b�g���܂��B
	 * 
	 * @param filter ������t�@�C���̊g���q
	 */
	public void setFilter(String filter){
		this.filter = filter;
	}
	
	public AssistInfo[] getAssistInfo(String value,boolean absolute){
		
		IPath  path   = null;
		String parent = null;
		
		// �X���b�V���ł͂��܂��Ă���ꍇ�͕⊮���Ȃ�
		if(absolute==false && value.startsWith("/")){
			return new AssistInfo[0];
		}
		if(absolute){
			path = file.getProject().getProjectRelativePath();
			try {
				S2JSFProjectParams params = new S2JSFProjectParams(file.getProject());
				path = path.append(params.getRoot());
			} catch(Exception ex){
				Util.logException(ex);
			}
		}
		// �X���b�V���ȊO�ł͂��܂��Ă���ꍇ�̓t�@�C������̑��΃p�X
		if(path==null){
			path = file.getParent().getProjectRelativePath();
		}
		
		// �m��ς̃p�X���擾����
		int index = value.lastIndexOf('/');
		if(index >= 0){
			path   = path.append(value.substring(0,index));
			parent = value.substring(0,index) + "/";
		} else if(absolute){
			parent = "/";
		} else {
			parent = "";
		}
		
		IResource resource = file.getProject().findMember(path);
		
		if(resource!=null && resource.exists() && resource instanceof IContainer){
			try {
				IContainer container = (IContainer)resource;
				IResource[] children = container.members();
				ArrayList list = new ArrayList();
				for(int i=0;i<children.length;i++){
					// �h�b�g�t�@�C���͂̂���
					if(children[i].getName().startsWith(".")){
						continue;
					}
					// WEB-INF�z���͂̂���
					if(!absolute){
						if(children[i].getName().equals("WEB-INF") || parent.indexOf("WEB-INF")>=0){
							continue;
						}
					}
					Image image = null;
					if(children[i] instanceof IContainer){
						image = S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_FOLDER);
					} else {
						if(!doFilter(children[i])){
							continue;
						}
						image = getFileImage(children[i].getName().toLowerCase());
					}
					
					list.add(new AssistInfo(
							parent + children[i].getName(),
							children[i].getName(), image
					));
				}
				return (AssistInfo[])list.toArray(new AssistInfo[list.size()]);
			} catch(Exception ex){
				Util.logException(ex);
			}
		}
		return new AssistInfo[0];
	}
	
	/**
	 * �t�@�C�����̃t�B���^�������s���܂��B
	 * <p>
	 * ������ꍇtrue�A�����Ȃ��ꍇfalse��Ԃ��܂��B
	 * �t�B���^���ݒ肳��Ă��Ȃ��ꍇ�A���true��Ԃ��܂��B
	 * 
	 * @param resource �`�F�b�N���郊�\�[�X
	 * @return ������ꍇtrue�A�����Ȃ��ꍇfalse
	 */
	private boolean doFilter(IResource resource){
		if(filter==null){
			return true;
		}
		String[] dim = filter.split(",");
		String name = resource.getName();
		for(int i=0;i<dim.length;i++){
			if(name.endsWith("." + dim[i].trim())){
				return true;
			}
		}
		return false;
	}
	
	private Image getFileImage(String name){
//		if(name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".shtml")){
//			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_HTML);
//		}
//		if(name.endsWith(".xml") || name.endsWith(".xhtml") || name.equals(".tld")){
//			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_XML);
//		}
//		if(name.endsWith(".jsp")){
//			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_JSP);
//		}
//		if(name.endsWith(".css")){
//			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_CSS);
//		}
//		for(int i=0;i<S2JSFPlugin.SUPPORTED_IMAGE_TYPES.length;i++){
//			if(name.endsWith("." + S2JSFPlugin.SUPPORTED_IMAGE_TYPES[i])){
//				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_WEB);
//			}
//		}
		return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_FILE);
	}
}
