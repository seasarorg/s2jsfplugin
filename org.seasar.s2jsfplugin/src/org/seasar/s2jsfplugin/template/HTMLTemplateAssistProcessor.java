package org.seasar.s2jsfplugin.template;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * 
 * @author Naoki Takezoe
 */
public class HTMLTemplateAssistProcessor extends TemplateCompletionProcessor {

	protected Template[] getTemplates(String contextTypeId) {
		HTMLTemplateManager manager = HTMLTemplateManager.getInstance();
		return manager.getTemplateStore().getTemplates();
	}

	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		HTMLTemplateManager manager = HTMLTemplateManager.getInstance();
		return manager.getContextTypeRegistry().getContextType(HTMLContextType.CONTEXT_TYPE);
	}

	protected Image getImage(Template template) {
		return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TEMPLATE);
	}
	
	protected ICompletionProposal[] joinProposals(
			ICompletionProposal[] array1, ICompletionProposal[] array2){
		
		ICompletionProposal[] result = new ICompletionProposal[array1.length + array2.length];
		
		for(int i=0; i < array1.length; i++){
			result[i] = array1[i];
		}
		
		for(int i=0; i < array2.length; i++){
			result[i + array1.length] = array2[i];
		}
		
		return result;
	}
}
