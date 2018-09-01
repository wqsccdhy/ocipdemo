package com.seeyon.oa.exchange;


import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.license.OcipKeyMananger;
import com.seeyon.ocip.common.online.IOnlineChecker;
import com.seeyon.ocip.common.utils.OcipService;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessHandler;
import com.seeyon.ocip.online.OnlineChecker;
import com.seeyon.ocip.spi.exchange.gbt33479.GB_T33479ExchangeSpi;

public class ServletListener implements ServletContextListener {

	private OnlineChecker checker;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		if (this.checker != null) {
			this.checker.stop();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		register();
		registerExchange();
	}
	
	/**
	 * 注册
	 */
	public void register() {
		try {
			String path = this.getClass().getResource("/").getPath();
			OcipKeyMananger.init(path);
			
			String sysCode = OcipKeyMananger.getSysCode();
			String regIp = OcipKeyMananger.getRegIp();
			String regPort = OcipKeyMananger.getRegPort();
			String keyGenTime = OcipKeyMananger.getPropValue("keyGenTime");
			
			//代理地址
			String agentServerAddr = regIp + ":"  + regPort + "/ServerAgent";
			String webserviceTimeout = "30";
			
			OcipService.setAgentServerAddr(agentServerAddr);
			OcipService.setSysCode(sysCode);
			OcipService.setWebServiceTimeOut(Integer.parseInt(webserviceTimeout));
			
			System.out.println("sysCode:" + sysCode);
			System.out.println("regIp:" + regIp);
			System.out.println("regPort:" + regPort);
			System.out.println("keyGenTime:" + keyGenTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 注册交换组件
	 */
	public void registerExchange() {
		
		OcipConfiguration configuration = OcipConfiguration.getInstance();
		// 注册异步的交换组件
		GB_T33479ExchangeSpi exchangeSpi = new GB_T33479ExchangeSpi();
		configuration.setExchangeSpi(exchangeSpi);
		// 设置ocip通讯的wsdl接口路径
		String regIp = OcipKeyMananger.getRegIp();
		String regPort = OcipKeyMananger.getRegPort();
		String format = "http://%s:%s/ServerAgent/services/DataController?wsdl";
		configuration.setExchangeWsdlUrl(String.format(format, regIp, regPort));
		// 注册本地交换的通讯地址
		Address systemIdentification = new Address();
		systemIdentification.id = OcipKeyMananger.getSysCode();
		systemIdentification.type = IConstant.AddressType.system.name();
		systemIdentification.name = OcipKeyMananger.getRegName();
		configuration.setSystemIdentification(systemIdentification);
		// 注册新国标交换处理接口
		//
		/**
		 * 通过Spring上下文获取IBussinessHandler的实现类EdocOFCExchangeHandler和EdocRETExchangeHandler
		 * EdocOFCExchangeHandler和EdocRETExchangeHandler死用于交换的接口
		 * EdocOFCExchangeHandler公文的发送和接收
		 * EdocRETExchangeHandler公文的回执：签收、回退、撤销操作
		 * 
		 */
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext(); 
		Map<String, IBussinessHandler> handlers = wac.getBeansOfType(IBussinessHandler.class);
        //Map<String, IBussinessHandler> handlers = applicationContext.getBeansOfType(IBussinessHandler.class);
		//Map<String, IBussinessHandler> handlers = new HashMap<String, IBussinessHandler>();
		//handlers.put(BIZContentType.OFC.name(), new EdocOFCExchangeHandler());
		//handlers.put(BIZContentType.RET.name(), new EdocRETExchangeHandler());
		for (IBussinessHandler handler : handlers.values()) {
			exchangeSpi.register(handler);
		}
		// 添加心跳检测
		
		this.checker = OnlineChecker.getOrCreate((IOnlineChecker) wac.getBean("ocipOnlineChecker"), null);
		this.checker.start();
		
	}

	
	

}
