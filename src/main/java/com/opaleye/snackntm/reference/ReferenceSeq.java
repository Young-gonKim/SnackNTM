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

package com.opaleye.snackntm.reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 * Title : ReferenceFile
 * Reference file manager class
 * The method readGenBankFromFile is derived from BioJAVA Legacy Cookbook (https://biojava.org/wiki/BioJava:Cookbook:SeqIO:ReadGESBiojavax)
 * @author Young-gon Kim
 *2018.5
 */
public class ReferenceSeq {
	private String refString;

	public ReferenceSeq(String refString) {
		this.refString = refString;
	}

	public String getRefString() {
		return refString;
	}
	public void setRefString(String refString) {
		this.refString = refString;
	}


	
}
