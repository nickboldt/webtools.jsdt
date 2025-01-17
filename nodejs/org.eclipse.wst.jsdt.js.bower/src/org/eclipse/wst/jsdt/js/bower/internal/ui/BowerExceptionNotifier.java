/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.wst.jsdt.js.bower.internal.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.wst.jsdt.js.bower.internal.Messages;
import org.eclipse.wst.jsdt.js.bower.internal.preference.BowerPreferencePage;

/**
 * @author "Ilya Buziuk (ibuziuk)"
 */
public class BowerExceptionNotifier {

	public static void bowerLocationNotDefined() {
		boolean define = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
				Messages.BowerErrorHandler_BowerNotDefinedTitle, Messages.BowerErrorHandler_BowerNotDefinedMessage);
		if (define) {
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(),
					BowerPreferencePage.PAGE_ID, null, null);
			dialog.open();
		}
	}

}
