package code_server.manager.controller;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;

import code_server.manager.utils.JSONUtil;
import code_server.manager.utils.QrBuildData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.spring.web.json.Json;

@RestController
@RequestMapping("/code")
@Api(tags="二维码服务中心")
public class CodeController { 
	QrBuildData qrbuild=new QrBuildData();
	@GetMapping("buildSJTQRData")
	@ApiOperation(value = "生成单程票", produces = MediaType.APPLICATION_JSON_VALUE)
	public String buildSJTQRData(	
			@ApiParam(value = "支付帐号", required = true) @RequestParam(name = "payAccount", required = true) String payAccount,
			@ApiParam(value = "用户主键", required = true) @RequestParam(name = "idNo", required = true) String idNo,
			@ApiParam(value = "用户类型", required = true) @RequestParam(name = "userType", required = true) byte userType,
			@ApiParam(value = "票价金额", required = true) @RequestParam(name = "ticketPrice", required = true) float ticketPrice,
			@ApiParam(value = "创建时间", required = true) @RequestParam(name = "createTime", required = true) Date createTime,
			@ApiParam(value = "票流水号", required = true) @RequestParam(name = "id", required = true) long id,
			@ApiParam(value = "票状态", required = true) @RequestParam(name = "ticketStatus", required = true) byte ticketStatus,
			@ApiParam(value = "进站编号", required = true) @RequestParam(name = "inStationNo", required = true) String inStationNo,
			@ApiParam(value = "蓝牙或WiFi", required = true) @RequestParam(name = "wifiMac") String wifiMac,
			@ApiParam(value = "CPU型号", required = true) @RequestParam(name = "phoneInfo", required = true) String phoneInfo
			) {
		return JSONUtil.toJson(qrbuild.buildQRData(payAccount, idNo, userType, ticketPrice, createTime, id, ticketStatus, inStationNo, wifiMac, phoneInfo));
	}
	
	@GetMapping("getSVTRootQRData")
	@ApiOperation(value = "生成储值票", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getSVTRootQRData(
			@ApiParam(value = "用户主键", required = true) @RequestParam(name = "idNo", required = true) String idNo,
			@ApiParam(value = "用户类型", required = true) @RequestParam(name = "userType", required = true) byte userType,
			@ApiParam(value = "钱包主键", required = true) @RequestParam(name = "id", required = true) long id,
			@ApiParam(value = "蓝牙或WiFi", required = false) @RequestParam(name = "wifiMac" , required = false) String wifiMac,
			@ApiParam(value = "CPU型号", required = true) @RequestParam(name = "phoneInfo", required = true) String phoneInfo
			) throws UnsupportedEncodingException{
		
		String string=JSONUtil.toJson(qrbuild.buildQRData(idNo, userType, id, wifiMac, phoneInfo));
		System.out.println(string);
		byte[] bss=string.getBytes("iso-8859-1");
		JsonArray newdata=new JsonArray();
		for (byte b : bss) {
			newdata.add(b);
		}
		System.out.println(newdata);
		return JSONUtil.toJson(qrbuild.buildQRData(idNo, userType, id, wifiMac, phoneInfo));
		
		
	}
	
	@GetMapping("getData")
	@ApiOperation(value = "二维码字符串", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getData(
			
			) throws UnsupportedEncodingException{
		String code=null;
	 code="\u0001e$\u0001\u0001\u0000\u0000\u0001\u0012\u0000\u0000\u0000\u0000ÿ\u0003\u00124V\u0004\u0004\u0000!\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124V\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001U\u0000\u0000\u0000\u0000\u00124Vx\u0001\u0000\u0003ø\u0011\u0011\u0011\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx#ÿ\"\u0019\u0000ÿ\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx\u00124Vx ÿ\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000ÿÿ\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
		String object=JSONUtil.toBean(code, String.class);
		byte[] bss=code.getBytes("iso-8859-1");
		JsonArray newdata=new JsonArray();
		for (byte b : bss) {
			newdata.add(b);
		}
		System.out.println(newdata);
		return JSONUtil.toJson("ss");
		
		
	}
}
