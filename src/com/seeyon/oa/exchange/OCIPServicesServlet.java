package com.seeyon.oa.exchange;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Strings;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.exceptions.InterfaceException;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessService;
import com.seeyon.ocip.exchange.exceptions.ExchangeException;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.edoc.EdocOperation;
import com.seeyon.ocip.org.agent.BaseAgent;
import com.seeyon.ocip.org.agent.UnitAgent;
import com.seeyon.ocip.org.agent.UserAgent;
import com.seeyon.ocip.org.entity.OcipUnit;
import com.seeyon.ocip.org.entity.OcipUser;

public class OCIPServicesServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3670842542658827130L;
	/**
	 * 发文单位名称
	 */
	public static String sendOrgName = "异构单位1";
	
	/**
	 * 发文单位本地ID
	 */
	public static String sendOrgLocalId = "2556842553182670622";
	
	/**
	 * 发文单位本地ID对应的OCIP单位ID
	 */
	public static String sendOrgOCIPId = "2723437946474089882";
	
	/**
	 * 公文发送人员名称
	 */
	public static String sendMemName = "异构测试人员1";
	
	/**
	 * 发文人员本地ID
	 */
	public static String sendMemLocalId = "138913789043295577";
	
	/**
	 * 发文人员本地ID对应的OCIP人员ID
	 */
	public static String sendMemOCIPId = "-8467766985147790299";
	
	/**
	 * 接收单位名称
	 */
	public static String recOrgName = "清镇市公安局";
	
	
	/**
	 * 接收单位本地ID
	 */
	public static String recOrgLocalId = "3153276437931052486";
	
	/**
	 * 接收单位本地ID对应的OCIP单位ID
	 */
	public static String recOrgOCIPId = "3027673676430002247";
	
	/**
	 * 公文接收人员名称
	 */
	public static String recMemName = "";
	
	/**
	 * 接收人员本地ID
	 */
	public static String recMemLocalId = "";
	
	/**
	 * 接收人员本地ID对应的OCIP人员ID
	 */
	public static String recMemOCIPId = "";
	
	

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		String param = request.getParameter("type");
		param = new String(param.getBytes("ISO-8859-1"), "UTF-8");
		PrintWriter outt = response.getWriter();
		JSONObject jsonobj = new JSONObject();
		//上传组织机构
		if ("org".equals(param)) {
			boolean upLoadOrgIUnit = upLoadOrgIUnit();
			boolean upLoadOrgUser = upLoadOrgUser();
			if (upLoadOrgIUnit && upLoadOrgUser) {
				jsonobj.put("msg", "success");
			} else {
				jsonobj.put("msg", "error");
			}
			outt.println(jsonobj);
		} else if ("send".equals(param)) {// 发送公文
			String title = request.getParameter("title");
			if (Strings.isNullOrEmpty(title)) {
				title = "公文测试";
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", title);//公文标题
			map.put("recOrgID", recOrgLocalId);//公文接收单位名称
			map.put("recOrgName", recOrgName);//公文接收单位本地ID
			try {
				getBussinessService().fireExchange(BIZContentType.OFC, map);
				jsonobj.put("msg", "success");
				outt.println(jsonobj);
			} catch (ExchangeException e) {
				jsonobj.put("msg", "error");
				outt.println(jsonobj);
				e.printStackTrace();
			}

		}else if ("revoked".equals(param)) {//撤销公文
			String revokedId = request.getParameter("revokedId");
			String revokedrelaid = request.getParameter("revokedrelaid");
			String comment = request.getParameter("comment");
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("detailId", Long.parseLong(revokedId));
			paramMap.put("comment", comment);
			String userName = "异构测试人员1";
			paramMap.put("name", userName);//公文撤销人员名称
			paramMap.put("originMessageId", revokedrelaid);//公文撤销人员名称
			/**
			 * 接入系统人员id：138913789043295577
			 * 接入系统人员对应的OCIP人员id：-8467766985147790299
			 */
			paramMap.put("id", "138913789043295577");//公文撤销人员ID
			paramMap.put("edocOperation", EdocOperation.REVOKED);
			try {
				getBussinessService().fireExchange(BIZContentType.RET, paramMap);
				jsonobj.put("msg", "success");
				outt.println(jsonobj);
			} catch (ExchangeException e) {
				jsonobj.put("msg", "error");
				outt.println(jsonobj);
				e.printStackTrace();
			}
			
		}else if ("accepted".equals(param)) {//签收公文
			String mainId = request.getParameter("acceptedId");
			String detailId = request.getParameter("detailId");
			String exchNo = request.getParameter("exchNo");
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("groupId", mainId);
			paramMap.put("detailId", detailId);
			paramMap.put("exchNo", exchNo);
			paramMap.put("comment", "签收回复");
			paramMap.put("edocOperation",  EdocOperation.ACCEPTED);
			try {
				getBussinessService().fireExchange(BIZContentType.RET, paramMap);
				jsonobj.put("msg", "success");
				outt.println(jsonobj);
			} catch (ExchangeException e) {
				jsonobj.put("msg", "error");
				outt.println(jsonobj);
				e.printStackTrace();
			}
			
		}else if ("stepBack".equals(param)) {//回退公文
			String groupId = request.getParameter("stepBackId");
			String detailId = request.getParameter("stepBackDetailId");
			String exchNo = request.getParameter("stepBackExchNo");
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("groupId", groupId);
			paramMap.put("detailId", detailId);
			paramMap.put("exchNo", exchNo);
			paramMap.put("comment", "回退原因");
			paramMap.put("edocOperation",  EdocOperation.STEPBACK);
			
			try {
				getBussinessService().fireExchange(BIZContentType.RET, paramMap);
				
			} catch (ExchangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonobj.put("msg", "success");
			outt.println(jsonobj);
			
		}
		

		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	
	/**
	 * 上报单位
	 * @return
	 */
	private boolean upLoadOrgIUnit() {
		boolean result = true;
		List<OcipUnit> unitInfos = new ArrayList<OcipUnit>();
		//根节点必须要上传
		OcipUnit root = new OcipUnit();
		root.setObjectId("-1730833917365171641");//单位id
		root.setName("异构单位根节点");//名称
		root.setForeignName("");
		root.setShortName("异构单位根节点");
		root.setAliasName("");
		root.setSortId(1);//排序号
		root.setCode("-1730833917365171641");////设置为单位id
		root.setIsEnable(IConstant.ENABLE);
		root.setParentId("0");//上级单位ID，根节点没有上级单位，设置为0
		
		OcipUnit unit1 = new OcipUnit();
		//unit1.setObjectId("2556842553182670622");//单位id
		unit1.setObjectId(sendOrgLocalId);//单位id
		unit1.setName(sendOrgName);//名称
		unit1.setForeignName("");
		unit1.setShortName("异构单位1");
		unit1.setAliasName("");
		unit1.setSortId(2);//排序号
		unit1.setCode(sendOrgLocalId);//单位id
		unit1.setIsEnable(IConstant.ENABLE);
		unit1.setParentId("-1730833917365171641");//上级单位ID
		
		unitInfos.add(root);
		unitInfos.add(unit1);
		JSONObject req = new JSONObject();
		req.put("units", unitInfos);
		
		try {
			//上传单位
			String rtn = BaseAgent.getInstance(UnitAgent.class).uploadFull(JSONObject.toJSONString(req, SerializerFeature.WriteMapNullValue));

			if (rtn == null || rtn.equals("")) {
				System.out.println("上报单位失败,代理或平台网络连接出错");
				result = false;
			}
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		}
		
		return result;
	}
	
	/**
	 * 上报人员
	 * @return
	 */
	private boolean upLoadOrgUser() {
		boolean result = true;
		List<OcipUser> ocipUsers = new ArrayList<OcipUser>();
		OcipUser user = new OcipUser();
		user.setObjectId(sendMemLocalId);// 人员ID
		user.setName(sendMemName);// 异构人员名称
		user.setCode(sendMemLocalId);// 设置为人员ID
		user.setLoginName("csry1");// 登陆名
		user.setPassword("12345");// 密码
		user.setSex(OcipUser.SEX_MALE);// 性别
		user.setBirthday(new Date());// 生日
		user.setNation("");
		user.setTelNumber("13541246000");// 手机号
		user.setSortId(1);// 排序号
		user.setIsEnable(IConstant.ENABLE);//启用
		user.setLevelId("");
		user.setIsAdmin(1);//设置为非单位管理员
		List<OcipUser.Relation> relations = new ArrayList<OcipUser.Relation>();
		OcipUser.Relation relation = new OcipUser.Relation();
		relation.setType(OcipUser.Relation.RELATION_TYPE_POST_MAIN);
		relation.setUnitId(sendOrgLocalId);// 单位ID
		relations.add(relation);
		user.setRelations(relations);
		ocipUsers.add(user);
		JSONObject req = new JSONObject();
		req.put("users", ocipUsers);

		try {
			// 上传人员
			String rtn = BaseAgent.getInstance(UserAgent.class)
					.uploadFull(JSONObject.toJSONString(req, SerializerFeature.WriteMapNullValue));

			if (rtn == null || rtn.equals("")) {
				System.out.println("上报人员失败,代理或平台网络连接出错");
				result = false;
			}
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		}

		return result;
	}
	
	private IBussinessService getBussinessService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getBussinessService();
	}

}
