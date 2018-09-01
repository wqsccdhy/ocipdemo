package com.seeyon.ocip.common;


import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ocip.common.exceptions.CommonException;
import com.seeyon.ocip.common.http.HttpHelper;
import com.seeyon.ocip.common.message.Message;
import com.seeyon.ocip.common.message.MessageHead;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.common.utils.LogUtils;
import com.seeyon.ocip.online.OnlineChecker;

/**
 * 对外模块-输出服务 功能： 控制整个平台主动对外输出的数据，报文格式等。 *
 *
 * @author linx
 */
public class ExtIntfOutServer {

    /**
     * 调用对外
     *
     * @param module    调动的远程模块
     * @param data      业务参数
     * @param optUser   操作用户
     * @param optSystem 操作系统
     * @return {@link Message}
     * @throws CommonException
     */
    public static String excRemoteNode(String module, String data, String optUser, String optSystem) throws CommonException {
		if (!OnlineChecker.isOnline()) {
			throw new CommonException("NET-0001", OnlineChecker.OFFLINE_ERROR);
		}
        try {
            String managerUrl = Global.getConfig("serverAddr");
            if (StringUtils.isBlank(managerUrl)) {
                throw new CommonException("数据平台接入地址为空");
            }
            String token = Global.getConfig("sysCode");
            if (StringUtils.isBlank(token)) {
                throw new CommonException("数据平台接入系统标识码为空");
            }
            // 数据加密，封包等动作
            MessageHead req = new MessageHead();
            req.setModule(module);
            // 设置token ,参数加密等
            req.setToken(token);
            // 请求设置
            req.setRequestId(MessageHead.createRequestId());
            req.setAsyn(false);
            req.setOptUser(optUser);
            req.setOptSystem(optSystem);
            Message requestInfo = new Message();
            // linx 这段代码没有用...
            requestInfo.setVersion(Global.getConfig("ocip.api.version"));
            requestInfo.setHead(req);
            
            requestInfo.setBody(data);
            requestInfo.setFormat(Global.getConfig("sdk.format"));
            try {
                LogUtils.debug(ExtIntfOutServer.class, "接口[" + module + "] 请求：" + JSONObject.toJSONString(requestInfo), false);
                data = URLEncoder.encode(data, "utf-8");
                String str = module + "|token=" + token + "&head=" + JSONObject.toJSONString(req) + "&body=" + data + "&version=" + Global.getConfig("ocip.api.version") + "&format=" + Global.getConfig("sdk.format");
                String rtn = HttpHelper.sendRPCClient("http://" + managerUrl + "/services/DataController?wsdl", str);
                LogUtils.debug(ExtIntfOutServer.class, "接口[" + module + "] 返回：" + rtn, false);
                return rtn;
            } catch (Exception e) {
                throw new CommonException("NET-0001", "网络访问错误！", e);
            }
        } catch (Exception e) {
            throw new CommonException("请求发生错误", e);
        }
    }
}
