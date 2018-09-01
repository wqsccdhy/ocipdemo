package com.seeyon.ocip.exchange.api.gbt33479;

import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessService;
import com.seeyon.ocip.exchange.api.IExchangeService;
import com.seeyon.ocip.exchange.exceptions.BussinessException;
import com.seeyon.ocip.exchange.model.*;
import com.seeyon.ocip.exchange.util.XmlObjectFactory;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by Seeyon on 2018-4-27.
 */
public class ReceiverAndSendMsgThread extends Thread{
    IExchangeService exchangeService;
    MessagePackage msgPkg;

    public  ReceiverAndSendMsgThread(IExchangeService exchangeService,MessagePackage msgPkg)
    {
        this.exchangeService = exchangeService;
        this.msgPkg = msgPkg;
    }

    @Override
    public void run() {
        // TODO 调用业务层实现消息分发
        MessageHeader header = msgPkg.getHeader();
        String id = header.getId();// 报文id
        // TODO 过滤本系统不能处理的接收者
        List<Organization> recivers = header.getRecivers();// 接收者
        Organization sender = header.getSender();// 发送者
        header.getSendTime();// 发送时间
        MessageType type = header.getType();
        MessageBody messageBody = msgPkg.getBody();
        List<BussinessResult> results = null;
        if (MessageType.SYS.equals(type)) {
            SYSMessage sysMessage = messageBody.getSysMessage();
            // 触发系统报文
            try {
                exchangeService.receiveSystemMessage(msgPkg);
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        } else if (MessageType.BIZ.equals(type)) {
            String msg = "";
            String desc = "";
            try {
                CryptAlgorithm cryptAlgorithm = header.getCryptAlgorithm();
                // 如果业务报文存在加密，需要进行解密处理
                String encryptName = null;
                String signName = null;
                if (cryptAlgorithm != null) {
                    encryptName = cryptAlgorithm.getEncryptName();// 加密算法名称
                    signName = cryptAlgorithm.getSignName();// 签名算法名称
                }
                // 存在签名，验证签名
                if (signName != null) {
                    Signature signature = msgPkg.getSignature();
                    byte[] signData = signature.getData();
                    // 将签名值设置为空后进行签名校验
                    signature.setData(new byte[0]);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    XmlObjectFactory.getInstance().writeObject(msgPkg, out);
                    try {
                        out.close();
                    } catch (Exception e) {
                    }
                }
                BIZMessage bizMessage = messageBody.getBizMessage();
                // 如果业务报文存在加密，需要进行解密处理
                if (bizMessage != null) {

                    IBussinessService bizService = OcipConfiguration.getInstance().getExchangeSpi().getBussinessService();
                    BIZExchangeData bizData = new BIZExchangeData();
                    bizData.setIdentifier(id);
                    bizData.setSource(header.getSource());
                    bizData.setGroupId(header.getGroupId());
                    bizData.setSender(sender);
                    bizData.setRecivers(recivers);
                    bizData.setSubject(msgPkg.getHeader().getSubject());
                    bizData.setBussnissMessage(bizMessage);
                    BIZExchangePackage pkg = new BIZExchangePackage();
                    pkg.getExchangeDatas().add(bizData);
                    // 触发业务报文
                    results = bizService.onReceive(pkg);
                    desc = IConstant.ExchMessageInfo.deal_success.getValue().toString();
                } else {
                    // TODO 抛出异常
                    msg = "bizMessage为空";
                    desc = IConstant.ExchMessageInfo.deal_fail.getValue().toString();
                }
            }catch (BussinessException e){
                msg = e.getMessage();
                desc = IConstant.ExchMessageInfo.deal_fail.getValue().toString();
            } finally {
                /** 发送系统消息，告诉对方我收到了-开始 **/
                SYSMessage sysMessage = new SYSMessage();
                sysMessage.setOriginMessageId(msgPkg.getHeader().getId());
                sysMessage.setDatetime(new Date());
                sysMessage.setType(msg);
                sysMessage.setDescription(desc);
                sysMessage.setBussinessResultList(results);//返回结果
                try{
                    exchangeService.sendSystemMessage(sysMessage,msgPkg);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                /** 发送系统消息，告诉对方我收到了-结束 **/
            }

        }

    }

}
