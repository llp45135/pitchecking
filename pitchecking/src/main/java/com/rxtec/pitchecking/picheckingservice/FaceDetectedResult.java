package com.rxtec.pitchecking.picheckingservice;

public class FaceDetectedResult {
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
	
	private int faceType; 	// 人脸类型 0:正面 * 1:左侧面 2:右侧面 
	private int xleft; 		// 左眼在原始图像中的位置 
	private int yleft;
	private int xright; 		// 右眼在原始图像中的位置 
	private int yright; 
	private int faceLeft; 	// 人脸左边缘的X坐标，-1表示未知 
	private int faceRight; 	// 人脸右边缘的X坐标，-1表示未知 
	private int headLeft; 	// 人头左边缘的X坐标，-1表示未知 
	private int headRight; 	// 人头右边缘的X坐标，-1表示未知 
	private int headTop; 	//  头顶的Y坐标，-1表示未知 
	private int chinPos; 	// 下巴的Y坐标，-1表示未知 
	private double skewAngle; // 平面内偏斜角度 -181表示未知 };
 	
    // 照片属性
	private int faceCount;					// 照片人脸数
   
    // (-) 偏左，偏上 (+) 偏右，偏下
	private float faceRoll;					// 脸旋转角度
	
	private float faceYaw;					// 脸侧转角度
	private float headPitch;					// 头俯昂角度

	private float faceUniform;				// 脸部光线均匀性
	private float faceHotspots;				// 脸部高光
	private float faceBlur;					// 脸部模糊度
	private float eyesOpen;					// 眼睛开闭
	private float eyesFrontal;				// 眼睛正视前方
	private float faceExpression;			// 表情自然
	private float eyesGlasses;				// 是否戴眼镜

	
	private boolean pass = false;						// 综合品质是否合格
	private boolean hasface = false;					// 脸数（非单人正脸照）
	private boolean eyesopen = false;					// 眼睛睁开
	private boolean faceblur = false;					// 脸部模糊（图像模糊）
	private boolean hotspots = false;					// 脸部高光
	private boolean lightuniform = false;				// 脸部曝光均匀性（光照不均匀）

	private boolean expression = false;					// 表情不自然

	private boolean facefrontal = false;				// 正脸（姿态不正）
	private boolean eyesfrontal = false;				// 正眼：眼睛朝前看（目光未视镜头）
    
	private boolean headhigh = false;					// 头整体偏上
	private boolean headlow = false;					// 头整体偏下
	private boolean headleft = false;					// 头整体偏左
	private boolean headright = false;					// 头整体偏右
	private boolean largehead = false;					// 头（/脸）偏大
	private boolean smallhead = false;					// 头（/脸）偏小
	private boolean wearsglasses = false;				// 是否戴眼镜	
	
	
	public FaceBounds getFaceBounds(){
		FaceBounds b = new FaceBounds();
		b.setX(this.headLeft);
		b.setY(this.headTop);
		b.setWidth(this.headRight - this.headLeft);
		b.setHeight(this.chinPos - this.headTop);
		return b;
	}
	
	public FaceBounds getFaceBoundsFromLocation(){
		FaceBounds b = new FaceBounds();
		b.setX(this.headLeft);
		b.setY(this.headTop);
		b.setWidth(this.headRight - this.headLeft);
		b.setHeight(this.chinPos - this.headTop);
		return b;
	}
	
	public int getFaceCount() {
		return faceCount;
	}
	public void setFaceCount(int faceCount) {
		this.faceCount = faceCount;
	}
	public float getFaceRoll() {
		return faceRoll;
	}
	public void setFaceRoll(float faceRoll) {
		this.faceRoll = faceRoll;
	}
	public float getFaceYaw() {
		return faceYaw;
	}
	public void setFaceYaw(float faceYaw) {
		this.faceYaw = faceYaw;
	}
	public float getHeadPitch() {
		return headPitch;
	}
	public void setHeadPitch(float headPitch) {
		this.headPitch = headPitch;
	}
	public float getFaceUniform() {
		return faceUniform;
	}
	public void setFaceUniform(float faceUniform) {
		this.faceUniform = faceUniform;
	}
	public float getFaceHotspots() {
		return faceHotspots;
	}
	public void setFaceHotspots(float faceHotspots) {
		this.faceHotspots = faceHotspots;
	}
	public float getFaceBlur() {
		return faceBlur;
	}
	public void setFaceBlur(float faceBlur) {
		this.faceBlur = faceBlur;
	}
	public float getEyesOpen() {
		return eyesOpen;
	}
	public void setEyesOpen(float eyesOpen) {
		this.eyesOpen = eyesOpen;
	}
	public float getEyesFrontal() {
		return eyesFrontal;
	}
	public void setEyesFrontal(float eyesFrontal) {
		this.eyesFrontal = eyesFrontal;
	}
	public float getFaceExpression() {
		return faceExpression;
	}
	public void setFaceExpression(float faceExpression) {
		this.faceExpression = faceExpression;
	}
	public float getEyesGlasses() {
		return eyesGlasses;
	}
	public void setEyesGlasses(float eyesGlasses) {
		this.eyesGlasses = eyesGlasses;
	}






	public int getFaceType() {
		return faceType;
	}
	public void setFaceType(int faceType) {
		this.faceType = faceType;
	}
	public int getXleft() {
		return xleft;
	}
	public void setXleft(int xleft) {
		this.xleft = xleft;
	}
	public int getYleft() {
		return yleft;
	}
	public void setYleft(int yleft) {
		this.yleft = yleft;
	}
	public int getXright() {
		return xright;
	}
	public void setXright(int xright) {
		this.xright = xright;
	}
	public int getYright() {
		return yright;
	}
	public void setYright(int yright) {
		this.yright = yright;
	}
	public int getFaceLeft() {
		return faceLeft;
	}
	public void setFaceLeft(int faceLeft) {
		this.faceLeft = faceLeft;
	}
	public int getFaceRight() {
		return faceRight;
	}
	public void setFaceRight(int faceRight) {
		this.faceRight = faceRight;
	}
	public int getHeadLeft() {
		return headLeft;
	}
	public void setHeadLeft(int headLeft) {
		this.headLeft = headLeft;
	}
	public int getHeadRight() {
		return headRight;
	}
	public void setHeadRight(int headRight) {
		this.headRight = headRight;
	}
	public int getHeadTop() {
		return headTop;
	}
	public void setHeadTop(int headTop) {
		this.headTop = headTop;
	}
	public int getChinPos() {
		return chinPos;
	}
	public void setChinPos(int chinPos) {
		this.chinPos = chinPos;
	}
	public double getSkewAngle() {
		return skewAngle;
	}
	public void setSkewAngle(double skewAngle) {
		this.skewAngle = skewAngle;
	}
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
	
	private byte[] frameImageBytes;
	private byte[] extractImageBytes;


	
	
	public byte[] getExtractImageBytes() {
		return extractImageBytes;
	}

	public void setExtractImageBytes(byte[] extractImageBytes) {
		this.extractImageBytes = extractImageBytes;
	}

	public byte[] getFrameImageBytes() {
		return frameImageBytes;
	}

	public void setFrameImageBytes(byte[] imageBytes) {
		this.frameImageBytes = imageBytes;
	}

//	private float faceRoll;					// 脸旋转角度
//	
//	private float faceYaw;					// 脸侧转角度
//	private float headPitch;					// 头俯昂角度
//
//	private float faceUniform;				// 脸部光线均匀性
//	private float faceHotspots;				// 脸部高光
//	private float faceBlur;					// 脸部模糊度
//	private float eyesOpen;					// 眼睛开闭
//	private float eyesFrontal;				// 眼睛正视前方
//	private float faceExpression;			// 表情自然
//	private float eyesGlasses;				// 是否戴眼镜

	
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Detected face ");
		sb.append(this.hashCode());
		sb.append("  是否检测到人脸：");
		sb.append(this.isHasface());
		sb.append("  是否面部模糊：");
		sb.append(this.isFaceblur());
		sb.append("  是否正脸：");
		sb.append(this.isFacefrontal());
		sb.append("  是否眼睛正视前方：");
		sb.append(this.isEyesfrontal());
		sb.append("  是否闭眼：");
		sb.append(!this.isEyesopen());
		sb.append("  是否表情自然：");
		sb.append(this.isExpression());
		return sb.toString();
	}
	
}
