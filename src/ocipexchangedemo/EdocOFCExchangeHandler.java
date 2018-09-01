package ocipexchangedemo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.seeyon.oa.exchange.OCIPServicesServlet;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.license.OcipKeyMananger;
import com.seeyon.ocip.common.org.OcipOrgUnit;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessHandler;
import com.seeyon.ocip.exchange.api.IBussinessService;
import com.seeyon.ocip.exchange.exceptions.BussinessException;
import com.seeyon.ocip.exchange.model.AttachmentFile;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.BIZExchangeData;
import com.seeyon.ocip.exchange.model.BIZMessage;
import com.seeyon.ocip.exchange.model.BussinessResult;
import com.seeyon.ocip.exchange.model.Organization;
import com.seeyon.ocip.exchange.model.PropertyValue;
import com.seeyon.ocip.exchange.model.edoc.EdocOperation;
import com.seeyon.ocip.exchange.model.edoc.OFCEdocObject;
import com.seeyon.ocip.exchange.model.edoc.SeeyonEdoc;
/**
 * 公文交换数据封装参考DEMO
 * @author Administrator
 *
 */
public class EdocOFCExchangeHandler implements IBussinessHandler {
	
	private IOrganizationManager organizationManager;

	/**
	 * 接收数据
	 */
	@Override
	public List<BussinessResult> exchangeReceive(BIZExchangeData in) throws BussinessException {
		List<BussinessResult> bussinessResults = new ArrayList<BussinessResult>();
		BIZMessage bussnissMessage = in.getBussnissMessage();
		/**
		 * TODO，收到文件时，需要保存
		 */
		String exchNo = in.getIdentifier();//交换号ID，需要保存
		String groupId = in.getGroupId();//需要保存，groupId为公文ID
		Organization organization = in.getSender();
		Address creater = organization.getIdentification();
		BIZContentType contentType = bussnissMessage.getContentType();
		List<Organization> reciList = in.getRecivers();
		for (Organization organization2 : reciList) {
			BussinessResult result = new BussinessResult();
			result.setCode("0");
			result.setIdentifier(in.getIdentifier());
			result.setOrganization(organization2);
			result.setMessage("公文接受成功");
			bussinessResults.add(result);
		}
		Long mainId = null;
		HashMap<Long, Long> detailIds = null;
		Long relationId = null;
		try {
			
			if (BIZContentType.OFC.equals(contentType)) {
				OFCEdocObject ofcEdocObject = (OFCEdocObject) bussnissMessage.getContent();
				SeeyonEdoc seeyonEdoc = (SeeyonEdoc)ofcEdocObject.getExtendAttr();
				String subject = seeyonEdoc.getSubject();
				relationId = Long.parseLong(seeyonEdoc.getDocumentIdentifier());
				mainId = Long.parseLong(seeyonEdoc.getMainId());
				detailIds = seeyonEdoc.getDetailIds();
				// TODO 记得完善，需要将本地的单位id转换为OCIP的单位id再进行比较
				Long localUnitId = 2723437946474089882l;
				Long myDetail = null;
				if (detailIds != null && !detailIds.isEmpty()) {
					for (Entry<Long, Long> entry : detailIds.entrySet()) {
						Long key = entry.getKey();// OCIP的单位id
						if (localUnitId.equals(key)) {
							myDetail = entry.getValue();
							break;
						}
					}
				}
				System.out.println("接收到公文:" + subject + " 公文ID为:" + groupId + " myDetail:" + myDetail + " exchNo:" + exchNo);
			}
		} catch (NumberFormatException e) {
			for (BussinessResult bussinessResult : bussinessResults) {
				bussinessResult.setCode("2");
				bussinessResult.setMessage("公文交换接收方接收数据处理失败！");
			}
		} finally {
			// TODO 记得完善，需要将本地的单位id转换为OCIP的单位id再进行比较
			Long localUnitId = 2723437946474089882l;
			if (detailIds != null && !detailIds.isEmpty()) {
				for (Entry<Long, Long> entry : detailIds.entrySet()) {
					Long key = entry.getKey();// OCIP的单位id
					if (localUnitId.equals(key)) {
						Long value = entry.getValue();
						if (value != null) {
							receiptEdoc(mainId, value, "0", exchNo, groupId);
						}
					}
				}
			}

		}
		
		
		
		
		return bussinessResults;
	}
	
	/**
	 * 异构系统接收到公文后，需要给OCIP发送一个回执信息，告知OCIP异构系统收到的数据
	 * @param mainId
	 * @param detailId
	 * @param exchangeStatus
	 */
	private void receiptEdoc(long mainId, long detailId,String exchangeStatus ,String relationId , String groupId) {
		try {
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("mainId", mainId);
			param.put("detailId", detailId);
			param.put("exchangeStatus", exchangeStatus);
			param.put("relationId", relationId);
			param.put("groupId", groupId);
			param.put("edocOperation",  EdocOperation.RECEIVED);
			getBussinessService().fireExchange(BIZContentType.RET, param);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	private IBussinessService getBussinessService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getBussinessService();
	}

	/**
	 * 发送数据
	 */
	@Override
	public BIZExchangeData exchangeSend(Map<String, Object> map) throws BussinessException {
		//封装公文数据包
		BIZExchangeData bizData = new BIZExchangeData();

		bizData.setIdentifier(UUID.randomUUID().toString().replaceAll("-", ""));
		
		//公文发送方信息
		Organization sender = new Organization();
		Address createUser = new Address();
		String localSystemCode = OcipKeyMananger.getSysCode();//本系统注册id值
		createUser.setResource(localSystemCode);
		//String userID = "138913789043295577";
		String userID = OCIPServicesServlet.sendMemLocalId;
		createUser.setId(userID);//发文人员本地ID
		createUser.setType(IConstant.AddressType.member.name());
		//公文发送人员名称
		String userName = OCIPServicesServlet.sendMemName;
		createUser.setName(userName);
		//String userPlatID = "-8467766985147790299";
		//TODO 将本地的人员ID转换为OCIP平台的人员ID
		String userPlatID = organizationManager.getPlatformId(createUser);
		createUser.setId(userPlatID);
		createUser.setResource("0");
		sender.setIdentification(createUser);
		sender.setName(createUser.getName());
		bizData.setSender(sender);
		
		HashMap<Long, Long> orgIdAndDetailId = new HashMap<Long, Long>();
		
		//TODO 需要传递公文的ID
		//公文id
		int intFlag = (int)(Math.random() * 1000000);
		String colSummaryId = String.valueOf(intFlag);
		String groupId = colSummaryId;
		bizData.setGroupId(groupId);//groupId的值设置为公文ID
		
		//公文接收者，接收者可以有多个,DEMO中只封装了一个接收者
		List<Organization> recivers = new ArrayList<Organization>();
		Organization reciverOrg = new Organization();
		//接收单位信息
		//接收单位本地ID
		String recOrgID = OCIPServicesServlet.recOrgLocalId;
		//接收单位名称
		String recOrgName = OCIPServicesServlet.recOrgName;
		Address recAdd = new Address();
		recAdd.setResource("0");
		recAdd.setName(recOrgName);
		recAdd.setType("account");//接受者类型设置为account
		//接收单位的ID
		//将接收单位的本地ID转换为OCIP对应的单位ID
		OcipOrgUnit account = organizationManager.getAccount(recOrgID , OCIPServicesServlet.hzSystenCode);//OCIP单位实体
		String accountId = account.getId();
		recOrgName = account.getName();
		recAdd.setId(accountId);
		reciverOrg.setIdentification(recAdd);
		//orgIdAndDetailId必须设置，格式如下，key为接受者的id，value为公文id
		orgIdAndDetailId.put(Long.valueOf(recAdd.getId()), Long.valueOf(colSummaryId));
		reciverOrg.setName(recOrgName);
		//接收者可以有多个，按照上面的格式封装
		recivers.add(reciverOrg);
		
		OFCEdocObject object = new OFCEdocObject();
		SeeyonEdoc seeyonEdoc = new SeeyonEdoc();
		BIZMessage bizMessage = new BIZMessage();
		seeyonEdoc.setDetailIds(orgIdAndDetailId);
		
		//发送者所在单位OCIP平台id
		//发文单位本地单位id
		//String unitID = "2556842553182670622";
		String unitID = OCIPServicesServlet.sendOrgLocalId;
		//将发文者所在本地单位id转换为OCIP平台对应的单位ID
		OcipOrgUnit unit = organizationManager.getAccount(unitID , localSystemCode);
		//发文者所在单位对应的平台id
		String ocipOrgUnitId = unit.getId().toString();
		//String ocipOrgUnitId = "2723437946474089882";
		object.setIssueOrganization(ocipOrgUnitId);
		
		seeyonEdoc.setDocumentIdentifier(colSummaryId);
		//mainID设置为公文ID
		seeyonEdoc.setMainId(colSummaryId);
		//初始化设置form表单中的值
		String title = (String) map.get("title");
		System.out.println("公文标题:" + title + " ID:" + colSummaryId);
		initFormData(object, seeyonEdoc, title, colSummaryId);
		//设置公文正文
		initGovdocContentAll(object);
		//设置附件
		initGovdocAttachment(bizMessage);
		
		bizData.setRecivers(recivers);
		
		//设置公文标题 colSummary.getSubject()
		String subject = title;
		bizData.setSubject(subject);
		
		bizMessage.setContentType(BIZContentType.OFC);
		object.setTitle(subject);
		object.setExtendAttr(seeyonEdoc);
		bizMessage.setContent(object);
		
		//发送源地址信息
		Address source = new Address();
		source.setId(localSystemCode);
		source.setName(localSystemCode);
		bizData.setSource(source);
		bizData.setBussnissMessage(bizMessage);
		return bizData;
	}
	

	//初始化设置附件
	private void initGovdocAttachment(BIZMessage bizMessage) {
		String path = this.getClass().getClassLoader().getResource("").getPath();
		// 可以设置多个附件
		List<AttachmentFile> attfiles = new ArrayList<AttachmentFile>();
		AttachmentFile att = new AttachmentFile();
		File file = new File(path + "file" + File.separator + "file1.docx");
		int intFlag = (int)(Math.random() * 1000000);
		att.setIdentification(String.valueOf(intFlag));
		att.setName(file.getName());
		att.setSize(file.length());
		try {
			byte[] bs = FileUtils.readFileToByteArray(file);
			att.setData(bs);
			attfiles.add(att);
			bizMessage.setAttachments(attfiles);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	//初始化设置公文正文
	private void initGovdocContentAll(OFCEdocObject object) {
		String path = this.getClass().getClassLoader().getResource("").getPath();
		File file = new File(path + "file" + File.separator + "help.pdf");
		// OfficeWord或者Pdf，正文格式为PDF，contentType="Pdf"，正文格式为word，contentType="OfficeWord"
		String contentType = "Pdf";
		object.setContentType(contentType);
		//正文格式为word： mimeType=application/msword 或者 正文格式为PDF：mimeType=application/pdf
		String mimeType = "application/pdf";
		object.setContentMimeType(mimeType);
		byte[] bs;
		try {
			bs = FileUtils.readFileToByteArray(file);
			object.setContent(bs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//初始化设置form表单中的值
	private void initFormData(OFCEdocObject object, SeeyonEdoc seeyonEdoc, String title, String summaryId) {
		// 紧急程度
		String urgentLevel = "普通";
		seeyonEdoc.setUrgentLevel(urgentLevel);
		// 标题
		seeyonEdoc.setSubject(title);
		// 发文单位名称
		seeyonEdoc.setSendUnit(OCIPServicesServlet.sendOrgName);
		// 送往单位名称
		seeyonEdoc.setSendTo(OCIPServicesServlet.recOrgName);
		// 密级
		seeyonEdoc.setSecretLevel("普通");
		seeyonEdoc.setDocumentIdentifier(summaryId);
		PropertyValue docMark = new PropertyValue();
		docMark.setDisplay("222");
		// 公文文号
		seeyonEdoc.setDocMark(docMark);

	}

	@Override
	public BIZContentType type() {
		return BIZContentType.OFC;
	}

	public IOrganizationManager getOrganizationManager() {
		return organizationManager;
	}

	public void setOrganizationManager(IOrganizationManager organizationManager) {
		this.organizationManager = organizationManager;
	}

}
