/*
 * $Id: CMSTest.java,v 1.1 2004/10/26 12:45:00 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;


/**
 * Content management system test/demo. 
 * <p>
 * Last modified: $Date: 2004/10/26 12:45:00 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.1 $
 */
public class CMSTest {

	/**
	 * Returns test/demo page. 
	 */
	public UIComponent createContent() {
		return new CMSPage();
	}	
}
