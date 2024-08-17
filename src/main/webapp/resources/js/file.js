let addedFiles = [];

// 파일 선택 시 동적으로 요소 생성 및 파일 관리
$('#file').on('change', function() {
    const files = $(this).prop('files');

    if (files.length + addedFiles.length > 3) {
        alert('파일은 최대 3개까지만 추가 가능합니다.');
        return;
    }

    $.each(files, function(index, file) {
        if (!addedFiles.some(f => f.name === file.name)) {
            addedFiles.push(file);

            const span = $('<span>').text(file.name);
            const delBtn = $('<span>', { class: 'delFileBtn' }).text('X');
            span.append(delBtn);
            $('.uploadFiles').append(span);

            // 삭제 버튼 클릭 이벤트
            delBtn.on('click', function() {
                const fileName = $(this).parent().text().slice(0, -1); // 'X' 제거
                addedFiles = addedFiles.filter(f => f.name !== fileName);
                $(this).parent().remove();
            });
        }
    });
});

function insertFiles(parentSeq, goToUrl) {
	// FormData 객체 생성
    const formData = new FormData();
	
	if(goToUrl.includes('eApproval')){
		goToUrl = goToUrl + '?docuSeq=' + parentSeq;
	}

    // addedFiles 배열의 파일들을 FormData에 추가
    addedFiles.forEach(file => formData.append('file', file));
	

    // AJAX 요청으로 파일 전송
    $.ajax({
        url: goToUrl,
        method: 'POST',
        dataType: 'json',
        data: formData,  // FormData 객체
    	processData: false,  // FormData를 사용하기 때문에 기본적으로 jQuery가 데이터를 처리하지 않도록 설정
    	contentType: false  // 기본 콘텐츠 타입을 설정하지 않도록 설정
    }).done(resp => {
    	console.log(resp);
    }).fail((jqXHR, textStatus, errorThrown) => {
    	console.error('AJAX request failed:', textStatus, errorThrown);  // 오류 로그 출력
	});
}
