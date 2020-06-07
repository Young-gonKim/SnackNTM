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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;

public class NTMSpecies implements Comparable<NTMSpecies>, Cloneable, Serializable {
	private String strain = "";
	private String speciesName = "";
	private double score = 0;
	private int qlen = 0;
	private int alen = 0;
	private boolean rgm = false;
	private String refSeq = null;

	protected transient SimpleStringProperty strainProperty;
	protected transient SimpleStringProperty speciesNameProperty;
	protected transient SimpleStringProperty scoreProperty;
	protected transient SimpleStringProperty qlenProperty;
	protected transient SimpleStringProperty alenProperty;
	protected transient SimpleStringProperty rgmProperty;


	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 

	//removeAll, retainAll 구현용
	@Override
	public boolean equals(Object o) {
		NTMSpecies ntm = (NTMSpecies)o;
		return this.speciesName.equals(ntm.getSpeciesName());
	}

	@Override
	public int compareTo(NTMSpecies ntm) {
		if (this.score < ntm.getScore()) 
			return 1;
		else if (this.score > ntm.getScore()) 
			return -1;
		else return ntm.getAlen()-this.getAlen();		
	}


	public NTMSpecies(String inputString) {
		boolean firstLine = true;
		String s_firstLine = "";
		String ATGC = "ATGCatgc";
		StringBuffer refSeqBuffer = new StringBuffer();
		for(int i=0;i<inputString.length();i++) {
			char thisChar = inputString.charAt(i);

			if(firstLine) {
				if(thisChar == '\n') {
					firstLine = false;
					String[] tokens = s_firstLine.split("\\|");
					strain = tokens[1];
					speciesName = tokens[3];
					speciesName = speciesName.trim();
					speciesName = speciesName.replace('_', ' ');
					//System.out.println(speciesName);
					if(RootController.rgmSet.contains(speciesName)) {
						rgm = true;
					}
				}
				else {
					s_firstLine += thisChar;
				}

			}
			else {
				String s_thisChar = String.format("%c",  thisChar);
				if(ATGC.contains(s_thisChar)) {
					refSeqBuffer.append(thisChar);
				}
			}
		}
		refSeq = refSeqBuffer.toString().toUpperCase();

		//System.out.println(String.format("%s, %s\n%s\n\n\n\n", strain, speciesName, refSeq.getRefString()));
		strainProperty= new SimpleStringProperty(strain);
		speciesNameProperty= new SimpleStringProperty(speciesName);
		if(rgm) 
			rgmProperty = new SimpleStringProperty("O");
		else
			rgmProperty = new SimpleStringProperty("");

	}

	public NTMSpecies(String speciesName, String s_score) {
		this.speciesName = speciesName;
		speciesNameProperty= new SimpleStringProperty(speciesName);
		try {
			this.score = Double.parseDouble(s_score);
		}
		catch (Exception ex) {
			this.score = 0;
		}
		scoreProperty = new SimpleStringProperty(s_score);
	}

	/*
	 * 	
	 private transient String strain = "";
	private transient String speciesName = "";
	private transient double score = 0;
	private transient int qlen = 0;
	private transient int alen = 0;
	private transient boolean rgm = false;
	private transient String refSeq = null;
	 */
	
	
	 private void writeObject(ObjectOutputStream s) throws IOException {
         s.defaultWriteObject();
        
     }
     
     private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
		strainProperty= new SimpleStringProperty(strain);
		speciesNameProperty= new SimpleStringProperty(speciesName);
		if(rgm) 
			rgmProperty = new SimpleStringProperty("O");
		else
			rgmProperty = new SimpleStringProperty("");
		qlenProperty =  new SimpleStringProperty(String.format("%d",  qlen));
		alenProperty =  new SimpleStringProperty(String.format("%d",  alen));
		scoreProperty = new SimpleStringProperty(String.format("%.2f",  score));
     }
	
	
	public String getStrain() {
		return strain;
	}

	public void setStrain(String strain) {
		this.strain = strain;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}



	public String getRefSeq() {
		return refSeq;
	}

	public void setRefSeq(String refSeq) {
		this.refSeq = refSeq;
	}

	public double getScore() {
		return score;
	}

	public int getQlen() {
		return qlen;
	}

	public int getAlen() {
		return alen;
	}

	public void setQlen(int qlen) {
		this.qlen = qlen;
		qlenProperty =  new SimpleStringProperty(String.format("%d",  qlen));
	}

	public void setAlen(int alen) {
		this.alen = alen;
		alenProperty =  new SimpleStringProperty(String.format("%d",  alen));
	}


	public void setScore(double score) {
		this.score = score;
		scoreProperty = new SimpleStringProperty(String.format("%.2f",  score));
	}

	public String getSpeciesNameProperty() {
		return speciesNameProperty.get();
	}

	public String getStrainProperty() {
		return strainProperty.get();
	}

	public String getScoreProperty() {
		return scoreProperty.get();
	}


	public String getQlenProperty() {
		return qlenProperty.get();
	}

	public String getAlenProperty() {
		return alenProperty.get();
	}

	public String getRgmProperty() {
		return rgmProperty.get();
	}

	public boolean isRgm() {
		return rgm;
	}
}
