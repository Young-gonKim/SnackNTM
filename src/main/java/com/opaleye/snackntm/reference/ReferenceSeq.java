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
	public static int FASTA=0;		//depricated
	public static int GenBank=1;
	
	//0: FASTA, 1:Genbank          FASTA : depricated
	private int refType = ReferenceSeq.GenBank;
	private String refString;
	private Vector<Integer> cDnaStart, cDnaEnd;
	//private Vector<Integer> exonStart, exonEnd;
	
	
	
	public ReferenceSeq(String refString) {
		//System.out.println("refString : " + refString);
		refType = FASTA;
		cDnaStart = new Vector<Integer>();
		cDnaEnd = new Vector<Integer>();
		cDnaStart.add(1);
		cDnaEnd.add(refString.length());
		this.refString = refString;
	}

	
	/**
	 * Getters and Setters of member variables
	 */
	public int getRefType() {
		return refType;
	}
	public void setRefType(int refType) {
		this.refType = refType;
	}
	public String getRefString() {
		return refString;
	}
	public void setRefString(String refString) {
		this.refString = refString;
	}
	public Vector<Integer> getcDnaStart() {
		return cDnaStart;
	}
	public void setcDnaStart(Vector<Integer> cDnaStart) {
		this.cDnaStart = cDnaStart;
	}
	public Vector<Integer> getcDnaEnd() {
		return cDnaEnd;
	}
	public void setcDnaEnd(Vector<Integer> cDnaEnd) {
		this.cDnaEnd = cDnaEnd;
	}


	
}
