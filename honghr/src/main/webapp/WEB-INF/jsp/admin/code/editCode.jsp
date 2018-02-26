<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<script src="${ctx }/resources/js/admin/code/code.js"></script>
<style>
.formError{
	position:absolute;
	top:46px !important;
	left:-106px !important;
}
.formError .formErrorArrow{
	margin-left:101px;
}
</style>


<form role="form" id="editForm">
	<input name="codeId" type="hidden" value="${code.codeId}"></input>
	<div class="form-group w_honghr_input">
		<label for="disabledSelect" class="col-sm-2 control-label"><b
			class="z_common_star">*</b>字段名</label>
		<div class="col-sm-10">
			<input class="form-control validate[required]" name="codeName"
				value="${code.codeName}" type="text" placeholder="请输入字段名"
				maxlength="20"></input>
		</div>
	</div>
	<c:if test="${!empty code.codeId}">
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b
				class="z_common_star"></b>父级</label>
			<div class="col-sm-10" title="父级无法修改">
				<select name="codeParentId"
					class="form-control input-sm w_department_name" 
					disabled="disabled">
					<option value="">请选择父级(不选默认为父级)</option>
					<c:forEach items="${list}" var="parentCode">
						<c:choose>
							<c:when test="${code.codeParentId == parentCode.codeId}">
								<option value="${parentCode.codeId}" selected>${parentCode.codeName}</option>
							</c:when>
							<c:otherwise>
								<option value="${parentCode.codeId}">${parentCode.codeName}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</div>
		</div>
	</c:if>
	<c:if test="${empty code.codeId}">
		<div class="form-group w_honghr_input">
			<label for="disabledSelect" class="col-sm-2 control-label"><b
				class="z_common_star">*</b>父级</label>
			<div class="col-sm-10">
				<select name="codeParentId"
					class="form-control input-sm w_department_name">
					<option value="">请选择父级(不选默认为父级)</option>
					<c:forEach items="${list}" var="parentCode">
						<c:choose>
							<c:when test="${code.codeParentId == parentCode.codeId}">
								<option value="${parentCode.codeId}" selected>${parentCode.codeName}</option>
							</c:when>
							<c:otherwise>
								<option value="${parentCode.codeId}">${parentCode.codeName}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</div>
		</div>
	</c:if>
</form>
<div class="modal-footer">
	<button type="button" class="btn btn-primary" id="save_btn">保存</button>
	<button type="button" class="btn btn-default" data-dismiss="modal"
		onclick="history.go(-1);">返回</button>
</div>