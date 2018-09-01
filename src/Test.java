import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

public class Test {

	public static void main(String[] args) throws Exception {
		/*JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean(); 



	    factory.setServiceClass(OcipWebService.class); 

	    factory.setAddress("http://127.0.0.1:8080/ocipdemo/services/ocipWebService?wsdl"); 

	    OcipWebService client = (OcipWebService) factory.create();

	    client.getJsonDataStr(null);
	    client.reqWebService(null, null, null, null);
	    client.sendBaseXMLEsbWebService(null, null);*/
		
		Test test = new Test();
		test.test();

	}
	
	public void test() throws Exception{
		String url = "http://127.0.0.1:8080/ocipdemo/services/ocipWebService?wsdl";
		try {
			// 使用RPC方式调用WebService
			RPCServiceClient serviceClient = new RPCServiceClient();
			// 指定调用WebService的URL
			EndpointReference targetEPR = new EndpointReference(url);
			Options options = serviceClient.getOptions();
			// 确定目标服务地址
			options.setTo(targetEPR);
			// 确定调用方法
			options.setAction("reqWebService");
			options.setTimeOutInMilliSeconds(60000);
			QName qname = new QName("http://impl.webservice.ocip.apps.seeyon.com", "reqWebService");
			Object[] parameters = new Object[]{"","","",""};
			OMElement element = serviceClient.invokeBlocking(qname, parameters);
			// 值得注意的是，返回结果就是一段由OMElement对象封装的xml字符串。
			String result = element.getFirstElement().getText();
			System.out.println(result);
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}

}
