package com.opaleye.ganseq;

import com.opaleye.ganseq.reference.ReferenceSeq;

import javafx.beans.property.SimpleStringProperty;

public class NTMSpecies implements Comparable<NTMSpecies> {
	private String accession = "";
	private String speciesName = "";
	private ReferenceSeq refSeq = null;
	private double score = 0;
	private int qlen = 0;
	private int alen = 0;
	private boolean rgm = false;

	protected SimpleStringProperty accessionProperty;
	protected SimpleStringProperty speciesNameProperty;
	protected SimpleStringProperty scoreProperty;
	protected SimpleStringProperty qlenProperty;
	protected SimpleStringProperty alenProperty;
	protected SimpleStringProperty rgmProperty;

	
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
		String refSeqString = null;
		for(int i=0;i<inputString.length();i++) {
			char thisChar = inputString.charAt(i);

			if(firstLine) {
				if(thisChar == '\n') {
					firstLine = false;
					String[] tokens = s_firstLine.split("\\|");
					accession = tokens[1];
					speciesName = tokens[3];
					speciesName = speciesName.trim();
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
		refSeqString = refSeqBuffer.toString();
		refSeq = new ReferenceSeq(refSeqString);

		//System.out.println(String.format("%s, %s\n%s\n\n\n\n", accession, speciesName, refSeq.getRefString()));
		accessionProperty= new SimpleStringProperty(accession);
		speciesNameProperty= new SimpleStringProperty(speciesName);
		if(rgm) 
			rgmProperty = new SimpleStringProperty("O");
		else
			rgmProperty = new SimpleStringProperty("");

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

	public String getAccessionProperty() {
		return accessionProperty.get();
	}

	public String getScoreProperty() {
		return scoreProperty.get();
	}
	
	public void setScoreProperty(String score) {
		scoreProperty = new SimpleStringProperty(score);
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
