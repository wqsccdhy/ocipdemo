package com.seeyon.ocip.exchange.api.gbt33479;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessHandler;
import com.seeyon.ocip.exchange.api.IBussinessService;
import com.seeyon.ocip.exchange.api.IExchangeService;
import com.seeyon.ocip.exchange.exceptions.BussinessException;
import com.seeyon.ocip.exchange.exceptions.ExchangeException;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.BIZExchangeData;
import com.seeyon.ocip.exchange.model.BIZExchangePackage;
import com.seeyon.ocip.exchange.model.BIZMessage;
import com.seeyon.ocip.exchange.model.BussinessResult;
import com.seeyon.ocip.exchange.model.MessageBody;
import com.seeyon.ocip.exchange.model.MessageHeader;
import com.seeyon.ocip.exchange.model.MessagePackage;
import com.seeyon.ocip.exchange.model.MessageType;
import com.seeyon.ocip.exchange.model.Organization;
/**
 * 调试用，记得删除
 * @author Administrator
 *
 */
public class BussinessServer implements IBussinessService {

	private Map<BIZContentType, IBussinessHandler> serviceMap = new HashMap<BIZContentType, IBussinessHandler>();

	private IExchangeService getExchangeService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getExchangeService();
	}

	public IBussinessHandler register(IBussinessHandler handler) {
		assert handler != null;
		BIZContentType serviceName = handler.type();
		assert serviceName != null;
		return serviceMap.put(serviceName, handler);
	}

	IBussinessHandler getService(BIZContentType serviceName) throws BussinessException {
		IBussinessHandler handler = serviceMap.get(serviceName);
		if (handler == null) {
			throw new BussinessException("未找到对应的业务处理器, type= " + serviceName);
		}
		return handler;
	}

	public IBussinessHandler unRegisterService(IBussinessHandler handler) {
		assert handler != null;
		BIZContentType serviceName = handler.type();
		assert serviceName != null;
		return serviceMap.remove(serviceName);
	}

	@Override
	public void onSend(BIZExchangePackage biz) throws ExchangeException {
		List<BIZExchangeData> datas = biz.getExchangeDatas();
		for (BIZExchangeData data : datas) {
			String identifier = data.getIdentifier();
			List<Organization> recivers = data.getRecivers();
			Organization sender = data.getSender();
			String subject = data.getSubject();
			BIZMessage bussnissMessage = data.getBussnissMessage();
			MessagePackage msgPkg = new MessagePackage();
			MessageHeader header = new MessageHeader();
			header.setId(identifier);
			header.setSubject(subject);
			Address source = data.getSource();
			header.setSource(source);
			header.setSender(sender);
			header.setRecivers(recivers);
			header.setGroupId(data.getGroupId());
			header.setSendTime(new Date());
			header.setType(MessageType.BIZ);
			msgPkg.setHeader(header);
			MessageBody body = new MessageBody();
			body.setBizMessage(bussnissMessage);
			msgPkg.setBody(body);
			Object onSend = getExchangeService().onSend(msgPkg);
			System.out.println("onSend:" + onSend);
		}

	}

	public void fireExchange(BIZContentType contentType, Map<String, Object> param) throws ExchangeException {
		IBussinessHandler service = getService(contentType);
		BIZExchangeData exchangeData = service.exchangeSend(param);
		BIZExchangePackage biz = new BIZExchangePackage();
		biz.getExchangeDatas().add(exchangeData);
		onSend(biz);
	}

	@Override
	public List<BussinessResult> onReceive(BIZExchangePackage biz) throws BussinessException {
		List<BIZExchangeData> exchangeDatas = biz.getExchangeDatas();
		List<BussinessResult> result = new ArrayList<BussinessResult>();
		for (BIZExchangeData bizExchangeData : exchangeDatas) {
			BIZMessage bizMessage = bizExchangeData.getBussnissMessage();
			BIZContentType contentType = bizMessage.getContentType();
			IBussinessHandler handler = getService(contentType);
			List<Organization> recivers = bizExchangeData.getRecivers();
			String identifier = bizExchangeData.getIdentifier();
			if (handler == null) {
				for (Organization r : recivers) {
					BussinessResult br = new BussinessResult();
					br.setIdentifier(identifier);
					br.setOrganization(r);
					br.setCode("-1");
					br.setMessage("系统未设置该类型业务处理器，contentType=" + contentType);
					result.add(br);
				}
			} else {
				try {
					result.addAll(handler.exchangeReceive(bizExchangeData));
				} catch (Throwable e) {
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					writer.flush();
					String msg = writer.toString();
					for (Organization r : recivers) {
						BussinessResult br = new BussinessResult();
						br.setIdentifier(identifier);
						br.setOrganization(r);
						br.setCode("2");
						br.setMessage("系统业务处理器异常，原因：" + msg);
						result.add(br);
					}
				}
			}
		}
		return result;

	}

}
