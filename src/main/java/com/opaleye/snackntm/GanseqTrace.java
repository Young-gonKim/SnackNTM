package com.opaleye.snackntm;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.TreeMap;

import org.biojava.bio.program.abi.ABITrace;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SymbolList;

import com.opaleye.snackntm.settings.SettingsController;
import com.opaleye.snackntm.tools.SymbolTools;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class GanseqTrace {

	//현재 : basecall 전후로 10칸씩. 총 21칸을 사용. 
	private static final int LSTMLength = 10;

	public static final int FORWARD = 1;
	public static final int REVERSE = -1;
	public static final int originalTraceHeight = 110;
	private int traceHeight = 0;
	public static final int traceWidth = 2;

	protected int direction = FORWARD;
	protected int[] traceA = null;
	protected int[] traceT = null;
	protected int[] traceG = null;
	protected int[] traceC = null;
	protected int traceLength = 0;
	protected String sequence = null;
	protected int sequenceLength = 0; 
	protected int[] qCalls = null;
	protected int[] baseCalls = null;

	protected int[] transformedA = null;
	protected int[] transformedT = null;
	protected int[] transformedG = null;
	protected int[] transformedC = null;
	protected int maxHeight = -1;
	protected double ratio = 1.0;

	public GanseqTrace() {};
	public GanseqTrace(File ABIFile) throws Exception {

		//function calls from ABITrace
		ABITrace tempTrace = new ABITrace(ABIFile);
		traceA = tempTrace.getTrace(DNATools.a());
		traceT = tempTrace.getTrace(DNATools.t());
		traceG = tempTrace.getTrace(DNATools.g());
		traceC = tempTrace.getTrace(DNATools.c());
		traceLength = Integer.min(Integer.min(traceA.length, traceT.length), 
				Integer.min(traceG.length, traceC.length));
		sequence = tempTrace.getSequence().seqString().toUpperCase();
		sequenceLength = sequence.length();
		qCalls = tempTrace.getQcalls();
		baseCalls = tempTrace.getBasecalls();

		resolveAmbiguousSymbols();

		transformTrace();
	}

	private void resolveAmbiguousSymbols() {

		String ATGC = "ATGC";
		StringBuffer buffer = new StringBuffer();
		for(int i=0;i<sequenceLength;i++) {
			String temp = sequence.substring(i, i+1);
			if(!ATGC.contains(temp)) {
				int[] peakHeights = new int[4];
				peakHeights[0] = traceA[baseCalls[i]];
				peakHeights[1] = traceT[baseCalls[i]];
				peakHeights[2] = traceG[baseCalls[i]];
				peakHeights[3] = traceC[baseCalls[i]];

				int maxJ = 0;
				int maxPeak = -1;
				for(int j=0;j<4;j++) {
					if(peakHeights[j] > maxPeak) {
						maxJ = j;
						maxPeak = peakHeights[j];
					}
				}

				char newBase = 'N';
				switch(maxJ) {
				case 0: newBase = 'A'; break;
				case 1: newBase = 'T'; break;
				case 2: newBase = 'G'; break;
				case 3: newBase = 'C'; break;
				}
				buffer.append(newBase);
			}
			else
				buffer.append(temp);
		}
		
		sequence = buffer.toString();

	}


	public void editBase(int pos, char oldBase, char newBase) {
		if(oldBase == newBase) return;

		pos--; //1부터 시작하는 index이므로

		//base 1개 insertion
		if(oldBase == Formatter.gapChar) {
			sequence = sequence.substring(0,pos) + newBase + sequence.substring(pos, sequence.length());
			sequenceLength++;
			int[] tempQcalls = new int[sequenceLength];
			int[] tempBasecalls = new int[sequenceLength];
			for(int i=0;i<pos;i++) {
				tempQcalls[i] = qCalls[i];
				tempBasecalls[i] = baseCalls[i];
			}
			tempQcalls[pos] = 10;
			//tempBasecalls[pos+1] = Integer.min(baseCalls[pos]+5, traceLength);
			tempBasecalls[pos] = Integer.max(baseCalls[pos]-5, 0);

			for(int i=pos+1;i<sequenceLength;i++) {
				tempQcalls[i] = qCalls[i-1];
				tempBasecalls[i] = baseCalls[i-1];
			}
			qCalls = tempQcalls;
			baseCalls = tempBasecalls;
		}

		else if(newBase == Formatter.gapChar) {
			sequence = sequence.substring(0, pos) + sequence.substring(pos+1, sequence.length());
			sequenceLength--;

			int[] tempQcalls = new int[sequenceLength];
			int[] tempBasecalls = new int[sequenceLength];
			for(int i=0;i<pos;i++) {
				tempQcalls[i] = qCalls[i];
				tempBasecalls[i] = baseCalls[i];
			}

			for(int i=pos+1;i<sequenceLength;i++) {
				tempQcalls[i-1] = qCalls[i];
				tempBasecalls[i-1] = baseCalls[i];
			}
			qCalls = tempQcalls;
			baseCalls = tempBasecalls;

		}

		else
			sequence = sequence.substring(0,pos) + newBase + sequence.substring(pos+1, sequence.length());
	}

	private void transformTrace() {
		maxHeight = -1;
		double imageHeightRatio = 0;
		for(int i=0;i<traceLength;i++) {
			if(traceA[i] > maxHeight) maxHeight = traceA[i];
			if(traceT[i] > maxHeight) maxHeight = traceT[i];
			if(traceG[i] > maxHeight) maxHeight = traceG[i];
			if(traceC[i] > maxHeight) maxHeight = traceC[i];
		}

		transformedA = new int[traceLength];
		transformedT = new int[traceLength];
		transformedG = new int[traceLength];
		transformedC = new int[traceLength];

		traceHeight = (int)(originalTraceHeight * ratio);
		imageHeightRatio = (double)traceHeight / (double)maxHeight;

		//System.out.println("maxHeight : " + maxHeight);
		//System.out.println("Image Height Ratio : " + imageHeightRatio);

		for(int i=0;i<traceLength;i++) {
			//System.out.print("traceA : " + traceA[i]);
			transformedA[i] = new Double((maxHeight-traceA[i]) * imageHeightRatio).intValue();
			//System.out.println("transformed traceA : " + traceA[i]);
			transformedT[i] = new Double((maxHeight-traceT[i]) * imageHeightRatio).intValue();
			transformedG[i] = new Double((maxHeight-traceG[i]) * imageHeightRatio).intValue();
			transformedC[i] = new Double((maxHeight-traceC[i]) * imageHeightRatio).intValue();
		}

	}

	public BufferedImage getDefaultImage() {
		BufferedImage image = new BufferedImage(traceLength*traceWidth, traceHeight+30, BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D g = image.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, traceLength*traceWidth, traceHeight+30);

		for(int i=0;i<traceLength-1;i++) {
			g.setColor(Color.GREEN);
			g.drawLine (i*traceWidth,transformedA[i],(i+1) * traceWidth, transformedA[i+1]);

			g.setColor(Color.RED);
			g.drawLine (i*traceWidth,transformedT[i],(i+1) * traceWidth, transformedT[i+1]);

			g.setColor(Color.BLACK);
			g.drawLine (i*traceWidth,transformedG[i],(i+1) * traceWidth, transformedG[i+1]);

			g.setColor(Color.BLUE);
			g.drawLine (i*traceWidth,transformedC[i],(i+1) * traceWidth, transformedC[i+1]);
		}

		for(int i=0;i<sequenceLength;i++) {
			char baseChar[] = {sequence.charAt(i)};
			int xPos = baseCalls[i]*traceWidth;
			switch(baseChar[0]) {
			case 'A' : 
				g.setColor(Color.GREEN);
				break;
			case 'T' : 
				g.setColor(Color.RED);
				break;
			case 'G' : 
				g.setColor(Color.BLACK);
				break;
			case 'C' : 
				g.setColor(Color.BLUE);
				break;
			default : 
				g.setColor(new Color(255,20,147));;
			}
			g.drawChars(baseChar,  0,  1, Integer.max(0, xPos-3), traceHeight+13);
			g.setColor(Color.BLACK);
			if((i+1)%10 ==1) {
				g.drawLine(traceWidth*baseCalls[i], traceHeight+13, traceWidth*baseCalls[i], traceHeight+18);
				g.drawString(Integer.toString((i+1)), traceWidth*baseCalls[i]-3, traceHeight+30);
			}

		}
		return image;
	}

	/**
	 * Returns a Shaded image
	 * @param startTrimPosition : Left side trim position
	 * @param endTrimPosition : Right side trim position
	 * @author Young-gon Kim
	 */
	public Image getTrimmingImage(int startTrimPosition, int endTrimPosition) {

		BufferedImage originalImage = getDefaultImage();
		Graphics2D g = originalImage.createGraphics();

		g.setColor(Color.BLUE);
		g.setComposite(AlphaComposite.SrcOver.derive(0.2f));
		g.fillRect(0, 0, (startTrimPosition+1), traceHeight);
		g.fillRect(Integer.min(endTrimPosition, traceWidth*traceLength-1), 0, (traceWidth*traceLength-endTrimPosition), traceHeight);

		Image ret = SwingFXUtils.toFXImage(originalImage, null);
		return ret;		
	}

	/**
	 * 
	 * @param startPosition
	 * @param endPosition
	 * @param option  : 0:no shading, 1:point (based on traceBaseNumbering) 2:area (based on newBaseNumbering) 
	 * @return
	 */
	public BufferedImage getShadedImage(int option, int startPosition, int endPosition, Formatter formatter) {

		int newTraceLength = 0;
		int startOffset = 0;

		if(direction == FORWARD) {
			newTraceLength = formatter.fwdNewLength;
			startOffset = formatter.fwdStartOffset;
		}
		else {
			newTraceLength = formatter.revNewLength;
			startOffset = formatter.revStartOffset;
		}

		BufferedImage image = new BufferedImage(newTraceLength, traceHeight+30, BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D g = image.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, newTraceLength, traceHeight+30);


		TreeMap<Integer, Integer> fwdMap = formatter.fwdCoordinateMap;
		TreeMap<Integer, Integer> revMap = formatter.revCoordinateMap;

		for(int i=0;i<traceLength-1;i++) {
			g.setColor(Color.GREEN);
			g.drawLine (startOffset + i*traceWidth,transformedA[i], startOffset +(i+1) * traceWidth, transformedA[i+1]);

			g.setColor(Color.RED);
			g.drawLine (startOffset + i*traceWidth,transformedT[i], startOffset +(i+1) * traceWidth, transformedT[i+1]);

			g.setColor(Color.BLACK);
			g.drawLine (startOffset + i*traceWidth,transformedG[i], startOffset +(i+1) * traceWidth, transformedG[i+1]);

			g.setColor(Color.BLUE);
			g.drawLine (startOffset + i*traceWidth,transformedC[i], startOffset +(i+1) * traceWidth, transformedC[i+1]);
		}

		for(int i=0;i<sequenceLength;i++) {
			//if(i+1<alignedRegionStart || i+1>alignedRegionEnd) continue;

			char baseChar[] = {sequence.charAt(i)};
			int xPos = startOffset + baseCalls[i]*traceWidth;
			switch(baseChar[0]) {
			case 'A' : 
				g.setColor(Color.GREEN);
				break;
			case 'T' : 
				g.setColor(Color.RED);
				break;
			case 'G' : 
				g.setColor(Color.BLACK);
				break;
			case 'C' : 
				g.setColor(Color.BLUE);
				break;
			default : 
				g.setColor(new Color(255,20,147));
			}
			g.drawChars(baseChar,  0,  1, Integer.max(0, xPos-3), traceHeight+13);
			g.setColor(Color.BLACK);

			int mappedNo = 0;
			Integer i_mappedNo = null;
			if(direction == FORWARD) {
				i_mappedNo = fwdMap.get(new Integer(i+1));
			}
			else {
				i_mappedNo = revMap.get(new Integer(i+1));
			}

			if(i_mappedNo !=null) {
				mappedNo = i_mappedNo.intValue();
				if(mappedNo%10 ==1) {
					g.drawLine(startOffset + traceWidth*baseCalls[i], traceHeight+13, startOffset + traceWidth*baseCalls[i], traceHeight+18);
					g.drawString(Integer.toString(mappedNo), startOffset + traceWidth*baseCalls[i]-3, traceHeight+30);
				}
			}
		}


		if(option == 1) {
			g.setColor(Color.BLUE);
			g.setComposite(AlphaComposite.SrcOver.derive(0.2f));
			g.fillRect(startOffset + (baseCalls[startPosition]-5) * traceWidth, 0, 10*traceWidth, traceHeight+30);
			return image;

		}
		else if (option == 2) {
			g.setColor(Color.BLUE);
			g.setComposite(AlphaComposite.SrcOver.derive(0.2f));
			g.fillRect(startOffset + (baseCalls[startPosition]-5) * traceWidth, 0, (baseCalls[endPosition]-baseCalls[startPosition]+10)*traceWidth, traceHeight+30);

			return image;
		}
		
		else if (option == 3) {
			
			g.setColor(Color.BLUE);
			g.setComposite(AlphaComposite.SrcOver.derive(0.2f));
			g.fillRect(startOffset + (baseCalls[startPosition]-8) * traceWidth, 0, 4*traceWidth, traceHeight+30);

			return image;
		}
		
		
		else 
			return image;
	}


	//ruler Image
	public Image getRulerImage() {
		BufferedImage image = new BufferedImage(28, traceHeight, BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D g = image.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, 28, traceHeight);
		g.setColor(Color.BLACK);

		double imageHeightRatio = (double)traceHeight / (double)maxHeight;
		for(int i=1000;i<=maxHeight;i+=1000) {
			int yPos = new Double((maxHeight-i) * imageHeightRatio).intValue();
			g.drawString(Integer.toString(i), 0, yPos);
		}
		return SwingFXUtils.toFXImage(image, null);
	}

	/**
	 * Returns a trimmed trace
	 * @param startTrimPosition : Left side trim position
	 * @param endTrimPosition : Right side trim position
	 * @author Young-gon Kim
	 */
	public void makeTrimmedTrace(int startTrimPosition, int endTrimPosition) throws Exception {
		int[] oldA = traceA;
		int[] oldT = traceT;
		int[] oldG = traceG;
		int[] oldC = traceC;

		String oldSequence = sequence;
		int oldSequenceLength = sequenceLength;
		int[] oldBaseCalls = baseCalls;
		int[] oldQCalls = qCalls;

		StringBuffer buffer = new StringBuffer();
		//System.out.println(String.format("startTrimPosition, endTrimposition : %d, %d", startTrimPosition, endTrimPosition));
		if(startTrimPosition != -1)
			startTrimPosition /= traceWidth;
		endTrimPosition /= traceWidth;
		//System.out.println(String.format("startTrimPosition, endTrimposition : %d, %d", startTrimPosition, endTrimPosition));
		traceLength = endTrimPosition - startTrimPosition - 1; 	//양 끝점이 포함되지 않으므로 -1

		traceA = new int[traceLength];
		traceT = new int[traceLength];
		traceG = new int[traceLength];
		traceC = new int[traceLength];

		for(int i=startTrimPosition+1; i<endTrimPosition; i++) {
			traceA[i-(startTrimPosition+1)] = oldA[i];
			traceT[i-(startTrimPosition+1)] = oldT[i];
			traceG[i-(startTrimPosition+1)] = oldG[i];
			traceC[i-(startTrimPosition+1)] = oldC[i];
		}
		for(int i=0;i<oldSequenceLength;i++) {
			if(oldBaseCalls[i]>startTrimPosition && oldBaseCalls[i] < endTrimPosition) {
				buffer.append(oldSequence.charAt(i));
			}
		}

		sequence = (buffer.toString()).toUpperCase();
		sequenceLength = sequence.length();
		qCalls = new int[sequenceLength];
		baseCalls = new int[sequenceLength];

		int count = 0;
		for(int i=0;i<oldSequenceLength;i++) {
			if(oldBaseCalls[i]>startTrimPosition && oldBaseCalls[i] < endTrimPosition) {
				qCalls[count] = oldQCalls[i];
				baseCalls[count] = oldBaseCalls[i]-(startTrimPosition+1);
				count++;
			}
		}

		transformTrace();
	}


	public String getComplementString() throws IllegalArgumentException {
		String ret = null;
		try {
			SymbolList tempSeq = DNATools.createDNA(sequence);
			tempSeq = DNATools.reverseComplement(tempSeq);
			ret = (tempSeq.seqString()).toUpperCase();
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Failed to make a complement file"); 
		}
		return ret;
	}


	/**
	 * Makes a complemented Trace
	 * @author Young-gon Kim
	 */

	public void makeComplement() throws IllegalArgumentException {
		int[] newQcalls = new int[sequenceLength];
		int[] newBaseCalls = new int[sequenceLength];
		int[] newA = new int[traceLength];
		int[] newT = new int[traceLength];
		int[] newG = new int[traceLength];
		int[] newC = new int[traceLength];

		try {
			SymbolList tempSeq = DNATools.createDNA(sequence);
			tempSeq = DNATools.reverseComplement(tempSeq);
			sequence = (tempSeq.seqString()).toUpperCase();
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Failed to make a complement file"); 
		}

		

		for(int i=0; i<sequenceLength; i++) {
			newQcalls[i] = qCalls[sequenceLength-1-i];
			newBaseCalls[i] = (traceLength-1) - baseCalls[sequenceLength-1-i];
		}

		for(int i=0; i<traceLength; i++) {
			newA[i] = traceT[traceLength-1-i];
			newT[i] = traceA[traceLength-1-i];
			newG[i] = traceC[traceLength-1-i];
			newC[i] = traceG[traceLength-1-i];
		}
		qCalls = newQcalls;
		baseCalls = newBaseCalls;
		traceA = newA;
		traceT = newT;
		traceG = newG;
		traceC = newC;
		if(direction == FORWARD) 
			direction = REVERSE;
		else
			direction = FORWARD;

		transformTrace();
	}


	/**
	 * @return return값 까지 trimming. -1일 경우 trimming 하지 않음.
	 */
	public int getFrontTrimPosition() {
		int scoreTrimPosition = -1;
		int ret = -1;
		final int windowSize = 10;
		int qualitySearchLength = 2000;	//일단 무한대
		final int scoreCutOff = 35;
		boolean qualityPointFound = false;

		qualitySearchLength = Integer.min(qualitySearchLength,  sequenceLength-windowSize);

		//error 생기면 그냥 trimming 안함.
		try {
			//1. Q-score sliding window
			//i : 0부터 시작하는 좌표.
			int basePosition = -1;

			for(int i=0;i<qualitySearchLength;i++) {
				int sum = 0;
				double avgScore = 0;

				for(int j=0;j<windowSize;j++) {
					sum += qCalls[i+j];
				}
				avgScore = sum/(double)windowSize;

				if(avgScore >= scoreCutOff) {
					qualityPointFound = true;
					for(basePosition=i+windowSize-1; basePosition>=i; basePosition--) {
						if(qCalls[basePosition]<scoreCutOff-10)
							break;
					}
					if(basePosition == -1) 
						scoreTrimPosition = -1;
					else {
						//searchLength->i->basePosition 추적해보면 basePosition의 최대값은 sequenceLength-2임. 따라서 아래코드 error X
						scoreTrimPosition = (baseCalls[basePosition] + baseCalls[basePosition+1])/2;
						scoreTrimPosition *= traceWidth;
					}
					break;
				}
			}

			if(!qualityPointFound) {
				//일단 searchLength 까지만이라도 자르기??
				//나중에 Troubleshooting
				basePosition = (qualitySearchLength-1);
				scoreTrimPosition = (baseCalls[basePosition] + baseCalls[basePosition+1])/2;
				scoreTrimPosition *= traceWidth;
			}

			System.out.println("5' trim, qSCore based : " + (basePosition+1) + " trace : " + scoreTrimPosition);



			ret = scoreTrimPosition;

			//5' terminal에 basecall 안된 trace 늘어져 있으면 자르기.
			if((ret == -1) && (baseCalls[0]>20)) {
				ret = (baseCalls[0]-3) * traceWidth;
			}

			return ret;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	/**
	 * 
	 * @return trimming 안할거면 traceLength*traceWidth
	 */
	public int getTailTrimPosition() {
		int scoreTrimPosition = traceLength*traceWidth;
		int ret = traceLength*traceWidth;
		final int windowSize = 10;
		int qScoreSearchLength = 2000;	// 일단 무한대
		final int scoreCutOff = 35;
		boolean qualityPointFound = false;


		//error 생기면 그냥 trimming 안함.
		try {
			// Q-score sliding window

			qScoreSearchLength = Integer.min(sequenceLength-windowSize, qScoreSearchLength);

			int basePosition = sequenceLength;
			for(int i=sequenceLength-1;i>=sequenceLength-qScoreSearchLength;i--) {
				int sum = 0;
				double avgScore = 0;

				for(int j=0;j<windowSize;j++) {
					sum += qCalls[i-j];
				}
				avgScore = sum/(double)windowSize;

				if(avgScore >= scoreCutOff) {
					qualityPointFound = true;
					for(basePosition=i-windowSize+1; basePosition<=i; basePosition++) {
						if(qCalls[basePosition]<scoreCutOff-10)
							break;
					}

					if(basePosition == sequenceLength) 
						scoreTrimPosition = traceLength*traceWidth;
					else {
						scoreTrimPosition = (baseCalls[basePosition] + baseCalls[basePosition-1])/2;
						scoreTrimPosition *= traceWidth;
					}
					break;
				}
			}

			// 끝까지 quality 안좋으면
			if(!qualityPointFound) {
				basePosition = sequenceLength-qScoreSearchLength;
				scoreTrimPosition = (baseCalls[basePosition] + baseCalls[basePosition-1])/2;
				scoreTrimPosition *= traceWidth;

				//나중에 troubleshooting
			}

			System.out.println("3' trim, qSCore based : " + (basePosition+1) + " trace : " + scoreTrimPosition);


			//ret = Integer.min(scoreTrimPosition, peakHeightPosition);
			ret = scoreTrimPosition;

			//3' terminal에 basecall 안된 trace 늘어져 있으면 자르기.
			if((ret == traceLength*traceWidth) && (baseCalls[sequenceLength-1]+20<traceLength)) {
				ret = (baseCalls[sequenceLength-1]+3) * traceWidth;
			}
			return ret;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
	public int getDirection() {
		return direction;
	}
	public int[] getTraceA() {
		return traceA;
	}
	public int[] getTraceT() {
		return traceT;
	}
	public int[] getTraceG() {
		return traceG;
	}
	public int[] getTraceC() {
		return traceC;
	}
	public int getTraceLength() {
		return traceLength;
	}
	public String getSequence() {
		return sequence;
	}
	public int getSequenceLength() {
		return sequenceLength;
	}
	public int[] getQCalls() {
		return qCalls;
	}
	public int[] getBaseCalls() {
		return baseCalls;
	}
	

	public int[] getqCalls() {
		return qCalls;
	}
	public void setqCalls(int[] qCalls) {
		this.qCalls = qCalls;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public void setBaseCalls(int[] baseCalls) {
		this.baseCalls = baseCalls;
	}
	public void setSequenceLength(int sequenceLength) {
		this.sequenceLength = sequenceLength;
	}

	public void zoomIn() {
		System.out.println("ratio: " + ratio);
		ratio += 0.5;
		transformTrace();
	}
	
	public void zoomOut() {
		System.out.println("ratio: " + ratio);
		if(ratio>0.6) 
			ratio -= 0.5;
		transformTrace();
	}


}
