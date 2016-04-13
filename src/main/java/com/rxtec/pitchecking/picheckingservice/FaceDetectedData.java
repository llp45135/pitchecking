package com.rxtec.pitchecking.picheckingservice;

public class FaceDetectedData {
	private int id;
	private int x;
	private int y;
	private int width;
	private int height;
	private float confidence;
	private int xFirstEye;
	private int yFirstEye;
	private float firstConfidence;
	private int xSecondEye;
	private int ySecondEye;
	private float secondConfidence;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public float getConfidence() {
		return confidence;
	}
	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}
	public int getxFirstEye() {
		return xFirstEye;
	}
	public void setxFirstEye(int xFirstEye) {
		this.xFirstEye = xFirstEye;
	}
	public int getyFirstEye() {
		return yFirstEye;
	}
	public void setyFirstEye(int yFirstEye) {
		this.yFirstEye = yFirstEye;
	}
	public float getFirstConfidence() {
		return firstConfidence;
	}
	public void setFirstConfidence(float firstConfidence) {
		this.firstConfidence = firstConfidence;
	}
	public int getxSecondEye() {
		return xSecondEye;
	}
	public void setxSecondEye(int xSecondEye) {
		this.xSecondEye = xSecondEye;
	}
	public int getySecondEye() {
		return ySecondEye;
	}
	public void setySecondEye(int ySecondEye) {
		this.ySecondEye = ySecondEye;
	}
	public float getSecondConfidence() {
		return secondConfidence;
	}
	public void setSecondConfidence(float secondConfidence) {
		this.secondConfidence = secondConfidence;
	}
	
	
	private boolean pass;						// 综合品质是否合格
	private boolean hasface;					// 脸数（非单人正脸照）
	private boolean eyesopen;					// 眼睛睁开
	private boolean faceblur;					// 脸部模糊（图像模糊）
	private boolean hotspots;					// 脸部高光
	private boolean lightuniform;				// 脸部曝光均匀性（光照不均匀）

	private boolean expression;					// 表情不自然

	private boolean facefrontal;				// 正脸（姿态不正）
	private boolean eyesfrontal;				// 正眼：眼睛朝前看（目光未视镜头）
    
	private boolean headhigh;					// 头整体偏上
	private boolean headlow;					// 头整体偏下
	private boolean headleft;					// 头整体偏左
	private boolean headright;					// 头整体偏右
	private boolean largehead;					// 头（/脸）偏大
	private boolean smallhead;					// 头（/脸）偏小
	private boolean wearsglasses;				// 是否戴眼镜	
	public boolean isPass() {
		return pass;
	}
	public void setPass(boolean pass) {
		this.pass = pass;
	}
	public boolean isHasface() {
		return hasface;
	}
	public void setHasface(boolean hasface) {
		this.hasface = hasface;
	}
	public boolean isEyesopen() {
		return eyesopen;
	}
	public void setEyesopen(boolean eyesopen) {
		this.eyesopen = eyesopen;
	}
	public boolean isFaceblur() {
		return faceblur;
	}
	public void setFaceblur(boolean faceblur) {
		this.faceblur = faceblur;
	}
	public boolean isHotspots() {
		return hotspots;
	}
	public void setHotspots(boolean hotspots) {
		this.hotspots = hotspots;
	}
	public boolean isLightuniform() {
		return lightuniform;
	}
	public void setLightuniform(boolean lightuniform) {
		this.lightuniform = lightuniform;
	}
	public boolean isFacefrontal() {
		return facefrontal;
	}
	public void setFacefrontal(boolean facefrontal) {
		this.facefrontal = facefrontal;
	}
	public boolean isEyesfrontal() {
		return eyesfrontal;
	}
	public void setEyesfrontal(boolean eyesfrontal) {
		this.eyesfrontal = eyesfrontal;
	}
	public boolean isHeadhigh() {
		return headhigh;
	}
	public void setHeadhigh(boolean headhigh) {
		this.headhigh = headhigh;
	}
	public boolean isHeadlow() {
		return headlow;
	}
	public void setHeadlow(boolean headlow) {
		this.headlow = headlow;
	}
	public boolean isHeadleft() {
		return headleft;
	}
	public void setHeadleft(boolean headleft) {
		this.headleft = headleft;
	}
	public boolean isHeadright() {
		return headright;
	}
	public void setHeadright(boolean headright) {
		this.headright = headright;
	}
	public boolean isLargehead() {
		return largehead;
	}
	public void setLargehead(boolean largehead) {
		this.largehead = largehead;
	}
	public boolean isSmallhead() {
		return smallhead;
	}
	public void setSmallhead(boolean smallhead) {
		this.smallhead = smallhead;
	}
	public boolean isWearsglasses() {
		return wearsglasses;
	}
	public void setWearsglasses(boolean wearsglasses) {
		this.wearsglasses = wearsglasses;
	}

	public boolean isExpression() {
		return expression;
	}
	public void setExpression(boolean expression) {
		this.expression = expression;
	}


	
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Detected face ");
		sb.append("人脸图像综合质量：");
		sb.append(pass);
		sb.append(" 是否戴眼镜：");
		sb.append(wearsglasses);
		return sb.toString();
	}
	
}
