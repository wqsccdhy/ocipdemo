package com.seeyon.ocip.exchange.api.gbt33479;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;

import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IExchangeService;
import com.seeyon.ocip.exchange.api.ITransportService;
import com.seeyon.ocip.exchange.exceptions.ExchangeException;
import com.seeyon.ocip.exchange.model.BIZMessage;
import com.seeyon.ocip.exchange.model.MessageBody;
import com.seeyon.ocip.exchange.model.MessageHeader;
import com.seeyon.ocip.exchange.model.MessagePackage;
import com.seeyon.ocip.exchange.model.MessageType;
import com.seeyon.ocip.exchange.model.Organization;
import com.seeyon.ocip.exchange.model.SYSMessage;
import com.seeyon.ocip.exchange.util.XmlObjectFactory;
/**
 * 调试使用，记得删除
 * @author Administrator
 *
 */
public class ExchangeServer implements IExchangeService {

	@Override
	public Object onSend(MessagePackage msgPkg) throws ExchangeException {
		StringWriter writer = new StringWriter();
		MessageType type = msgPkg.getHeader().getType();
		// TODO 对消息对象加工，签名
		if (MessageType.BIZ.equals(type)) {
			// TODO 对消息对象加工，加密
		}
		try {
			XmlObjectFactory.getInstance().writeObject(msgPkg, writer);
		} catch (Exception e) {
			throw new ExchangeException("发送数据失败，序列化出错", e);
		}
		try {
			writer.flush();
			String message = writer.toString();
			return getTransportService().requestTransport(message);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ITransportService getTransportService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getTransportService();
	}

	@Override
	public Object onReceive(MessagePackage msgPkg) throws ExchangeException {
		// // TODO 调用业务层实现消息分发
		// MessageHeader header = msgPkg.getHeader();
		// String id = header.getId();// 报文id
		// // TODO 过滤本系统不能处理的接收者
		// List<Organization> recivers = header.getRecivers();// 接收者
		// Organization sender = header.getSender();// 发送者
		// header.getSendTime();// 发送时间
		// MessageType type = header.getType();
		// MessageBody messageBody = msgPkg.getBody();
		// List<BussinessResult> results = null;
		// if (MessageType.SYS.equals(type)) {
		// SYSMessage sysMessage = messageBody.getSysMessage();
		// // 触发系统报文
		// receiveSystemMessage(msgPkg);
		// } else if (MessageType.BIZ.equals(type)) {
		// String msg = "";
		// String desc = "";
		// try {
		// CryptAlgorithm cryptAlgorithm = header.getCryptAlgorithm();
		// // 如果业务报文存在加密，需要进行解密处理
		// String encryptName = null;
		// String signName = null;
		// if (cryptAlgorithm != null) {
		// encryptName = cryptAlgorithm.getEncryptName();// 加密算法名称
		// signName = cryptAlgorithm.getSignName();// 签名算法名称
		// }
		// // 存在签名，验证签名
		// if (signName != null) {
		// Signature signature = msgPkg.getSignature();
		// byte[] signData = signature.getData();
		// // 将签名值设置为空后进行签名校验
		// signature.setData(new byte[0]);
		// ByteArrayOutputStream out = new ByteArrayOutputStream();
		// XmlObjectFactory.getInstance().writeObject(msgPkg, out);
		// try {
		// out.close();
		// } catch (Exception e) {
		// }
		// checkSignature(signName, signData, out.toByteArray());
		// }
		// BIZMessage bizMessage = messageBody.getBizMessage();
		// // 如果业务报文存在加密，需要进行解密处理
		// if (bizMessage == null && encryptName != null) {
		// byte[] encryptData = messageBody.getEncryptMessage();
		// bizMessage = decrypt(encryptName, encryptData);
		// }
		// if (bizMessage != null) {
		//
		// IBussinessService bizService = getBussinessService();
		// BIZExchangeData bizData = new BIZExchangeData();
		// bizData.setIdentifier(id);
		// bizData.setSource(header.getSource());
		// bizData.setGroupId(header.getGroupId());
		// bizData.setSender(sender);
		// bizData.setRecivers(recivers);
		// bizData.setSubject(msgPkg.getHeader().getSubject());
		// bizData.setBussnissMessage(bizMessage);
		// BIZExchangePackage pkg = new BIZExchangePackage();
		// pkg.getExchangeDatas().add(bizData);
		// // 触发业务报文
		// results = bizService.onReceive(pkg);
		// desc = IConstant.ExchMessageInfo.deal_success.getValue().toString();
		// } else {
		// // TODO 抛出异常
		// msg = "bizMessage为空";
		// desc = IConstant.ExchMessageInfo.deal_fail.getValue().toString();
		// }
		// }catch (BussinessException e){
		// msg = e.getMessage();
		// desc = IConstant.ExchMessageInfo.deal_fail.getValue().toString();
		// } finally {
		// /** 发送系统消息，告诉对方我收到了-开始 **/
		// SYSMessage sysMessage = new SYSMessage();
		// sysMessage.setOriginMessageId(msgPkg.getHeader().getId());
		// sysMessage.setDatetime(new Date());
		// sysMessage.setType(msg);
		// sysMessage.setDescription(desc);
		// sysMessage.setBussinessResultList(results);//返回结果
		// sendSystemMessage(sysMessage,msgPkg);
		// /** 发送系统消息，告诉对方我收到了-结束 **/
		// }
		//
		// }

		ReceiverAndSendMsgThread thread = new ReceiverAndSendMsgThread(this, msgPkg);
		thread.start();
		return null;

	}

	public void receiveSystemMessage(MessagePackage msgPkg) throws ExchangeException {

		// TODO 根据系统消息，处理本地 MessagePackage 的状态

	}

	public void sendSystemMessage(SYSMessage sysMessage, MessagePackage msgPkg) throws ExchangeException {

		// String originMessageId = sysMessage.getOriginMessageId();

		// TODO 根据originMessageId，获取源数据对象
		MessagePackage originPackage = msgPkg;

		/** 发送系统消息，告诉对方我收到了-开始 **/
		MessagePackage sysPackage = new MessagePackage();

		MessageHeader sysHeader = new MessageHeader();
		sysHeader.setId(UUID.randomUUID().toString());

		Organization organization = getLocalSystemOrganization();
		Address identification = organization.getIdentification();
		sysHeader.setSender(organization);

		// 设置这个系统消息源消息的发送系统
		Organization originSender = originPackage.getHeader().getSender();
		sysHeader.getRecivers().add(originSender);
		sysHeader.setType(MessageType.SYS);
		sysHeader.setSendTime(new Date());
		sysHeader.setSubject(originPackage.getHeader().getSubject() + "收到数据");
		Address address = new Address();
		address.setResource(identification.getResource());
		address.setType(IConstant.AddressType.system.name());
		address.setName(identification.getName());
		address.setId(identification.getId());
		sysHeader.setSource(address);
		sysPackage.setHeader(sysHeader);

		MessageBody sysBody = new MessageBody();
		sysBody.setSysMessage(sysMessage);
		sysPackage.setBody(sysBody);

		// 调用消息包发送接口
		this.onSend(sysPackage);
		/** 发送系统消息，告诉对方我收到了-结束 **/

	}

	/**
	 * 获取本系统的收发机构类型对象
	 * 
	 * @return
	 */
	public Organization getLocalSystemOrganization() {
		Organization localSystemOrganization = new Organization();
		Address systemAddress = OcipConfiguration.getInstance().getSystemIdentification();
		localSystemOrganization.setDescription("SY-OCIP 提供");
		localSystemOrganization.setName(systemAddress != null ? systemAddress.getName() : "未知系统");
		localSystemOrganization.setIdentification(systemAddress);
		return localSystemOrganization;
	}

	/**
	 * 检查报文的签名值
	 * 
	 * @param signName 签名方法名称
	 * @param signData 签名值
	 * @param msgPkg 数据值
	 */
	protected void checkSignature(String signName, byte[] signData, byte[] msgPkg) {
	}

	/**
	 * 将加密信息解密成业务消息对象
	 * 
	 * @param encryptName 加密方法名称
	 * @param encryptData 机密信息
	 * @return 业务消息对象
	 */
	protected BIZMessage decrypt(String encryptName, byte[] encryptData) {
		return null;
	}

	@Override
	public Object onReceive(String message) throws ExchangeException {
		StringReader reader = new StringReader(message);
		MessagePackage msgPkg = null;
		try {
			msgPkg = XmlObjectFactory.getInstance().readObject(reader);
		} catch (Exception e) {
			throw new ExchangeException("2", "处理数据失败，反序列化错误！", e);
		}
		// 调用内部接口
		return onReceive(msgPkg);
	}

}
