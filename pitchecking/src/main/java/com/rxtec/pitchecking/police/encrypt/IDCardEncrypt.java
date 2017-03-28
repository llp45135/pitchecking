package com.rxtec.pitchecking.police.encrypt;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class IDCardEncrypt {
	private Logger log = LoggerFactory.getLogger("IDCardEncrypt");
	private static IDCardEncrypt _instance;
	private JNative jnativeAfcScyInit = null;
	private JNative jnativeAfcEncrypt = null;
	private JNative jnativeAfcScyClearup = null;

	public static synchronized IDCardEncrypt getInstance() {
		if (_instance == null) {
			_instance = new IDCardEncrypt();
		}
		return _instance;
	}

	private IDCardEncrypt() {
		// TODO Auto-generated constructor stub
		JNative.setLoggingEnabled(true);
		try {
			jnativeAfcScyInit = new JNative("afc-encrypt.dll", "AfcScyInit");
			jnativeAfcEncrypt = new JNative("afc-encrypt.dll", "AfcEncrypt");
			jnativeAfcScyClearup = new JNative("afc-encrypt.dll", "AfcScyClearup");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("Init afc-encrypt.dll Failed!", e);
		}
	}

	/**
	 * 加密接口初始化
	 * 
	 * @return
	 */
	public String AfcScyInit(String password, int pwdLength) {
		String retval = "";
		try {
			int i = 0;
			jnativeAfcScyInit.setRetVal(Type.INT);
			jnativeAfcScyInit.setParameter(i++, password);
			jnativeAfcScyInit.setParameter(i++, pwdLength);
			jnativeAfcScyInit.invoke();

			retval = jnativeAfcScyInit.getRetVal();
			log.debug("AfcScyInit: retval==" + retval);// 获取返回值
			if (retval.equals("0")) {
				log.debug("AfcScyInit 成功!");
			}
		} catch (NativeException e) {
			log.error("IDCardEncrypt  AfcScyInit:" + e);
		} catch (IllegalAccessException e) {
			log.error("IDCardEncrypt  AfcScyInit:" + e);
		} catch (Exception e) {
			log.error("IDCardEncrypt  AfcScyInit:" + e);
		}
		return retval;
	}

	/**
	 * 使用 UKey 内的加密公钥加密字符串
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public String AfcEncrypt(String idNo) {
		String retval = "";
		String encryptIdNo = "";
		Pointer encryptIdNoPointer = null;
		try {
			encryptIdNoPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(256));
			int i = 0;
			jnativeAfcEncrypt.setRetVal(Type.INT);
			jnativeAfcEncrypt.setParameter(i++, idNo);
			jnativeAfcEncrypt.setParameter(i++, encryptIdNoPointer);
			jnativeAfcEncrypt.invoke();

			retval = jnativeAfcEncrypt.getRetVal();
			log.debug("AfcEncrypt: retval==" + retval);// 获取返回值
			if (retval.equals("0")) {
				log.debug("AfcEncrypt 成功!");
				encryptIdNo = encryptIdNoPointer.getAsString();
				log.debug("ecryptIdNo.length==" + encryptIdNo.length() + ",encryptIdNo==" + encryptIdNo);
			}
		} catch (NativeException e) {
			log.error("IDCardEncrypt  AfcEncrypt:" + e);
		} catch (IllegalAccessException e) {
			log.error("IDCardEncrypt  AfcEncrypt:" + e);
		} catch (Exception e) {
			log.error("IDCardEncrypt  AfcEncrypt:" + e);
		}
		return encryptIdNo;
	}

	/**
	 * 加密接口清理
	 * 
	 * @return
	 */
	public String AfcScyClearup() {
		String retval = "";
		try {
			int i = 0;
			jnativeAfcScyClearup.setRetVal(Type.INT);
			jnativeAfcScyClearup.invoke();

			retval = jnativeAfcScyClearup.getRetVal();
			log.debug("AfcScyClearup: retval==" + retval);// 获取返回值
			if (retval.equals("0")) {
				log.debug("AfcScyClearup 成功!");
			}
		} catch (NativeException e) {
			log.error("IDCardEncrypt  AfcScyClearup:" + e);
		} catch (IllegalAccessException e) {
			log.error("IDCardEncrypt  AfcScyClearup:" + e);
		} catch (Exception e) {
			log.error("IDCardEncrypt  AfcScyClearup:" + e);
		}
		return retval;
	}

	public static void main(String[] args) throws ParseException {
		IDCardEncrypt idCardEncrypt = IDCardEncrypt.getInstance();
		String ret = idCardEncrypt.AfcScyInit("12345678", 8);
		String encryptIdNo = "";
		if (ret.equals("0")) {
			encryptIdNo=idCardEncrypt.AfcEncrypt("520203197912141118");
		}
		
		idCardEncrypt.AfcScyClearup();
	}
}
