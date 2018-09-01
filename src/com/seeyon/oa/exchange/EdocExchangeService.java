package com.seeyon.oa.exchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.exceptions.CommonException;
import com.seeyon.ocip.common.online.IOnlineChecker;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.exchange.entry.ExchangeHeader;
import com.seeyon.ocip.exchange.entry.ExchangePackage;
import com.seeyon.ocip.exchange.entry.ExchangeProperty;
import com.seeyon.ocip.exchange.service.IExchangeService;

public class EdocExchangeService implements IExchangeService {

	@Override
	public Object exchangeReceive(ExchangePackage in) throws CommonException {
		ExchangeHeader header = in.getHeader();
		String exchType = header.getExchSubType();
		if (IConstant.ExchangeSubType.send.name().equals(exchType)){
			analysisEdocData(in);
		}
		
		return "aaaaa";
	}

	@Override
	public ExchangePackage exchangeSend(String arg0, Object... arg1) throws CommonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServiceName() {
		return IConstant.ExchangeType.edoc.name();
	}
	
	
	/**
	 * 解析跨系统交换过来的公文数据
	 * 
	 * @param in
	 */
	private void analysisEdocData(ExchangePackage in) {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext(); 
		IOrganizationManager iOrganizationManager = (IOrganizationManager) wac.getBean("organizationManager");
		ExchangeHeader header = in.getHeader();
		Address sender = header.getSender();
		String resource = sender.getResource();
		// 公文的发送系统与接收系统为统一不需要解析数据，测试时屏蔽此条件
		if (!Global.getConfig("sysCode").equals(resource)) {
			List<ExchangeProperty> body = in.getBody();
			if (header != null && body != null && !body.isEmpty()) {
				Map<String, String> hashMap = new HashMap<String, String>(body.size());
				Map<String, ExchangeProperty> elementDataMap = new HashMap<String, ExchangeProperty>(body.size());
				for (ExchangeProperty exchangeProperty : body) {
					String fieldName = exchangeProperty.getFieldName();
					String fieldValue = exchangeProperty.getFieldValue();
					hashMap.put(fieldName, fieldValue);
					elementDataMap.put(fieldName, exchangeProperty);
				}
				if (hashMap != null && !hashMap.isEmpty()) {
					// 0、获取公共变量
					Address creater = header.getCreater();
					Address createUnit = header.getCreateUnit();
					
					Long createUnitId = Long.valueOf(createUnit.getId());
					String subject = String.valueOf(hashMap.get("subject") == null ? "" : hashMap.get("subject"));
					Object contentTypeObject = hashMap.get("contentType");
					// 正文类型
					String contentType = String.valueOf(contentTypeObject);

					// =================保存GovdocExchangeMain=START===============
					// 关联ID，即公文发送方edocSummary的主键ID
					Object relationIdObj = hashMap.get("relationId");
					Long relationId = Long.valueOf(String.valueOf(relationIdObj));
					// 根据创建者的Address转换成平台ID，这样才能在本地orgManager接口中获取到
					Long createId = Long.valueOf(iOrganizationManager.getPlatformId(creater));
					List<Address> receiver = header.getReceiver();
					for (Address receiverUnit : receiver) {
						Long unit = Long.valueOf(iOrganizationManager.getLocalObjectId(receiverUnit));
						System.out.print("11111111111111"+unit);
					}
					
//					Object affairIdObj = hashMap.get("affairId");
//					Long affairId = Long.valueOf(String.valueOf(affairIdObj));
				}
			}
		}

	}

}
