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
package org.seasar.s2jsfplugin.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

/**
 * IStorageEditorInput�ȊO�̏ꍇ�Ɏg�p����h�L�������g�v���o�C�_�B
 * 
 * @author Naoki Takezoe
 */
public class HTMLTextDocumentProvider extends TextFileDocumentProvider {
	
	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo info = super.createFileInfo(element);
		if(info==null){
			info = createEmptyFileInfo();
		}
		IDocument document = info.fTextFileBuffer.getDocument();
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new HTMLPartitionScanner(),
					new String[] {
						HTMLPartitionScanner.HTML_TAG,
						HTMLPartitionScanner.HTML_COMMENT,
						HTMLPartitionScanner.HTML_SCRIPT,
						HTMLPartitionScanner.HTML_DOCTYPE,
						HTMLPartitionScanner.JAVASCRIPT,
						HTMLPartitionScanner.HTML_CSS});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return info;
	}

}
