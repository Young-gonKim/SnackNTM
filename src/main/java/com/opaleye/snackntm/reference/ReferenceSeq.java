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
