package com.seeyon.ocip.common.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;

import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.common.utils.LogUtils;

public class HttpHelper {
    /**
     * @param url
     * @param param
     * @return URL
     * @throws Exception
     */
    public static String sendGet(String url, String param) throws Exception {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            /*Map<String, List<String>> map = connection.getHeaderFields();
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }*/
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("" + e);
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @param url
     * @param param
     * @return
     * @throws Exception
     */
    public static String sendPost(String url, String param) throws Exception {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
            // utf-8编码
            out.write(new String(param.getBytes("utf-8"), "utf-8"));
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("post" + e);
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 使用webservice接口
     *
     * @throws Exception
     */
    public static String sendRPCClient(String url, String param) throws Exception {
        try {
            // axis2 服务端
            // 使用RPC方式调用WebService
            RPCServiceClient serviceClient = new RPCServiceClient();
            // 指定调用WebService的URL
            EndpointReference targetEPR = new EndpointReference(url);
            Options options = serviceClient.getOptions();
            //确定目标服务地址
            options.setTo(targetEPR);
            //确定调用方法
            String action = Global.getConfig("ws.action");
            if(StringUtils.isBlank(action)){
                action="urn:server";
            }
            options.setAction(action);
            int timeout = 600;
			try {
				timeout = Global.getWebServiceTimeOut();
			} catch (Exception e) {
				LogUtils.error(HttpHelper.class, "获取webservice超时错误，使用默认值：" + timeout);
			}


            options.setProperty(HTTPConstants.SO_TIMEOUT, timeout * 1000);
            /**
             * 指定要调用的getPrice方法及WSDL文件的命名空间
             * 如果 webservice 服务端由axis2编写
             * 命名空间 不一致导致的问题
             * org.apache.axis2.AxisFault: java.lang.RuntimeException: Unexpected subelement arg0
             */
            String namespace = Global.getConfig("ws.namespace");
            if(StringUtils.isBlank(namespace)){
                namespace="http://quickstart.samples/xsd";
            }
            String method = Global.getConfig("ws.method");
            if(StringUtils.isBlank(method)){
                method="server";
            }
            QName qname = new QName(namespace, method);
            // 指定getPrice方法的参数值
            Object[] parameters = new Object[]{param};


            // 调用方法一 传递参数，调用服务，获取服务返回结果集
            OMElement element = serviceClient.invokeBlocking(qname, parameters);
            //值得注意的是，返回结果就是一段由OMElement对象封装的xml字符串。
            //我们可以对之灵活应用,下面我取第一个元素值，并打印之。因为调用的方法返回一个结果
            return element.getFirstElement().getText();
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        }
    }
}
