<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>出差申请单</title>
</head>
<body>
	<script type="text/javascript" src="${ctx}/resources/js/admin/businessTripApply/businessTripApplyCommon.js"></script>
	<script type="text/javascript" src="${ctx}/resources/js/admin/businessTripApply/addBusinessTripApply.js"></script>
	<form id="applyForm" class="mainForm">
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差单编号</label>
			<div class="col-sm-10">
				<input class="form-control form-control_readonly" type="text" name="businessTripApplyNum" value="${businessTripApplyVo.businessTripApplyNum}" readonly="readonly"/>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差人</label>
			<div class="col-sm-10">
				<input class="form-control form-control_readonly" type="text" name="applyEmployeeName" value="${businessTripApplyVo.applyEmployeeName}" readonly="readonly"/>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>所属部门</label>
			<div class="col-sm-10">
				<c:choose>
					<c:when test="${fn:length(departmentList) > 1}">
						<select name="applyDepartmentNum" class="form-control input-sm">
							<c:forEach items="${departmentList}" var="department">
								<option value="${department.departmentNum}">${department.departmentName}</option>
							</c:forEach>
						</select>
					</c:when>
					<c:otherwise>
						<input class="form-control form-control_readonly" type="text" value="${departmentList[0].departmentName}" readonly="readonly"/>
						<input type="hidden" name="applyDepartmentNum" value="${departmentList[0].departmentNum}" />
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差类型</label>
			<div class="col-sm-10">
				<c:forEach items="${applyTypeList}" var="code">
					<label class="w_honghr_radio_label" title="${code.codeName}" >
						<input type="radio" name="applyType" class="w_honghr_radio" value="${code.codeValue}" />${code.codeName}
					</label>
				</c:forEach>
			</div>
		</div>
		<div class="form-group w_honghr_input w_edit_textArea clearfix">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差事由</label>
			<div class="col-sm-10">
			 	<textarea name="applyReason" class="form-control w_honghr_textArea" placeholder="请输入出差事由" ></textarea> 
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差始发地</label>
			<div class="col-sm-5">
			 	<select class="form-control input-sm beginProvince">
			 		<option class="defaultChoice" value="-1">请选择</option>
			 		<c:forEach items="${provinceList}" var="code">
						<option class="defaultChoice" value="${code.codeId}">${code.codeName}</option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-5 w_select_near">
			 	<select name="applyBeginAddress" class="form-control input-sm w_edit_showBeginCity">
					<option class="defaultChoice" value="-1">请选择</option>
				</select>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差目的地</label>
			<div class="col-sm-5">
			 	<select class="form-control input-sm endProvince">
					<option class="defaultChoice" value="-1">请选择</option>
					<c:forEach items="${provinceList}" var="code">
						<option class="defaultChoice" value="${code.codeId}">${code.codeName}</option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-5 w_select_near">
			 	<select name="applyEndAddress" class="form-control input-sm w_edit_showEndCity">
					<option class="defaultChoice" value="-1">请选择</option>
				</select>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差开始时间</label>
			<div class="col-sm-10">
				<input type="text" id="startDate" name="applyBeginTime" class="form-control w_date_input" placeholder="请输入开始时间" maxlength="100" onFocus="var endDate=$dp.$('endDate');WdatePicker({onpicked:function(){endDate.focus();},dateFmt:'yyyy-MM-dd H:00',isShowClear:false,maxDate:'#F{$dp.$D(\'endDate\',{d:0})}'})" onchange="changeDuration();" readonly="readonly"/>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差结束时间</label>
			<div class="col-sm-10">
				<input type="text" id="endDate" name="applyEndTime" class="form-control w_date_input" placeholder="请输入结束时间" maxlength="100" onFocus="WdatePicker({minDate:'#F{$dp.$D(\'startDate\',{d:0})}',dateFmt:'yyyy-MM-dd H:00',isShowClear:false})" onchange="changeDuration();" readonly="readonly"/>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>出差时长</label>
			<div class="col-sm-10">
				<input type="text" class="form-control form-control_readonly w_edit_applyDuration" value="0天" readonly="readonly" />
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>申请日期</label>
			<div class="col-sm-10">
				<input type="text" name="applyDateTime" class="form-control form-control_readonly" value="<fmt:formatDate type="both" value="${businessTripApplyVo.applyDateTime}" pattern="yyyy-MM-dd" />" readonly="readonly"/>
			</div>
		</div>
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>审批人流程</label>
			<div class="col-sm-10">
				<button type="button" id="showEmployee" class="btn btn-default" data-toggle="modal" data-target="#myModal_add">+添加</button>
			</div>
		</div>
		<div class="modal-footer">
			<button type="button" class="btn btn-default save_form" id="save_default">保存草稿</button>
			<button type="button" class="btn btn-primary save_form" id="save_submit">提交审批</button>
		</div>
	</form>
	<!-- 模态框（Modal） -->
	<div class="modal fade d_model_box" id="myModal_add" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true"   data-backdrop="static">
	    <div class="modal-dialog modal-show">
	        <div class="modal-content">
	        	<div class="modal-header">
	                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
	                	&times;
	                </button>
	                <h4 class="modal-title w_absence_title" id="myModalLabel">添加审批人</h4>
	            </div>
	            <div class="modal-body">
	           		<div class="form-group w_honghr_input">
						<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>部门名称</label>
						<div class="col-sm-10">
							<select class="form-control input-sm showDepartment">
								<option class="defaultChoice" value="-1">请选择</option>
							</select>
						</div>
					</div>
					<div class="form-group w_honghr_input">
						<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>员工姓名</label>
						<div class="col-sm-10">
							<select class="form-control input-sm showName">
								<option class="defaultChoice" value="-1">请选择</option>
							</select>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-primary" id="save_checkEmployee">保存</button>
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					</div>
				</div>
	        </div><!-- /.modal-content -->
	    </div><!-- /.modal -->
	</div>
</body>
</html>