/**
 * Copyright (c) 2001-2008, Swedish Institute of Computer Science
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This file is part of the TAC Supply Chain Management Simulator.
 *
 * $Id: MinAvgZeroScoreMerger.java,v 1.6 2008/01/07 17:47:05 nfi Exp $
 * -----------------------------------------------------------------
 *
 * MinAvgZeroScoreMerger
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Jun 20 14:51:25 2005
 * Updated : $Date: 2008/01/07 17:47:05 $
 *           $Revision: 1.6 $
 */
package se.sics.tasim.is.score;

import java.util.Comparator;
import java.util.logging.Logger;

import se.sics.tasim.is.common.CompetitionParticipant;
import se.sics.tasim.is.common.ScoreMerger;

/**
 */
public class MinAvgZeroScoreMerger extends ScoreMerger {

	private static final Logger log = Logger
			.getLogger(MinAvgZeroScoreMerger.class.getName());

	public MinAvgZeroScoreMerger() {
		setShowingAllAgents(true);
		setShowingZeroGameAgents(true);
		setShowingAverageScoreWithoutZeroGames(true);
		setShowingWeightedAverageScoreWithoutZeroGames(true);
	}

	protected Comparator getComparator(boolean isWeightUsed) {
		return isWeightUsed ? CompetitionParticipant
				.getMinAvgZeroWeightedComparator() : CompetitionParticipant
				.getMinAvgZeroComparator();
	}

	protected void addPostInfo(StringBuffer page) {
		page.append("<em>Agents are ranked by the lowest of average score and "
				+ "average score without zero games.</em><br>");
		super.addPostInfo(page);
	}

} // MinAvgZeroScoreMerger
