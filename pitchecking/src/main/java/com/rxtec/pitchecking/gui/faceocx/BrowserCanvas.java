package com.rxtec.pitchecking.gui.faceocx;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 人脸采集控件的SWT图形控件
 * @author ZhaoLin
 *
 */
public class BrowserCanvas extends Canvas {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Thread swtThread;
    private Button swtBrowser;
    private int videoWidth = 720;
    private int videoHeight = 1280;
    private int cameraNo = 0;
    
    public BrowserCanvas(int cameraNo,int width,int height){
    	this.cameraNo = cameraNo;
    	this.videoWidth = width;
    	this.videoHeight = height;
    }
 
    /**
     * Connect this canvas to a SWT shell with a Browser component and starts a
     * background thread to handle SWT events. This method waits until the
     * browser component is ready.
     */
    public void initFaceVideoControl() {
        if (this.swtThread == null) {
            final Canvas canvas = this;
            this.swtThread = new Thread() {
 
                @Override
                public void run() {
                    try {
                    	// swt的初始化
						Display display = new Display();
						Shell shell = SWT_AWT.new_Shell(display, canvas);
						shell.setLayout(new org.eclipse.swt.layout.RowLayout());
						shell.setSize(videoWidth, videoHeight);
						// 加载第一个ocx窗口
						OleFrame oleFrame = new OleFrame(shell, SWT.NONE);
						oleFrame.setSize(videoWidth, videoHeight);
						
						
						// tesoClsid ： 注册表中已注册的控件PROGID，也就是name
//						String tesoClsid = "{1FB831AF-8A7C-4B86-8634-F491B8562997}";
						String tesoClsid = "FACEVIDEOCONTROL.FaceVideoControlCtrl.1";
						
						
						
						OleControlSite oleControlSite = new OleControlSite(oleFrame, SWT.NONE, tesoClsid);
						oleControlSite.doVerb(OLE.OLEIVERB_SHOW);
						
						OleAutomation oleAutomation = new OleAutomation(oleControlSite);
												
//						//获取Method Name的ID，Method Name为ActiveX中具体的方法名 
//						int[] regspid = oleAutomation.getIDsOfNames(new String[] { "Init" }); 
//						int dispIdMember = regspid[0]; 
//						//方法调用 
//						oleAutomation.invoke(dispIdMember); 
						
						//获取Method Name的ID，Method Name为ActiveX中具体的方法名 
						int[] regspid = oleAutomation.getIDsOfNames(new String[] { "Init" }); 
						int dispIdMember = regspid[0]; 
						//设置方法的具体参数。Variant数组的长度为Method Name方法参数的个数 
						//假设有四个参数 
						Variant[] rgvarg = new Variant[1]; 
						rgvarg[0] = new Variant(cameraNo); 
						//方法调用 
						oleAutomation.invoke(dispIdMember, rgvarg); 
						
//						shell.pack();
						shell.open();
                        while (!isInterrupted() && !shell.isDisposed()) {
                            if (!display.readAndDispatch()) {
                                display.sleep();
                            }
                        }
                        shell.dispose();
                        display.dispose();
                    } catch (Exception e) {
                    	System.err.println(e.getMessage());
                        interrupt();
                    }
                }
            };
            this.swtThread.start();
        }
 
        // Wait for the Browser instance to become ready
//        synchronized (this.swtThread) {
//            while (this.swtBrowser == null) {
//                try {
//                    this.swtThread.wait(100);
//                } catch (InterruptedException e) {
//                    this.swtBrowser = null;
//                    this.swtThread = null;
//                    break;
//                }
//            }
//        }
    }
 
    /**
     * Returns the Browser instance. Will return "null" before "connect()" or
     * after "disconnect()" has been called.
     */
    public Button getBrowser() {
        return this.swtBrowser;
    }
 
    /**
     * Stops the swt background thread.
     */
    public void disconnect() {
        if (swtThread != null) {
            swtBrowser = null;
            swtThread.interrupt();
            swtThread = null;
        }
    }
 
    /**
     * Ensures that the SWT background thread is stopped if this canvas is
     * removed from it's parent component (e.g. because the frame has been
     * disposed).
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        disconnect();
    }
 
    /**
     * Opens a new JFrame with BrowserCanvas in it
     */
    public static void main(String[] args) {
        // Required for Linux systems
//        System.setProperty("sun.awt.xembedserver", "true");
 
        // Create container canvas. Note that the browser
        // widget will not be created, yet.
        final BrowserCanvas browserCanvas = new BrowserCanvas(0,720,1280);
        browserCanvas.setPreferredSize(new Dimension(800, 600));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(browserCanvas, BorderLayout.CENTER);
 
        // Add container to Frame
        final JFrame frame = new JFrame("My SWT Browser");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
 
        // This is VERY important: Make the frame visible BEFORE
        // connecting the SWT Shell and starting the event loop!
        frame.setVisible(true);
        browserCanvas.initFaceVideoControl();
 
        // Now we can open a webpage, but remember that we have
        // to use the SWT thread for this.
//        browserCanvas.getBrowser().getDisplay().asyncExec(new Runnable() {
// 
//            @Override
//            public void run() {
//                browserCanvas.getBrowser().setText("aaaaaa");
//            }
//        });
    }
}
