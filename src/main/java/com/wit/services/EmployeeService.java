package com.wit.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wit.commons.BoardConfig;
import com.wit.dao.AnnualLeaveDAO;
import com.wit.dao.EmployeeDAO;
import com.wit.dto.DeptDTO;
import com.wit.dto.EmployeeDTO;
import com.wit.dto.EmployeeInfoDTO;
import com.wit.dto.RoleDTO;
import com.wit.utill.PWUtill;

@Service
public class EmployeeService {

	@Autowired
	private EmployeeDAO dao;

	@Autowired
	private AnnualLeaveDAO adao;

	// 모든 직급 정보 가져오기
	public List<RoleDTO> AllRoles() {
		return dao.AllRoles();
	}

	// 모든 부서 정보 가져오기
	public List<DeptDTO> AllDepts() {
		return dao.AllDepts();
	}

	// 입사 순서대로 부서코드 생성을 위한 DB 조회 (사번 생성)
	public String getHighestEmployeeIDByDept(String dept) {
		return dao.getHighestEmployeeIDByDept(dept);
	}

	// 회원가입
	public int register(EmployeeDTO dto) {
		dto.setPw(PWUtill.encryptPassword(dto.getPw()));
		return dao.register(dto);
	}

	// 직원 정보 조회
	public EmployeeDTO employeeInfo(String emp_no) {
		return dao.employeeInfo(emp_no);
	}

	// 로그인
	public EmployeeDTO login(String empNo, String pw) {
		String encryptedPw = PWUtill.encryptPassword(pw);
		EmployeeDTO employee = dao.login(empNo, encryptedPw);

		// 퇴사자인 경우 로그인 실패 처리
		if (employee != null && employee.getQuit_yn() == 'Y') {
			// 퇴사자인 경우 null을 반환
			return null;
		}

		return employee;
	}

	// 추가 정보 업데이트 를 위한 직원 정보 조회
	public boolean FirstLogin(String empNo) {
		EmployeeDTO employee = dao.findByEmpNo(empNo);
		// 직원 정보가 존재하고 특정 컬럼이 비어있는지 확인
		return employee != null && (isEmptyOrWhitespace(employee.getNickname())
				|| isEmptyOrWhitespace(employee.getPhone()) || isEmptyOrWhitespace(employee.getEmail())
				|| isEmptyOrWhitespace(employee.getZip_code()) || isEmptyOrWhitespace(employee.getAddress())
				|| isEmptyOrWhitespace(employee.getDetail_address()) || isEmptyOrWhitespace(employee.getSsn()));
	}

	// 추가 정보 업데이트 를 위한 헬퍼메서드
	// 필드가 빈 문자열 또는 공백인지 확인하는 헬퍼 메서드
	private boolean isEmptyOrWhitespace(String str) {
		return str == null || str.trim().isEmpty();
	}

	// 추가 정보 업데이트
	@Transactional
	public int updateInfo(EmployeeDTO dto) {
		dto.setPw(PWUtill.encryptPassword(dto.getPw()));
		// 직원 정보 업데이트
		int result = dao.updateInfo(dto);

		// 연차 데이터 생성 (연차 15일 추가)
		adao.insertOrUpdateAnnualLeave(dto.getEmp_no());

		return result;
	}

	// ID찾기
	public String findID(String name, String ssn) {
		EmployeeDTO employee = dao.findID(name, ssn);
		// 삼항연산자 및 사원이 존재하지 않으면 null 반환
		return (employee != null) ? employee.getEmp_no() : null;
	}

	// PW찾기(수정) 사원 정보 확인
	public boolean verifyEmployee(String empNo, String name, String ssn) {
		EmployeeDTO employee = dao.findEmployee(empNo, name, ssn);
		// null 이 아니라면 true 반환, null 이 맞다면 false 반환
		return employee != null;
	}

	// PW찾기 (수정)
	public boolean modifyPassword(String empNo, String newPassword) {
		String encryptedPassword = PWUtill.encryptPassword(newPassword);
		return dao.modifyPassword(empNo, encryptedPassword) > 0;
	}

	// 직원 정보 조회(마이페이지)
	public EmployeeDTO findByEmpNo(String empNo) {
		return dao.findByEmpNo(empNo);
	}

	// 닉네임 중복 체크(마이페이지)
	public boolean checkNickname(String nickname) {
		return dao.checkNickname(nickname) == 0;
	}

	// 마이페이지 정보 업데이트
	public int updateMyPage(EmployeeDTO dto) {
		// 비밀번호와 닉네임 개별 업데이트
		if (dto.getPw() != null && !dto.getPw().isEmpty()) {
			dto.setPw(PWUtill.encryptPassword(dto.getPw()));
			dao.updatePassword(dto);
		}
		if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
			dao.updateNickname(dto);
		}
		return 1;
	}

	// 회원탈퇴
	public int delete(String empNo) {
		return dao.delete(empNo);
	}

	// 주소록 조회 메소드 추가
	public List<Map<String, Object>> getEmployeeList(String chosung, String category, int cpage) {
		int startNum = (cpage - 1) * BoardConfig.recordCountPerPage + 1;
		int endNum = cpage * BoardConfig.recordCountPerPage;

		Map<String, Object> params = new HashMap<>();
		params.put("chosung", chosung);
		params.put("category", category);
		params.put("startNum", startNum);
		params.put("endNum", endNum);
		return dao.getEmployeeAddressList(params);
	}

	// 주소록 조회 메소드 추가
	public List<EmployeeDTO> searchEmployeeList(String keyword, int cpage) {
		return dao.searchEmployeeAddressList(keyword, cpage);
	}

	// 주소록 검색 카운트 값 조회
	@Transactional
	public int CountPageAddress(String chosung, String category, int cpage) {
		int startNum = (cpage - 1) * BoardConfig.recordCountPerPage + 1;
		int endNum = cpage * BoardConfig.recordCountPerPage;
		Map<String, Object> params = new HashMap<>();
		params.put("chosung", chosung);
		params.put("category", category);
		params.put("startNum", startNum);
		params.put("endNum", endNum);
		return dao.CountPageAddress(params);
	}

	// 주소록 검색 카운트 값 조회
	@Transactional
	public int totalCountPage(String emp_no) {
		return dao.totalCountPage(emp_no);
	}

	// 주소록 카테고리 조회
	@Transactional
	public List<Map<String, Object>> getCategories() {
		return dao.getCategories();
	}

	// 주소록 데이터 가져오기
	public Map<String, Object> getContactByEmp_no(String emp_no) {
		return dao.getContactByEmp_no(emp_no);
	}

	// 주소록 검색 카운트 값 조회
	@Transactional
	public int totalCountPageSearch(String keyword, String dept_code) {
		Map<String, Object> params = new HashMap<>();
		params.put("keyword", keyword);
		params.put("dept_code", dept_code);
		return dao.totalCountPageSearch(params);
	}

	// 주소록 검색값 레코드 조회
	@Transactional
	public List<Map<String, Object>> selectByCon(String keyword, int cpage, String dept_code) {
		return dao.selectByCon(keyword, cpage * BoardConfig.recordCountPerPage - (BoardConfig.recordCountPerPage - 1),
				cpage * BoardConfig.recordCountPerPage, dept_code);
	}

	// 메신저 주소록 조회
	@Transactional
	public List<Map<String, Object>> getAllMessengerEmp(String emp_no) {
		return dao.getAllMessengerEmp(emp_no);
	}

	// 메신저 주소록 상세 조회
	@Transactional
	public Map<String, Object> getContactByEmpNo(String emp_no) {
		return dao.getContactByEmpNo(emp_no);
	}

	// 부서별 사원 목록 조회
	public List<EmployeeDTO> getListByDept(String deptCode) {
		return dao.getListByDept(deptCode);
	}
	
	// 부서별 사원 수 조회
	public List<Map<String, Integer>> getEmpNumByDept() {
		return dao.getEmpNumByDept();
	}

	// 해당 사번을 가진 사원의 이름과 부서명 조회
	public EmployeeInfoDTO getNameNDept(String empNo) {
		return dao.getNameNDept(empNo);
	}

	// 해당 사번을 가진 사원의 이름 조회
	public String getName(String empNo) {
		return dao.getName(empNo);
	}

	// 해당 사번을 가진 사원의 부서명 조회
	public String getDept(String empNo) {
		return dao.getDept(empNo);
	}

	// 해당 사번을 가진 사원의 직급 조회
	public String getRole(String empNo) {
		return dao.getRole(empNo);
	}

	// 해당 사번을 가진 사원의 직급 조회
	public String getRoleCode(String empNo) {
		return dao.getRoleCode(empNo);
	}

	// 메신저 emp_no 이름으로 변경
	@Transactional
	public String getEmployeeName(String emp_no) {
		return dao.getEmployeeName(emp_no);
	}

	// 관리자 사원 조회
	@Transactional
	public List<Map<String, Object>> getManagementList(String emp_no, int cpage) {
		int startNum = (cpage - 1) * BoardConfig.recordCountPerPage + 1;
		int endNum = cpage * BoardConfig.recordCountPerPage;
		Map<String, Object> params = new HashMap<>();
		params.put("emp_no", emp_no);
		params.put("startNum", startNum);
		params.put("endNum", endNum);
		return dao.getManagementList(params);
	}

	// 관리자 사원 검색
	@Transactional
	public List<Map<String, Object>> selectByManage(String emp_no, String column, String keyword, int cpage) {
		int startNum = (cpage - 1) * BoardConfig.recordCountPerPage + 1;
		int endNum = cpage * BoardConfig.recordCountPerPage;
		Map<String, Object> params = new HashMap<>();
		params.put("emp_no", emp_no);
		params.put("column", column);
		params.put("keyword", keyword);
		params.put("startNum", startNum);
		params.put("endNum", endNum);
		return dao.selectByManage(params);
	}

	// 관리자 사원 검색 페이지네이션 총합
	@Transactional
	public int totalCountManageSearch(String emp_no, String column, String keyword) {
		Map<String, Object> params = new HashMap<>();
		params.put("emp_no", emp_no);
		params.put("column", column);
		params.put("keyword", keyword);
		return dao.totalCountManageSearch(params);
	}

	// 관리자 사원 조회 상세
	@Transactional
	public Map<String, Object> managementDetail(String emp_no) {
		return dao.managementDetail(emp_no);
	}

	// 관리자 사원 상세 업데이트
	@Transactional
	public void updateManage(String empNo, String photoUrl, String phone, String quit_yn, String email,
			String dept_code, String role_code) {
		Map<String, Object> params = new HashMap<>();
		params.put("emp_no", empNo);
		params.put("phone", phone);
		params.put("email", email);
		params.put("phone", phone);
		params.put("dept_code", dept_code);
		params.put("role_code", role_code);
		params.put("quit_yn", quit_yn);
		params.put("photo", photoUrl);

		dao.updateManage(params);
	}

	public List<Map<String, Object>> getEmpNumByRole() {
		return dao.getEmpNumByRole();
	}
}
