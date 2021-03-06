package com.hongedu.honghr.honghr.service;

import com.hongedu.honghr.honghr.entity.Employee;

public class MailService implements Runnable {

	private Employee sender;

	private Integer receiverId;

	private AbsenceApplyService absenceApplyService;

	private OvertimeWorkApplyService overtimeWorkApplyService;

	private BusinessTripApplyService businessTripApplyService;

	public Employee getSender() {
		return sender;
	}

	public void setSender(Employee sender) {
		this.sender = sender;
	}

	public Integer getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(Integer receiverId) {
		this.receiverId = receiverId;
	}

	public AbsenceApplyService getAbsenceApplyService() {
		return absenceApplyService;
	}

	public void setAbsenceApplyService(AbsenceApplyService absenceApplyService) {
		this.absenceApplyService = absenceApplyService;
	}

	public OvertimeWorkApplyService getOvertimeWorkApplyService() {
		return overtimeWorkApplyService;
	}

	public void setOvertimeWorkApplyService(OvertimeWorkApplyService overtimeWorkApplyService) {
		this.overtimeWorkApplyService = overtimeWorkApplyService;
	}

	public BusinessTripApplyService getBusinessTripApplyService() {
		return businessTripApplyService;
	}

	public void setBusinessTripApplyService(BusinessTripApplyService businessTripApplyService) {
		this.businessTripApplyService = businessTripApplyService;
	}

	public MailService(Employee sender, Integer receiverId) {
		this.sender = sender;
		this.receiverId = receiverId;
	}

	@Override
	public void run() {
		if (sender != null && receiverId != null) {
			if (absenceApplyService != null) {
				absenceApplyService.sendNoticeMail(getSender(), getReceiverId());
			} else if (overtimeWorkApplyService != null) {
				overtimeWorkApplyService.sendNoticeMail(getSender(), getReceiverId());
			} else if (businessTripApplyService != null) {
				businessTripApplyService.sendNoticeMail(getSender(), getReceiverId());
			}
		}
	}
}
