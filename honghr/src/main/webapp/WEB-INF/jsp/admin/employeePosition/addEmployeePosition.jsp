<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>员工关系管理</title>
<link href="${ctx }/resources/css/common/nstyle.css" type="text/css" rel="stylesheet">
</head>
<body>
	<script type="text/javascript" src="${ctx}/resources/js/admin/employeePosition/addEmployeePosition.js"></script>
	<script type="text/javascript" src="${ctx}/resources/js/admin/position/position.js"></script>
	<form id="applyForm" class="mainForm">
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b
				class="z_common_star">*</b>员工姓名</label>
			<div class="col-sm-10">
				<select name="employeeId"
					class="form-control input-sm w_employee_name">
					<option value="">请选择要分配的员工姓名</option>
					<c:forEach items="${employeeList}" var="employee">
						<option value="${employee.employeeId}">${employee.employeeName}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b
				class="z_common_star">*</b>选择部门</label>
			<div class="col-sm-10">
				<select name="departmentNum"
					onchange="getPosition(this)"
					class="form-control input-sm w_department_name" id="departmentFlag">
					<option value="">请选择要分配的部门名称</option>
					<c:forEach items="${departmentList}" var="department">
						<option value="${department.departmentNum}">${department.departmentName}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b
				class="z_common_star">*</b>选择职位</label>
			<div class="col-sm-10">
				<select name="positionNum"
					class="form-control input-sm w_position_name" id="positionDiv">
					<option value="">请选择要分配的职位名称</option>
				</select>
			</div>
		</div>
		<div class="modal-footer">
			<button type="button" class="btn btn-primary save_form"
				id="save_submit">保存</button>
			<button type="button" class="btn btn-default" data-dismiss="modal"
				onclick="history.go(-1);">返回</button>
		</div>
	</form>
	<script type="text/javascript">
	function getPosition(obj){
		var departmentNum = obj.value;
		$.ajax({
    		url: ctx+"/admin/employeePosition/findPosition",
    		type: "get",
    		async: true,
    		data: {"departmentNum" : departmentNum},
    		success: function(data){
    			if(data.errorCode =="000000"){
    				var HTMLArray = [];
    				HTMLArray.push("<option value=''>请选择要分配的职位名称</option>");
    				$(data.data).each(function() {
    					HTMLArray.push("<option value="+this.positionNum+">"+this.positionName+"</option>");
    				})
    				$("#positionDiv").html("");
    				$("#positionDiv").append(HTMLArray.join(""))
    			}
    			else{
    				layer.msg(data.errorMessage,{time:1000});
    			}
    		},
    		error: function(){
    		}
    	}); 
	}
</script>
</body>
</html>