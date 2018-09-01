package com.seeyon.ocip.org.agent;

import com.seeyon.ocip.common.ExtIntfOutServer;
import com.seeyon.ocip.common.exceptions.CommonException;
import com.seeyon.ocip.common.exceptions.InterfaceException;
import com.seeyon.ocip.common.utils.Global;

/**
 * 单位代理服务
 * 
 * @author wxt.touxin
 * @version 2017-6-12
 */
public class UnitAgent extends BaseAgent<UnitAgent> {

	@Override
	public String uploadFull(String data) throws InterfaceException {
		try {
			return ExtIntfOutServer.excRemoteNode(Global.getConfig("unit.uploadFull"), data, getOptUser(), getOptSystem());
		} catch (CommonException e) {
			throw new InterfaceException(e.getErrorCode(), e.getErrorMsg(), e);
		}
	}

	@Override
	public String queryFull(String data) throws InterfaceException {
		try {
			return ExtIntfOutServer.excRemoteNode(Global.getConfig("unit.queryFull"), data, getOptUser(), getOptSystem());
		} catch (CommonException e) {
			throw new InterfaceException(e.getErrorCode(), e.getErrorMsg(), e);
		}
	}

	@Override
	public String upload(String data) throws InterfaceException {
		try {
			return ExtIntfOutServer.excRemoteNode(Global.getConfig("unit.upload"), data, getOptUser(), getOptSystem());
		} catch (CommonException e) {
			throw new InterfaceException(e.getErrorCode(), e.getErrorMsg(), e);
		}
	}

	@Override
	public String delete(String data) throws InterfaceException {
		try {
			return ExtIntfOutServer.excRemoteNode(Global.getConfig("unit.delete"), data, getOptUser(), getOptSystem());
		} catch (CommonException e) {
			throw new InterfaceException(e.getErrorCode(), e.getErrorMsg(), e);
		}
	}

	@Override
	public String queryUpdate(String data) throws InterfaceException {
		try {
			return ExtIntfOutServer.excRemoteNode(Global.getConfig("unit.queryUpdate"), data, getOptUser(), getOptSystem());
		} catch (CommonException e) {
			throw new InterfaceException(e.getErrorCode(), e.getErrorMsg(), e);
		}
	}

	@Override
	public String queryDelete(String data) throws InterfaceException {
		try {
			return ExtIntfOutServer.excRemoteNode(Global.getConfig("unit.queryDelete"), data, getOptUser(), getOptSystem());
		} catch (CommonException e) {
			throw new InterfaceException(e.getErrorCode(), e.getErrorMsg(), e);
		}
	}

	@Override
	public String uploadResult(String data) throws InterfaceException {
		try {
			return ExtIntfOutServer.excRemoteNode(Global.getConfig("unit.uploadResult"), data, getOptUser(), getOptSystem());
		} catch (CommonException e) {
			throw new InterfaceException(e.getErrorCode(), e.getErrorMsg(), e);
		}
	}
}
