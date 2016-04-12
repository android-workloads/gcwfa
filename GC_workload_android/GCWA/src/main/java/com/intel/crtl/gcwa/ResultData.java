package com.intel.crtl.GCWA;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ResultData implements Parcelable {
	boolean mIsArtVm;
	long[] mThreadCompletionTime;
	long[] mBytesAllocated;
	int[] mHeapFootprint;
	float[] mGcPauseTime;
	float[] mGcTotalTime;
	String[] mGcCause;
	
	ResultData(boolean isArt, long[] completeTime, long[] bytesAllocated, int[] heapFootprint,
			ArrayList<Float> gcPauseTime, ArrayList<Float> gcTotalTime, ArrayList<String> gcCause) {
		mIsArtVm = isArt;
		int len = completeTime.length;
		mThreadCompletionTime = new long[len];
		for (int i = 0; i < len; i++)
			mThreadCompletionTime[i] = completeTime[i];
		completeTime = null;
		
		len = bytesAllocated.length;
		mBytesAllocated = new long[len];
		mHeapFootprint = new int[len];
		for (int i = 0; i < len; i++) {
			mBytesAllocated[i] = bytesAllocated[i];
			mHeapFootprint[i] = heapFootprint[i];
		}
		
		len = gcPauseTime.size();
		mGcPauseTime = new float[len];
		mGcTotalTime = new float[len];
		mGcCause = new String[len];
		for (int i = 0; i < len; i++) {
			mGcPauseTime[i] = gcPauseTime.get(i).floatValue();
			mGcTotalTime[i] = gcTotalTime.get(i).floatValue();
			mGcCause[i] = gcCause.get(i);
		}
	}

	ResultData(Parcel source) {
		mIsArtVm = source.readInt() == 1;		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}

	public static final Parcelable.Creator<ResultData> CREATOR = new Parcelable.Creator<ResultData>() {

		@Override
		public ResultData createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new ResultData(source);
		}

		@Override
		public ResultData[] newArray(int size) {
			// TODO Auto-generated method stub
			return null;
		}
	};
}
