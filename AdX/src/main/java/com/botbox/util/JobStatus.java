/*
 * @(#)PoolThread.java	Created date: 03-07-03
 * $Revision: 3765 $, $Date: 2008-02-24 11:03:02 -0600 (Sun, 24 Feb 2008) $
 *
 * Copyright (c) 2000 BotBox AB.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * BotBox AB. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * BotBox AB.
 */
package com.botbox.util;

/**
 * 
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 3765 $, $Date: 2008-02-24 11:03:02 -0600 (Sun, 24 Feb
 *          2008) $
 */
public interface JobStatus {

	public String getDescription();

	public void setDescription(String description);

	public void stillAlive();

} // JobStatus
