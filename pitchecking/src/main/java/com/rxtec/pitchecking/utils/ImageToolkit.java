package com.rxtec.pitchecking.utils;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

/**
 * ͼƬ�������ࣺ<br>
 * ���ܣ�����ͼ���и�ͼ��ͼ������ת������ɫת�ڰס�����ˮӡ��ͼƬˮӡ��
 * @author Administrator
 */
public class ImageToolkit {

    /**
     * ���ֳ�����ͼƬ��ʽ
     */
    public static String IMAGE_TYPE_GIF = "gif";// ͼ�ν�����ʽ
    public static String IMAGE_TYPE_JPG = "jpg";// ������Ƭר����
    public static String IMAGE_TYPE_JPEG = "jpeg";// ������Ƭר����
    public static String IMAGE_TYPE_BMP = "bmp";// Ӣ��Bitmap��λͼ���ļ�д������Windows����ϵͳ�еı�׼ͼ���ļ���ʽ
    public static String IMAGE_TYPE_PNG = "png";// ����ֲ����ͼ��
    public static String IMAGE_TYPE_PSD = "psd";// Photoshop��ר�ø�ʽPhotoshop

    /**
     * ������ڣ����ڲ���
     * @param args
     */
    public static void main(String[] args) {
        // 1-����ͼ��
        // ����һ������������
//    	ImageToollit.scale("e:/abc.jpg", "e:/abc_scale.jpg", 2, true);//����OK
//        // �����������߶ȺͿ������
//    	ImageToollit.scale2("e:/abc.jpg", "e:/abc_scale2.jpg", 500, 300, true);//����OK

        // 2-�и�ͼ��
        // ����һ����ָ���������Ϳ���и�
    	ImageToolkit.cut("C:/imagestemp/abc.jpg", "C:/imagestemp/abc_cut.jpg", 50,50, 200, 200 );//����OK
        // ��������ָ����Ƭ������������
    	ImageToolkit.cut2("C:/imagestemp/abc.jpg", "C:/imagestemp/", 2, 2 );//����OK
        // ��������ָ����Ƭ�Ŀ�Ⱥ͸߶�
    	ImageToolkit.cut3("C:/imagestemp/abc.jpg", "C:/imagestemp/", 300, 300 );//����OK

//        // 3-ͼ������ת����
//    	ImageToollit.convert("e:/abc.jpg", "GIF", "e:/abc_convert.gif");//����OK
//
//        // 4-��ɫת�ڰף�
//    	ImageToollit.gray("e:/abc.jpg", "e:/abc_gray.jpg");//����OK
//
//        // 5-��ͼƬ�������ˮӡ��
//        // ����һ��
//    	ImageToollit.pressText("����ˮӡ����","e:/abc.jpg","e:/abc_pressText.jpg","����",Font.BOLD,Color.white,80, 0, 0, 0.5f);//����OK
//        // ��������
//    	ImageToollit.pressText2("��Ҳ��ˮӡ����", "e:/abc.jpg","e:/abc_pressText2.jpg", "����", 36, Color.white, 80, 0, 0, 0.5f);//����OK
//        
//        // 6-��ͼƬ���ͼƬˮӡ��
//    	ImageToollit.pressImage("e:/abc2.jpg", "e:/abc.jpg","e:/abc_pressImage.jpg", 0, 0, 0.5f);//����OK
    }

    /**
     * ����ͼ�񣨰��������ţ�
     * @param srcImageFile Դͼ���ļ���ַ
     * @param result ���ź��ͼ���ַ
     * @param scale ���ű���
     * @param flag ����ѡ��:true �Ŵ�; false ��С;
     */
    public final static void scale(String srcImageFile, String result,
            int scale, boolean flag) {
        try {
            BufferedImage src = ImageIO.read(new File(srcImageFile)); // �����ļ�
            int width = src.getWidth(); // �õ�Դͼ��
            int height = src.getHeight(); // �õ�Դͼ��
            if (flag) {// �Ŵ�
                width = width * scale;
                height = height * scale;
            } else {// ��С
                width = width / scale;
                height = height / scale;
            }
            Image image = src.getScaledInstance(width, height,
                    Image.SCALE_DEFAULT);
            BufferedImage tag = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // ������С���ͼ
            g.dispose();
            ImageIO.write(tag, "JPEG", new File(result));// ������ļ���
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����ͼ�񣨰��߶ȺͿ�����ţ�
     * @param srcImageFile Դͼ���ļ���ַ
     * @param result ���ź��ͼ���ַ
     * @param height ���ź�ĸ߶�
     * @param width ���ź�Ŀ��
     * @param bb ��������ʱ�Ƿ���Ҫ���ף�trueΪ����; falseΪ������;
     */
    public final static void scale2(String srcImageFile, String result, int height, int width, boolean bb) {
        try {
            double ratio = 0.0; // ���ű���
            File f = new File(srcImageFile);
            BufferedImage bi = ImageIO.read(f);
            Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);
            // �������
            if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
                if (bi.getHeight() > bi.getWidth()) {
                    ratio = (new Integer(height)).doubleValue()
                            / bi.getHeight();
                } else {
                    ratio = (new Integer(width)).doubleValue() / bi.getWidth();
                }
                AffineTransformOp op = new AffineTransformOp(AffineTransform
                        .getScaleInstance(ratio, ratio), null);
                itemp = op.filter(bi, null);
            }
            if (bb) {//����
                BufferedImage image = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height);
                if (width == itemp.getWidth(null))
                    g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
                            itemp.getWidth(null), itemp.getHeight(null),
                            Color.white, null);
                else
                    g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
                            itemp.getWidth(null), itemp.getHeight(null),
                            Color.white, null);
                g.dispose();
                itemp = image;
            }
            ImageIO.write((BufferedImage) itemp, "JPEG", new File(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * ͼ���и�(��ָ���������Ϳ���и�)
     * @param srcImageFile Դͼ���ַ
     * @param result ��Ƭ���ͼ���ַ
     * @param x Ŀ����Ƭ�������X
     * @param y Ŀ����Ƭ�������Y
     * @param width Ŀ����Ƭ���
     * @param height Ŀ����Ƭ�߶�
     */
    public final static void cut(String srcImageFile, String result,
            int x, int y, int width, int height) {
        try {
            // ��ȡԴͼ��
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // Դͼ���
            int srcHeight = bi.getWidth(); // Դͼ�߶�
            if (srcWidth > 0 && srcHeight > 0) {
                Image image = bi.getScaledInstance(srcWidth, srcHeight,
                        Image.SCALE_DEFAULT);
                // �ĸ������ֱ�Ϊͼ���������Ϳ��
                // ��: CropImageFilter(int x,int y,int width,int height)
                ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
                Image img = Toolkit.getDefaultToolkit().createImage(
                        new FilteredImageSource(image.getSource(),
                                cropFilter));
                BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, width, height, null); // �����и���ͼ
                g.dispose();
                
                // ���Ϊ�ļ�
                ImageIO.write(tag, "JPEG", new File(result));
                
                ByteArrayOutputStream bs =new ByteArrayOutputStream();

                ImageOutputStream imOut =ImageIO.createImageOutputStream(bs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public final static BufferedImage cut(BufferedImage sourceImage,int x, int y, int width, int height){
        
    	BufferedImage resultImage = null;
    	int srcWidth = sourceImage.getHeight(); // Դͼ���
        int srcHeight = sourceImage.getWidth(); // Դͼ�߶�
        if (srcWidth > 0 && srcHeight > 0) {
            Image image = sourceImage.getScaledInstance(srcWidth, srcHeight,
                    Image.SCALE_DEFAULT);
            // �ĸ������ֱ�Ϊͼ���������Ϳ��
            // ��: CropImageFilter(int x,int y,int width,int height)
            ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
            Image img = Toolkit.getDefaultToolkit().createImage(
                    new FilteredImageSource(image.getSource(),
                            cropFilter));
            resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = resultImage.getGraphics();
            g.drawImage(img, 0, 0, width, height, null); // �����и���ͼ
            g.dispose();

        }
        return resultImage;
    }
    
  
    /**
     * ͼ���иָ����Ƭ��������������
     * @param srcImageFile Դͼ���ַ
     * @param descDir ��ƬĿ���ļ���
     * @param rows Ŀ����Ƭ������Ĭ��2�������Ƿ�Χ [1, 20] ֮��
     * @param cols Ŀ����Ƭ������Ĭ��2�������Ƿ�Χ [1, 20] ֮��
     */
    public final static void cut2(String srcImageFile, String descDir,
            int rows, int cols) {
        try {
            if(rows<=0||rows>20) rows = 2; // ��Ƭ����
            if(cols<=0||cols>20) cols = 2; // ��Ƭ����
            // ��ȡԴͼ��
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // Դͼ���
            int srcHeight = bi.getWidth(); // Դͼ�߶�
            if (srcWidth > 0 && srcHeight > 0) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int destWidth = srcWidth; // ÿ����Ƭ�Ŀ��
                int destHeight = srcHeight; // ÿ����Ƭ�ĸ߶�
                // ������Ƭ�Ŀ�Ⱥ͸߶�
                if (srcWidth % cols == 0) {
                    destWidth = srcWidth / cols;
                } else {
                    destWidth = (int) Math.floor(srcWidth / cols) + 1;
                }
                if (srcHeight % rows == 0) {
                    destHeight = srcHeight / rows;
                } else {
                    destHeight = (int) Math.floor(srcWidth / rows) + 1;
                }
                // ѭ��������Ƭ
                // �Ľ����뷨:�Ƿ���ö��̼߳ӿ��и��ٶ�
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // �ĸ������ֱ�Ϊͼ���������Ϳ��
                        // ��: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                                destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(
                                new FilteredImageSource(image.getSource(),
                                        cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth,
                                destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // ������С���ͼ
                        g.dispose();
                        // ���Ϊ�ļ�
                        ImageIO.write(tag, "JPEG", new File(descDir
                                + "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ͼ���иָ����Ƭ�Ŀ�Ⱥ͸߶ȣ�
     * @param srcImageFile Դͼ���ַ
     * @param descDir ��ƬĿ���ļ���
     * @param destWidth Ŀ����Ƭ��ȡ�Ĭ��200
     * @param destHeight Ŀ����Ƭ�߶ȡ�Ĭ��150
     */
    public final static void cut3(String srcImageFile, String descDir,
            int destWidth, int destHeight) {
        try {
            if(destWidth<=0) destWidth = 200; // ��Ƭ���
            if(destHeight<=0) destHeight = 150; // ��Ƭ�߶�
            // ��ȡԴͼ��
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // Դͼ���
            int srcHeight = bi.getWidth(); // Դͼ�߶�
            if (srcWidth > destWidth && srcHeight > destHeight) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int cols = 0; // ��Ƭ��������
                int rows = 0; // ��Ƭ��������
                // ������Ƭ�ĺ������������
                if (srcWidth % destWidth == 0) {
                    cols = srcWidth / destWidth;
                } else {
                    cols = (int) Math.floor(srcWidth / destWidth) + 1;
                }
                if (srcHeight % destHeight == 0) {
                    rows = srcHeight / destHeight;
                } else {
                    rows = (int) Math.floor(srcHeight / destHeight) + 1;
                }
                // ѭ��������Ƭ
                // �Ľ����뷨:�Ƿ���ö��̼߳ӿ��и��ٶ�
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // �ĸ������ֱ�Ϊͼ���������Ϳ��
                        // ��: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                                destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(
                                new FilteredImageSource(image.getSource(),
                                        cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth,
                                destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // ������С���ͼ
                        g.dispose();
                        // ���Ϊ�ļ�
                        ImageIO.write(tag, "JPEG", new File(descDir
                                + "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ͼ������ת����GIF->JPG��GIF->PNG��PNG->JPG��PNG->GIF(X)��BMP->PNG
     * @param srcImageFile Դͼ���ַ
     * @param formatName ������ʽ����ʽ���Ƶ� String����JPG��JPEG��GIF��
     * @param destImageFile Ŀ��ͼ���ַ
     */
    public final static void convert(String srcImageFile, String formatName, String destImageFile) {
        try {
            File f = new File(srcImageFile);
            f.canRead();
            f.canWrite();
            BufferedImage src = ImageIO.read(f);
            ImageIO.write(src, formatName, new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ɫתΪ�ڰ� 
     * @param srcImageFile Դͼ���ַ
     * @param destImageFile Ŀ��ͼ���ַ
     */
    public final static void gray(String srcImageFile, String destImageFile) {
        try {
            BufferedImage src = ImageIO.read(new File(srcImageFile));
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp op = new ColorConvertOp(cs, null);
            src = op.filter(src, null);
            ImageIO.write(src, "JPEG", new File(destImageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ͼƬ�������ˮӡ
     * @param pressText ˮӡ����
     * @param srcImageFile Դͼ���ַ
     * @param destImageFile Ŀ��ͼ���ַ
     * @param fontName ˮӡ����������
     * @param fontStyle ˮӡ��������ʽ
     * @param color ˮӡ��������ɫ
     * @param fontSize ˮӡ�������С
     * @param x ����ֵ
     * @param y ����ֵ
     * @param alpha ͸���ȣ�alpha �����Ƿ�Χ [0.0, 1.0] ֮�ڣ������߽�ֵ����һ����������
     */
    public final static void pressText(String pressText,
            String srcImageFile, String destImageFile, String fontName,
            int fontStyle, Color color, int fontSize,int x,
            int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            g.setColor(color);
            g.setFont(new Font(fontName, fontStyle, fontSize));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                    alpha));
            // ��ָ���������ˮӡ����
            g.drawString(pressText, (width - (getLength(pressText) * fontSize))
                    / 2 + x, (height - fontSize) / 2 + y);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));// ������ļ���
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ͼƬ�������ˮӡ
     * @param pressText ˮӡ����
     * @param srcImageFile Դͼ���ַ
     * @param destImageFile Ŀ��ͼ���ַ
     * @param fontName ��������
     * @param fontStyle ������ʽ
     * @param color ������ɫ
     * @param fontSize �����С
     * @param x ����ֵ
     * @param y ����ֵ
     * @param alpha ͸���ȣ�alpha �����Ƿ�Χ [0.0, 1.0] ֮�ڣ������߽�ֵ����һ����������
     */
    public final static void pressText2(String pressText, String srcImageFile,String destImageFile,
            String fontName, int fontStyle, Color color, int fontSize, int x,
            int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            g.setColor(color);
            g.setFont(new Font(fontName, fontStyle, fontSize));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                    alpha));
            // ��ָ���������ˮӡ����
            g.drawString(pressText, (width - (getLength(pressText) * fontSize))
                    / 2 + x, (height - fontSize) / 2 + y);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ͼƬ���ͼƬˮӡ
     * @param pressImg ˮӡͼƬ
     * @param srcImageFile Դͼ���ַ
     * @param destImageFile Ŀ��ͼ���ַ
     * @param x ����ֵ�� Ĭ�����м�
     * @param y ����ֵ�� Ĭ�����м�
     * @param alpha ͸���ȣ�alpha �����Ƿ�Χ [0.0, 1.0] ֮�ڣ������߽�ֵ����һ����������
     */
    public final static void pressImage(String pressImg, String srcImageFile,String destImageFile,
            int x, int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int wideth = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(wideth, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, wideth, height, null);
            // ˮӡ�ļ�
            Image src_biao = ImageIO.read(new File(pressImg));
            int wideth_biao = src_biao.getWidth(null);
            int height_biao = src_biao.getHeight(null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                    alpha));
            g.drawImage(src_biao, (wideth - wideth_biao) / 2,
                    (height - height_biao) / 2, wideth_biao, height_biao, null);
            // ˮӡ�ļ�����
            g.dispose();
            ImageIO.write((BufferedImage) image,  "JPEG", new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
	public static byte[] getImageBytes(BufferedImage img,String type){
		byte[] buff = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream ();
		try {
			ImageIO.write(img, type, ImageIO.createImageOutputStream(output));
			buff = output.toByteArray();
		} catch (IOException e) {
			buff = null;
			e.printStackTrace();
		}
		return buff;
	} 

    /**
     * ����text�ĳ��ȣ�һ�������������ַ���
     * @param text
     * @return
     */
    public final static int getLength(String text) {
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            if (new String(text.charAt(i) + "").getBytes().length > 1) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length / 2;
    }
}