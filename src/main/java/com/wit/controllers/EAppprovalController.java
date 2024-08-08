package com.wit.controllers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.wit.dto.ApprLineDTO;
import com.wit.dto.DocuDTO;
import com.wit.dto.DocuFilesDTO;
import com.wit.dto.DocuInfoListDTO;
import com.wit.dto.DocuListDTO;
import com.wit.dto.LatenessDTO;
import com.wit.dto.LeaveRequestDTO;
import com.wit.dto.WorkPropDTO;
import com.wit.services.EApprovalService;
import com.wit.services.EmployeeService;
import com.wit.services.FileService;

@Controller
@RequestMapping("/eApproval/")
public class EAppprovalController {

	@Autowired
	private EApprovalService serv;

	@Autowired
	private EmployeeService eServ;

	@Autowired
	private FileService fServ;

	@Autowired
	private HttpSession session;

	// 전자 결재 메인페이지로 이동 시 노출할 데이터를 담아서 전달하는 메서드
	@RequestMapping("home")
	public String home(Model model) throws Exception {

		// 세션에서 접속자 정보를 꺼내 변수에 저장
		String empNo = (String) session.getAttribute("loginID");

		// 결재 진행 중인 문서 목록 최신순으로 5개만 받아와서 JSP로 전달
		model.addAttribute("currentDocuList", serv.selectByStatus("진행중", empNo));
		// 결재 완료된 문서 목록 최신순으로 5개만 받아와서 JSP로 전달
		model.addAttribute("doneDocuList", serv.selectByStatus("완료", empNo));
		// 전자 결재 메인 화면으로 이동
		return "eApproval/home";
	}

	// 브라우저에서 선택한 type에 따라 결재하기 페이지로 이동 시 해당 페이지에서 초기에 노출할 데이터를 담아서 전달하는 메서드
	@RequestMapping("apprList")
	public String apprList(String type, String docuCode, Model model) throws Exception {

		// 세션에서 접속자 정보를 꺼내 변수에 저장
		String empNo = (String) session.getAttribute("loginID");
		
		System.out.println(type);
		System.out.println(docuCode);
		
		// 문서 정보를 저장할 변수 생성 후 type에 따라 해당하는 데이터를 변수에 저장
		List<DocuInfoListDTO> list = null;
		switch (type) {
		case "todo":
			list = serv.selectListByType(empNo, "결재 대기");
			break;
		case "upcoming":
			list = serv.selectListByType(empNo, "결재 예정");
			break;
		default:
			// 추후 에러 페이지로 변경
			// return "redirect:/";
		}

		// 작성자와 마지막 결재자의 사번 정보로 이름을 조회해서 dto에 저장 후 model 객체로 전달
		for (DocuInfoListDTO dto : list) {
			dto.setWriter(eServ.getName(dto.getEmp_no()));
			dto.setLast_appr_name(eServ.getName(dto.getLast_appr()));
		}
		model.addAttribute("docuCode", docuCode);
		model.addAttribute("docuList", list);
		model.addAttribute("type", type);

		// 해당 문서함 화면으로 이동
		return "eApproval/list/" + type + "List";
	}

	// 브라우저에서 선택한 type에 따라 개인 문서함 페이지로 이동 시 해당 페이지에서 초기에 노출할 데이터를 담아서 전달하는 메서드
	@RequestMapping("privateList")
	public String privateList(String type, String docuCode, Model model) throws Exception {

		// 세션에서 접속자 정보를 꺼내 변수에 저장
		String empNo = (String) session.getAttribute("loginID");
		
		// 문서 정보를 저장할 변수 생성 후 type에 따라 해당하는 데이터를 변수에 저장 후 model 객체로 전달
		List<DocuInfoListDTO> list = null;
		model.addAttribute("type", type);
		
		switch (type) {
		case "write":
			list = serv.selectWriteList(empNo);
			// 마지막 결재자의 사번 정보로 이름을 조회해서 dto에 저장 후 전달
			for (DocuInfoListDTO dto : list) {
				dto.setLast_appr_name(eServ.getName(dto.getLast_appr()));
			}
			model.addAttribute("docuList", list);
			break;
		case "save":
			model.addAttribute("docuList", serv.selecSavetList(empNo));
			break;
		case "approved":
			model.addAttribute("docuList", serv.selectApprovedList(empNo));
			break;
		case "return":
			model.addAttribute("docuList", serv.selectReturnList(empNo));
			break;
		case "view":
			model.addAttribute("docuList", serv.selectViewList(empNo));
			break;
		default:
			// 추후 에러 페이지로 변경
			return "redirect:/eApproval/home";
		}
		model.addAttribute("docuCode", docuCode);
		
		// 해당 문서함 화면으로 이동
		return "eApproval/list/" + type + "List";
	}

	// ajax로 문서 양식 리스트를 요청했을 때 서버로 보내기 위한 메서드
	@ResponseBody
	@RequestMapping(value = "getDocuList", produces = "application/json;charset=utf8")
	public List<DocuListDTO> getDocuList() throws Exception {
		List<DocuListDTO> list = serv.getDocuList();
		return list;
	}

	// 전자 결재 작성페이지로 이동 시 노출할 데이터를 담아서 전달하는 메서드
	@RequestMapping(value = "writeProc", method = RequestMethod.POST)
	public String writeProc(String docuCode, @RequestParam("apprList") String[] apprList,
			@RequestParam(value = "refeList", required = false) String[] refeList, Model model) throws Exception {

		// 세션에서 접속자 정보를 꺼내 변수에 저장
		String empNo = (String) session.getAttribute("loginID");

		// 현재 날짜를 객체로 생성 후 문자열로 변환
		LocalDate today = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedDate = today.format(formatter);

		// model에 클라이언트에서 필요한 데이터 추가
		model.addAttribute("today", formattedDate);
		model.addAttribute("empInfo", eServ.getNameNDept(empNo));
		model.addAttribute("apprList", apprList);
		if (refeList != null) {
			model.addAttribute("refeList", refeList);
		}

		// 선택한 문서에 따라 해당 전자 결재 작성 화면으로 이동
		switch (docuCode) {
		case "M1":
			return "eApproval/write/writeProp";
		case "M2":
			return "eApproval/write/writeLeave";
		case "M3":
			return "eApproval/write/writeLateness";
		default:
			// 추후 에러 페이지로 변경
			return "redirect:/eApproval/home";
		}
	}

	// 업무 기안 문서를 작성 완료했을 경우 문서 정보 & 결재 라인 & 참조 라인을 저장하기 위한 메서드
	@ResponseBody
	@RequestMapping(value = { "/write/Prop", "/write/tempProp" }, produces = "application/json;charset=utf8")
	public int writeProp(@RequestParam("apprList") String[] apprList,
			@RequestParam(value = "refeList", required = false) String[] refeList, DocuDTO dto, WorkPropDTO subDTO,
			HttpServletRequest request) throws Exception {

		// 세션에서 접속자 정보를 꺼내 저장
		String empNo = (String) session.getAttribute("loginID");
		dto.setEmp_no(empNo);

		// 현재 요청된 URL을 확인
		String currentUrl = request.getRequestURI();

		// 요청된 URL에 따라 분기 처리
		if ("/eApproval/write/Prop".equals(currentUrl)) {

			// 문서 상태 설정
			dto.setStatus("진행중");
			// 문서 정보 입력 후 문서 번호를 저장
			int docuSeq = serv.insertDocu(dto);
			dto.setDocument_seq(docuSeq);

			// 결재 라인에 대한 정보를 순서에 따라서 전달
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[0], "결재 대기", 1));
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[1], "결재 예정", 2));
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[2], "결재 예정", 3));

		} else if ("/eApproval/write/tempProp".equals(currentUrl)) {

			// 문서 상태 설정
			dto.setStatus("임시 저장");
			// 문서 정보 입력 후 문서 번호를 변수에 저장
			int docuSeq = serv.insertDocu(dto);
			dto.setDocument_seq(docuSeq);

			// 결재 라인에 대한 정보를 순서대로 전달
			for (int i = 0; i < 3; i++) {
				serv.setApprLine(new ApprLineDTO(docuSeq, apprList[i], "임시 라인", (i + 1)));
			}
		}

		// 문서의 세부 정보 입력
		subDTO.setDocument_seq(dto.getDocument_seq());
		serv.insertPropDocu(subDTO);

		// 참조 라인이 존재한다면 정보를 전달
		if (refeList != null) {
			for (String refe : refeList) {
				serv.createRefeLine(dto.getDocument_seq(), refe);
			}
		}
		// 문서 번호를 반환
		return dto.getDocument_seq();
	}

	// 지각 사유서 문서를 작성 완료했을 경우 문서 정보 & 결재 라인 & 참조 라인을 저장하기 위한 메서드
	@ResponseBody
	@RequestMapping(value = { "/write/Lateness", "/write/tempLateness" }, produces = "application/json;charset=utf8")
	public int writeLateness(@RequestParam("apprList") String[] apprList,
			@RequestParam(value = "refeList", required = false) String[] refeList, DocuDTO dto, LatenessDTO subDTO,
			HttpServletRequest request) throws Exception {

		// 세션에서 접속자 정보를 꺼내 저장
		String empNo = (String) session.getAttribute("loginID");
		dto.setEmp_no(empNo);

		// 현재 요청된 URL을 확인
		String currentUrl = request.getRequestURI();
		System.out.println(currentUrl);
		// 요청된 URL에 따라 분기 처리
		if ("/eApproval/write/Lateness".equals(currentUrl)) {

			// 문서 상태 설정
			dto.setStatus("진행중");
			// 문서 정보 입력 후 문서 번호를 저장
			int docuSeq = serv.insertDocu(dto);
			dto.setDocument_seq(docuSeq);

			// 결재 라인에 대한 정보를 순서에 따라서 전달
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[0], "결재 대기", 1));
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[1], "결재 예정", 2));
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[2], "결재 예정", 3));

		} else if ("/eApproval/write/tempLateness".equals(currentUrl)) {

			// 문서 상태 설정
			dto.setStatus("임시 저장");
			// 문서 정보 입력 후 문서 번호를 변수에 저장
			int docuSeq = serv.insertDocu(dto);
			dto.setDocument_seq(docuSeq);

			// 결재 라인에 대한 정보를 순서대로 전달
			for (int i = 0; i < 3; i++) {
				serv.setApprLine(new ApprLineDTO(docuSeq, apprList[i], "임시 라인", (i + 1)));
			}
		}

		// 문서의 세부 정보 입력
		subDTO.setDocument_seq(dto.getDocument_seq());
		serv.insertLateDocu(subDTO);

		// 참조 라인이 존재한다면 정보를 전달
		if (refeList != null) {
			for (String refe : refeList) {
				serv.createRefeLine(dto.getDocument_seq(), refe);
			}
		}
		// 문서 번호를 반환
		return dto.getDocument_seq();
	}

	// 휴가 신청서 문서를 작성 완료 or 임시 저장했을 경우 정보를 저장하기 위한 메서드
	@ResponseBody
	@RequestMapping(value = { "/write/Leave", "/write/tempLeave" }, produces = "application/json;charset=utf8")
	public int writeLeave(@RequestParam("apprList") String[] apprList,
			@RequestParam(value = "refeList", required = false) String[] refeList, DocuDTO dto, LeaveRequestDTO subDTO,
			HttpServletRequest request) throws Exception {

		// 세션에서 접속자 정보를 꺼내 변수에 저장
		String empNo = (String) session.getAttribute("loginID");
		dto.setEmp_no(empNo);

		// 현재 요청된 URL을 확인
		String currentUrl = request.getRequestURI();

		// 요청된 URL에 따라 분기 처리
		if ("/eApproval/write/Leave".equals(currentUrl)) {

			// 문서 상태 설정
			dto.setStatus("진행중");
			// 문서 정보 입력 후 문서 번호를 저장
			int docuSeq = serv.insertDocu(dto);
			dto.setDocument_seq(docuSeq);

			// 결재 라인에 대한 정보를 순서에 따라서 전달
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[0], "결재 대기", 1));
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[1], "결재 예정", 2));
			serv.setApprLine(new ApprLineDTO(docuSeq, apprList[2], "결재 예정", 3));

		} else if ("/eApproval/write/tempLeave".equals(currentUrl)) {

			// 문서 상태 설정
			dto.setStatus("임시 저장");
			// 문서 정보 입력 후 문서 번호를 변수에 저장
			int docuSeq = serv.insertDocu(dto);
			dto.setDocument_seq(docuSeq);

			// 결재 라인에 대한 정보를 순서대로 전달
			for (int i = 0; i < 3; i++) {
				serv.setApprLine(new ApprLineDTO(docuSeq, apprList[i], "임시 라인", (i + 1)));
			}
		}

		// 문서의 세부 정보 입력
		subDTO.setDocument_seq(dto.getDocument_seq());
		serv.insertLeaveDocu(subDTO);

		// 참조 라인이 존재한다면 정보를 전달
		if (refeList != null) {
			for (String refe : refeList) {
				serv.createRefeLine(dto.getDocument_seq(), refe);
			}
		}
		return dto.getDocument_seq();
	}

	// 결재 문서 작성 완료 시 파일을 업로드 하기 위한 메서드
	@RequestMapping("uploadFiles")
	public String upload(int docuSeq, MultipartFile[] file) throws Exception {

		// 파일을 저장할 서버 경로 설정 및 파일 업로드
		String realPath = session.getServletContext().getRealPath("eApproval/upload");
		// System.out.println(realPath);
		fServ.uploadDocuFile(docuSeq, realPath, file);

		return "redirect:/eApproval/home";
	}

	@RequestMapping("downloadFiles")
	public void download(String oriname, String sysname, HttpServletResponse response) throws Exception {
		
		String realPath = session.getServletContext().getRealPath("upload");
		File target = new File(realPath + "/" + sysname);

		oriname = new String(oriname.getBytes(), "ISO-8859-1");
		response.setHeader("content-Disposition", "attachment;filename=\"" + oriname + "\"");

		try (DataInputStream dis = new DataInputStream(new FileInputStream(target)); // 파일에서 내용 뽑아오기
				DataOutputStream dos = new DataOutputStream(response.getOutputStream()); // 네트워크 방향으로 출력하기
		) {
			byte[] fileContents = new byte[(int) target.length()];
			dis.readFully(fileContents);
			dos.write(fileContents);
			dos.flush();
		}
	}

	@ResponseBody
	@RequestMapping("list")
	public List<DocuFilesDTO> list(int docuSeq) throws Exception {
		return fServ.getList(docuSeq);
	}

	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e) {
		e.printStackTrace();
		return "redirect:/";
	}
}
