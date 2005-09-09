/*
 * $Id: CMSTest.java,v 1.1 2005/09/09 16:14:05 tryggvil Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.old;

import javax.faces.component.UIComponent;


/**
 * Content management system test/demo. 
 * <p>
 * Last modified: $Date: 2005/09/09 16:14:05 $ by $Author: tryggvil $
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
