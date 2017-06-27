package com.rxtec.pitchecking.picheckingservice.realsense;

import intel.rssdk.PXCMFaceData;

public class SortedFace implements Comparable<SortedFace> {
	public PXCMFaceData.Face face;
	private int quality = 0;

	public SortedFace(PXCMFaceData.Face face, int quality) {
		this.face = face;
		this.setQuality(quality);

	}

	@Override
	public int compareTo(SortedFace o) {
		if (getQuality() < o.getQuality())
			return 0;
		else
			return 1;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

}
