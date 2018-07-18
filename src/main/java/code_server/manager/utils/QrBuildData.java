/**
 * 
 */
package code_server.manager.utils;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;

import code_server.manager.exception.MFCException;

/**
 * @author QinYuHui
 * 2018年7月5日
 */
@Component
public class QrBuildData {
	/**
	 * <Strong>Description：</Strong>二维码生成方案，需要手机APP补充第209位票状态，214到217位
	 *  入站时间，291到294位二维码生成时间，295到359位手机APP签名</Strong>
	 * 
	 *  支付帐号  String  payAccount(payAct)
	 *  关联用户信息的ID  String idNo
	 *  卡类型/用户类型        byte userType
	 *  票价金额                    float ticketPrice
	 *  创建时间 （单程票购票时候生成，储值票过闸时候生成）  Date createTime
	 *  用户票流水号（16进制8位）  long id
	 *  用户票状态                              byte ticketStatus
     *  进站站点编号                          String inStationNo(inStationNum)
     *  通过Linux命令获取              String wifiMac
     *  CPU型号+AndroidId+Serial   String phoneInfo
     *  
	 * @param sjt 单程票
	 * @return String
	 */
	public String buildQRData(
			String payAct
			,String idNo
			,byte userType
			,float ticketPrice
			,Date createTime
			,long id
			,byte ticketStatus
			,String inStationNum
			,String wifiMac
			,String phoneInfo
			) {
		JsonArray data = new JsonArray();
		// 1 二维码版本 当前版本为0x81 1位
		data.addAll(hexStringToByte("81"));
		// 2 二维码数据长度 第3～17字段的总长度 2位
		JsonArray length = hexStringToByte(Integer.toHexString(357));
		if (length.size() > 2) {
			throw new MFCException("数据长度过长");
		}
		if (length.size() < 2) {
			// data.add((byte) 0);
			data.add((byte) 0);
		}
		data.addAll(length);
		// 3 ----发卡机构公钥证书 二维码版本为0x81时包含此字段 117位
		// 3.1 记录头 十六进制值24 1位
		data.addAll(hexStringToByte("24"));
		// 3.2 服务标识 右补0十六进制，01010000 交通电子现金应用 4位
		JsonArray serviceMark = hexStringToByte("01010000");
		data.addAll(serviceMark);
		for (int i = 0; i < 4 - serviceMark.size(); i++) {
			data.add((byte) 0);
		}
		// 3.3 根CA公钥索引 1位
		data.addAll(hexStringToByte("01"));
		// 3.4 证书格式 值为十六进制'12' 1位
		data.addAll(hexStringToByte("12"));
		// 3.5 发卡机构标识 cn8格式 4位
		byte[] orgMark = BCDCode.str2BCD(("00000000"));
		if (orgMark.length != 4) {
			throw new MFCException("发卡机构标识长度不对");
		}
		for (byte b : orgMark) {
			data.add(b);
		}
		// 3.6 证书失效日期 n4格式 月，日
		data.addAll(hexStringToByte(Integer.toHexString(12)));
		data.addAll(hexStringToByte(Integer.toHexString(3)));
		// 3.7 证书序列号
		JsonArray certId = hexStringToByte("123456");
		if (certId.size() != 3) {
			throw new MFCException("证书序列号长度不对");
		}
		data.addAll(certId);
		// 3.8 发卡机构公钥签名算法标识 04:SM2 1位
		data.addAll(hexStringToByte("04"));
		// 3.9 发卡机构公钥加密算法标识 04:SM2 1位
		data.addAll(hexStringToByte("04"));
		// 3.10 发卡机构公钥参数标识 默认00 1位
		data.addAll(hexStringToByte("00"));
		// 3.11 发卡机构公钥长度 1位
		data.addAll(hexStringToByte(Integer.toHexString(33)));
		// 3.12发卡机构公钥 33位
		JsonArray certPublic = hexStringToByte("123456789012345678901234567890123456789012345678901234567890123456");
		if (certPublic.size() != 33) {
			throw new MFCException("发卡机构公钥长度不对");
		}
		data.addAll(certPublic);
		// 3.13 数字签名 64位
		JsonArray caSign = hexStringToByte("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678");
		if (caSign.size() != 64) {
			throw new MFCException("数字签名长度不对");
		}
		data.addAll(caSign);
		// ------

		// 4 支付账户号 16位
		byte[] payAccount = payAct.getBytes();
		for (int i = 0; i < 16 - payAccount.length; i++) {
			data.add((byte) 0);
		}
		for (byte b : payAccount) {
			data.add(b);
		}
		// 5 卡账户号 10位
		JsonArray cardAccount = hexStringToByte(idNo);
		for (int i = 0; i < 10 - cardAccount.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(cardAccount);
		// 6 发卡机构号 4位
		data.addAll(hexStringToByte("00000000"));
		// 7 发卡平台编号 4位
		data.addAll(hexStringToByte("12345678"));
		// 8卡账户类型 1位
		data.addAll(hexStringToByte("" + userType));
		// 9 单次消费金额上限 3位 单程票获取购票金额作为上限
		JsonArray maxConsume = hexStringToByte(Integer.toHexString((int)ticketPrice * 100));
		for (int i = 0; i < 3 - maxConsume.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(maxConsume);
		// 10 支付账户用户公钥 33位
		data.addAll(hexStringToByte("111111123456789012345678901234567890123456789012345678901234567890"));
		// 11 支付账户系统授权过期时间 4位 获取购票时间3个月作为授权过期时间
		JsonArray payDeadLine = hexStringToByte(Long.toHexString(DateUtils.addMonths(createTime, 3)
						.getTime() / 1000 - 946656000));
		for (int i = 0; i < 4 - payDeadLine.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(payDeadLine);
		// 12 二维码有效时间 2位 默认值30秒
		data.add((byte) 0);
		data.addAll(hexStringToByte(Integer.toHexString(30)));
		// 13 发卡机构授权签名 签名数据包括：本表中3～14 字段 65位
		data.addAll(hexStringToByte("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));
		// 14 发卡机构自定义域长度 1位
		data.addAll(hexStringToByte(Integer.toHexString(32)));

		// 15 ---发卡机构自定义域
		// 15.1票卡类型 1位 单程票类型95
		data.addAll(hexStringToByte(Integer.toHexString(95)));
		// 15.2 票号 8位
		JsonArray tickId = hexStringToByte(Long.toHexString(id));
		for (int i = 0; i < 8 - tickId.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(tickId);
		// 15.3 车票金额 2位
		JsonArray price = hexStringToByte(Integer.toHexString((int) (ticketPrice * 100)));
		if (price.size() == 1) {
			data.add((byte) 0);
		}
		data.addAll(price);
		// 15.4票卡状态（是否进站） 1位 票第209位
		data.add(ticketStatus);
		// 15.5 进站可使用站点 4位
		JsonArray inStationNo = hexStringToByte(Integer.toHexString(Integer
				.parseInt(inStationNum, 10)));
		for (int i = 0; i < 4 - inStationNo.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(inStationNo);
		// 15.6 入站时间 4位 服务器生成票的时候预留0000，数组索引第214到217位
		JsonArray inStationTime = hexStringToByte(Long
				.toHexString(946656000 - 946656000));
		for (int i = 0; i < 4 - inStationTime.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(inStationTime);
		// 15.7手机CPUID 8位
		String phoneMsg = null;
		if (wifiMac != null) {
			phoneMsg = wifiMac.replaceAll(":", "");
		} else if (phoneInfo == null) {
			phoneMsg = "0";
		} else {
			phoneMsg = phoneInfo.split("\\+")[1];
		}

		JsonArray CpuID = hexStringToByte(phoneMsg);
		data.addAll(CpuID);
		for (int i = 0; i < 8 - CpuID.size(); i++) {
			data.add((byte) 0);
		}
		// ---
		// 16 二维码生成时间 4位 索引291到294位
		for (int i = 0; i < 69; i++) {
			data.add((byte) 0);
		}
		// 17 支付账户用户私钥签名 65位 295到359位
		byte[] bytes = new byte[360];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = data.get(i).getAsByte();
		}
		try {
			return new String(bytes, "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new MFCException(e);
		}
	}
	/**
	 * <Strong>Description：</Strong>二维码生成方案，需要手机APP补充第209位票状态，214到217位
	 *  入站时间，291到294位二维码生成时间，295到359位手机APP签名</Strong>
	 * 
	 *  关联用户信息的ID  String idNo
	 *  卡类型/用户类型        byte userType
	 *  用户票流水号（16进制8位）  long id
     *  通过Linux命令获取              String wifiMac
     *  CPU型号+AndroidId+Serial   String phoneInfo
     *  
	 * @param sjt
	 * @return String
	 */
	public String buildQRData(
			String idNo //wallet.getUser().getIdNo()
			,byte userType //wallet.getUser().getUserType()
			,long id  //wallet.getId()
			,String wifiMac //user.getWifiMac()
			,String phoneInfo //user.getPhoneInfo()
			) {

		JsonArray data = new JsonArray();
		// 1 二维码版本 当前版本为0x81 1位
		data.addAll(hexStringToByte("81"));
		// 2 二维码数据长度 第3～17字段的总长度 2位
		JsonArray length = hexStringToByte(Integer.toHexString(357));
		if (length.size() > 2) {
			throw new MFCException("数据长度过长");
		}
		if (length.size() < 2) {
			data.add((byte) 0);
		}
		data.addAll(length);
		// 3 ----发卡机构公钥证书 二维码版本为0x81时包含此字段 117位
		// 3.1 记录头 十六进制值24 1位
		data.addAll(hexStringToByte("24"));
		// 3.2 服务标识 右补0十六进制，01010000 交通电子现金应用 4位
		JsonArray serviceMark = hexStringToByte("01010000");
		data.addAll(serviceMark);
		for (int i = 0; i < 4 - serviceMark.size(); i++) {
			data.add((byte) 0);
		}
		// 3.3 根CA公钥索引 1位
		data.addAll(hexStringToByte("01"));
		// 3.4 证书格式 值为十六进制'12' 1位
		data.addAll(hexStringToByte("12"));
		// 3.5 发卡机构标识 cn8格式 4位
		byte[] orgMark = BCDCode.str2BCD(("00000000"));
		if (orgMark.length != 4) {
			throw new MFCException("发卡机构标识长度不对");
		}
		for (byte b : orgMark) {
			data.add(b);
		}
		// 3.6 证书失效日期 n4格式 月，日
		data.addAll(hexStringToByte(Integer.toHexString(12)));
		data.addAll(hexStringToByte(Integer.toHexString(3)));
		// 3.7 证书序列号
		JsonArray certId = hexStringToByte("123456");
		if (certId.size() != 3) {
			throw new MFCException("证书序列号长度不对");
		}
		data.addAll(certId);
		// 3.8 发卡机构公钥签名算法标识 04:SM2 1位
		data.addAll(hexStringToByte("04"));
		// 3.9 发卡机构公钥加密算法标识 04:SM2 1位
		data.addAll(hexStringToByte("04"));
		// 3.10 发卡机构公钥参数标识 默认00 1位
		data.addAll(hexStringToByte("00"));
		// 3.11 发卡机构公钥长度 1位
		data.addAll(hexStringToByte(Integer.toHexString(33)));
		// 3.12发卡机构公钥 33位
		JsonArray certPublic = hexStringToByte("123456789012345678901234567890123456789012345678901234567890123456");
		if (certPublic.size() != 33) {
			throw new MFCException("发卡机构公钥长度不对");
		}
		data.addAll(certPublic);
		// 3.13 数字签名 64位
		JsonArray caSign = hexStringToByte("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678");
		if (caSign.size() != 64) {
			throw new MFCException("数字签名长度不对");
		}
		data.addAll(caSign);
		// ------
		// 4 支付账户号 16位
		byte[] payAccount = new byte[] { 0 };
		for (int i = 0; i < 16 - payAccount.length; i++) {
			data.add((byte) 0);
		}
		for (byte b : payAccount) {
			data.add(b);
		}
		// 5 卡账户号 10位
		JsonArray cardAccount = hexStringToByte(idNo);
		for (int i = 0; i < 10 - cardAccount.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(cardAccount);
		// 6 发卡机构号 4位
		data.addAll(hexStringToByte("00000000"));
		// 7 发卡平台编号 4位
		data.addAll(hexStringToByte("12345678"));
		// 8卡账户类型 1位
		data.addAll(hexStringToByte("" + userType));
		// 9 单次消费金额上限 3位 储值票写最高票价
		JsonArray maxConsume = hexStringToByte(Integer.toHexString(10 * 100));
		for (int i = 0; i < 3 - maxConsume.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(maxConsume);
		// 10 支付账户用户公钥 33位
		data.addAll(hexStringToByte("111111123456789012345678901234567890123456789012345678901234567890"));
		// 11 支付账户系统授权过期时间 4位 获取购票时间3个月作为授权过期时间
		JsonArray payDeadLine = hexStringToByte(Long.toHexString(DateUtils
				.addMonths(new Date(), 3).getTime() / 1000 - 946656000));
		for (int i = 0; i < 4 - payDeadLine.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(payDeadLine);
		// 12 二维码有效时间 2位 默认值30秒
		data.add((byte) 0);
		data.addAll(hexStringToByte(Integer.toHexString(30)));
		// 13 发卡机构授权签名 签名数据包括：本表中3～14 字段 65位
		data.addAll(hexStringToByte("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));

		// 14 发卡机构自定义域长度 1位
		data.addAll(hexStringToByte(Integer.toHexString(32)));
		// 15 ---发卡机构自定义域
		// 15.1票卡类型 1位 储值票类型92
		data.addAll(hexStringToByte(Integer.toHexString(92)));
		// 15.2 票号 8位
		JsonArray tickId = hexStringToByte(Long.toHexString(id));
		for (int i = 0; i < 8 - tickId.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(tickId);
		// 15.3 车票金额 2位
		JsonArray price = hexStringToByte(Integer.toHexString(0));
		if (price.size() == 1) {
			data.add((byte) 0);
		}
		data.addAll(price);
		// 15.4票卡状态（是否进站） 1位 票第209位
		data.add(0);
		// 15.5 进站可使用站点 4位
		JsonArray inStationNo = hexStringToByte("00000000");
		data.addAll(inStationNo);
		// 15.6 入站时间 4位 服务器生成票的时候预留0000，数组索引第214到217位
		JsonArray inStationTime = hexStringToByte(Long
				.toHexString(946656000 - 946656000));
		for (int i = 0; i < 4 - inStationTime.size(); i++) {
			data.add((byte) 0);
		}
		data.addAll(inStationTime);
		// 15.7手机CPUID 8位
		String phoneMsg = null;
		if (wifiMac != null) {
			phoneMsg = wifiMac.replaceAll(":", "");
		} else if (phoneInfo == null) {
			phoneMsg = "0";
		} else {
			phoneMsg = phoneInfo.split("\\+")[1];
		}
		JsonArray CpuID = hexStringToByte(phoneMsg);
		data.addAll(CpuID);
		for (int i = 0; i < 8 - CpuID.size(); i++) {
			data.add((byte) 0);
		}
		// ---
		// 16 二维码生成时间 4位 索引291到294位
		for (int i = 0; i < 69; i++) {
			data.add((byte) 0);
		}
		// 17 支付账户用户私钥签名 65位 295到359位
		byte[] bytes = new byte[360];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = data.get(i).getAsByte();
		}
		try {
			return new String(bytes, "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new MFCException(e);
		}
	}


	private JsonArray hexStringToByte(String hex) {
		if (hex.length() % 2 != 0) {
			hex = "0" + hex;
		}
		int len = (hex.length() / 2);
		JsonArray result = new JsonArray();
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result.add((byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1])));
		}
		return result;
	}
	private int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}
}
