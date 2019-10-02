package com.opaleye.snackntm.mmalignment;

import java.io.File;

import com.opaleye.snackntm.Formatter;
import com.opaleye.snackntm.GanseqTrace;
import com.opaleye.snackntm.RootController;
import com.opaleye.snackntm.reference.ReferenceSeq;

public class MMAlignment {

	private double gapOpenPenalty = RootController.gapOpenPenalty;
	private static final double h = 0.5;
	private static final double minusLimit = -100;


	private short indexOfBase(char c) {
		switch(c) {
		case 'A' : return 0;
		case 'T' : return 1;
		case 'G' : return 2; 
		case 'C' : return 3;
		case 'S' : return 4;
		case 'W' : return 5;
		case 'R' : return 6;
		case 'Y' : return 7;
		case 'K' : return 8;
		case 'M' : return 9;
		case 'B' : return 10;
		case 'V' : return 11;
		case 'H' : return 12;
		case 'D' : return 13;
		default : return 14;
		}
	}

	private short[] getIntArray(char[] charArray) {
		short[] ret = new short[charArray.length];
		for(int i=0;i<charArray.length;i++) 
			ret[i] = indexOfBase(charArray[i]);
		return ret;
	}


	private double[][] matrix = null;

	private double[] CC, DD, RR, SS;
	private int maxI = 0, maxJ = 0;
	private double maxC = 0;

	public MMAlignment() {
		matrix = (new SubstitutionMatrix()).getMatrix();
	}

	private double gap(int n) {
		if(n==0) return 0;
		else return gapOpenPenalty+(double)n*h;
	}

	private String getReverse(String s) {
		StringBuffer sb = new StringBuffer();
		int length = s.length();
		for(int i=0;i<length;i++) {
			sb.append(s.charAt(length-i-1));
		}
		return sb.toString();
	}

	public void firstScan (char[] s1, char[] s2) {
		short[] intArray1 = getIntArray (s1);
		short[] intArray2 = getIntArray (s2);

		maxI = 0; maxJ = 0; maxC = 0;

		int M = s1.length;
		int N = s2.length;

		CC = new double[N+1];
		DD = new double[N+1];

		double e=0, c=0, s=0;

		DD[0] = minusLimit;
		CC[0] = 0;


		for(int j=1;j<=N;j++) {
			CC[j] = 0;
			DD[j] = minusLimit;
		}

		for(int i=1;i<=M;i++) {
			//s : 대각선 왼쪽 위에꺼 (이때까지 CC는 윗줄)
			s = CC[0];

			//c : 한칸 왼쪽
			c = 0;

			//e: 현재 I값
			e = minusLimit;


			for(int j=1;j<=N;j++) {
				if(e>=c-gapOpenPenalty) e = e-h;
				else e = c-gapOpenPenalty-h;

				if(DD[j] >= CC[j]-gapOpenPenalty) DD[j] = DD[j]-h;
				else DD[j] = CC[j]-gapOpenPenalty-h;

				if(DD[j] >= e) c = DD[j];
				else c = e;

				double newC;
				//newC = s+weight(s1[i-1], s2[j-1], false);
				newC = s+matrix[intArray1[i-1]][intArray2[j-1]];

				if(c <= newC) c= newC;
				if(c<0) c = 0; 
				if(c>=maxC) {
					maxI = i;
					maxJ = j;
					maxC = c;
				}
				s = CC[j];
				CC[j] = c;
			}
		}
	}

	public void computeCCDD(char[] s1, char[] s2, double tbte ) {
		int M = s1.length;
		int N = s2.length;

		short[] intArray1 = getIntArray (s1);
		short[] intArray2 = getIntArray (s2);

		double e=0, c=0, s=0, t=0;

		CC[0] = 0;
		t = gapOpenPenalty;

		for(int j=1;j<=N;j++) {
			t = t+h;
			CC[j] = t;
			DD[j] = t+gapOpenPenalty;
		}

		t=tbte;

		for(int i=1;i<=M;i++) {
			s = CC[0];
			t = t + h;
			c =  t;
			CC[0] = c;
			e = t+gapOpenPenalty;
			for(int j=1;j<=N;j++) {
				if(e<c+gapOpenPenalty) e = e+h;
				else e = c+gapOpenPenalty+h;

				if(DD[j] < CC[j]+gapOpenPenalty) DD[j] = DD[j]+h;
				else DD[j] = CC[j]+gapOpenPenalty+h;

				if(DD[j] < e) c = DD[j];
				else c = e;

				double newC;
				newC = s-matrix[intArray1[i-1]][intArray2[j-1]];
				if(newC < c) 
					c = newC;

				s = CC[j];
				CC[j] = c;
			}
		}
		DD[0] = CC[0];
	}


	public void computeRRSS(char[] s1, char[] s2, double tbte ) {
		int M = s1.length;
		int N = s2.length;

		short[] intArray1 = getIntArray (s1);
		short[] intArray2 = getIntArray (s2);

		double e=0, c=0, s=0, t=0;

		RR[0] = 0;
		t = gapOpenPenalty;

		for(int j=1;j<=N;j++) {
			t = t+h;
			RR[j] = t;
			SS[j] = t+gapOpenPenalty;
		}

		t=tbte;

		for(int i=1;i<=M;i++) {
			s = RR[0];
			t = t + h;
			c =  t;
			RR[0] = c;
			e = t+gapOpenPenalty;
			for(int j=1;j<=N;j++) {
				if(e<c+gapOpenPenalty) e = e+h;
				else e = c+gapOpenPenalty+h;

				if(SS[j] < RR[j]+gapOpenPenalty) SS[j] = SS[j]+h;
				else SS[j] = RR[j]+gapOpenPenalty+h;

				if(SS[j] < e) c = SS[j];
				else c = e;

				double newC;
				newC = s-matrix[intArray1[i-1]][intArray2[j-1]];
				if(newC < c) 
					c = newC;

				s = RR[j];
				RR[j] = c;
			}
		}
		SS[0] = RR[0];
	}

	private AlignedPair diff(String A, String B, double tb, double te) {
		String alignedString1 = "";
		String alignedString2 = "";
		AlignedPair ret = null;
		int M = A.length();
		int N = B.length();
		if(N==0) {
			if(M>0) {
				alignedString1 = A;
				for(int i=0;i<A.length();i++) {
					alignedString2 += "-";
				}
				ret = new AlignedPair(alignedString1, alignedString2);
				return ret;
			}
			else return new AlignedPair("", "");
		}
		else if(M==0) {
			alignedString2 = B;
			for(int i=0;i<B.length();i++) {
				alignedString1 += "-";
			}
			ret = new AlignedPair(alignedString1, alignedString2);
			return ret;
		}
		else if(M==1) {
			double cost1 = Double.min(tb, te) + h + gap(N);
			double cost2 = Double.MAX_VALUE;
			int cost2Index = 0;

			for(int j=1;j<=N;j++) {
				double tempCost = gap(j-1) - matrix[indexOfBase(A.charAt(0))][indexOfBase(B.charAt(j-1))] + gap(N-j);
				if(tempCost < cost2) {
					cost2 = tempCost;
					cost2Index = j;
				}
			}
			if(cost1 < cost2) {
				if(tb == 0) {	//deletion A -> insertion B
					alignedString1 = A;
					for(int i=0;i<B.length();i++) {
						alignedString1 += "-";
					}
					alignedString2 = "-"+B;
				}
				else if(te == 0) { //insertion B -> deletion A
					for(int i=0;i<B.length();i++) {
						alignedString1 += "-";
					}
					alignedString1 += A;

					alignedString2 = B + "-";
				}
				else {		//아무렇게나 해도 되지만 deletion A -> insertion B
					alignedString1 = A;
					for(int i=0;i<B.length();i++) {
						alignedString1 += "-";
					}
					alignedString2 = "-"+B;
				}
			}
			else {
				alignedString2 = B;
				for(int i=1;i<cost2Index;i++) {
					alignedString1 += "-";
				}
				alignedString1 += A;
				for(int i=cost2Index+1;i<=B.length();i++) {
					alignedString1 += "-";
				}
			}
			ret = new AlignedPair(alignedString1, alignedString2);
			return ret;
		}

		else {
			int I = M/2;
			String AI = A.substring(0,I);
			String AT = A.substring(I, A.length());
			String ATrev = getReverse(AT);
			String Brev = getReverse(B);

			computeCCDD(AI.toCharArray(), B.toCharArray(), tb);
			computeRRSS(ATrev.toCharArray(), Brev.toCharArray(), te);

			int type = 0;
			int minType = 0;
			int minJ = 0;
			double minValue = Double.MAX_VALUE;

			for(int j=0;j<=N;j++) {
				double value = 0;
				double CR = CC[j] + RR[N-j];
				double DS = DD[j] + SS[N-j] - gapOpenPenalty;
				if(DS < CR) {
					value = DS;
					type = 2;
				}
				else {
					value = CR;
					type = 1;
				}
				if(value < minValue) {
					minValue = value;
					minJ = j;
					minType = type;
				}
			}

			String BJ = B.substring(0,minJ);
			String BT = B.substring(minJ, B.length());


			if(minType == 1) {
				return diff(AI, BJ, tb, gapOpenPenalty).addRight(diff(AT, BT, gapOpenPenalty, te));
			}
			else {
				AlignedPair middle = new AlignedPair(A.substring(I-1, I+1), "-" + "-");
				return diff(AI.substring(0,AI.length()-1), BJ, tb, 0).addRight(middle).addRight(diff(AT.substring(1,AT.length()), BT, 0, te));
			}
		}
	}

	public AlignedPair globalAlignment(String inputString1, String inputString2) {
		inputString1 = inputString1.toUpperCase();
		inputString2 = inputString2.toUpperCase();

		CC = new double[inputString2.length()+1];
		DD = new double[inputString2.length()+1];
		RR = new double[inputString2.length()+1];
		SS = new double[inputString2.length()+1];
		return diff(inputString1, inputString2, gapOpenPenalty, gapOpenPenalty);
	}

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @param option : 1이면 앞뒤 padding, 0이면 안함
	 * @return
	 */
	public AlignedPair localAlignment(String s1, String s2) {
		int start1 = 0, start2 = 0;
		int end1 = 0, end2 = 0;
		//long timeStamp = System.currentTimeMillis();

		s1 = s1.toUpperCase();
		s2 = s2.toUpperCase();

		firstScan(s1.toCharArray(),  s2.toCharArray());
		//System.out.println("time1 : " + (System.currentTimeMillis() - timeStamp));

		end1 = maxI;
		end2 = maxJ;

		s1 = s1.substring(0,maxI);
		s2 = s2.substring(0,maxJ);

		String revS1 = getReverse(s1);
		String revS2 = getReverse(s2);
		//System.out.println("time2 : " + (System.currentTimeMillis() - timeStamp));

		firstScan(revS1.toCharArray(), revS2.toCharArray());
		//System.out.println("time3 : " + (System.currentTimeMillis() - timeStamp));
		start1 = revS1.length() - maxI;
		start2 = revS2.length() - maxJ;


		revS1 = revS1.substring(0,maxI);
		revS2 = revS2.substring(0,maxJ);


		//	System.out.println("start1 : " + start1 + ", start2 : " + start2);

		s1 = getReverse(revS1);
		s2 = getReverse(revS2);

		AlignedPair ret = globalAlignment(s1, s2);
		ret.setStart1(start1);
		ret.setStart2(start2);
		ret.setEnd1(end1);
		ret.setEnd2(end2);

		//System.out.println("time4 : " + (System.currentTimeMillis() - timeStamp));
	return ret;
		
	}
	
	/*
	public AlignedPair localAlignmentPadding (String s1, String s2) {
		AlignedPair ap =  localAlignment(s1, s2);
		int start1 = ap.getStart1(), start2 = ap.getStart2();
		int newStart1 = 0, newStart2 = 0;	//이거를 newstart1 = start1 이렇게 하면 에러남. deep copy 아님.. 
		int end1 = ap.getEnd1(), end2 = ap.getEnd2();
		int newEnd1 =0, newEnd2 = 0;
		
		String newS1 = new String(s1);
		String newS2 = new String(s2);

		newS1 = s1.substring(start1-start2, start1) + newS1;
		newS2 = s2.substring(0, start2) + newS2;
		newStart1 = start1-start2;
		newStart2 = 0;
		newS1 += s1.substring(end1, end1+s2.length()-end2);
		newS2 += s2.substring(end2, s2.length());
		newEnd1 = end1 + (s2.length()-end2);
		newEnd2 = s2.length();


		
		AlignedPair ret = new AlignedPair(newS1, newS2);
		ret.setStart1(newStart1);
		ret.setStart2(newStart2);
		ret.setEnd1(newEnd1);
		ret.setEnd2(newEnd2);


		

		return ret;

	}
			*/

				
			/*
			s1 = originalS1; s2 = originalS2;
			if(start1-start2>=0 && start2 > 0) {
				AlignedPair leftPad = new AlignedPair(s1.substring(start1-start2, start1), s2.substring(0, start2));
				ret = ret.addLeft(leftPad);
				ret.setStart1(start1-start2);
				ret.setStart2(0);
			}

			if(end1+s2.length()-end2 <= s1.length() && end2<s2.length()) {
				AlignedPair rightPad = new AlignedPair(s1.substring(end1, end1+s2.length()-end2), s2.substring(end2, s2.length()));
				ret = ret.addRight(rightPad);
				ret.setEnd1(end1 + (s2.length()-end2));
				ret.setEnd2(s2.length());
			}


			ret.printStrings();
			System.out.println("start1 : " + ret.getStart1());
			System.out.println("start2 : " + ret.getStart2());
			System.out.println("end1 : " + ret.getEnd1());
			System.out.println("end2 : " + ret.getEnd2());
			 */



		

	public void setGapOpenPenalty(double gapOpenPenalty) {
		this.gapOpenPenalty = gapOpenPenalty;
	}

	public static void main(String[] args) {
		MMAlignment mma = new MMAlignment();
		String s1 = "GGAAATTTTTTTAAATT";
		String s2 = "CCCTTTTTTCCC";
		AlignedPair ret =  mma.localAlignment(s1, s2);

		ret.printStrings();
		System.out.println("start1 : " + ret.getStart1() + ", start2 : " + ret.getStart2());
		System.out.println("end1 : " + ret.getEnd1() + ", end2 : " + ret.getEnd2());

		int start1 = ret.getStart1(), start2 = ret.getStart2();
		int end1 = ret.getEnd1(), end2 = ret.getEnd2();
		/*
		AlignedPair leftPad = new AlignedPair(s1.substring(start1-start2, start1), s2.substring(0, start2));
		AlignedPair rightPad = new AlignedPair(s1.substring(end1, end1+s2.length()-end2),
				s2.substring(end2, s2.length()));

		ret = ret.addLeft(leftPad);
		ret = ret.addRight(rightPad);
		ret.setStart1(start1-start2);
		ret.setStart2(0);
		ret.setEnd1(end1 + (s2.length()-end2));
		ret.setEnd2(s2.length());

		ret.printStrings();
		System.out.println("start1 : " + ret.getStart1());
		System.out.println("start2 : " + ret.getStart2());
		System.out.println("end1 : " + ret.getEnd1());
		System.out.println("end2 : " + ret.getEnd2());
		 */


	}
}
