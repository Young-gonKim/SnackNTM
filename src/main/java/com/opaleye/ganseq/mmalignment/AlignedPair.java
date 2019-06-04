package com.opaleye.ganseq.mmalignment;

public class AlignedPair {
	private String alignedString1 = null;
	private String alignedString2 = null;
	private int start1 = 0;
	private int start2 = 0;
	
	public AlignedPair(String alignedString1, String alignedString2) {
		super();
		this.alignedString1 = alignedString1;
		this.alignedString2 = alignedString2;
	}
	
	public String getAlignedString1() {
		return alignedString1;
	}
	public String getAlignedString2() {
		return alignedString2;
	}
	
	public AlignedPair addLeft(AlignedPair ap) {
		String as1 = ap.getAlignedString1() + this.getAlignedString1();
		String as2 = ap.getAlignedString2() + this.getAlignedString2();
		AlignedPair ret = new AlignedPair(as1, as2);
		return ret;
	}
	
	public AlignedPair addRight(AlignedPair ap) {
		String as1 = this.getAlignedString1() + ap.getAlignedString1();
		String as2 = this.getAlignedString2() + ap.getAlignedString2();
		AlignedPair ret = new AlignedPair(as1, as2);
		return ret;
	}
	
	public void printStrings() {
		System.out.println(alignedString1);
		System.out.println(alignedString2);
	}

	public int getStart1() {
		return start1;
	}

	public void setStart1(int start1) {
		this.start1 = start1;
	}

	public int getStart2() {
		return start2;
	}

	public void setStart2(int start2) {
		this.start2 = start2;
	}

}
