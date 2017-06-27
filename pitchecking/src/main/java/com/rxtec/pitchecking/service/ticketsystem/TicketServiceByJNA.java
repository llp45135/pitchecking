package com.rxtec.pitchecking.service.ticketsystem;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.EASENFaceVerifyJNAEntry.SDKMethod;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class TicketServiceByJNA {

	public TicketServiceByJNA() {
		// TODO Auto-generated constructor stub
	}

	public interface SDKMethod extends Library {
		SDKMethod INSTANCE = (SDKMethod) Native.loadLibrary("C:\\afcdriver\\RSIVSRV.dll", SDKMethod.class);

		int Init(byte[] pInput, byte[] pOut);
	}

	public int initService(String pIn, String pOut) {
		byte[] path;
		int ret = -1;
		try {
			path = pIn.getBytes();

			byte[] pathBytes = new byte[path.length + 2];
			Arrays.fill(pathBytes, (byte) 0);
			System.arraycopy(path, 0, pathBytes, 0, path.length);
			ret = SDKMethod.INSTANCE.Init(pathBytes, pOut.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TicketServiceByJNA service = new TicketServiceByJNA();
		String pInStr = "RXTa" + "0JWAG-GT" + "00000000000000001111111111111111" + "20170523";
		String pOutStr = "";
//		int retVal = service.initService(pInStr, pOutStr);
		SDKMethod.INSTANCE.Init(pInStr.getBytes(), pOutStr.getBytes());
	}

}
