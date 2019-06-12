package com.opaleye.ganseq;

import com.opaleye.ganseq.reference.ReferenceSeq;

import javafx.beans.property.SimpleStringProperty;

public class NTMSpecies implements Comparable<NTMSpecies> {
	private String accession = "";
	private String speciesName = "";
	private ReferenceSeq refSeq = null;
	private double score = 0;
	private int qlen = 0;

	protected SimpleStringProperty accessionProperty;
	protected SimpleStringProperty speciesNameProperty;
	protected SimpleStringProperty scoreProperty;
	protected SimpleStringProperty qlenProperty;

	@Override
	public int compareTo(NTMSpecies s) {
		if (this.score < s.getScore()) 
			return 1;
		else if (this.score > s.getScore()) 
			return -1;
		else return s.getQlen()-this.getQlen();		
	}
	

	public NTMSpecies(String inputString) {
		boolean firstLine = true;
		String s_firstLine = "";
		String ATGC = "ATGCatgc";
		StringBuffer refSeqBuffer = new StringBuffer();
		String refSeqString = null;
		for(int i=0;i<inputString.length();i++) {
			char thisChar = inputString.charAt(i);

			if(firstLine) {
				if(thisChar == '\n') {
					firstLine = false;
					String[] tokens = s_firstLine.split("\\|");
					accession = tokens[3];
					speciesName = tokens[4];
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
		refSeqString = refSeqBuffer.toString();
		refSeq = new ReferenceSeq(refSeqString);

		//System.out.println(String.format("%s, %s\n%s\n\n\n\n", accession, speciesName, refSeq.getRefString()));
		accessionProperty= new SimpleStringProperty(accession);
		speciesNameProperty= new SimpleStringProperty(speciesName);

	}
	
	public NTMSpecies(String speciesName, String score) {
		speciesNameProperty= new SimpleStringProperty(speciesName);
		scoreProperty = new SimpleStringProperty(score);
	}


	public ReferenceSeq getRefSeq() {
		return refSeq;
	}

	public void setRefSeq(ReferenceSeq refSeq) {
		this.refSeq = refSeq;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}



	public double getScore() {
		return score;
	}

	public int getQlen() {
		return qlen;
	}

	public void setQlen(int qlen) {
		this.qlen = qlen;
		qlenProperty =  new SimpleStringProperty(String.format("%d",  qlen));
	}

	public void setScore(double score) {
		this.score = score;
		scoreProperty = new SimpleStringProperty(String.format("%.2f",  score));
	}

	public String getSpeciesNameProperty() {
		return speciesNameProperty.get();
	}

	public String getAccessionProperty() {
		return accessionProperty.get();
	}

	public String getScoreProperty() {
		return scoreProperty.get();
	}

	public String getQlenProperty() {
		return qlenProperty.get();
	}

}
