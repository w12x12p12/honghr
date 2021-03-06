package com.hongedu.honghr.honghr.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;

import com.hongedu.honghr.honghr.common.constant.SessionConstant;
import com.hongedu.honghr.honghr.entity.Employee;
import com.hongedu.honghr.honghr.entity.Mail;
import com.hongedu.honghr.util.mail.MailUtil;

public class ExceptionFilter implements Filter {
	private static String sendHost;// 发件人所用协议服务器
	private static String sendUser;// 发件人邮箱地址
	private static String password;// 发件人邮箱密码
	private static String receiveUser;// 收件人邮箱地址

	static {
		// 读取配置文件
		Properties properties = new Properties();
		InputStream input = ExceptionFilter.class.getResourceAsStream("/mail.properties");
		try {
			properties.load(input);
			sendHost = properties.getProperty("mail.sendHost");
			sendUser = properties.getProperty("mail.sendUser");
			password = properties.getProperty("mail.password");
			receiveUser = properties.getProperty("mail.receiveUser");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			// 获取客户端IP
			String ip = getRemoteHost((HttpServletRequest) request);
			// 非生产环境时发送邮件
			if (ip != null && !"127.0.0.1".equals(ip)) {
				Employee employee = (Employee) SecurityUtils.getSubject().getSession()
						.getAttribute(SessionConstant.SESSION_EMPLOYEE_KEY);
				if (employee != null) {
					Mail mail = new Mail();
					mail.setHost(sendHost);
					mail.setUsername(sendUser);
					mail.setPassword(password);
					mail.setMail_head_name("this is head of this mail");
					mail.setMail_head_value("this is head of this mail");
					mail.setMail_to(receiveUser);
					mail.setMail_from(sendUser);
					StringBuffer message = new StringBuffer();
					// 设置邮件的标题
					mail.setMail_subject(message.append(employee.getEmployeeName()).append("使用考勤系统时程序发生异常").toString());
					message.delete(0, message.length());
					// 获取程序异常日期
					String dateTime = new SimpleDateFormat("yyyy年M月d日 HH时mm分ss秒").format(new Date());
					// 获取请求的URL
					String url = ((HttpServletRequest) request).getRequestURL().toString();
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer, true));
					// 获取异常详情
					String errorDetail = writer.toString();
					message.append(
							"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">")
							.append("<html>").append("<head>")
							.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">")
							.append("<title>异常程序</title>").append("<style type=\"text/css\">")
							.append(".error{font-size:14px; color:red;}").append("</style>").append("</head>")
							.append("<body>").append("<p class=\"error\">异常报告：</p>").append("<p class=\"error\">用户名：")
							.append(employee.getUsername()).append("</p>").append("<p class=\"error\">用户姓名：")
							.append(employee.getEmployeeName()).append("</p>").append("<p class=\"error\">客户端IP：")
							.append(ip).append("</p>").append("<p class=\"error\">异常url：").append(url).append("</p>")
							.append("<p class=\"error\">异常日期：").append(dateTime).append("</p>")
							.append("<p class=\"error\">异常详情：</p>").append("<p class=\"error\">").append(errorDetail)
							.append("</p>").append("</body>").append("</html>");
					// 设置邮件的内容
					mail.setMail_content(message.toString());
					// 设置邮件的发件人名称
					mail.setPersonalName("考勤系统程序异常");
					// 发送邮件
					MailUtil.sendMail(mail);
				}
			}
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

	/**
	 * 获取客户端IP的方法
	 * 
	 * @param request
	 * @return
	 */
	private String getRemoteHost(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}
}