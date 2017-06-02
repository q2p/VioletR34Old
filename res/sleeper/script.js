var list_el;
var date_el;
var info_el;
var show_el;
var edit_el;

var edit_button;
var save_button;
var restore_button;
var delete_button;
var close_dream_button;
var close_editor_button;

var date_out;

var year;
var month;
var day;

var isNew = false;

function init() {
	list_el = document.getElementById('list');
	date_el = document.getElementById('date');
	info_el = document.getElementById('info');
	show_el = document.getElementById('show_text');
	edit_el = document.getElementById('edit_text');
	
	edit_button = document.getElementById('edit_button');
	save_button = document.getElementById('save_button');
	restore_button = document.getElementById('restore_button');
	delete_button = document.getElementById('delete_button');
	date_out = document.getElementById('date_output');
	close_dream_button = document.getElementById('close_dream_button');
	close_editor_button = document.getElementById('close_editor_button');
	
	updateList();
}

function updateList() {
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/sleeperDiaryList', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		var list = JSON.parse(this.responseText);
		
		var t = '';
		var m;
		
				
		for(var i = 0; i < list.length; i++) {
			m = formatDate(list[i][0], list[i][1], list[i][2]);
			t = '<div onclick="openDream('+list[i][0]+','+list[i][1]+','+list[i][2]+')">'+m+'</div>'+t;
		}
		
		list_el.innerHTML = t;
	};
	
	xhr.send();
}

function today() {
	var date = new Date();
	date_el.value = formatDate(date.getFullYear(), date.getMonth()+1, date.getDate());
}

function openByInput() {
	var dateArray = date_el.value.split('.');
	if(dateArray.length != 3) {
		alert('Неправильный формат даты.');
		return;
	}
	
	openDream(dateArray[2],dateArray[1],dateArray[0]);
}

function openDream(cyear, cmonth, cday) {
	year = cyear;
	month = cmonth;
	day = cday;
	
	var xhr = new XMLHttpRequest();
	
	xhr.open('POST', '/sleeperDreamInstance', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		if (this.status == 200) {
			isNew = false;
			var t = JSON.parse(this.responseText);
			year = t[0];
			month = t[1];
			day = t[2];
			show_el.innerHTML = t[3];
			date_out.innerHTML = formatDate(year, month, day);
			date_out.style.display = 'block';
			showDream();
		} else if(this.status == 404) {
			isNew = true;
			show_el.innerHTML = '';
			date_out.innerHTML = formatDate(year, month, day);
			date_out.style.display = 'block';
			createEditDream();
		} else {
			alert(this.responseText);
		}
	};
	
	xhr.send(year+'\n'+month+'\n'+day);
}

function formatDate(year, month, day) {
	month = ''+month;
	if(month.length == 1) month = '0'+month;
	day = ''+day;
	if(day.length == 1) day = '0'+day;
	return day+'.'+month+'.'+year;
}

function closeDream() {
	show_el.style.display = 'none';
	edit_button.style.display = 'none';
	restore_button.style.display = 'none';
	save_button.style.display = 'none';
	delete_button.style.display = 'none';
	close_dream_button.style.display = 'none';
	close_editor_button.style.display = 'none';
			date_out.style.display = 'none';
	edit_el.style.display = 'none';
}

function deleteDream() {
	if(!confirm('Удалить запись?')) return;
	
	var xhr = new XMLHttpRequest();
	
	xhr.open('POST', '/sleeperDeleteDream', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		if (this.status != 200) {
			alert(this.responseText);
			return;
		}
		closeDream();
		updateList();
	};
	
	xhr.send(year+'\n'+month+'\n'+day);
}

function editDream() {
	edit_el.value = show_el.innerHTML;
	show_el.style.display = 'none';
	edit_button.style.display = 'none';
	restore_button.style.display = 'block';
	save_button.style.display = 'block';
	delete_button.style.display = 'block';
	close_dream_button.style.display = 'none';
	close_editor_button.style.display = 'block';
	edit_el.style.display = 'block';
}

function createEditDream() {
	edit_el.value = '';
	show_el.style.display = 'none';
	edit_button.style.display = 'none';
	restore_button.style.display = 'none';
	save_button.style.display = 'block';
	delete_button.style.display = 'none';
	close_dream_button.style.display = 'block';
	close_editor_button.style.display = 'none';
	edit_el.style.display = 'block';
}

function showDream() {
	edit_el.style.display = 'none';
	edit_button.style.display = 'block';
	save_button.style.display = 'none';
	restore_button.style.display = 'none';
	delete_button.style.display = 'none';
	close_dream_button.style.display = 'block';
	close_editor_button.style.display = 'none';
	show_el.style.display = 'block';
}

function saveDream() {
	if(!confirm('Сохранить запись?')) return;
	
	var ret = year+'\n'+month+'\n'+day+'\n'+edit_el.value;
		
	var xhr = new XMLHttpRequest();

	xhr.open('POST', '/sleeperSaveDream', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		if (this.status != 200) {
			alert(this.responseText);
			return;
		}
		
		openDream(year, month, day);
		updateList();
	};
	xhr.send(ret);
}

window.onbeforeunload = function () {
	if(edit_el.style.display == 'block') return 'Закрыть вкладку?';
};

window.onload = init;