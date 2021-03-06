/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opaleye.snackntm;

import java.io.Serializable;

/**
 * Title : AlignedPoint
 * A container class for a column of alignment (reference symbol, fwd/rev trace symbol, indicies, etc.)
 * @author Young-gon Kim
 * 2018.5.
 */
public class AlignedPoint implements Serializable {
	
	private char refChar, fwdChar, revChar, consensusChar;
	private boolean coding;
	//private boolean exon;
	//private String stringCIndex;
	private int gIndex;
	private int fwdQuality, revQuality;
	private int fwdTraceIndex, revTraceIndex;

	/**
	 * A constructor 
	 * @param refChar : reference character
	 * @param fwdChar : forward trace character
	 * @param revChar : reverse trace character
	 * @param discrepency : if discrepency exits : '*', poor quality : '+'
	 * @param gIndex : genomic DNA index
	 * @param fwdTraceIndex : forward trace index
	 * @param revTraceIndex : reverse trace index
	 */
	public AlignedPoint(char refChar, char fwdChar, char revChar, char consensusChar, int gIndex, int fwdTraceIndex, int revTraceIndex) {
		super();
		this.refChar = refChar;
		this.fwdChar = fwdChar;
		this.revChar = revChar;
		this.consensusChar = consensusChar;
		this.gIndex = gIndex;
		this.fwdTraceIndex = fwdTraceIndex;
		this.revTraceIndex = revTraceIndex;
	}

	/**
	 * Getters and setters for member variables
	 */
	public int getGIndex() {
		return gIndex;
	}
	public void setGIndex(int gIndex) {
		this.gIndex = gIndex;
	}
	
	/*
	public boolean isExon() {
		return exon;
	}
	public void setExon(boolean exon) {
		this.exon = exon;
	}
	*/
	public int getFwdQuality() {
		return fwdQuality;
	}
	public void setFwdQuality(int fwdQuality) {
		this.fwdQuality = fwdQuality;
	}
	public int getRevQuality() {
		return revQuality;
	}
	public void setRevQuality(int revQuality) {
		this.revQuality = revQuality;
	}
	public char getRefChar() {
		return refChar;
	}
	public void setRefChar(char refChar) {
		this.refChar = refChar;
	}
	public char getFwdChar() {
		return fwdChar;
	}
	public void setFwdChar(char fwdChar) {
		this.fwdChar = fwdChar;
	}
	public char getRevChar() {
		return revChar;
	}
	public void setRevChar(char revChar) {
		this.revChar = revChar;
	}
	
	public char getConsensusChar() {
		return consensusChar;
	}

	public void setConsensusChar(char consensusChar) {
		this.consensusChar = consensusChar;
	}

	public boolean isCoding() {
		return coding;
	}
	public void setCoding(boolean coding) {
		this.coding = coding;
	}
	public int getFwdTraceIndex() {
		return fwdTraceIndex;
	}
	public void setFwdTraceIndex(int fwdTraceIndex) {
		this.fwdTraceIndex = fwdTraceIndex;
	}
	public int getRevTraceIndex() {
		return revTraceIndex;
	}
	public void setRevTraceIndex(int revTraceIndex) {
		this.revTraceIndex = revTraceIndex;
	}
}
