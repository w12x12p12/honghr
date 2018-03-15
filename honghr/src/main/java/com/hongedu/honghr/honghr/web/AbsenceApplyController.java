
package com.hongedu.honghr.honghr.web;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hongedu.honghr.honghr.common.constant.CodeConstant;
import com.hongedu.honghr.honghr.common.constant.DataConstant;
import com.hongedu.honghr.honghr.common.constant.FileConstant;
import com.hongedu.honghr.honghr.common.constant.PageConstant;
import com.hongedu.honghr.honghr.common.constant.SessionConstant;
import com.hongedu.honghr.honghr.common.enums.EnumAbsenceApplyType;
import com.hongedu.honghr.honghr.common.enums.EnumApplyCheckStatus;
import com.hongedu.honghr.honghr.common.enums.EnumApplyStatus;
import com.hongedu.honghr.honghr.entity.AbsenceApplyCheck;
import com.hongedu.honghr.honghr.entity.Attachment;
import com.hongedu.honghr.honghr.entity.Code;
import com.hongedu.honghr.honghr.entity.Department;
import com.hongedu.honghr.honghr.entity.Employee;
import com.hongedu.honghr.honghr.entity.EmployeeAnnualLeave;
import com.hongedu.honghr.honghr.entity.vo.AbsenceApplyCheckVo;
import com.hongedu.honghr.honghr.entity.vo.AbsenceApplyTimeVo;
import com.hongedu.honghr.honghr.entity.vo.AbsenceApplyVo;
import com.hongedu.honghr.honghr.entity.vo.EmployeeVo;
import com.hongedu.honghr.honghr.entity.vo.OvertimeWorkApplyVo;
import com.hongedu.honghr.honghr.service.AbsenceApplyCheckService;
import com.hongedu.honghr.honghr.service.AbsenceApplyService;
import com.hongedu.honghr.honghr.service.CodeService;
import com.hongedu.honghr.honghr.service.DepartmentService;
import com.hongedu.honghr.honghr.service.EmployeeAnnualLeaveService;
import com.hongedu.honghr.honghr.service.EmployeeService;
import com.hongedu.honghr.honghr.service.MailService;
import com.hongedu.honghr.honghr.service.OvertimeWorkApplyService;
import com.hongedu.honghr.util.json.JsonResult;
import com.hongedu.honghr.util.page.Pager;
import com.hongedu.honghr.util.upload.UploadUtil;

/**
 * @author 王雪鹏 表对应的controller 2017/12/07 04:04:38
 */
@Controller
@RequestMapping("/admin/absenceApply")
public class AbsenceApplyController {
	@Autowired
	private EmployeeAnnualLeaveService employeeAnnualLeaveService;
	@Autowired
	private OvertimeWorkApplyService overtimeWorkApplyService;
	@Autowired
	private AbsenceApplyCheckService absenceApplyCheckService;
	@Autowired
	private AbsenceApplyService absenceApplyService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private CodeService codeService;

	/**
	 * 跳转至休假时长统计
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/absenceApplyTime", method = RequestMethod.GET)
	public String getAbsenceApplyTime(Model model) throws Exception {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			AbsenceApplyTimeVo absenceApplyTimeVo = new AbsenceApplyTimeVo();
			absenceApplyTimeVo.setEmployeeName(employee.getEmployeeName());// 添加员工姓名
			absenceApplyTimeVo.setDepartments(departmentService.findDepartmentListByEmployee(employee.getEmployeeId()));// 添加员工部门
			absenceApplyTimeVo.setOverTimeWorkHourMonth(
					overtimeWorkApplyService.getOvertimeWorkApplyDurationThisMonth(employee.getEmployeeId()));// 添加员工本月加班时长
			absenceApplyTimeVo.setOverTimeWorkHourToTal(
					overtimeWorkApplyService.getOvertimeWorkApplyDuration(employee.getEmployeeId()));// 添加员工累计加班时长
			String remainingLeaveHour = getRemainingDurationHour(absenceApplyTimeVo.getOverTimeWorkHourToTal(),
					absenceApplyService.getAbsenceApplyDuration(employee.getEmployeeId(),
							EnumAbsenceApplyType.LEAVE.getCode()));// 计算员工剩余可调休时长
			if (!JsonResult.FAILURE.equals(remainingLeaveHour)) {
				absenceApplyTimeVo.setRemainingLeaveHour(remainingLeaveHour);// 添加员工剩余可调休时长
			}
			String remainingAnnualLeaveHourLastYear = getRemainingAnnualLeaveHourLastYear(employee,
					getAnnualLeaveHourTotalLastYear(employee)) + "";// 获取员工去年剩余年假时长
			if (!JsonResult.FAILURE.equals(remainingAnnualLeaveHourLastYear)) {
				absenceApplyTimeVo.setRemainingAnnualHourLastYear(remainingAnnualLeaveHourLastYear);// 添加员工去年剩余年假时长
			}
			absenceApplyTimeVo.setAnnualHour(absenceApplyService.getAbsenceApplyDuration(employee.getEmployeeId(),
					EnumAbsenceApplyType.ANNUAL_LEAVE.getCode()));// 添加员工今年已休年假时长
			String annualLeaveHourTotalThisYear = getAnnualLeaveHourTotalThisYear(employee);// 计算员工今年累计年假时长
			if (!JsonResult.FAILURE.equals(annualLeaveHourTotalThisYear)) {
				absenceApplyTimeVo.setAnnualHourThisYear(annualLeaveHourTotalThisYear);// 添加员工今年累计年假时长
				if (StringUtils.isNotEmpty(remainingAnnualLeaveHourLastYear)) {
					EmployeeAnnualLeave employeeAnnualLeave = employeeAnnualLeaveService.getClearAnnualLeaveHour(
							employee.getEmployeeId(),
							Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "");
					// 判断员工去年年假是否清空
					if (employeeAnnualLeave != null) {
						absenceApplyTimeVo.setDecRemainingHourLastYear(
								getRemainingDurationHour(absenceApplyTimeVo.getRemainingAnnualHourLastYear(),
										employeeAnnualLeave.getRemainingHour()));// 添加去年年假抵扣时长
						String remainingAnnualHourThisYear = getRemainingDurationHour(annualLeaveHourTotalThisYear,
								getRemainingDurationHour(absenceApplyTimeVo.getAnnualHour(), getRemainingDurationHour(
										remainingAnnualLeaveHourLastYear, employeeAnnualLeave.getRemainingHour())));
						if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
							absenceApplyTimeVo.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余可调休年假时长
						}
						absenceApplyTimeVo.setRemainingAnnualHourLastYear("0");// 改变员工去年剩余可调休年假时长
					} else {
						// 判断员工去年剩余年假是否够用
						if (Integer.parseInt(remainingAnnualLeaveHourLastYear) >= Integer
								.parseInt(absenceApplyTimeVo.getAnnualHour())) {
							absenceApplyTimeVo.setDecRemainingHourLastYear(absenceApplyTimeVo.getAnnualHour());// 添加去年年假抵扣时长
							String remainingAnnualHourLastYear = getRemainingDurationHour(
									remainingAnnualLeaveHourLastYear, absenceApplyTimeVo.getAnnualHour());
							if (!JsonResult.FAILURE.equals(remainingAnnualHourLastYear)) {
								absenceApplyTimeVo.setRemainingAnnualHourLastYear(remainingAnnualHourLastYear);// 添加员工去年剩余年假时长
							}
							absenceApplyTimeVo.setRemainingAnnualHourThisYear(annualLeaveHourTotalThisYear);// 添加员工今年剩余可调休年假时长
						} else {
							absenceApplyTimeVo
									.setDecRemainingHourLastYear(absenceApplyTimeVo.getRemainingAnnualHourLastYear());// 添加去年年假抵扣时长
							String remainingAnnualHourThisYear = getRemainingDurationHour(annualLeaveHourTotalThisYear,
									getRemainingDurationHour(absenceApplyTimeVo.getAnnualHour(),
											remainingAnnualLeaveHourLastYear));
							if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
								absenceApplyTimeVo.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余可调休年假时长
							}
							absenceApplyTimeVo.setRemainingAnnualHourLastYear("0");// 改变员工去年剩余年假时长
						}
					}
				} else {
					absenceApplyTimeVo.setDecRemainingHourLastYear("0");// 添加去年年假抵扣时长
					String remainingAnnualHourThisYear = getRemainingDurationHour(annualLeaveHourTotalThisYear,
							absenceApplyTimeVo.getAnnualHour());// 获取员工今年剩余年假时长
					if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
						absenceApplyTimeVo.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余年假时长
					}
				}
			}
			// 添加员工剩余总年假时长
			if (StringUtils.isNotEmpty(absenceApplyTimeVo.getRemainingAnnualHourLastYear())
					&& StringUtils.isNotEmpty(absenceApplyTimeVo.getRemainingAnnualHourThisYear())) {
				absenceApplyTimeVo
						.setRemainingAnnualHour(Integer.parseInt(absenceApplyTimeVo.getRemainingAnnualHourLastYear())
								+ Integer.parseInt(absenceApplyTimeVo.getRemainingAnnualHourThisYear()) + "");
			}
			List<AbsenceApplyTimeVo> absenceApplyTimeVoList = absenceApplyService.findAbsenceApplyDurationListThisMonth(
					employee.getEmployeeId(),
					new String[] { EnumAbsenceApplyType.LEAVE.getCode(), EnumAbsenceApplyType.SICK_LEAVE.getCode(),
							EnumAbsenceApplyType.CASUAL_LEAVE.getCode(), EnumAbsenceApplyType.PUBLIC_LEAVE.getCode(),
							EnumAbsenceApplyType.MARRIAGE_LEAVE.getCode(),
							EnumAbsenceApplyType.BEREAVEMENT_LEAVE.getCode(),
							EnumAbsenceApplyType.MATERNITY_LEAVE.getCode(),
							EnumAbsenceApplyType.PATERNITY_LEAVE.getCode() });// 获取员工本月其余假期时长
			for (AbsenceApplyTimeVo newAbsenceApplyTimeVo : absenceApplyTimeVoList) {
				for (EnumAbsenceApplyType enumAbsenceApplyType : EnumAbsenceApplyType.values()) {
					if (enumAbsenceApplyType.getCode().equals(newAbsenceApplyTimeVo.getApplyType())) {
						// 动态添加员工本月其余假期时长
						absenceApplyTimeVo.getClass()
								.getMethod(enumAbsenceApplyType.getSetMonthMethodName(), new Class[] { String.class })
								.invoke(absenceApplyTimeVo, newAbsenceApplyTimeVo.getDuration());
						break;
					}
				}
			}
			model.addAttribute("absenceApplyTimeVo", absenceApplyTimeVo);
		}
		return "admin/absenceApply/absenceApplyTime";
	}

	/**
	 * 计算员工去年累计年假时长
	 * 
	 * @param employee
	 * @return
	 */
	private String getAnnualLeaveHourTotalLastYear(Employee employee) {
		Date workBeginTime = employee.getWorkBeginTime();
		Date entryTime = employee.getEntryTime();
		if (workBeginTime != null && entryTime != null) {
			try {
				int lastYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1;// 去年年份
				int entryYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(entryTime));// 入职年份
				if (entryYear > lastYear) {
					return 0 + "";
				}
				int workBeginYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(workBeginTime));
				int workYear = lastYear - workBeginYear + 1;
				Integer workYearCode = null;
				// 计算工作年限
				if (workYear >= 1 && workYear < 10) {
					workYearCode = 5;
				} else if (workYear >= 10 && workYear < 20) {
					workYearCode = 10;
				} else if (workYear >= 20) {
					workYearCode = 15;
				}
				Integer workMonthCode = null;
				// 判断是去年入职还是去年之前入职的
				if (entryYear == lastYear) {
					int entryMonth = Integer.parseInt(new SimpleDateFormat("M").format(entryTime));// 获取入职的月份
					int entryDay = Integer.parseInt(new SimpleDateFormat("d").format(entryTime));// 获取入职的日期
					// 判断入职时间是否为12月
					if (entryMonth < 12) {
						// 15日之后入职时间算下个月
						if (entryDay > 15) {
							++entryMonth;
						}
						workMonthCode = 13 - entryMonth;
					} else {
						// 判断入职时间是否在12月15日之后
						if (entryDay > 15) {
							return 0 + "";
						}
						workMonthCode = 1;
					}
				} else {
					workMonthCode = 12;
				}
				return (int) ((float) workYearCode / 12 * (float) workMonthCode * 8) + "";
			} catch (Exception e) {
				e.printStackTrace();
				return JsonResult.FAILURE;
			}
		}
		return JsonResult.FAILURE;
	}

	/**
	 * 计算员工今年累计年假时长
	 * 
	 * @param employee
	 * @return
	 */
	private String getAnnualLeaveHourTotalThisYear(Employee employee) {
		Date workBeginTime = employee.getWorkBeginTime();
		Date entryTime = employee.getEntryTime();
		if (workBeginTime != null && entryTime != null) {
			try {
				// 当前年份
				int nowYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
				int workBeginYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(workBeginTime));
				int workYear = nowYear - workBeginYear + 1;
				Integer workYearCode = null;
				// 计算工作年限
				if (workYear >= 1 && workYear < 10) {
					workYearCode = 5;
				} else if (workYear >= 10 && workYear < 20) {
					workYearCode = 10;
				} else if (workYear >= 20) {
					workYearCode = 15;
				}
				// 入职年份
				int entryYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(entryTime));
				// 计算进入公司的年限
				int entryCompanyYear = nowYear - entryYear;
				// 获取当前月份
				int nowMonth = Integer.parseInt(new SimpleDateFormat("M").format(new Date()));
				Integer workMonthCode = null;
				// 判断进入公司是否跨年
				if (entryCompanyYear == 0) {
					// 获取入职的月份
					int entryMonth = Integer.parseInt(new SimpleDateFormat("M").format(entryTime));
					workMonthCode = nowMonth - entryMonth;
					// 判断是否为非当月入职
					if (workMonthCode > 0) {
						// 获取入职的日期
						int entryDay = Integer.parseInt(new SimpleDateFormat("d").format(entryTime));
						// 该月15日之后入职算下个月
						if (entryDay > 15) {
							--workMonthCode;
						}
						// 12月份加一个月
						if (nowMonth == 12) {
							++workMonthCode;
						}
					}
				} else {
					workMonthCode = nowMonth;
					// 判断当前月份是否为12月
					if (nowMonth < 12) {
						--workMonthCode;
					}
				}
				// 计算员工今年年假时长
				return (int) ((float) workYearCode / 12 * (float) workMonthCode * 8) + "";
			} catch (Exception e) {
				e.printStackTrace();
				return JsonResult.FAILURE;
			}
		}
		return JsonResult.FAILURE;
	}

	/**
	 * 计算员工累计总年假时长(去年剩余未清空+今年)
	 * 
	 * @param employee
	 * @param lastYearHours
	 * @param thisYearHours
	 * @return
	 */
	private String getAllAnnualLeaveHours(Employee employee, String lastYearHours, String thisYearHours) {
		if (!JsonResult.FAILURE.equals(lastYearHours) && !JsonResult.FAILURE.equals(thisYearHours)) {
			if (employeeAnnualLeaveService.getIsClearAnnualLeaveHours(employee.getEmployeeId(),
					Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "")) {
				return 0 + Integer.parseInt(thisYearHours) + "";
			}
			return getRemainingAnnualLeaveHourLastYear(employee, lastYearHours) + Integer.parseInt(thisYearHours) + "";
		}
		return JsonResult.FAILURE;
	}

	/**
	 * 计算员工去年剩余年假
	 * 
	 * @param employee
	 * @param lastYearHours
	 * @return
	 */
	private int getRemainingAnnualLeaveHourLastYear(Employee employee, String lastYearHours) {
		// 获取去年剩余年假时长
		String remainingDuration = getRemainingDurationHour(lastYearHours,
				absenceApplyService.getAnnualHourLastYear(employee.getEmployeeId()));
		if (!JsonResult.FAILURE.equals(remainingDuration)) {
			return Integer.parseInt(remainingDuration);
		}
		return 0;
	}

	/**
	 * 批量计算员工去年剩余年假
	 * 
	 * @param employee
	 * @param lastYearHours
	 * @return
	 */
	private List<AbsenceApplyTimeVo> findAnnualLeaveHourListLastYear(List<Integer> employeeIds) {
		List<AbsenceApplyTimeVo> absenceApplyTimeVoList = absenceApplyService.findAnnualHourLastYearList(employeeIds);// 获取去年已休年假
		List<EmployeeAnnualLeave> employeeAnnualLeaveList = employeeAnnualLeaveService
				.getClearAnnualLeaveHoursEmployeeIds(employeeIds,
						Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "");// 获取已清空的人员
		if (employeeAnnualLeaveList != null && !employeeAnnualLeaveList.isEmpty()) {
			for (AbsenceApplyTimeVo absenceApplyTimeVo : absenceApplyTimeVoList) {
				for (EmployeeAnnualLeave employeeAnnualLeave : employeeAnnualLeaveList) {
					if (employeeAnnualLeave.getEmployeeId() != null
							&& employeeAnnualLeave.getEmployeeId().compareTo(absenceApplyTimeVo.getEmployeeId()) == 0) {
						absenceApplyTimeVo.setRemainingHourClear(employeeAnnualLeave.getRemainingHour());
						break;
					}
				}
			}
		}
		return absenceApplyTimeVoList;
	}

	/**
	 * 计算剩余可休假时长
	 * 
	 * @param totalTime
	 * @param applyDuration
	 * @return
	 */
	private String getRemainingDurationHour(String totalTime, String applyDuration) {
		if (StringUtils.isNotEmpty(totalTime) && StringUtils.isNotEmpty(applyDuration)) {
			try {
				int totalHours = Integer.parseInt(totalTime);
				int leaveHours = Integer.parseInt(applyDuration);
				return totalHours - leaveHours + "";
			} catch (Exception e) {
				e.printStackTrace();
				return JsonResult.FAILURE;
			}
		}
		return JsonResult.FAILURE;
	}

	/**
	 * 跳转至填写休假申请页面
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/addAbsenceApply", method = RequestMethod.GET)
	public String addAbsenceApply(Model model) {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			List<Department> departmentList = departmentService.findDepartmentListByEmployee(employee.getEmployeeId());
			List<Code> codeList = codeService.findCodeById(CodeConstant.ABSENCE_APPLY_TYPE_MENU);
			AbsenceApplyVo absenceApplyVo = new AbsenceApplyVo();
			absenceApplyVo.setAbsenceApplyNum(absenceApplyService.getAbsenceApplyNum(employee.getEmployeeId()));
			absenceApplyVo.setApplyEmployeeName(employee.getEmployeeName());
			absenceApplyVo.setApplyDateTime(new Date());
			model.addAttribute("departmentList", departmentList);
			model.addAttribute("codeList", codeList);
			model.addAttribute("absenceApplyVo", absenceApplyVo);
		}
		return "admin/absenceApply/addAbsenceApply";
	}

	/**
	 * 查询分配员工的部门
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/showDepartmentList", method = RequestMethod.GET)
	public String findAllDepartmentList() {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			List<Department> departmentList = departmentService.findAllDepartmentList(employee.getEmployeeId());
			return JSONArray.toJSONString(departmentList);
		}
		return null;
	}

	/**
	 * 查询部门的员工
	 * 
	 * @param departmentNum
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/showCheckEmployeeList", method = RequestMethod.GET)
	public String findEmployeeListByDepartmentNum(String departmentNum) {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			List<Employee> employeeList = employeeService.findEmployeeListByDepartmentNum(departmentNum,
					employee.getEmployeeId());
			return JSONArray.toJSONString(employeeList);
		}
		return null;
	}

	/**
	 * 查询部门的员工
	 * 
	 * @param employeeId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEmployeeById", method = RequestMethod.GET)
	public String getEmployeeById(Integer employeeId) {
		Employee employee = employeeService.getEmployeeById(employeeId);
		return JSONObject.toJSONString(employee);
	}

	/**
	 * 查询休假可申请时长
	 * 
	 * @param applyType
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/showRemainingDuration", method = RequestMethod.GET)
	public String showRemainingDuration(String applyType) {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			// 获取已申请(包括已通过)的休假时长
			String applyDuration = absenceApplyService.getAbsenceApplyDurationForCheck(employee.getEmployeeId(),
					applyType);
			String totalTime = null;
			if (EnumAbsenceApplyType.ANNUAL_LEAVE.getCode().equals(applyType)) {
				totalTime = getAllAnnualLeaveHours(employee, getAnnualLeaveHourTotalLastYear(employee),
						getAnnualLeaveHourTotalThisYear(employee));// 获取累计年假时长
				EmployeeAnnualLeave employeeAnnualLeave = employeeAnnualLeaveService.getClearAnnualLeaveHour(
						employee.getEmployeeId(),
						Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "");
				// 判断员工去年年假是否清空
				if (employeeAnnualLeave != null) {
					// 如果去年年假被清空则已申请(包括已通过)的休假时长为今年总已申请(包括已通过)的休假时长减去去年年假清空前抵扣时长
					applyDuration = getRemainingDurationHour(applyDuration, getRemainingDurationHour(
							getAnnualLeaveHourTotalLastYear(employee), employeeAnnualLeave.getRemainingHour()));
					if (!JsonResult.FAILURE.equals(totalTime)) {
						return Integer.parseInt(getRemainingDurationHour(totalTime, applyDuration)) < 0 ? "0"
								: getRemainingDurationHour(totalTime, applyDuration);
					}
				}
			} else if (EnumAbsenceApplyType.LEAVE.getCode().equals(applyType)) {
				totalTime = overtimeWorkApplyService.getOvertimeWorkApplyDuration(employee.getEmployeeId());// 获取累计加班时长
			} else if (EnumAbsenceApplyType.PATERNITY_LEAVE.getCode().equals(applyType)) {
				totalTime = "120";
			} else if (EnumAbsenceApplyType.PUBLIC_LEAVE.getCode().equals(applyType)) {
				totalTime = "8";// 获取累计公假时长
			}
			if (!JsonResult.FAILURE.equals(totalTime)) {
				return getRemainingDurationHour(totalTime, applyDuration);
			}
		}
		return JsonResult.FAILURE;
	}

	/**
	 * 计算休假剩余时长
	 * 
	 * @param employee
	 * @param applyType
	 * @return
	 */
	private String getRemainingDuration(Employee employee, String applyType) {
		if (employee != null) {
			// 获取总休假时长
			String applyDuration = absenceApplyService.getAbsenceApplyDuration(employee.getEmployeeId(), applyType);
			String totalTime = null;
			if (EnumAbsenceApplyType.ANNUAL_LEAVE.getCode().equals(applyType)) {
				EmployeeAnnualLeave employeeAnnualLeave = employeeAnnualLeaveService.getClearAnnualLeaveHour(
						employee.getEmployeeId(),
						Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "");
				// 判断员工去年年假是否清空
				if (employeeAnnualLeave != null) {
					// 如果去年年假被清空则休假时长为今年总休假时长减去去年年假清空前抵扣时长
					applyDuration = getRemainingDurationHour(applyDuration, getRemainingDurationHour(
							getAnnualLeaveHourTotalLastYear(employee), employeeAnnualLeave.getRemainingHour()));
				}
				totalTime = getAllAnnualLeaveHours(employee, getAnnualLeaveHourTotalLastYear(employee),
						getAnnualLeaveHourTotalThisYear(employee));// 获取累计年假时长
			} else if (EnumAbsenceApplyType.LEAVE.getCode().equals(applyType)) {
				totalTime = overtimeWorkApplyService.getOvertimeWorkApplyDuration(employee.getEmployeeId());// 获取累计加班时长
			} else if (EnumAbsenceApplyType.PATERNITY_LEAVE.getCode().equals(applyType)) {
				totalTime = "120";
			} else if (EnumAbsenceApplyType.PUBLIC_LEAVE.getCode().equals(applyType)) {
				totalTime = "8";// 获取累计公假时长
			}
			if (!JsonResult.FAILURE.equals(totalTime)) {
				return getRemainingDurationHour(totalTime, applyDuration);
			}
		}
		return JsonResult.FAILURE;
	}

	/**
	 * 验证病假和事假是否可休
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/validateByApplyType", method = RequestMethod.GET)
	public String validateByApplyType() {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			String overTimeApplyDurations = overtimeWorkApplyService
					.getOvertimeWorkApplyDuration(employee.getEmployeeId());// 获取累计加班时长
			String leaveDurations = absenceApplyService.getAbsenceApplyDuration(employee.getEmployeeId(),
					EnumAbsenceApplyType.LEAVE.getCode());// 获取员工累计调休时长
			String remainingLeaveDuration = getRemainingDurationHour(overTimeApplyDurations, leaveDurations);// 计算剩余调休时间
			String annualLeaveHour = getAllAnnualLeaveHours(employee, getAnnualLeaveHourTotalLastYear(employee),
					getAnnualLeaveHourTotalThisYear(employee));// 获取累计年假时长
			String annualHour = absenceApplyService.getAbsenceApplyDuration(employee.getEmployeeId(),
					EnumAbsenceApplyType.ANNUAL_LEAVE.getCode());// 获取已休年假时长
			EmployeeAnnualLeave employeeAnnualLeave = employeeAnnualLeaveService.getClearAnnualLeaveHour(
					employee.getEmployeeId(),
					Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "");
			// 判断员工去年年假是否清空
			if (employeeAnnualLeave != null) {
				// 如果去年年假被清空则休假时长为今年总休假时长减去去年年假清空前抵扣时长
				annualHour = getRemainingDurationHour(annualHour, getRemainingDurationHour(
						getAnnualLeaveHourTotalLastYear(employee), employeeAnnualLeave.getRemainingHour()));
			}
			String remainingAnnualHour = getRemainingDurationHour(annualLeaveHour, annualHour);// 计算剩余年假时间
			// 判断调休时间是否大于2小时或年假时间是否大于4小时
			if (Integer.valueOf(remainingLeaveDuration).intValue() >= 2
					|| Integer.valueOf(remainingAnnualHour).intValue() >= 4) {
				return JsonResult.SUCCESS_CODE;
			}
		}
		return JsonResult.FAILE_CODE;
	}

	/**
	 * 保存休假申请单
	 * 
	 * @param absenceApplyVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addForAbsenceApplyCheck", method = RequestMethod.POST)
	public String addForAbsenceApplyCheck(AbsenceApplyVo absenceApplyVo, HttpServletRequest request) {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			absenceApplyVo.setEmployeeId(employee.getEmployeeId());
			absenceApplyVo.setCreateEmployeeId(employee.getEmployeeId());
			absenceApplyVo.setCreateDateTime(new Date());
			absenceApplyVo.setDeleted(DataConstant.EXIST);
		}
		absenceApplyVo.setApplyDuration(getHours(absenceApplyVo.getApplyBeginTime(), absenceApplyVo.getApplyEndTime()));
		absenceApplyVo.setAttachment(uploadImageAttachment(absenceApplyVo, request));// 上传图片附件
		absenceApplyService.insertAbsenceApplyVo(absenceApplyVo);
		// 判断申请单是否为提交审批
		if (EnumApplyStatus.WAIT_CHECK.getCode().equals(absenceApplyVo.getApplyCheckStatus())) {
			List<AbsenceApplyCheck> absenceApplyCheckList = absenceApplyVo.getAbsenceApplyChecks();
			if (absenceApplyCheckList != null && !absenceApplyCheckList.isEmpty()) {
				Integer receiverId = null;
				for (AbsenceApplyCheck absenceApplyCheck : absenceApplyCheckList) {
					// 找到第一个审批人
					if (absenceApplyCheck.getApplyCheckSequence() != null
							&& absenceApplyCheck.getApplyCheckSequence().intValue() == 1) {
						receiverId = absenceApplyCheck.getCheckEmployeeId();
						break;
					}
				}
				// 发邮件提醒审批人进行审批
				MailService mailService = new MailService(employee, receiverId);
				mailService.setAbsenceApplyService(absenceApplyService);
				new Thread(mailService).start();
			}
		}
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 上传休假申请单图片附件
	 * 
	 * @param absenceApplyVo
	 * @param request
	 * @return
	 */
	private Attachment uploadImageAttachment(AbsenceApplyVo absenceApplyVo, HttpServletRequest request) {
		String imageAttachment = absenceApplyVo.getImageAttachment();
		Attachment attachment = null;
		// 将图片附件上传至服务器
		if (StringUtils.isNotEmpty(absenceApplyVo.getImageAttachment())) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			StringBuffer buffer = new StringBuffer(FileConstant.ATTACHMENT_URL);
			String savePath = buffer.append(format.format(new Date())).append("/").toString();
			Map<String, Object> resultMap = UploadUtil.uploadCustomImage(request, imageAttachment, savePath);
			if (resultMap != null && !resultMap.isEmpty()) {
				attachment = new Attachment();
				attachment.setAttachmentDir((String) resultMap.get(FileConstant.PHYSICS_URL));
				attachment.setAttachmentUrl((String) resultMap.get(FileConstant.WEB_URL));
				attachment.setDeleted(DataConstant.EXIST);
			}
		}
		return attachment;
	}

	/**
	 * 查询休假申请列表页面
	 * 
	 * @param currentPage:当前页
	 * @param pageSize:分页数
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/absenceApplyList", method = RequestMethod.GET)
	public String absenceApplyList(
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_START) Integer currentPage,
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_SIZE) Integer pageSize,
			AbsenceApplyVo search, Model model) {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			Pager<AbsenceApplyVo> page = absenceApplyService.findPage(search, employee.getEmployeeId(), currentPage,
					pageSize);
			model.addAttribute("page", page);
		}
		model.addAttribute("applyTypeList", codeService.findCodeById(CodeConstant.ABSENCE_APPLY_TYPE_MENU));
		return "admin/absenceApply/absenceApplyList";
	}

	/**
	 * 查看休假申请单
	 * 
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getAbsenceApply", method = RequestMethod.GET)
	public String getAbsenceApplyVo(Integer absenceApplyId) {
		AbsenceApplyVo absenceApplyVo = absenceApplyService.getAbsenceApplyVo(absenceApplyId);
		return JSONObject.toJSONString(absenceApplyVo);
	}

	/**
	 * 打开编辑休假申请单弹框
	 * 
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/absenceApplyToEdit", method = RequestMethod.GET)
	public String absenceApplyToEdit(Integer absenceApplyId) {
		AbsenceApplyVo absenceApplyVo = absenceApplyService.getAbsenceApplyVo(absenceApplyId);
		List<Code> codeList = codeService.findCodeById(CodeConstant.ABSENCE_APPLY_TYPE_MENU);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("absenceApplyVo", absenceApplyVo);
		resultMap.put("codeList", codeList);
		if (StringUtils.isNotEmpty(absenceApplyVo.getApplyType()))
			resultMap.put("remainingDuration", showRemainingDuration(absenceApplyVo.getApplyType()));
		return JSONObject.toJSONString(resultMap);
	}

	/**
	 * 编辑休假申请单
	 * 
	 * @param request
	 * @param absenceApplyVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/editForAbsenceApplyCheck", method = RequestMethod.POST)
	public String editForAbsenceApplyCheck(AbsenceApplyVo absenceApplyVo, HttpServletRequest request) {
		String imageAttachment = absenceApplyVo.getImageAttachment();
		if (StringUtils.isNotEmpty(imageAttachment) && imageAttachment.startsWith("data")) {
			absenceApplyVo.setAttachment(uploadImageAttachment(absenceApplyVo, request));
		}
		absenceApplyVo.setApplyDuration(getHours(absenceApplyVo.getApplyBeginTime(), absenceApplyVo.getApplyEndTime()));
		absenceApplyService.updateAbsenceApplyVo(absenceApplyVo);
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 验证休假申请单时长
	 * 
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/validateAbsenceApplyDuration", method = RequestMethod.GET)
	public String validateAbsenceApplyDuration(Integer absenceApplyId) {
		AbsenceApplyVo absenceApplyVo = absenceApplyService.getAbsenceApplyDurationForValidate(absenceApplyId);
		if (absenceApplyVo != null && absenceApplyVo.getEmployeeId() != null
				&& absenceApplyVo.getApplyEmployeeWorkBeginTime() != null
				&& absenceApplyVo.getApplyEmployeeEntryTime() != null
				&& StringUtils.isNotEmpty(absenceApplyVo.getApplyDuration())
				&& StringUtils.isNotEmpty(absenceApplyVo.getApplyType())) {
			boolean flag = false;
			for (String code : new String[] { EnumAbsenceApplyType.ANNUAL_LEAVE.getCode(),
					EnumAbsenceApplyType.LEAVE.getCode(), EnumAbsenceApplyType.PATERNITY_LEAVE.getCode(),
					EnumAbsenceApplyType.PUBLIC_LEAVE.getCode() }) {
				if (code.equals(absenceApplyVo.getApplyType())) {
					flag = true;
					break;
				}
			}
			if (flag) {
				Employee employee = new Employee();
				employee.setEmployeeId(absenceApplyVo.getEmployeeId());
				employee.setWorkBeginTime(absenceApplyVo.getApplyEmployeeWorkBeginTime());
				employee.setEntryTime(absenceApplyVo.getApplyEmployeeEntryTime());
				// 计算休假剩余时长
				String remainingDuration = getRemainingDuration(employee, absenceApplyVo.getApplyType());
				int remainingDurationHour = 0;
				if (!JsonResult.FAILURE.equals(remainingDuration)) {
					remainingDurationHour = Integer.parseInt(remainingDuration);
				}
				// 比较休假时长和剩余时长的大小
				if (Integer.parseInt(absenceApplyVo.getApplyDuration()) <= remainingDurationHour) {
					return JSONObject.toJSONString(new JsonResult(JsonResult.SUCCESS_CODE, null, null));
				}
				StringBuilder sb = new StringBuilder();
				sb.append(absenceApplyVo.getApplyEmployeeName()).append("的剩余").append(absenceApplyVo.getApplyTypeShow())
						.append("时长为").append(remainingDuration).append("小时，无法休")
						.append(absenceApplyVo.getApplyTypeShow()).append(absenceApplyVo.getApplyDuration())
						.append("小时");
				return JSONObject.toJSONString(new JsonResult(JsonResult.FAILE_CODE, sb.toString(), null));
			}
			return JSONObject.toJSONString(new JsonResult(JsonResult.SUCCESS_CODE, null, null));
		}
		return JSONObject.toJSONString(new JsonResult(JsonResult.FAILE_CODE, "员工信息或申请单数据不完整，请更正后同意", null));
	}

	/**
	 * 提交审批
	 * 
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/submitApplyCheck", method = RequestMethod.POST)
	public String submitApplyCheck(Integer absenceApplyId) {
		absenceApplyService.updateAbsenceApplyStatus(EnumApplyStatus.WAIT_CHECK.getCode(), absenceApplyId);
		MailService mailService = new MailService(
				(Employee) SecurityUtils.getSubject().getSession().getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY),
				absenceApplyService.getFirstCheckEmployeeId(absenceApplyId));
		mailService.setAbsenceApplyService(absenceApplyService);
		// 发邮件提醒审批人进行审批
		new Thread(mailService).start();
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 撤回休假申请单
	 * 
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/backApplyCheck", method = RequestMethod.POST)
	public String backApplyCheck(Integer absenceApplyId) {
		absenceApplyService.updateAbsenceApplyStatus(EnumApplyStatus.DRAFT.getCode(), absenceApplyId);
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 删除休假申请单
	 * 
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteAbsenceApply/{absenceApplyId}", method = RequestMethod.DELETE)
	public String deleteAbsenceApply(@PathVariable("absenceApplyId") Integer absenceApplyId) {
		absenceApplyService.deleteAbsenceApply(absenceApplyId);
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 查看休假申请单审批记录
	 * 
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getAbsenceApplyProgress", method = RequestMethod.GET)
	public String getAbsenceApplyProgress(Integer absenceApplyId) {
		List<AbsenceApplyCheckVo> absenceApplyCheckVoList = absenceApplyCheckService
				.findAbsenceApplyCheckVosByApplyId(absenceApplyId);
		return JSONArray.toJSONString(absenceApplyCheckVoList);
	}

	/**
	 * 查看年假时长管理
	 * 
	 * @param currentPage:当前页
	 * @param pageSize:分页数
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/annualLeaveHourManage", method = RequestMethod.GET)
	public String annualLeaveHourManage(
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_START) Integer currentPage,
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_SIZE) Integer pageSize,
			EmployeeVo search, Model model) throws UnsupportedEncodingException {
		Pager<EmployeeVo> page = employeeService.findAllEmployeeList(search, currentPage, pageSize);
		List<EmployeeVo> employeeVoList = page.getDataList();
		if (employeeVoList != null && !employeeVoList.isEmpty()) {
			List<Integer> employeeIds = new ArrayList<Integer>();
			for (EmployeeVo employeeVo : employeeVoList) {
				if (employeeVo.getEmployeeId() != null) {
					employeeIds.add(employeeVo.getEmployeeId());
				}
			}
			List<AbsenceApplyTimeVo> annualLeaveHourListLastYear = findAnnualLeaveHourListLastYear(employeeIds);// 获取员工去年已休年假时长
			List<AbsenceApplyTimeVo> annualLeaveHourList = absenceApplyService.findAbsenceApplyDurationList(employeeIds,
					EnumAbsenceApplyType.ANNUAL_LEAVE.getCode());// 获取员工今年已休年假时长
			for (EmployeeVo employeeVo : employeeVoList) {
				String annualLeaveHourTotalLastYear = getAnnualLeaveHourTotalLastYear(employeeVo);// 计算员工去年累计年假时长
				if (!JsonResult.FAILURE.equals(annualLeaveHourTotalLastYear)) {
					employeeVo.setAnnualLeaveHourTotalLastYear(annualLeaveHourTotalLastYear);// 添加员工去年累计年假
					// 添加员工去年剩余年假
					if (annualLeaveHourListLastYear != null && !annualLeaveHourListLastYear.isEmpty()) {
						for (AbsenceApplyTimeVo annualLeaveHourLastYear : annualLeaveHourListLastYear) {
							if (employeeVo.getEmployeeId().compareTo(annualLeaveHourLastYear.getEmployeeId()) == 0) {
								// 判断员工去年年假是否被清空
								if (StringUtils.isNotEmpty(annualLeaveHourLastYear.getRemainingHourClear())) {
									employeeVo.setRemainingHourClear(annualLeaveHourLastYear.getRemainingHourClear());
								}
								String remainingDurationHour = getRemainingDurationHour(annualLeaveHourTotalLastYear,
										annualLeaveHourLastYear.getDuration());// 计算员工去年剩余可调休年假时长
								if (!JsonResult.FAILURE.equals(remainingDurationHour)) {
									employeeVo.setRemainingAnnualHourLastYear(remainingDurationHour);// 添加员工去年剩余可调休年假时长
								}
								break;
							}
						}
					}
				}
				String annualLeaveHourTotalThisYear = getAnnualLeaveHourTotalThisYear(employeeVo);// 计算员工今年累计年假
				if (!JsonResult.FAILURE.equals(annualLeaveHourTotalThisYear)) {
					employeeVo.setAnnualLeaveHourTotalThisYear(annualLeaveHourTotalThisYear);// 添加员工今年累计年假
					// 添加员工今年剩余年假
					if (annualLeaveHourList != null && !annualLeaveHourList.isEmpty()) {
						for (AbsenceApplyTimeVo annualLeaveHour : annualLeaveHourList) {
							if (employeeVo.getEmployeeId().compareTo(annualLeaveHour.getEmployeeId()) == 0) {
								if (StringUtils.isNotEmpty(employeeVo.getRemainingAnnualHourLastYear())) {
									// 判断去年年假是否已被清空
									if (StringUtils.isNotEmpty(employeeVo.getRemainingHourClear())) {
										String remainingAnnualHourThisYear = getRemainingDurationHour(
												annualLeaveHourTotalThisYear,
												getRemainingDurationHour(annualLeaveHour.getDuration(),
														getRemainingDurationHour(
																employeeVo.getRemainingAnnualHourLastYear(),
																employeeVo.getRemainingHourClear())));
										if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
											employeeVo.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余可调休年假时长
										}
										employeeVo.setRemainingAnnualHourLastYear("0");// 改变员工去年剩余可调休年假时长
									} else {
										// 判断去年剩余年假是否够用
										if (Integer.parseInt(employeeVo.getRemainingAnnualHourLastYear()) >= Integer
												.parseInt(annualLeaveHour.getDuration())) {
											String remainingAnnualHourLastYear = getRemainingDurationHour(
													employeeVo.getRemainingAnnualHourLastYear(),
													annualLeaveHour.getDuration());
											if (!JsonResult.FAILURE.equals(remainingAnnualHourLastYear)) {
												employeeVo.setRemainingAnnualHourLastYear(remainingAnnualHourLastYear);// 改变员工去年剩余可调休年假时长
											}
											employeeVo.setRemainingAnnualHourThisYear(annualLeaveHourTotalThisYear);// 添加员工今年剩余可休年假
										} else {
											String remainingAnnualHourThisYear = getRemainingDurationHour(
													annualLeaveHourTotalThisYear,
													getRemainingDurationHour(annualLeaveHour.getDuration(),
															employeeVo.getRemainingAnnualHourLastYear()));
											if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
												employeeVo.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);
											}
											employeeVo.setRemainingAnnualHourLastYear("0");// 改变员工去年剩余可调休年假时长
										}
									}
								} else {
									String remainingAnnualHourThisYear = getRemainingDurationHour(
											annualLeaveHourTotalThisYear, annualLeaveHour.getDuration());// 计算员工今年剩余可调休年假时长
									if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
										employeeVo.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余可调休年假时长
									}
								}
								break;
							}
						}
					}
				}
				// 添加员工累计剩余年假
				if (StringUtils.isNotEmpty(employeeVo.getRemainingAnnualHourLastYear())
						&& StringUtils.isNotEmpty(employeeVo.getRemainingAnnualHourThisYear())) {
					employeeVo.setRemainingAnnualHour(Integer.parseInt(employeeVo.getRemainingAnnualHourLastYear())
							+ Integer.parseInt(employeeVo.getRemainingAnnualHourThisYear()) + "");
				}
			}
		}
		model.addAttribute("page", page);
		model.addAttribute("departmentList", departmentService.findDepartmentList());
		return "admin/absenceApply/annualLeaveHourManage";
	}

	/**
	 * 清空去年年假
	 * 
	 * @param totalTime
	 * @param applyDuration
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/clearRemainingAnnualLeaveHourLastYear", method = RequestMethod.POST)
	public String clearRemainingAnnualLeaveHourLastYear(Integer employeeId) {
		if (!employeeAnnualLeaveService.getIsClearAnnualLeaveHours(employeeId,
				Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "")) {
			Employee employee = employeeService.getEmployeeById(employeeId);
			int remainingAnnualLeaveHourLastYear = getRemainingAnnualLeaveHourLastYear(employee,
					getAnnualLeaveHourTotalLastYear(employee));// 获取去年剩余年假时长
			String absenceApplyDuration = absenceApplyService.getAbsenceApplyDuration(employee.getEmployeeId(),
					EnumAbsenceApplyType.ANNUAL_LEAVE.getCode());// 获取今年已休年假时长
			String remainingHour = "0";
			if (remainingAnnualLeaveHourLastYear >= Integer.parseInt(absenceApplyDuration)) {
				remainingHour = getRemainingDurationHour(remainingAnnualLeaveHourLastYear + "", absenceApplyDuration);
			}
			EmployeeAnnualLeave employeeAnnualLeave = new EmployeeAnnualLeave();
			employeeAnnualLeave.setEmployeeId(employeeId);
			employeeAnnualLeave.setYearName(Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())) - 1 + "");
			employeeAnnualLeave.setRemainingHour(remainingHour);
			employeeAnnualLeave.setIsClear(DataConstant.CLEAR);
			employeeAnnualLeaveService.insertClearAnnualLeaveHours(employeeAnnualLeave);
		}
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 查询待审批休假申请列表页面
	 * 
	 * @param currentPage:当前页
	 * @param pageSize:分页数
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/absenceApplyListForCheck", method = RequestMethod.GET)
	public String absenceApplyListForCheck(
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_START) Integer currentPage,
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_SIZE) Integer pageSize,
			AbsenceApplyVo search, Model model) throws UnsupportedEncodingException {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			Pager<AbsenceApplyVo> page = absenceApplyService.findForCheckPage(search, employee.getEmployeeId(),
					currentPage, pageSize);
			model.addAttribute("page", page);
		}
		model.addAttribute("departmentList", departmentService.findDepartmentList());
		return "admin/absenceApply/absenceApplyListForCheck";
	}

	/**
	 * 同意休假申请单
	 * 
	 * @param applyCheckSuggest
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/agreeAbsenceApply", method = RequestMethod.POST)
	public String agreeAbsenceApply(@RequestParam(required = false, defaultValue = "") String applyCheckSuggest,
			Integer absenceApplyId) {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			AbsenceApplyCheck absenceApplyCheck = new AbsenceApplyCheck();
			absenceApplyCheck.setAbsenceApplyId(absenceApplyId);
			absenceApplyCheck.setCheckEmployeeId(employee.getEmployeeId());
			absenceApplyCheck.setApplyCheckTime(new Date());
			absenceApplyCheck.setApplyIsAllowed(EnumApplyCheckStatus.AGREE.getCode());
			absenceApplyCheck.setApplyCheckSuggest(applyCheckSuggest);
			absenceApplyService.checkAbsenceApply(absenceApplyCheck);
			MailService mailService = new MailService(employeeService.getEmployeeByAbsenceApplyId(absenceApplyId),
					absenceApplyService.getNextCheckEmployeeId(absenceApplyId, employee.getEmployeeId()));
			mailService.setAbsenceApplyService(absenceApplyService);
			// 发邮件提醒审批人进行审批
			new Thread(mailService).start();
		}
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 不同意休假申请单
	 * 
	 * @param applyCheckSuggest
	 * @param absenceApplyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/disAgreeAbsenceApply", method = RequestMethod.POST)
	public String disAgreeAbsenceApply(String applyCheckSuggest, Integer absenceApplyId) {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			AbsenceApplyCheck absenceApplyCheck = new AbsenceApplyCheck();
			absenceApplyCheck.setAbsenceApplyId(absenceApplyId);
			absenceApplyCheck.setCheckEmployeeId(employee.getEmployeeId());
			absenceApplyCheck.setApplyCheckTime(new Date());
			absenceApplyCheck.setApplyIsAllowed(EnumApplyCheckStatus.DISAGREE.getCode());
			absenceApplyCheck.setApplyCheckSuggest(applyCheckSuggest);
			absenceApplyService.checkAbsenceApply(absenceApplyCheck);
		}
		return JsonResult.SUCCESS_CODE;
	}

	/**
	 * 查询已审批休假申请列表页面
	 * 
	 * @param currentPage:当前页
	 * @param pageSize:分页数
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/absenceApplyListHasCheck", method = RequestMethod.GET)
	public String absenceApplyListHasCheck(
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_START) Integer currentPage,
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_SIZE) Integer pageSize,
			AbsenceApplyVo search, Model model) throws UnsupportedEncodingException {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			Pager<AbsenceApplyVo> page = absenceApplyService.findHasCheckPage(search, employee.getEmployeeId(),
					currentPage, pageSize);
			model.addAttribute("page", page);
		}
		model.addAttribute("departmentList", departmentService.findDepartmentList());
		return "admin/absenceApply/absenceApplyListHasCheck";
	}

	/**
	 * 查询休假时长汇总页面
	 * 
	 * @param currentPage:当前页
	 * @param pageSize:分页数
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/remainingLeaveHoursCount", method = RequestMethod.GET)
	public String remainingLeaveHoursCount(
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_START) Integer currentPage,
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_SIZE) Integer pageSize,
			EmployeeVo search, Model model) throws UnsupportedEncodingException {
		Pager<AbsenceApplyTimeVo> page = employeeService.findAbsenceApplyTimeVoList(search, currentPage, pageSize);
		List<AbsenceApplyTimeVo> absenceApplyTimeVoList = page.getDataList();
		if (absenceApplyTimeVoList != null && !absenceApplyTimeVoList.isEmpty()) {
			List<Integer> employeeIds = new ArrayList<Integer>();
			for (AbsenceApplyTimeVo absenceApplyTimeVo : absenceApplyTimeVoList) {
				if (absenceApplyTimeVo.getEmployeeId() != null) {
					employeeIds.add(absenceApplyTimeVo.getEmployeeId());
				}
			}
			List<OvertimeWorkApplyVo> overtimeWorkApplyVoList = overtimeWorkApplyService
					.getOvertimeWorkApplyDurationListThisMonth(employeeIds);// 获取员工本月加班时长
			List<OvertimeWorkApplyVo> overtimeWorkApplyVoTotal = overtimeWorkApplyService
					.getOvertimeWorkApplyDurationList(employeeIds);// 获取员工累计加班时长
			List<AbsenceApplyTimeVo> leaveHourThisMonthList = absenceApplyService
					.getAbsenceApplyDurationListThisMonth(employeeIds, EnumAbsenceApplyType.LEAVE.getCode());// 获取员工本月调休时长
			List<AbsenceApplyTimeVo> leaveHourTotalList = absenceApplyService.findAbsenceApplyDurationList(employeeIds,
					EnumAbsenceApplyType.LEAVE.getCode());// 获取员工累计调休时长
			List<AbsenceApplyTimeVo> annualLeaveHourList = absenceApplyService.findAbsenceApplyDurationList(employeeIds,
					EnumAbsenceApplyType.ANNUAL_LEAVE.getCode());// 获取员工已休年假时长
			List<AbsenceApplyTimeVo> annualLeaveHourListLastYear = findAnnualLeaveHourListLastYear(employeeIds);// 获取员工去年已休年假时长
			List<AbsenceApplyTimeVo> otherAbsenceApplyHourMonthList = absenceApplyService
					.getOtherApplyDurationListThisMonth(employeeIds);// 获取本月其余休假时长
			for (AbsenceApplyTimeVo absenceApplyTimeVo : absenceApplyTimeVoList) {
				// 添加员工本月加班时长
				if (overtimeWorkApplyVoList != null && !overtimeWorkApplyVoList.isEmpty()) {
					for (OvertimeWorkApplyVo overtimeWorkApplyVo : overtimeWorkApplyVoList) {
						if (absenceApplyTimeVo.getEmployeeId()
								.compareTo(overtimeWorkApplyVo.getApplyEmployeeId()) == 0) {
							absenceApplyTimeVo.setOverTimeWorkHourMonth(overtimeWorkApplyVo.getApplyDuration());
							break;
						}
					}
				}
				// 添加员工累计加班时长
				if (overtimeWorkApplyVoTotal != null && !overtimeWorkApplyVoTotal.isEmpty()) {
					for (OvertimeWorkApplyVo overtimeWorkApplyVo : overtimeWorkApplyVoTotal) {
						if (absenceApplyTimeVo.getEmployeeId()
								.compareTo(overtimeWorkApplyVo.getApplyEmployeeId()) == 0) {
							absenceApplyTimeVo.setOverTimeWorkHourToTal(overtimeWorkApplyVo.getApplyDuration());
							break;
						}
					}
				}
				// 添加员工本月调休时长
				if (leaveHourThisMonthList != null && !leaveHourThisMonthList.isEmpty()) {
					for (AbsenceApplyTimeVo leaveHourThisMonth : leaveHourThisMonthList) {
						if (absenceApplyTimeVo.getEmployeeId().compareTo(leaveHourThisMonth.getEmployeeId()) == 0) {
							absenceApplyTimeVo.setLeaveHourMonth(leaveHourThisMonth.getDuration());
							break;
						}
					}
				}
				// 添加员工剩余调休时长
				if (leaveHourTotalList != null && !leaveHourTotalList.isEmpty()) {
					for (AbsenceApplyTimeVo leaveHourTotal : leaveHourTotalList) {
						if (absenceApplyTimeVo.getEmployeeId().compareTo(leaveHourTotal.getEmployeeId()) == 0) {
							String remainingLeaveHour = getRemainingDurationHour(
									absenceApplyTimeVo.getOverTimeWorkHourToTal(), leaveHourTotal.getDuration());
							if (!JsonResult.FAILURE.equals(remainingLeaveHour)) {
								absenceApplyTimeVo.setRemainingLeaveHour(remainingLeaveHour);
							}
							break;
						}
					}
				}
				Employee employee = new Employee();
				employee.setWorkBeginTime(absenceApplyTimeVo.getWorkBeginTime());
				employee.setEntryTime(absenceApplyTimeVo.getEntryTime());
				String annualLeaveHourTotalLastYear = getAnnualLeaveHourTotalLastYear(employee);// 计算员工去年累计年假时长
				// 添加员工去年剩余年假
				if (annualLeaveHourListLastYear != null && !annualLeaveHourListLastYear.isEmpty()) {
					for (AbsenceApplyTimeVo annualLeaveHourLastYear : annualLeaveHourListLastYear) {
						if (absenceApplyTimeVo.getEmployeeId()
								.compareTo(annualLeaveHourLastYear.getEmployeeId()) == 0) {
							// 判断员工去年年假是否被清空
							if (StringUtils.isNotEmpty(annualLeaveHourLastYear.getRemainingHourClear())) {
								absenceApplyTimeVo
										.setRemainingHourClear(annualLeaveHourLastYear.getRemainingHourClear());
							}
							String remainingAnnualLeaveHourLastYear = getRemainingDurationHour(
									annualLeaveHourTotalLastYear, annualLeaveHourLastYear.getDuration());// 计算员工去年剩余可调休年假时长
							if (!JsonResult.FAILURE.equals(remainingAnnualLeaveHourLastYear)) {
								absenceApplyTimeVo.setRemainingAnnualHourLastYear(remainingAnnualLeaveHourLastYear);// 添加员工去年剩余可调休年假时长
							}
							break;
						}
					}
				}
				String annualLeaveHourTotalThisYear = getAnnualLeaveHourTotalThisYear(employee);// 计算员工今年累计年假
				if (!JsonResult.FAILURE.equals(annualLeaveHourTotalThisYear)) {
					absenceApplyTimeVo.setAnnualHourThisYear(annualLeaveHourTotalThisYear);// 添加员工今年累计年假时长
					// 添加员工今年剩余年假
					if (annualLeaveHourList != null && !annualLeaveHourList.isEmpty()) {
						for (AbsenceApplyTimeVo annualLeaveHour : annualLeaveHourList) {
							if (absenceApplyTimeVo.getEmployeeId().compareTo(annualLeaveHour.getEmployeeId()) == 0) {
								absenceApplyTimeVo.setAnnualHour(annualLeaveHour.getDuration());// 添加员工今年已休年假时长
								if (StringUtils.isNotEmpty(absenceApplyTimeVo.getRemainingAnnualHourLastYear())) {
									// 判断去年年假是否已被清空
									if (StringUtils.isNotEmpty(absenceApplyTimeVo.getRemainingHourClear())) {
										absenceApplyTimeVo.setDecRemainingHourLastYear(getRemainingDurationHour(
												absenceApplyTimeVo.getRemainingAnnualHourLastYear(),
												absenceApplyTimeVo.getRemainingHourClear()));// 添加去年年假抵扣时长
										String remainingAnnualHourThisYear = getRemainingDurationHour(
												annualLeaveHourTotalThisYear,
												getRemainingDurationHour(annualLeaveHour.getDuration(),
														getRemainingDurationHour(
																absenceApplyTimeVo.getRemainingAnnualHourLastYear(),
																absenceApplyTimeVo.getRemainingHourClear())));
										if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
											absenceApplyTimeVo
													.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余可调休年假时长
										}
										absenceApplyTimeVo.setRemainingAnnualHourLastYear("0");// 改变员工去年剩余可调休年假时长
									} else {
										// 判断去年剩余年假时长是否够用
										if (Integer.parseInt(
												absenceApplyTimeVo.getRemainingAnnualHourLastYear()) >= Integer
														.parseInt(annualLeaveHour.getDuration())) {
											absenceApplyTimeVo
													.setDecRemainingHourLastYear(annualLeaveHour.getDuration());// 添加去年年假抵扣时长
											String remainingAnnualHourLastYear = getRemainingDurationHour(
													absenceApplyTimeVo.getRemainingAnnualHourLastYear(),
													annualLeaveHour.getDuration());
											if (!JsonResult.FAILURE.equals(remainingAnnualHourLastYear)) {
												absenceApplyTimeVo
														.setRemainingAnnualHourLastYear(remainingAnnualHourLastYear);// 改变员工去年剩余可调休年假时长
											}
											absenceApplyTimeVo
													.setRemainingAnnualHourThisYear(annualLeaveHourTotalThisYear);// 添加员工今年剩余可调休年假时长
										} else {
											absenceApplyTimeVo.setDecRemainingHourLastYear(
													absenceApplyTimeVo.getRemainingAnnualHourLastYear());// 添加去年年假抵扣时长
											String remainingAnnualHourThisYear = getRemainingDurationHour(
													annualLeaveHourTotalThisYear,
													getRemainingDurationHour(annualLeaveHour.getDuration(),
															absenceApplyTimeVo.getRemainingAnnualHourLastYear()));
											if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
												absenceApplyTimeVo
														.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余可调休年假时长
											}
											absenceApplyTimeVo.setRemainingAnnualHourLastYear("0");// 改变员工去年剩余可调休年假时长
										}
									}
								} else {
									absenceApplyTimeVo.setDecRemainingHourLastYear("0");// 添加去年年假抵扣时长
									String remainingAnnualHourThisYear = getRemainingDurationHour(
											annualLeaveHourTotalThisYear, annualLeaveHour.getDuration());// 计算员工今年剩余年假时长
									if (!JsonResult.FAILURE.equals(remainingAnnualHourThisYear)) {
										absenceApplyTimeVo.setRemainingAnnualHourThisYear(remainingAnnualHourThisYear);// 添加员工今年剩余可调休年假时长
									}
								}
								break;
							}
						}
					}
				}
				// 添加员工累计剩余年假
				if (StringUtils.isNotEmpty(absenceApplyTimeVo.getRemainingAnnualHourThisYear())
						&& StringUtils.isNotEmpty(absenceApplyTimeVo.getRemainingAnnualHourLastYear())) {
					absenceApplyTimeVo.setRemainingAnnualHour(
							Integer.parseInt(absenceApplyTimeVo.getRemainingAnnualHourThisYear())
									+ Integer.parseInt(absenceApplyTimeVo.getRemainingAnnualHourLastYear()) + "");
				}
				// 添加员工本月其余假期时长
				if (otherAbsenceApplyHourMonthList != null && !otherAbsenceApplyHourMonthList.isEmpty()) {
					for (AbsenceApplyTimeVo otherAbsenceApplyHourMonth : otherAbsenceApplyHourMonthList) {
						if (absenceApplyTimeVo.getEmployeeId()
								.compareTo(otherAbsenceApplyHourMonth.getEmployeeId()) == 0) {
							absenceApplyTimeVo.setOtherAbsenceApplyHourMonth(otherAbsenceApplyHourMonth.getDuration());
							break;
						}
					}
				}
			}
		}
		model.addAttribute("page", page);
		model.addAttribute("departmentList", departmentService.findDepartmentList());
		return "admin/absenceApply/remainingLeaveHoursCount";
	}

	/**
	 * 查看本月其余休假详情
	 * 
	 * @param employeeId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getOtherAbsenceApplyHourThisMonth", method = RequestMethod.GET)
	public String getOtherAbsenceApplyHourThisMonth(Integer employeeId) throws Exception {
		AbsenceApplyTimeVo absenceApplyTimeVo = new AbsenceApplyTimeVo();
		absenceApplyTimeVo.setEmployeeName(employeeService.getEmployeeNameById(employeeId));
		List<AbsenceApplyTimeVo> absenceApplyTimeVoList = absenceApplyService.findAbsenceApplyDurationListThisMonth(
				employeeId,
				new String[] { EnumAbsenceApplyType.SICK_LEAVE.getCode(), EnumAbsenceApplyType.CASUAL_LEAVE.getCode(),
						EnumAbsenceApplyType.PUBLIC_LEAVE.getCode(), EnumAbsenceApplyType.MARRIAGE_LEAVE.getCode(),
						EnumAbsenceApplyType.BEREAVEMENT_LEAVE.getCode(),
						EnumAbsenceApplyType.MATERNITY_LEAVE.getCode(),
						EnumAbsenceApplyType.PATERNITY_LEAVE.getCode() });// 获取员工本月其余假期时长
		for (AbsenceApplyTimeVo newAbsenceApplyTimeVo : absenceApplyTimeVoList) {
			for (EnumAbsenceApplyType enumAbsenceApplyType : EnumAbsenceApplyType.values()) {
				if (enumAbsenceApplyType.getCode().equals(newAbsenceApplyTimeVo.getApplyType())) {
					// 动态添加员工本月其余假期时长
					absenceApplyTimeVo.getClass()
							.getMethod(enumAbsenceApplyType.getSetMonthMethodName(), new Class[] { String.class })
							.invoke(absenceApplyTimeVo, newAbsenceApplyTimeVo.getDuration());
					break;
				}
			}
		}
		return JSONObject.toJSONString(absenceApplyTimeVo);
	}

	/**
	 * 查看休假申请汇总页面
	 * 
	 * @param currentPage:当前页
	 * @param pageSize:分页数
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/absenceApplyListTotal", method = RequestMethod.GET)
	public String absenceApplyListTotal(
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_START) Integer currentPage,
			@RequestParam(required = false, defaultValue = PageConstant.DEFAULT_PAGE_SIZE) Integer pageSize,
			AbsenceApplyVo search, Model model) throws UnsupportedEncodingException {
		Employee employee = (Employee) SecurityUtils.getSubject().getSession()
				.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
		if (employee != null) {
			Pager<AbsenceApplyVo> page = absenceApplyService.findAllPage(search, currentPage, pageSize);
			model.addAttribute("page", page);
		}
		model.addAttribute("departmentList", departmentService.findDepartmentList());
		return "admin/absenceApply/absenceApplyListTotal";
	}

	/**
	 * 计算时长
	 * 
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	private String getHours(Date beginTime, Date endTime) {
		if (beginTime != null && endTime != null) {
			long msec = endTime.getTime() - beginTime.getTime();
			long days = msec / (1000 * 60 * 60 * 24);
			long hours = (msec % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
			long time = days * 8 + hours <= days * 8 + 8 ? days * 8 + hours : days * 8 + 8;
			return time + "";
		}
		return "";
	}
}