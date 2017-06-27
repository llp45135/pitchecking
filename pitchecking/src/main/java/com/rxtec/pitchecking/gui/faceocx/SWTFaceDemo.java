package com.rxtec.pitchecking.gui.faceocx;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SWTFaceDemo {

	public SWTFaceDemo() {
		// TODO Auto-generated constructor stub
	}

	public void open() {
		Display display = Display.getDefault();
		Shell shell = new Shell();
		shell.setText("Auto Fit");
		shell.setSize(640, 480);
		shell.setLayout(new FillLayout());

		// 加载第一个ocx窗口
		OleFrame oleFrame = new OleFrame(shell, SWT.NONE);
		oleFrame.setSize(640, 480);

		// tesoClsid ： 注册表中已注册的控件PROGID，也就是name
		String tesoClsid = "{1FB831AF-8A7C-4B86-8634-F491B8562997}";
		// String tesoClsid = "FACEVIDEOCONTROL.FaceVideoControlCtrl.1";

		OleControlSite oleControlSite = new OleControlSite(oleFrame, SWT.NONE, tesoClsid);
		oleControlSite.setSize(640, 480);
		oleControlSite.doVerb(OLE.OLEIVERB_SHOW);

		OleAutomation oleAutomation = new OleAutomation(oleControlSite);

		// //获取Method Name的ID，Method Name为ActiveX中具体的方法名
		// int[] regspid = oleAutomation.getIDsOfNames(new String[] { "Init" });
		// int dispIdMember = regspid[0];
		// //方法调用
		// oleAutomation.invoke(dispIdMember);

		// 获取Method Name的ID，Method Name为ActiveX中具体的方法名
		int[] regspid = oleAutomation.getIDsOfNames(new String[] { "Init" });
		int dispIdMember = regspid[0];
		// 设置方法的具体参数。Variant数组的长度为Method Name方法参数的个数
		// 假设有四个参数
		Variant[] rgvarg = new Variant[1];
		rgvarg[0] = new Variant(0);
		// 方法调用
		oleAutomation.invoke(dispIdMember, rgvarg);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new SWTFaceDemo().open();
	}

}
