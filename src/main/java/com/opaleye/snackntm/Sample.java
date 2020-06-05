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

import java.io.File;
import java.io.Serializable;
import java.util.Vector;


/**
 * 멤버변수들 모두 RootController에서 가져온것. Rootcontroller 안에서 자유롭게 access하는 식으로 코딩되어있음.
 * 에러 확률줄이기 위해 멤버변수 public으로 하고 getter, setter 사용안함.   
 */
public class Sample implements Comparable, Serializable {
	
	public int compareTo(Object o) {
		String s1 = this.sampleId;
		String s2 = ((Sample)o).sampleId;
		return s1.compareTo(s2);
	}
	
	public String sampleId = "";
	

	//context-specific variables
	public Vector<NTMSpecies>[] speciesList = new Vector[3];
	public Vector<NTMSpecies>[] selectedSpeciesList = new Vector[3];
	public Vector<NTMSpecies> finalList = null;
	public Vector<AlignedPoint>[] alignedPoints = new Vector[3];
	public File[] fwdTraceFile = new File[3], revTraceFile = new File[3];
	public GanseqTrace[] trimmedFwdTrace = new GanseqTrace[3], trimmedRevTrace = new GanseqTrace[3];
	public boolean fwdLoaded[] = {false, false, false}, revLoaded[] = {false, false, false};

	public boolean split[] = {false, false, false};
	public boolean fwdNotUsed[] = {false, false, false};
	public boolean revNotUsed[] = {false, false, false};
	
	public boolean alignmentPerformed[] = new boolean[3];
	
	public String[] fwdTraceFileName = new String[3];
	public String[] revTraceFileName = new String[3];
	//edit base 용
	public int[] selectedAlignmentPos = {-1, -1, -1};
	public String[] consensusSequence= new String[3];

	public Formatter[] formatter = new Formatter[3];
	
	public String consensusSeq[] = new String[3];

	
	public boolean containsIcSeq = false;
	public boolean containsChSeq = false;
	
	
	
	
	public Sample(String sampleId) {
		this.sampleId = sampleId;
	}
	
}
