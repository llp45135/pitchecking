package com.rxtec.pitchecking.gui;

import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.SwingConstants;

import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceData;
import com.rxtec.pitchecking.picheckingservice.FaceTrackingService;
import com.rxtec.pitchecking.picheckingservice.IDCard;

public class FaceCheckFrame extends JFrame {

	private JPanel contentPane;
	private JButton showBmp;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FaceCheckFrame frame = new FaceCheckFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FaceCheckFrame() {
		initGui();
	}
	
	public void setIdcardBmp(ImageIcon icon){
		this.showBmp.setIcon(icon);
	}
	
	
	private JPanel videoPanel;
	

	public JPanel getVideoPanel() {
		return videoPanel;
	}


	private JLabel resultLabel;

	
	private void initGui(){
		videoPanel = new JPanel();
		videoPanel.setSize(320, 480);
		resultLabel = new JLabel("0");
		resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
		resultLabel.setFont(new Font("Times New Roman",Font.ITALIC,24));
		Panel p = new Panel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(videoPanel);
		p.add(resultLabel);
		getContentPane().add(p);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setBounds(10,10,500,500);
		//this.pack();
		this.setVisible(true);
		
	}
	

	
	public void setResultValue(String v){
		resultLabel.setText(v);
	}
	
	
	
	public void simulateIDCardReader(){
		JRootPane rp= this.getRootPane();

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5,0);

		InputMap inputMap = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); inputMap.put(stroke, KeyEvent.VK_F5); rp.getActionMap().put(KeyEvent.VK_F5, new AbstractAction() {

		    public void actionPerformed(ActionEvent e) {

		    	try {
					simulateCheckFace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    }

		});
	}
	
	
	private void simulateCheckFace() throws InterruptedException{
		FaceTrackingService.getInstance().beginCheckingFace(createIDCard());
		while(true){
			FaceData fd = FaceCheckingService.getInstance().getCheckedFaceData();
			float result = fd.getFaceCheckResult();
			if(result>=0.7){
				setResultValue("验证通过" + String.format("%<2.2f", result));
				break;
			}else {
				setResultValue("验证不通过" + String.format("%<2.2f", result));
			}
		}

	}
	
	
	private  IDCard createIDCard(){
		IDCard card = new IDCard();
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File("C:/DCZ/20160412/llp.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		card.setCardImage(bi);
		return card;
	}
}
