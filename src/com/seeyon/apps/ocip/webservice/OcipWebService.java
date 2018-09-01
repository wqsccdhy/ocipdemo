package com.seeyon.apps.ocip.webservice;

import javax.jws.WebService;

/**
 * 数据服务平台webservice服务端
 * <p>Title: OcipWebService
 * <p>Description: TODO
 * <p>Copyright: Copyright (c) 2016
 * @author wxt.shenchunyou
 * @date 2016-12-14 上午11:34:44
 * @version TODO
 */
//@WebService  
public interface OcipWebService {

	public String getJsonDataStr(String data) throws Exception;
	
	public String reqWebService(String unitId, String linkCode, String seconds, String reqType);

	public String sendBaseXMLEsbWebService(String transCode, String message);

}
