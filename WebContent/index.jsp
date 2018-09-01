<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
<title>基础数据上传</title>
<script src="${pageContext.request.contextPath}/js/jquery-1.9.1.js"></script>
<style type="text/css">
.bu {
	text-decoration: none;
	background: #4dc86f;
	color: #f2f2f2;
	padding: 10px 30px 10px 30px;
	font-size: 16px;
	font-family: 微软雅黑, 宋体, Arial, Helvetica, Verdana, sans-serif;
	font-weight: bold;
	border-radius: 3px;
	-webkit-transition: all linear 0.30s;
	-moz-transition: all linear 0.30s;
	transition: all linear 0.30s;
}

table {
	border-collapse: collapse;
	margin: 0 auto;
	text-align: center;
}

table td, table th {
	border: 1px solid #cad9ea;
	color: #666;
	height: 30px;
}

table thead th {
	background-color: #CCE8EB;
	width: 100px;
}

table tr:nth-child(odd) {
	background: #fff;
}

table tr:nth-child(even) {
	background: #F5FAFA;
}
</style>
</head>

<body style="text-align: center;">
	<div style="margin-top: 30px;">
		<button onclick="uploadUnit();" class="bu">上报组织机构</button>
		<label id="orgmsg" style="color: red;"></label>
	</div>
	<hr>

	<div style="margin-top: 30px;">
		<dvi>发送公文</dvi>
		<div align="center">
			<table border="1">
				<tr>
					<td>公文标题：</td>
					<td><input type="text" name="title" id="title"
						style="width: 300px;"></td>
				</tr>
				<!-- <tr>
					<td>收文单位ID：</td>
					<td><input type="text" name="recOrgID" id="recOrgID"
						style="width: 300px;"></td>
				</tr>
				<tr>
					<td>收文单位名称：</td>
					<td><input type="text" name="recOrgName" id="recOrgName"
						style="width: 300px;"></td>
				</tr> -->
			</table>
			<button onclick="sendOrg();" class="bu">发送</button>
			<label id="msg" style="color: red;"></label>
		</div>
	</div>
	<hr>
	
	<div>
		<div align="center">
			<table border="1">
				<tr>
					<td align="center" colspan="3">签收公文</td>
				</tr>
				<tr>
					<td>签收公文ID：</td>
					<td>
						<div>
							公文ID：<input type="text" name="accepted" id="accepted" style="width: 250px;">
						</div>
						<div>
							detailId：<input type="text" name="detailId" id="detailId" style="width: 250px;">
						</div>
						<div>
							EXCHNO：<input type="text" name="exchNo" id="exchNo" style="width: 250px;">
						</div>
					</td>
					<td style="width: 200px;" align="left" rowspan="2">
						<span>
							<button onclick="accepted();" class="bu">签收</button>
						</span>
						<span style="padding-left: 30px;">
							<label id="acceptedMsg" style="color: red;"></label>
						</span>
					</td>
				</tr>
				<tr>
					<td>签收后回复：</td>
					<td>
						<input type="text" name="acceptedComment" id="acceptedComment" style="width: 250px;">
					</td>
				</tr>
				<!-- 撤销公文 -->
				<tr>
					<td align="center" colspan="3">撤销公文</td>
				</tr>
				 <tr>
					<td>撤销公文ID：</td>
					<td>
						<div><input type="text" name="revoked" id="revoked" style="width: 250px;"></div>
						<div><input type="text" name="revokedrelaid" id="revokedrelaid" style="width: 250px;"></div>
						
					</td>
					<td style="width: 200px;" align="left" rowspan="2">
						<span>
							<button onclick="revoked();" class="bu">撤销</button>
						</span>
						<span style="padding-left: 30px;">
							<label id="revokedMsg" style="color: red;"></label>
						</span>
					</td>
				</tr>
				<tr>
					<td>撤销原因：</td>
					<td>
						<input type="text" name="revokedComment" id="revokedComment" style="width: 250px;">
					</td>
				</tr> 
				<!-- 回退公文 -->
				<tr>
					<td align="center" colspan="3">回退公文</td>
				</tr>
				 <tr>
					<td>回退公文ID：</td>
					<td>
						<div>
							公文ID：<input type="text" name="stepBackId" id="stepBackId" style="width: 250px;">
						</div>
						<div>
							detailId：<input type="text" name="stepBackDetailId" id="stepBackDetailId" style="width: 250px;">
						</div>
						<div>
							EXCHNO:<input type="text" name="stepBackExchNo" id="stepBackExchNo" style="width: 250px;">
						</div>
					</td>
					<td style="width: 200px;" align="left" rowspan="3">
						<span>
							<button onclick="stepBack();" class="bu">回退</button>
						</span>
						<span style="padding-left: 30px;">
							<label id="stepBackMsg" style="color: red;"></label>
						</span>
					</td>
				</tr>
				<!-- <tr>
					<td>回退原因：</td>
					<td>
						<input type="text" name="stepBackComment" id="stepBackComment" style="width: 250px;">
					</td>
					
				</tr> -->
			</table>
		</div>
	</div>
	<hr>
</body>

<script type="text/javascript">
	function uploadUnit() {
		$.ajax({
			type : "POST",
			url : "${pageContext.request.contextPath}/uploadUnit",
			data : {
				"type" : 'org'
			},
			success : function(msg) {
				var obj = JSON.parse(msg);
				if ("success" == obj.msg) {
					$('#orgmsg').html('同步成功');
				} else {
					$('#orgmsg').html('同步失败');
				}

			},
			error : function(msg) {
				alert(msg);
			}
		});
	}

	function sendOrg() {
		$('#msg').html('');
		$.ajax({
			type : "POST",
			url : "${pageContext.request.contextPath}/uploadUnit",
			data : {
				"type" : 'send',
				"title" : $('#title').val()
			},
			success : function(msg) {
				var obj = JSON.parse(msg);
				if ("success" == obj.msg) {
					$('#msg').html('发送公文成功');
				} else {
					$('#msg').html('发送公文失败');
				}
			},
			error : function(msg) {
				alert(msg);
			}
		});
	}
	
	function revoked() {
		$('#revokedMsg').html('');
		$.ajax({
			type : "POST",
			url : "${pageContext.request.contextPath}/uploadUnit",
			data : {
				"type" : 'revoked',
				"revokedId" : $('#revoked').val(),
				"revokedrelaid" : $('#revokedrelaid').val(),
				"comment":$('#revokedComment').val()
			},
			success : function(msg) {
				var obj = JSON.parse(msg);
				if ("success" == obj.msg) {
					$('#revokedMsg').html('发送成功');
				} else {
					$('#revokedMsg').html('发送失败');
				}
			},
			error : function(msg) {
				alert(msg);
			}
		});
	}
	
	function accepted() {
		$('#acceptedMsg').html('');
		$.ajax({
			type : "POST",
			url : "${pageContext.request.contextPath}/uploadUnit",
			data : {
				"type" : 'accepted',
				"acceptedId" : $('#accepted').val(),
				"detailId" : $('#detailId').val(),
				"exchNo" : $('#exchNo').val()
			},
			success : function(msg) {
				var obj = JSON.parse(msg);
				if ("success" == obj.msg) {
					$('#acceptedMsg').html('签收成功');
				} else {
					$('#acceptedMsg').html('签收失败');
				}
			},
			error : function(msg) {
				alert(msg);
			}
		});
	}
	
	function stepBack() {
		$('#stepBackMsg').html('');
		$.ajax({
			type : "POST",
			url : "${pageContext.request.contextPath}/uploadUnit",
			data : {
				"type" : 'stepBack',
				"stepBackId" : $('#stepBackId').val(),
				"stepBackDetailId" : $('#stepBackDetailId').val(),
				"stepBackExchNo" : $('#stepBackExchNo').val()
			},
			success : function(msg) {
				var obj = JSON.parse(msg);
				if ("success" == obj.msg) {
					$('#stepBackMsg').html('发送成功');
				} else {
					$('#stepBackMsg').html('发送失败');
				}
			},
			error : function(msg) {
				alert(msg);
			}
		});
	}
</script>
</html>