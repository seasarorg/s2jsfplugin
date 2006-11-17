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
package org.seasar.s2jsfplugin.model.autoregister;

import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.eclipse.jdt.core.IJavaProject;
import org.seasar.framework.container.autoregister.AutoNaming;
import org.seasar.s2jsfplugin.model.ManagedBean;


/**
 * S2��AutoRegister���Č����邽�߂̃C���^�[�t�F�[�X�B
 * 
 * @author Naoki Takezoe
 */
public interface IAutoRegister {
	
	/**
	 * IAutoRegister�����������܂��B
	 * @param e AutoRegister��component�v�f�ɊY������FuzzyXMLElement�I�u�W�F�N�g
	 */
	public void init(FuzzyXMLElement e);
	
	/**
	 * ����IAutoRegister�ɂ���ēo�^���ꂽ�}�l�[�W�h�E�r�[�����擾���܂��B
	 * @return �}�l�[�W�h�E�r�[��
	 */
	public ManagedBean[] getRegisteredBeans();
	
	/**
	 * Java�v���W�F�N�g��ݒ肵�܂��B
	 * @param project Java�v���W�F�N�g
	 */
	public void setProject(IJavaProject project);
	
	/**
	 * �����o�^�����s���܂��B
	 * <p>
	 * ���̃��\�b�h���Ăяo�������Ƃ�<code>getRegisteredBeans()</code>
	 * �Ŏ����o�^���ꂽ�}�l�[�W�h�E�r�[�����擾���邱�Ƃ��ł��܂��B
	 */
	public void registerAll();
	
	/**
	 * �o�^����N���X�p�^�[����ǉ����܂��B
	 * 
	 * @param packageName �p�b�P�[�W��
	 * @param shortClassNames �N���X���̃p�^�[��
	 */
	public void addClassPattern(String packageName, String shortClassNames);
	
	/**
	 * ��������N���X�p�^�[����ǉ����܂��B
	 * 
	 * @param packageName �p�b�P�[�W��
	 * @param shortClassNames �N���X���̃p�^�[��
	 */
	public void addIgnoreClassPattern(String packageName, String shortClassNames);
	
	/**
	 * �����o�^���̃R���|�[�l���g���̖����K����ݒ肵�܂��B
	 * 
	 * @param naming �}�l�[�W�h�E�r�[���̎����������s���I�u�W�F�N�g
	 */
	public void setAutoNaming(AutoNaming naming);
	
	/**
	 * �����o�^�����}�l�[�W�h�E�r�[���̃X�R�[�v��ݒ肵�܂��B
	 * 
	 * @param scope request�܂���session
	 */
	public void setScope(String scope);
}
