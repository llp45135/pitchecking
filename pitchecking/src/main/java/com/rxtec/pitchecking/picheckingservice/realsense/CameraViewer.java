package com.rxtec.pitchecking.picheckingservice.realsense;

import intel.rssdk.*;
import java.lang.System.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;

public class CameraViewer {
	static int cWidth = 640;
	static int cHeight = 480;
	static int dWidth, dHeight;
	static boolean exit = false;

	private static void PrintConnectedDevices() {
		PXCMSession session = PXCMSession.CreateInstance();
		PXCMSession.ImplDesc desc = new PXCMSession.ImplDesc();
		PXCMSession.ImplDesc outDesc = new PXCMSession.ImplDesc();
		desc.group = EnumSet.of(PXCMSession.ImplGroup.IMPL_GROUP_SENSOR);
		desc.subgroup = EnumSet.of(PXCMSession.ImplSubgroup.IMPL_SUBGROUP_VIDEO_CAPTURE);

		int numDevices = 0;
		for (int i = 0;; i++) {
			if (session.QueryImpl(desc, i, outDesc).isError())
				break;

			PXCMCapture capture = new PXCMCapture();
			if (session.CreateImpl(outDesc, capture).isError())
				continue;

			for (int j = 0;; j++) {
				PXCMCapture.DeviceInfo info = new PXCMCapture.DeviceInfo();
				if (capture.QueryDeviceInfo(j, info).isError())
					break;

				System.out.println(info.name);
				numDevices++;
			}
		}

		System.out.println("Found " + numDevices + " devices");
	}

	public static void main(String s[]) {
		PrintConnectedDevices();

		PXCMSenseManager senseMgr = PXCMSenseManager.CreateInstance();

		pxcmStatus sts = senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, cWidth, cHeight);
		sts = senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_DEPTH);

		sts = senseMgr.Init();

		System.out.println(sts);

		PXCMCapture.Device device = senseMgr.QueryCaptureManager().QueryDevice();
		PXCMCapture.Device.StreamProfileSet profiles = new PXCMCapture.Device.StreamProfileSet();
		device.QueryStreamProfileSet(profiles);

		dWidth = profiles.depth.imageInfo.width;
		dHeight = profiles.depth.imageInfo.height;

		Listener listener = new Listener();

		CameraViewer c_raw = new CameraViewer();
		DrawFrame c_df = new DrawFrame(cWidth, cHeight);
		JFrame cframe = new JFrame("Intel(R) RealSense(TM) SDK - Color Stream");
		cframe.addWindowListener(listener);
		cframe.setSize(cWidth, cHeight);
		cframe.add(c_df);
		cframe.setVisible(true);

		CameraViewer d_raw = new CameraViewer();
		DrawFrame d_df = new DrawFrame(dWidth, dHeight);
		JFrame dframe = new JFrame("Intel(R) RealSense(TM) SDK - Depth Stream");
		dframe.addWindowListener(listener);
		dframe.setSize(dWidth, dHeight);
		dframe.add(d_df);
		dframe.setVisible(true);

		if (sts == pxcmStatus.PXCM_STATUS_NO_ERROR) {
			while (listener.exit == false) {
				sts = senseMgr.AcquireFrame(true);

				if (sts == pxcmStatus.PXCM_STATUS_NO_ERROR) {
					PXCMCapture.Sample sample = senseMgr.QuerySample();

					if (sample.color != null) {
						PXCMImage.ImageData cData = new PXCMImage.ImageData();
						sts = sample.color.AcquireAccess(PXCMImage.Access.ACCESS_READ,
								PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32, cData);
						if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
							System.out.println("Failed to AcquireAccess of color image data");
							System.exit(3);
						}

						int cBuff[] = new int[cData.pitches[0] / 4 * cHeight];

						cData.ToIntArray(0, cBuff);
						c_df.image.setRGB(0, 0, cWidth, cHeight, cBuff, 0, cData.pitches[0] / 4);
						c_df.repaint();
						sts = sample.color.ReleaseAccess(cData);

						if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
							System.out.println("Failed to ReleaseAccess of color image data");
							System.exit(3);
						}
					}

					if (sample.depth != null) {
						PXCMImage.ImageData dData = new PXCMImage.ImageData();
						sample.depth.AcquireAccess(PXCMImage.Access.ACCESS_READ,
								PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32, dData);
						if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
							System.out.println("Failed to AcquireAccess of depth image data");
							System.exit(3);
						}

						int dBuff[] = new int[dData.pitches[0] / 4 * dHeight];
						dData.ToIntArray(0, dBuff);
						d_df.image.setRGB(0, 0, dWidth, dHeight, dBuff, 0, dData.pitches[0] / 4);
						d_df.repaint();
						sts = sample.depth.ReleaseAccess(dData);
						if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
							System.out.println("Failed to ReleaseAccess of depth image data");
							System.exit(3);
						}
					}
				} else {
					System.out.println("Failed to acquire frame");
				}

				senseMgr.ReleaseFrame();
			}

			senseMgr.Close();
			System.out.println("Done streaming");
		} else {
			System.out.println("Failed to initialize");
		}

		cframe.dispose();
		dframe.dispose();
	}
}

class Listener extends WindowAdapter {
	public boolean exit = false;

	@Override
	public void windowClosing(WindowEvent e) {
		exit = true;
	}
}

class DrawFrame extends Component {
	public BufferedImage image;

	public DrawFrame(int width, int height) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	public void paint(Graphics g) {
		((Graphics2D) g).drawImage(image, 0, 0, null);
	}
}