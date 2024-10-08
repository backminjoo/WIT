<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>근태 관리</title>
<link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css'
	rel='stylesheet'>
<link rel="stylesheet" href="/css/style.main.css">
<link rel="stylesheet" href="/css/wit.css">
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script defer src="/js/wit.js"></script>

</head>
<body>
	<div class="container">
		<!-- 공통영역 -->
		<c:choose>
			<c:when test="${employee.role_code == '사장'}">
				<%@ include file="/WEB-INF/views/Includes/sideBarAdmin.jsp"%>
			</c:when>
			<c:otherwise>
				<%@ include file="/WEB-INF/views/Includes/sideBar.jsp"%>
			</c:otherwise>
		</c:choose>
		<!-- 공통영역 끝 -->

		<div class="main-content">
			<%@ include file="/WEB-INF/views/Includes/header.jsp"%>
			<div class="contents">
				<div class="sideAbout">
					<div class="sideTxt">
						<a href="/attendance/attendance">
							<h2 class="sideTit">근태관리</h2>
						</a>
					</div>
					<div class="addressListPrivate">
						<ul class="privateList">
							<li class="toggleItem">
								<h3 class="toggleTit">근태관리</h3>
								<div style="padding: 5px;"></div>
								<ul class="subList">
									<li><a href="/attendance/attendance_month" class="active">월간
											근태현황</a></li>
								</ul>
							</li>
						</ul>
					</div>
					<a href="/annualLeave/attendanceVacation">
						<h3 class="toggleTit">휴가관리</h3>
					</a>
					<div style="padding: 10px;"></div>
					<!-- 사장일 때만 부서별 근태현황과 부서별 휴가현황을 보여줌 -->
					<c:if test="${employee.role_code == '사장'}">
						<a href="/attendance/attendanceDept">
							<h3 class="toggleTit">부서별 근태현황</h3>
						</a>
						<div style="padding: 10px;"></div>
						<a href="/annualLeave/attendanceDeptVacation">
							<h3 class="toggleTit">부서별 휴가현황</h3>
						</a>
					</c:if>
				</div>
				<div class="sideContents Attendance">
					<h2>근태관리</h2>
					<div class="Attendance_container">
						<div class="Attendance_sections">
							<div class="status_container">
								<h3>월간 근무현황</h3>
								<div class="status_row status_header">
									<div class="status_col">
										<span>지각</span>
									</div>
									<div class="status_col">
										<span>조퇴</span>
									</div>
									<div class="status_col">
										<span>결근</span>
									</div>
								</div>
								<div class="status_row">
									<div class="status_col">
										<span>${monthlyStatus.LATE}회</span>
									</div>
									<div class="status_col">
										<span>${monthlyStatus.EARLYLEAVE}회</span>
									</div>
									<div class="status_col">
										<span>${monthlyStatus.ABSENCE}회</span>
									</div>
								</div>
							</div>
							<div class="hours_container">
								<h3>월간 근무시간</h3>
								<div class="hours_row status_header">
									<div class="hours_col">
										<span>근무일수</span>
									</div>
									<div class="hours_col">
										<span>총 근무시간</span>
									</div>
								</div>
								<div class="hours_row">
									<div class="hours_col">
										<span>${monthlyWorkHours.WORKINGDAYS}일</span>
									</div>
									<div class="hours_col">
										<span>${monthlyWorkHours.totalWorkingHours}</span>
									</div>
								</div>
							</div>
						</div>
						<div class="week_status">
							<h3>주간 근태현황</h3>
							<div class="week_row week_header">
								<div class="week_col">
									<span>근무일</span>
								</div>
								<div class="week_col">
									<span>출근시간</span>
								</div>
								<div class="week_col">
									<span>퇴근시간</span>
								</div>
								<div class="week_col">
									<span>근무시간</span>
								</div>
							</div>
							<c:forEach var="status" items="${weeklyStatus}">
								<div class="week_row">
									<div class="week_col">
										<fmt:formatDate value="${status.WORK_DATE}"
											pattern="yyyy-MM-dd" />
									</div>
									<div class="week_col">
										<span>${status.START_TIME}</span>
									</div>
									<div class="week_col">
										<span>${status.END_TIME}</span>
									</div>
									<div class="week_col">
										<span>${status.WORK_HOURS}</span>
									</div>
								</div>
							</c:forEach>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
