package com.opaleye.ganseq;

import com.opaleye.ganseq.reference.ReferenceSeq;

public class NTMSpecies implements Comparable<NTMSpecies> {
	private String accession = "";
	private String speciesName = "";
	private ReferenceSeq refSeq = null;
	private double score = 0;

	   @Override
	    public int compareTo(NTMSpecies s) {
	        if (this.score < s.getScore()) {
	            return 1;
	        } else if (this.score > s.getScore()) {
	            return -1;
	        }
	        return 0;
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

	public void setScore(double score) {
		this.score = score;
	}


}
