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
import java.util.TreeMap;
import java.util.Vector;

import com.opaleye.snackntm.mmalignment.AlignedPair;
import com.opaleye.snackntm.reference.ReferenceSeq;

/**
 * Title : Formatter
 * Contains functions that make a list of AlignedPoints based on the result of jAligner
 * Formatting functions are derived from jaligner.formats.Pair.format() 
 * @author Young-gon Kim
 * 2018.5.
 */

public class Formatter implements Serializable {
	public static final char gapChar = '-';
	public TreeMap<Integer, Integer> fwdCoordinateMap = new TreeMap<Integer, Integer>();
	public TreeMap<Integer, Integer> revCoordinateMap = new TreeMap<Integer, Integer>();

	//public static BiMap<Integer, Integer> fwdCoordinateMap = HashBiMap.create();
	//public static BiMap<Integer, Integer> revCoordinateMap = HashBiMap.create();

	public int fwdStartOffset = 700;
	public int revStartOffset = 700;
	public int fwdEndOffset = 700;
	public int revEndOffset = 700;
	public int fwdNewLength = 0;
	public int revNewLength = 0;
	public int fwdTraceAlignStartPoint = 1;
	public int revTraceAlignStartPoint = 1;

	public void init() {
		//fwdCoordinateMap = HashBiMap.create();
		//revCoordinateMap = HashBiMap.create();

		fwdCoordinateMap = new TreeMap<Integer, Integer> ();
		revCoordinateMap = new TreeMap<Integer, Integer> ();


		//getImage2에서 그림 그려줄때 fwd, rev 길이 맞춰주기 위한 변수. 짧은놈에 offset 만큼 길이를 더해줘서 같은길이 되게
		//그래서 scroll 같이 될 수 있게.  단위는 Trace (base 아님)
		// 기본 : 화면 절반. 대략 700.
		fwdStartOffset = 700;
		revStartOffset = 700;
		fwdEndOffset = 700;
		revEndOffset = 700;
		fwdNewLength = 0;
		revNewLength = 0;
		fwdTraceAlignStartPoint = 1;
		revTraceAlignStartPoint = 1;

	}

	/**
	 * Returns a list (Vector) of AlignedPoints
	 * Used for the alignment of 2 sequences (reference vs fwd or rev)
	 * @param alignment : the result of the alignment of jAligner (ref vs fwd or rev)
	 * @param s_refSeq : reference file
	 * @param trace : fwd or rev trace
	 * @param direction : 1:forward, -1:reverse
	 * 	@throws ArrayIndexOutOfBoundsExeption when the alignment fails
	 * 
	 * This functions is derived from jaligner.formats.Pair.format()
	 * 2018.5
	 */

	public Vector<AlignedPoint> format2(AlignedPair ap, String s_refSeq, GanseqTrace trace, int direction) throws ArrayIndexOutOfBoundsException {

		//front padding
		int frontPaddingCnt = Integer.min(ap.getStart1(), ap.getStart2());
		String paddedString1 = s_refSeq.substring(ap.getStart1()-frontPaddingCnt, ap.getStart1()) + ap.getAlignedString1();
		String paddedString2 = trace.sequence.substring(ap.getStart2()-frontPaddingCnt, ap.getStart2()) + ap.getAlignedString2();
		
		//tail padding
		int tailPaddingCnt = Integer.min(s_refSeq.length() - ap.getEnd1(), trace.sequenceLength - ap.getEnd2());
		paddedString1 += s_refSeq.substring(ap.getEnd1(), ap.getEnd1()+tailPaddingCnt);
		paddedString2 += trace.sequence.substring(ap.getEnd2(), ap.getEnd2()+tailPaddingCnt);

		
		int refPos = ap.getStart1()+1 - frontPaddingCnt;
		//(2) Trace 상에서의 좌표. Qcall, BaseCall 읽기위함. Qcall, BaseCall은 0부터 저장되어 있으므로 0부터시작. 
		int tracePos = ap.getStart2() - frontPaddingCnt;
		char[] refSeq = paddedString1.toCharArray();
		char[] traceSeq = paddedString2.toCharArray();
		int alignmentLength = paddedString1.length();

		
		/*
		//(1) Reference 상에서의 좌표. 1부터 시작. +1 해줌. genomic DNA의 의미. 실제 array access에 사용되지 않음. 
		int refPos = ap.getStart1()+1;
		//(2) Trace 상에서의 좌표. Qcall, BaseCall 읽기위함. Qcall, BaseCall은 0부터 저장되어 있으므로 0부터시작. 
		int tracePos = ap.getStart2();
		char[] refSeq = ap.getAlignedString1().toCharArray();
		char[] traceSeq = ap.getAlignedString2().toCharArray();
		int alignmentLength = ap.getAlignedString1().length();
		*/

		//(3)Alignment 상에서의 좌표. (즉 char[] refSeq, traceSeq 의 index) 0부터 시작. 
		int alignmentPos = 0;
		Vector<AlignedPoint> alignedPoints = new Vector<AlignedPoint>();
		while(alignmentPos < alignmentLength) {
			char refChar = refSeq[alignmentPos];
			char traceChar = traceSeq[alignmentPos];
			AlignedPoint tempPoint = null;

			if(refChar != gapChar) {
				char discrepency = ' ';
				if(refChar!=traceChar)
					discrepency = '*';

				if(direction == 1) 
					tempPoint = new AlignedPoint (refChar, traceChar, gapChar, traceChar, refPos, tracePos+1, -1);
				else if(direction == -1)
					tempPoint = new AlignedPoint (refChar, gapChar, traceChar, traceChar, refPos, -1, tracePos+1);

				if(traceChar!=gapChar) {
					if(direction == 1) {
						tempPoint.setFwdQuality(trace.getQCalls()[tracePos]);
						fwdCoordinateMap.put(new Integer(tracePos+1), new Integer(alignedPoints.size()+1));
					}
					else if(direction == -1) {
						tempPoint.setRevQuality(trace.getQCalls()[tracePos]);
						revCoordinateMap.put(new Integer(tracePos+1), new Integer(alignedPoints.size()+1));
					}
					tracePos++;
				}
				refPos++;
				alignmentPos++;

			}

			else if(refChar == gapChar) {
				char discrepency = '*';
				if(direction == 1)
					tempPoint = new AlignedPoint (refChar, traceChar, gapChar, traceChar, refPos-1, tracePos+1, -1);
				else if (direction == -1)
					tempPoint = new AlignedPoint (refChar, gapChar, traceChar, traceChar, refPos-1, -1, tracePos+1);
				if(traceChar!=gapChar) {
					if(direction == 1) {
						tempPoint.setFwdQuality(trace.getQCalls()[tracePos]);
						fwdCoordinateMap.put(new Integer(tracePos+1), new Integer(alignedPoints.size()+1));
					}
					else if(direction == -1) {
						tempPoint.setRevQuality(trace.getQCalls()[tracePos]);
						revCoordinateMap.put(new Integer(tracePos+1), new Integer(alignedPoints.size()+1));
					}
					tracePos++;
				}
				alignmentPos++; 

			}

			alignedPoints.add(tempPoint);
		}

		if(direction ==1) 
			fwdNewLength = fwdStartOffset + trace.getTraceLength()*2 + fwdEndOffset;
		else if (direction == -1)
			revNewLength = revStartOffset + trace.getTraceLength()*2 + revEndOffset;

		//cDNA number 만들기
		int startGIndex = 0, endGIndex = 0;
		startGIndex = ap.getStart1()+1;
		endGIndex = refPos;

		//alignedPoints = addCDnaNumber(alignedPoints, startGIndex, endGIndex, refFile);

		return alignedPoints;
	}

	/**
	 * Returns a list (Vector) of AlignedPoints
	 * Used for the alignment of 3 sequences (reference vs fwd and rev)
	 * @param fwdAlignment : the result of the alignment of jAligner (fwd vs ref)
	 * @param revAlignment : the result of the alignment of jAligner (rev vs ref)
	 * @param refFile : reference file
	 * @param fwdTrace : fwd trace
	 * @param revTrace : rev trace
	 * @throws ArrayIndexOutOfBoundsExeption when the alignment fails
	 * @throws NoContigExeption when fwd trace and rev trace don't have overlap
	 * 
	 * This functions is derived from jaligner.formats.Pair.format()
	 * 2018.5
	 */
	public Vector<AlignedPoint> format3(AlignedPair fwdAp, AlignedPair revAp, String s_refSeq, GanseqTrace fwdTrace, GanseqTrace revTrace) throws ArrayIndexOutOfBoundsException, NoContigException {

		//padding : V1.2.1 --> V1.3.0, alignment 양 끝에 discrepancy가 있는경우도 다 포함시키기 위함.
		
		//fwd Trace
		//front padding
		int fwdFrontPaddingCnt = Integer.min(fwdAp.getStart1(), fwdAp.getStart2());
		String fwdPaddedString1 = s_refSeq.substring(fwdAp.getStart1()-fwdFrontPaddingCnt, fwdAp.getStart1()) + fwdAp.getAlignedString1();
		String fwdPaddedString2 = fwdTrace.sequence.substring(fwdAp.getStart2()-fwdFrontPaddingCnt, fwdAp.getStart2()) + fwdAp.getAlignedString2();
		
		//tail padding
		int fwdTailPaddingCnt = Integer.min(s_refSeq.length() - fwdAp.getEnd1(), fwdTrace.sequenceLength - fwdAp.getEnd2());
		fwdPaddedString1 += s_refSeq.substring(fwdAp.getEnd1(), fwdAp.getEnd1()+fwdTailPaddingCnt);
		fwdPaddedString2 += fwdTrace.sequence.substring(fwdAp.getEnd2(), fwdAp.getEnd2()+fwdTailPaddingCnt);

		//rev Trace
		//front padding
		int revFrontPaddingCnt = Integer.min(revAp.getStart1(), revAp.getStart2());
		String revPaddedString1 = s_refSeq.substring(revAp.getStart1()-revFrontPaddingCnt, revAp.getStart1()) + revAp.getAlignedString1();
		String revPaddedString2 = revTrace.sequence.substring(revAp.getStart2()-revFrontPaddingCnt, revAp.getStart2()) + revAp.getAlignedString2();
		
		//tail padding
		int revTailPaddingCnt = Integer.min(s_refSeq.length() - revAp.getEnd1(), revTrace.sequenceLength - revAp.getEnd2());
		revPaddedString1 += s_refSeq.substring(revAp.getEnd1(), revAp.getEnd1()+revTailPaddingCnt);
		revPaddedString2 += revTrace.sequence.substring(revAp.getEnd2(), revAp.getEnd2()+revTailPaddingCnt);

		
		//System.out.println(String.format("fwdFrontPaddintCnt, fwdTailPaddingCnt, revFrontPaddingCnt, revTailPaddingCnt : %d, %d, %d, %d", fwdFrontPaddingCnt, fwdTailPaddingCnt, revFrontPaddingCnt, revTailPaddingCnt));
		//System.out.println(String.format("fwdPaddedString1, fwdPaddedString2:\n%s\n%s", fwdPaddedString1, fwdPaddedString2));
		//System.out.println(String.format("revPaddedString1, revPaddedString2:\n%s\n%s", revPaddedString1, revPaddedString2));
		
		int fwdAlignmentLength = fwdPaddedString1.length();
		int revAlignmentLength = revPaddedString1.length();

		//(1) Reference 상에서의 좌표. 1부터 시작. +1 해줌. genomic DNA의 의미. 실제 array access에 사용되지 않음. 
		int fwdRefPos = fwdAp.getStart1()+1 - fwdFrontPaddingCnt;
		int revRefPos = revAp.getStart1()+1 - revFrontPaddingCnt; 

		//(2) Trace 상에서의 좌표. Qcall, BaseCall 읽기위함. Qcall, BaseCall은 0부터 저장되어 있으므로 0부터시작. 
		int fwdTracePos = fwdAp.getStart2() - fwdFrontPaddingCnt;;
		int revTracePos = revAp.getStart2() - revFrontPaddingCnt; 


		char[] fwdRefSeq = fwdPaddedString1.toCharArray();
		char[] fwdTraceSeq = fwdPaddedString2.toCharArray();
		char[] revRefSeq = revPaddedString1.toCharArray();
		char[] revTraceSeq = revPaddedString2.toCharArray();

		//(3)Alignment 상에서의 좌표. (즉 char[] fwdRefSeq, fwdTraceSeq, revRefSeq, revTraceSeq 의 index) 0부터 시작. 
		int fwdAlignmentPos = 0;
		int revAlignmentPos = 0;


		Vector<AlignedPoint> alignedPoints = new Vector<AlignedPoint>();

		//앞쪽에 fwd, rev 중 하나만 있는 영역
		//fwd가 앞에 튀어나온 경우 
		if(fwdRefPos < revRefPos) {

			//fwd와 rev가 너무 멀리 떨어져 있어서 겹치는 부분 없을 경우 (그냥 두면 ArrayIndexOutofBoundsException 발생)
			//Reference 좌표끼리의 비교.  어디 어디에 붙었나 가지고 비교하는거니까.
			if(revRefPos-fwdRefPos > fwdRefSeq.length) 
				throw new NoContigException();

			while(fwdRefPos<revRefPos) {	// 만약에 loop 끝나고 나갔을 때 fwdRefChar가 gap이다 : 괜찮음. 조건문에 따라 적절히 처리됨. 
				char fwdRefChar = fwdRefSeq[fwdAlignmentPos];
				char fwdTraceChar = fwdTraceSeq[fwdAlignmentPos];
				char revTraceChar = gapChar;
				
				char discrepency = ' ';
				if(fwdRefChar == gapChar || fwdRefChar != fwdTraceChar)
					discrepency = '*';
				

				AlignedPoint tempPoint = null;
				if(fwdRefChar == gapChar)
					tempPoint = new AlignedPoint (fwdRefChar, fwdTraceChar, revTraceChar, fwdTraceChar, fwdRefPos-1, fwdTracePos+1, revTracePos+1);
				else {
					tempPoint = new AlignedPoint (fwdRefChar, fwdTraceChar, revTraceChar, fwdTraceChar, fwdRefPos++, fwdTracePos+1, revTracePos+1);
				}

				if(fwdTraceChar!=gapChar) {
					tempPoint.setFwdQuality(fwdTrace.getQCalls()[fwdTracePos]);
					//Trace 그림 보여줄때 좌표 통일하기 위해 mapping 만들어둠.
					//보여주는 좌표는 1부터 시작하므로 +1 (fwdTracePos는 위에서 ++ 했으므로 그냥)
					fwdCoordinateMap.put(new Integer(fwdTracePos+1), new Integer(alignedPoints.size()+1));
					fwdTracePos++;
				}
				alignedPoints.add(tempPoint);
				fwdAlignmentPos++;

			}

			revStartOffset += fwdTrace.getBaseCalls()[fwdTracePos-1] * 2;
		}

		//rev가 앞에 튀어나온 경우
		else if(revRefPos < fwdRefPos) {

			if(fwdRefPos-revRefPos > revRefSeq.length) 
				throw new NoContigException();

			while(revRefPos<fwdRefPos) {
				char revRefChar = revRefSeq[revAlignmentPos];
				char revTraceChar = revTraceSeq[revAlignmentPos];
				char fwdTraceChar = gapChar;
				char discrepency = ' ';
				if(revRefChar == gapChar || revRefChar != revTraceChar)
					discrepency = '*';
				
				AlignedPoint tempPoint = null;
				if(revRefChar == gapChar)
					tempPoint = new AlignedPoint (revRefChar, fwdTraceChar, revTraceChar, revTraceChar, revRefPos-1, fwdTracePos+1, revTracePos+1);
				else {
					tempPoint = new AlignedPoint (revRefChar, fwdTraceChar, revTraceChar, revTraceChar, revRefPos++, fwdTracePos+1, revTracePos+1);
				}

				if(revTraceChar!=gapChar) {
					tempPoint.setRevQuality(revTrace.getQCalls()[revTracePos]);
					//Trace 그림 보여줄때 좌표 통일하기 위해 mapping 만들어둠.
					//보여주는 좌표는 1부터 시작하므로 +1 (revTracePos는 위에서 ++ 했으므로 그냥)
					revCoordinateMap.put(new Integer(revTracePos+1), new Integer(alignedPoints.size()+1));
					revTracePos++;
				}
				alignedPoints.add(tempPoint);
				revAlignmentPos++;
			}
			fwdStartOffset += revTrace.getBaseCalls()[revTracePos-1] * 2;
		}
		fwdTraceAlignStartPoint = fwdTracePos+1;
		revTraceAlignStartPoint = revTracePos+1;

		//둘 중에 하나가 끝나면 loop 빠져나감.
		while(fwdAlignmentPos < fwdAlignmentLength && revAlignmentPos < revAlignmentLength) {

			char fwdRefChar = fwdRefSeq[fwdAlignmentPos];
			char revRefChar = revRefSeq[revAlignmentPos];
			char fwdTraceChar = fwdTraceSeq[fwdAlignmentPos];
			char revTraceChar = revTraceSeq[revAlignmentPos];
			
			
			//System.out.println(String.format("fwdPos, fwdLength, revPos, revLength : %d, %d, %d, %d", fwdAlignmentPos, fwdAlignmentLength, revAlignmentPos, revAlignmentLength));
			//System.out.println(String.format("fwdPos, fwdLength, revPos, revLength : %d, %d, %d, %d", fwdAlignmentPos, fwdAlignmentLength, revAlignmentPos, revAlignmentLength));
			
			
			AlignedPoint tempPoint = null;

			//System.out.println("fwdAli, revAli, fwdRef, revRef, fwdTra, revTra, fwdRC, revRc, fwdTc, revTc");
			//System.out.println(String.format("%d, %d, %d, %d, %d, %d, %c, %c, %c, %c", 
			//		fwdAlignmentPos, revAlignmentPos, fwdRefPos, revRefPos, fwdTracePos,
			//		revTracePos, fwdRefChar, revRefChar, fwdTraceChar, revTraceChar));

			char consensusBase = 'N';
			int fwdQ = 0, revQ = 0;
			int fwdTracePosPenalty = 0, revTracePosPenalty = 0;
			//fwd, rev 중에 Q값 높은쪽으로 

			//현재 position에서 trace가 gap이면 
			// 1) Q score는 다음 base의 Q 값 갖고 있음. (이전 base gap 아니었다고 가정하면 거기서 traceindex를 +1 미리 해버렸음)
			// 2) 여기가 마지막 base일 리는 없음 (trace가 gap으로 끝나지는않음. alignment 결과도 그렇고 padding도 그렇고. padding은 substring을 더하는 것이므로.)
			// 3) 여기가 첫 base일리도 없음. (위와 마찬가지 이유로)
			
			
			
			if(fwdTraceChar == Formatter.gapChar) {	//gap이면 앞뒤 평균을 Q score로
				fwdQ = (fwdTrace.getQCalls()[fwdTracePos] + fwdTrace.getQCalls()[Integer.max(fwdTracePos-1,  0)]) / 2;
			}
			else 
				fwdQ = fwdTrace.getQCalls()[fwdTracePos];

			if(revTraceChar == Formatter.gapChar) {	//gap이면 앞뒤 평균을 Q score로
				revQ = (revTrace.getQCalls()[revTracePos] + revTrace.getQCalls()[Integer.max(revTracePos-1,  0)]) / 2;
			}
			else 
				revQ = revTrace.getQCalls()[revTracePos];
			
			//System.out.println(String.format("fwdOriQscore : %d, revOriQscore : %d",  fwdQ, revQ));
			
			double penaltyWeight = RootController.endPenalty;
			
			if(fwdTracePos<20) {
				fwdTracePosPenalty = (int)((20-fwdTracePos) * penaltyWeight);
			}
			else if(fwdTracePos >= fwdTrace.sequenceLength-20) {
				fwdTracePosPenalty = (int)((20-(fwdTrace.sequenceLength - fwdTracePos)) * penaltyWeight);
			}
			
			

			if(revTracePos<20) {
				revTracePosPenalty =(int)((20-revTracePos) * penaltyWeight);
			}
			else if(revTracePos >= revTrace.sequenceLength-20) {
				revTracePosPenalty = (int)((20-(revTrace.sequenceLength - revTracePos)) * penaltyWeight);
			}

			fwdQ -= fwdTracePosPenalty;
			revQ -= revTracePosPenalty;
			
			//System.out.println(String.format("fwdQscore : %d, revQscore : %d",  fwdQ, revQ));
			
			if(fwdQ >= revQ) 
				consensusBase = fwdTraceChar;
			else 
				consensusBase = revTraceChar;
			
			//homozygous insertion 없음. 
			if(fwdRefChar == revRefChar && fwdRefChar != gapChar) {
				char discrepency = ' ';
				
				if(fwdRefChar != consensusBase)	//fwdRefChar와 revRefchar는 어차피 같음. (저~~위에 if)
					discrepency = '*';
				
				
				tempPoint = new AlignedPoint (fwdRefChar, fwdTraceChar, revTraceChar, consensusBase, fwdRefPos, fwdTracePos+1, revTracePos+1);
				if(fwdTraceChar!=gapChar) {
					tempPoint.setFwdQuality(fwdTrace.getQCalls()[fwdTracePos]);
					fwdCoordinateMap.put(new Integer(fwdTracePos+1), new Integer(alignedPoints.size()+1));
					fwdTracePos++;
				}
				if(revTraceChar!=gapChar) {
					tempPoint.setRevQuality(revTrace.getQCalls()[revTracePos]);
					revCoordinateMap.put(new Integer(revTracePos+1), new Integer(alignedPoints.size()+1));
					revTracePos++;
				}
				fwdRefPos++;
				revRefPos++;
				fwdAlignmentPos++;
				revAlignmentPos++;
			}

			//양쪽 alignment에서 둘다 homozygous insertion이 있는 경우.
			else if(fwdRefChar == gapChar && revRefChar == gapChar) {
				char discrepency = '*';
				
				tempPoint = new AlignedPoint (fwdRefChar, fwdTraceChar, revTraceChar, consensusBase, fwdRefPos-1, fwdTracePos+1, revTracePos+1);
				if(fwdTraceChar!=gapChar) {
					tempPoint.setFwdQuality(fwdTrace.getQCalls()[fwdTracePos]);
					fwdCoordinateMap.put(new Integer(fwdTracePos+1), new Integer(alignedPoints.size()+1));
					fwdTracePos++;
				}
				if(revTraceChar!=gapChar) {
					tempPoint.setRevQuality(revTrace.getQCalls()[revTracePos]);
					revCoordinateMap.put(new Integer(revTracePos+1), new Integer(alignedPoints.size()+1));
					revTracePos++;
				}
				fwdAlignmentPos++; 
				revAlignmentPos++;
			}

			//fwd에만 homo insertion으로 인한 GAP 있는 경우. rev는 쉰다.
			else if(fwdRefChar==gapChar) {
				tempPoint = new AlignedPoint (gapChar, fwdTraceChar, gapChar, fwdTraceChar, fwdRefPos-1, fwdTracePos+1, revTracePos+1);

				tempPoint.setFwdQuality(fwdTrace.getQCalls()[fwdTracePos]);
				fwdCoordinateMap.put(new Integer(fwdTracePos+1), new Integer(alignedPoints.size()+1));

				fwdAlignmentPos++; 
				fwdTracePos++;
			}

			//rev에만 homo insertion으로 인한 GAP 있는경우. fwd는 쉰다.
			else if(revRefChar==gapChar) {
				tempPoint = new AlignedPoint (gapChar, gapChar, revTraceChar, revTraceChar, fwdRefPos-1, fwdTracePos+1, revTracePos+1);
				tempPoint.setRevQuality(revTrace.getQCalls()[revTracePos]);
				revCoordinateMap.put(new Integer(revTracePos+1), new Integer(alignedPoints.size()+1));
				revAlignmentPos++;
				revTracePos++;
			}
			//System.out.println(String.format("fwdPos, fwdLength, revPos, revLength : %d, %d, %d, %d", fwdAlignmentPos, fwdAlignmentLength, revAlignmentPos, revAlignmentLength));
			//System.out.println(alignedPoints.size());
			alignedPoints.add(tempPoint);
		}

		//뒷쪽에 튀어나온 부분 처리.
		//fwd가 튀어나온 경우
		if(revAlignmentPos == revAlignmentLength) {
			if(fwdTracePos<=0) throw new NoContigException();
			revEndOffset += (fwdTrace.getTraceLength()-fwdTrace.getBaseCalls()[fwdTracePos-1])*2; 

			while(fwdAlignmentPos < fwdAlignmentLength) {
				char refChar = fwdRefSeq[fwdAlignmentPos];
				char fwdTraceChar = fwdTraceSeq[fwdAlignmentPos];
				char revTraceChar = gapChar;

				char discrepency = ' ';
				if(refChar == gapChar || refChar != fwdTraceChar)
					discrepency = '*';
				
				AlignedPoint tempPoint = null;
				if(refChar == gapChar) 
					tempPoint = new AlignedPoint (refChar, fwdTraceChar, revTraceChar, fwdTraceChar, fwdRefPos-1, fwdTracePos+1, revTracePos);
				else
					tempPoint = new AlignedPoint (refChar, fwdTraceChar, revTraceChar, fwdTraceChar, fwdRefPos++, fwdTracePos+1, revTracePos);

				if(fwdTraceChar!=gapChar) {
					tempPoint.setFwdQuality(fwdTrace.getQCalls()[fwdTracePos]);
					fwdCoordinateMap.put(new Integer(fwdTracePos+1), new Integer(alignedPoints.size()+1));
					fwdTracePos++;
				}
				alignedPoints.add(tempPoint);
				fwdAlignmentPos++;

			}
		}

		//rev가 튀어나온 경우
		else if(fwdAlignmentPos == fwdAlignmentLength) {
			if(revTracePos<=0) throw new NoContigException(); 
			fwdEndOffset += (revTrace.getTraceLength()-revTrace.getBaseCalls()[revTracePos-1])*2; 

			while(revAlignmentPos < revAlignmentLength) {
				char refChar = revRefSeq[revAlignmentPos];
				char fwdTraceChar = gapChar;
				char revTraceChar = revTraceSeq[revAlignmentPos];

				char discrepency = ' ';
				if(refChar == gapChar || refChar != revTraceChar)
					discrepency = '*';
				
				AlignedPoint tempPoint = null;
				if(refChar == gapChar) 
					tempPoint = new AlignedPoint (refChar, fwdTraceChar, revTraceChar, revTraceChar, revRefPos-1, fwdTracePos, revTracePos+1);
				else
					tempPoint = new AlignedPoint (refChar, fwdTraceChar, revTraceChar, revTraceChar, revRefPos++, fwdTracePos, revTracePos+1);

				if(revTraceChar!=gapChar) {
					tempPoint.setRevQuality(revTrace.getQCalls()[revTracePos]);
					revCoordinateMap.put(new Integer(revTracePos+1), new Integer(alignedPoints.size()+1));
					revTracePos++;
				}
				alignedPoints.add(tempPoint);
				revAlignmentPos++;

			}
		}

		fwdNewLength = fwdStartOffset + fwdTrace.getTraceLength()*2 + fwdEndOffset;
		revNewLength = revStartOffset + revTrace.getTraceLength()*2 + revEndOffset;

		//cDNA number 만들기


		int startGIndex = 0, endGIndex = 0;
		if(fwdAp.getStart1() <= revAp.getStart1()) startGIndex = fwdAp.getStart1()+1;
		else startGIndex = revAp.getStart1()+1;

		if(fwdRefPos >= revRefPos) endGIndex = fwdRefPos;
		else endGIndex = revRefPos;

		//alignedPoints = addCDnaNumber(alignedPoints, startGIndex, endGIndex, refFile);

		return alignedPoints;
	}

	/**
	 * returns true if the gIndex is on exon
	 * returns false if the gIndex is on intron
	 * @param exonStart : a list of start positions of exons in the gene
	 * @param exonEnd : a list of end positions of exons in the gene
	 * @param gIndex : target Index (genomic DNA index)
	 */
	/*
	private static boolean isExon(Vector<Integer> exonStart, Vector<Integer> exonEnd, int gIndex) {
		if(exonStart == null || exonEnd == null) 
			return false;

		for(int i=0;i<exonStart.size();i++) {
			int start = (exonStart.get(i)).intValue();
			int end = (exonEnd.get(i)).intValue();
			if(gIndex >= start && gIndex <= end) return true;
		}
		return false;
	}
	 */

}
