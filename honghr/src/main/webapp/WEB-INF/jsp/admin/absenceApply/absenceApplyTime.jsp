<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>休假时长统计</title>
</head>
<body>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>员工姓名</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.employeeName}" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>员工部门</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.departments[0].departmentName}" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月加班时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.overTimeWorkHourMonth}<c:if test="${absenceApplyTimeVo.overTimeWorkHourMonth!=null && absenceApplyTimeVo.overTimeWorkHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>累计加班时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.overTimeWorkHourToTal}<c:if test="${absenceApplyTimeVo.overTimeWorkHourToTal!=null && absenceApplyTimeVo.overTimeWorkHourToTal!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月已调休时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.leaveHourMonth}<c:if test="${absenceApplyTimeVo.leaveHourMonth!=null && absenceApplyTimeVo.leaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>剩余可调休时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.remainingLeaveHour}<c:if test="${absenceApplyTimeVo.remainingLeaveHour!=null && absenceApplyTimeVo.remainingLeaveHour!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>今年累计年假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.annualHourThisYear}<c:if test="${absenceApplyTimeVo.annualHourThisYear!=null && absenceApplyTimeVo.annualHourThisYear!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>今年已休年假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.annualHour}<c:if test="${absenceApplyTimeVo.annualHour!=null && absenceApplyTimeVo.annualHour!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>抵扣去年年假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.decRemainingHourLastYear}<c:if test="${absenceApplyTimeVo.decRemainingHourLastYear!=null && absenceApplyTimeVo.decRemainingHourLastYear!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>今年剩余年假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.remainingAnnualHourThisYear}<c:if test="${absenceApplyTimeVo.remainingAnnualHourThisYear!=null && absenceApplyTimeVo.remainingAnnualHourThisYear!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>去年剩余年假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.remainingAnnualHourLastYear}<c:if test="${absenceApplyTimeVo.remainingAnnualHourLastYear!=null && absenceApplyTimeVo.remainingAnnualHourLastYear!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>剩余年假总时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.remainingAnnualHour}<c:if test="${absenceApplyTimeVo.remainingAnnualHour!=null && absenceApplyTimeVo.remainingAnnualHour!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月休病假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.sickLeaveHourMonth}<c:if test="${absenceApplyTimeVo.sickLeaveHourMonth!=null && absenceApplyTimeVo.sickLeaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月休事假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.casualLeaveHourMonth}<c:if test="${absenceApplyTimeVo.casualLeaveHourMonth!=null && absenceApplyTimeVo.casualLeaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月休公假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.publicLeaveHourMonth}<c:if test="${absenceApplyTimeVo.publicLeaveHourMonth!=null && absenceApplyTimeVo.publicLeaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月休婚假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.marriageLeaveHourMonth}<c:if test="${absenceApplyTimeVo.marriageLeaveHourMonth!=null && absenceApplyTimeVo.marriageLeaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月休丧假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.bereavementLeaveHourMonth}<c:if test="${absenceApplyTimeVo.bereavementLeaveHourMonth!=null && absenceApplyTimeVo.bereavementLeaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月休产检假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.maternityLeaveHourMonth}<c:if test="${absenceApplyTimeVo.maternityLeaveHourMonth!=null && absenceApplyTimeVo.maternityLeaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
	<div class="form-group w_honghr_input" style="padding-bottom:70px;">
		<label for="disabledSelect" class="col-sm-2 control-label"><b class="z_common_star">*</b>本月休陪产假时长</label>
		<div class="col-sm-10">
			<input class="form-control form-control_readonly" type="text" value="${absenceApplyTimeVo.paternityLeaveHourMonth}<c:if test="${absenceApplyTimeVo.paternityLeaveHourMonth!=null && absenceApplyTimeVo.paternityLeaveHourMonth!=''}">小时</c:if>" readonly="readonly"/>
		</div>
	</div>
</body>
</html>