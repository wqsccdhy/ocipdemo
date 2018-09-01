
package com.seeyon.apps.ocip.webservice.impl;

import javax.jws.WebParam;
import javax.jws.WebService;

import com.seeyon.apps.ocip.webservice.OcipWebService;
import com.seeyon.ocip.common.ExtIntfController;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.ITransportService;

/**
 * 数据服务平台webservice服务端
 * @author wxt.touxin
 * @version 20170615
 *
 */
//@WebService(endpointInterface = "com.seeyon.apps.ocip.webservice.OcipWebService")  
public class OcipWebServiceImpl implements OcipWebService {

	@Override
	public String getJsonDataStr(String data) throws Exception {
		
		/*System.out.println("getJsonDataStr");
		return "";*/
		return ExtIntfController.main(data);
	}

	@Override
	public String reqWebService(String unitId,String linkCode, String seconds, String reqType) {
		/*System.out.println("reqWebService");
		return "";*/
		return transportService().getResponse().responseTransportCode(unitId, linkCode, seconds, reqType);
	}

	@Override
	public String sendBaseXMLEsbWebService(String transCode, String message) {
		/*System.out.println("sendBaseXMLEsbWebService");
		return "";*/
		return transportService().getResponse().responseTransport(transCode, message);
	}
	
	private ITransportService transportService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getTransportService();
	}

}
