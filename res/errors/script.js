var errorsDiv;
var errorsSelector;

function init() {
	errorsDiv = document.getElementById('errors_div');
	errorsSelector = document.getElementById('errors_selector');
	requestIdeas();
}
//
function cancel(e) {
	if (e.preventDefault) e.preventDefault();
	return false;
}
//
function processFiles(e, id) {
	e = e || window.event;
	if (e.preventDefault) e.preventDefault();
	
	var fDrop = e.dataTransfer.files;
	var types = [[],[],[],[]];
	// i a v f
	for (var i=0; i<fDrop.length; i++) {
		var name = fDrop[i].name;
		var index = name.lastIndexOf('.');
		
		var type = 3;
		if(index > 0 && index < (name.length - 1)) type = getHtmlExtention(name.substring(name.lastIndexOf('.')+1, name.length));
		
		types[type].push(name);
	}
	
	var container = document.getElementsByClassName('post_inside')[id-1];
	for(var i = 0; i < types.length; i++) {
		var m;
		if(i == 0) m = container.getElementsByClassName('image_input')[0];
		else if(i == 1) m = container.getElementsByClassName('audio_input')[0];
		else if(i == 2) m = container.getElementsByClassName('video_input')[0];
		else m = container.getElementsByClassName('file_input')[0];
		var n = '';
		for(var j = 0; j < types[i].length; j++) n += '\n' + types[i][j];
		if(n != '') {
			n = m.value.trim() + n + '\n';
			m.value = n.trim();
		};
	}
	
	return false;
}
//
function getHtmlExtention(ext) {
	var types = [['jpeg','jpg','png','gif'],['mp3','wav','ogg'],['mp4','webm']];
	for(var i = 0; i < types.length; i++) for(var j = 0; j < types[i].length; j++) if(ext.toLowerCase() == types[i][j]) return i;
	return 3;
}
//
function requestIdeas() {
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/ideasData', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		var m = JSON.parse(this.responseText);
		var k = '';
		
		var l = 1;
		
		var zeros = '';
		for(var i = (''+(m[0].length + m[1].length)).length; i != 0; i--) zeros += '0';
		
		for(var i = 0; i < m[0].length; i++)
			k += '<div class="post_block"><div class="post_title" onclick="requestPost(' + (i+1) + ')">' + (zeros + l++).substr(-zeros.length) + '. ' + m[0][i] + '</div><div class="post_inside"></div></div>';
		document.getElementById('posts_selector').innerHTML = k;
		
		k = '';
		for(var i = 0; i < m[1].length; i++)
			k += '<div class="post_block"><div class="post_title" onclick="requestPost(' + (i+1) + ')">' + (zeros + l++).substr(-zeros.length) + '. ' + m[1][i] + '</div><div class="post_inside"></div></div>';
		document.getElementById('notes_selector').innerHTML = k;
	};
	
	xhr.send();
}
//
function errorReplace(old, replace) {
	if(!confirm('Want to replace\n'+old+'\nwith\n' + replace+'\n?')) return;
	
	var posts = document.getElementsByClassName('error_block');
	for(var i = 0; i < posts.length; i++) if(posts[i].getElementsByClassName('error_path')[0].innerHTML.trim() === old) document.getElementById('errors_selector').removeChild(posts[i]);
	
	var xhr = new XMLHttpRequest();
	xhr.open('POST', '/ideasFixError', true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		var m = this.responseText.trim();
		if (m.includes(':')) {
			openErrors();
			var s = m.split(':');
			alert('Unable to replace\n'+s[0]+'\nwith\n'+s[1]);
		}
	};
	
	xhr.send(old + ':' + replace);
}
//
function errorByHand(old) {
	var replace = prompt('Input new path',old);
	if(replace === null || replace.trim() === '') return;
	errorReplace(old, replace);
}

function openErrors() {
	errorsDiv.style.display = 'none';
	errorsSelector.innerHTML = '';
	
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/ideasErrors', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		var m = JSON.parse(this.responseText);
		document.getElementById('selector_div').style.display = 'none';
		
		var out = '';
		for(var i = 0; i < m.length; i++) {
			out += '<div class="error_block"><div class="error_path" onclick="errorByHand(\''+m[i][0]+'\')">'+m[i][0]+'</div><div class="error_replaces">';
			if(m[i][1].length == 0) out += 'No replaces';
			else for(var j = 0; j < m[i][1].length; j++) out += '<div class="error_replace" style="background-image: url('+m[i][1][j]+'?thumb=398)"><div class="error_overlay"onclick="prompt(\'\',\'file://E:/@MyFolder/p/'+m[i][1][j]+'\')">'+m[i][1][j]+'</div><div class="error_overlay" onclick="errorReplace(\''+m[i][0]+'\',\''+m[i][1][j]+'\')">Применить</div></div>';
			out += '</div><div class="error_posts">';
			for(var j = 0; j < m[i][2].length; j++) out += '<div class="error_post_link">'+m[i][2][j]+'</div>';
			out += '</div></div>';
		}
		
		errorsSelector.innerHTML = out;
		errorsDiv.style.display = 'block';
	};
	xhr.send();
}

function errorsLoaded(json) {
	
}
//
function requestPost(id) {
	document.getElementsByClassName('post_title')[id-1].removeAttribute('onclick');
	
	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/ideaSoloData?id='+id, true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		console.log(this.responseText);
		var m = JSON.parse(this.responseText);
		console.log(m);
		var titleOfPost = document.getElementsByClassName('post_title')[(m[0]-1)];
		var insideOfPost = document.getElementsByClassName('post_inside')[(m[0]-1)];
		insideOfPost.removeAttribute('ondragover');
		insideOfPost.removeAttribute('ondragenter');
		insideOfPost.removeAttribute('ondrop');
		titleOfPost.setAttribute('onclick', 'closePost('+m[0]+')');
		insideOfPost.style.display = 'block';
		var n = '<div class="action_button" onclick="editPost('+m[0]+')">Редактировать</div>';
		if(m[2].trim() != '') n += '<div class="post_container_name">Текст:</div><div class="post_container"><p>'+m[2]+'</p></div>';
		if(m[3].trim() != '') n += '<div class="post_container_name">TODO:</div><div class="post_container"><p>'+m[3]+'</p></div>';
		
		if(m[4].length != 0) {
			n += '<div class="post_container_name">Якоря:</div><div class="post_container">';
			for(var i = 0; i < m[4].length; i++) {
				n += '<div class="anch" onclick="requestPost(' + m[4][i] + ')">' + document.getElementsByClassName('post_title')[m[4][i]-1].innerHTML + '</div>';
			}
			n += '</div>';
		}
		if(m[5].length != 0) {
			n += '<div class="post_container_name">Изображения:</div><div class="post_container">';
			for(var i = 0; i < m[5].length; i++) {
				n += '<div class="nm" onclick="prompt(\'\',\'file://E:/@MyFolder/MEGA/p/'+m[5][i]+'\')" style="background-image: url('+m[5][i]+'?thumb=398)"></div>';
			}
			n += '</div>';
		}
		if(m[6].length != 0) {
			n += '<div class="post_container_name">Аудио:</div><div class="post_container">';
			for(var i = 0; i < m[6].length; i++) {
				n += '<p>other/ideas/files/' + m[6][i] + '</p>';
				n += '<audio controls loop src="' + m[6][i] + '"></div>';
			}
			n += '</div>';
		}
		if(m[7].length != 0) {
			n += '<div class="post_container_name">Видео:</div><div class="post_container">';
			for(var i = 0; i < m[7].length; i++) {
				n += '<p>other/ideas/files/' + m[7][i] + '</p>';
				n += '<div class="nm"><video controls loop src="' + m[7][i] + '"></video></div>';
			}
			n += '</div>';
		}
		if(m[8].length != 0) {
			n += '<div class="post_container_name">Ссылки:</div><div class="post_container">';
			for(var i = 0; i < m[8].length; i++) {
				n += '<div class="anch" onclick="prompt(\'\',\'' + m[8][i] + '\')">' + m[8][i] + '</div>';
			}
			n += '</div>';
		}
		if(m[9].length != 0) {
			n += '<div class="post_container_name">Файлы:</div><div class="post_container">';
			for(var i = 0; i < m[9].length; i++) {
				n += '<div class="anch" onclick="prompt(\'\',\'' + m[9][i] + '\')">' + m[9][i] + '</div>';
			}
			n += '</div>';
		}
		
		insideOfPost.innerHTML = n;
		document.getElementsByClassName('post_block')[(m[0]-1)].scrollIntoViewIfNeeded(true);
		document.getElementsByClassName('post_block')[(m[0]-1)].scrollIntoView();
	};
	xhr.send();
}
//
function closePost(id) {
	var insideOfPost = document.getElementsByClassName('post_inside')[(id-1)];
	var titleOfPost = document.getElementsByClassName('post_title')[(id-1)];
	insideOfPost.style.display = 'none';
	insideOfPost.innerHTML = '';
	insideOfPost.removeAttribute('ondragover');
	insideOfPost.removeAttribute('ondragenter');
	insideOfPost.removeAttribute('ondrop');
	titleOfPost.setAttribute('onclick', 'requestPost('+id+')');
}
//
function removePost(id) {
	if(!confirm('Удалить пост "'+ document.getElementsByClassName('post_title')[id-1].innerHTML+'" навсегда?')) return;
	var xhr = new XMLHttpRequest();

	xhr.open('GET', '/ideaRemove?id='+id, true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if(this.responseText == 'false') alert('Не удалось удалить пост.');
		else requestIdeas();
	};
	
	xhr.send();
}
//
function editPost(id) {
	document.getElementsByClassName('post_title')[id-1].removeAttribute('onclick');
	
	var xhr = new XMLHttpRequest();
	xhr.open('POST', '/ideaSoloData', true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		var m = JSON.parse(this.responseText);
		var titleOfPost = document.getElementsByClassName('post_title')[(m[0]-1)];
		var insideOfPost = document.getElementsByClassName('post_inside')[(m[0]-1)];
		insideOfPost.setAttribute('ondragover', 'cancel(event)');
		insideOfPost.setAttribute('ondragenter', 'cancel(event)');
		insideOfPost.setAttribute('ondrop', 'processFiles(event,'+m[0]+')');
		titleOfPost.setAttribute('onclick', 'closePost('+m[0]+')');
		insideOfPost.style.display = 'block';
		var n = '<div class="action_button" onclick="requestPost('+m[0]+')">Закрыть редактор</div>';
		n += '<div class="action_button" onclick="editPost('+m[0]+')">Отменить изменения</div>';
		if(m[0] > document.getElementById('posts_selector').getElementsByClassName('post_title').length) n += '<div class="action_button" onclick="moveToPosts('+m[0]+')">Перевести в посты</div>';
		n += '<div class="action_button" onclick="removePost('+m[0]+')">Удалить</div>';
		n += '<div class="action_button" onclick="saveChanges('+m[0]+')">Сохранить изменения</div>';
		n += '<div class="post_container_name">Название:</div><div class="post_container"><input class="title_input" type="text" size="50" value="'+m[1]+'"></div>';
		n += '<div class="post_container_name">Текст:</div><div class="post_container"><textarea class="text_input">' + m[2] + '</textarea></div>';
		n += '<div class="post_container_name">TODO:</div><div class="post_container"><textarea class="todo_input">'+m[3]+'</textarea></div>';
		n += '<div class="post_container_name">Якоря:</div><div class="post_container"><textarea class="anchor_input">';
		for(var i = 0; i < m[4].length; i++) n += m[4][i]+"\n";
		n += '</textarea></div>';
		n += '<div class="post_container_name">Изображения:</div><textarea class="image_input">';
		for(var i = 0; i < m[5].length; i++) n += m[5][i]+'\n';
		n += '</textarea></div>';
		n += '<div class="post_container_name">Аудио:</div><textarea class="audio_input">';
		for(var i = 0; i < m[6].length; i++) n += m[6][i]+'\n';
		n += '</textarea></div>';
		n += '<div class="post_container_name">Видео:</div><textarea class="video_input">';
		for(var i = 0; i < m[7].length; i++) n += m[7][i]+'\n';
		n += '</textarea></div>';
		n += '<div class="post_container_name">Ссылки:</div><textarea class="link_input">';
		for(var i = 0; i < m[8].length; i++) n += m[8][i]+'\n';
		n += '</textarea></div>';
		n += '<div class="post_container_name">Файлы:</div><textarea class="file_input">';
		for(var i = 0; i < m[9].length; i++) n += m[9][i]+'\n';
		n += '</textarea></div>';
		
		insideOfPost.innerHTML = n;
		document.getElementsByClassName('post_block')[(m[0]-1)].scrollIntoViewIfNeeded(true);
		document.getElementsByClassName('post_block')[(m[0]-1)].scrollIntoView();
	};
	
	xhr.send(''+id);
}
//
function closeAllPosts() {
	var list = document.getElementById('posts_selector').children;
	var id = '';
	for(var i = 0; i < list.length; i++) {
		id = list[i].getElementsByClassName('post_title')[0].innerHTML;
		closePost(id.substring(0,id.indexOf('.')));
	}
	list = document.getElementById('notes_selector').children;
	for(var i = 0; i < list.length; i++) {
		id = list[i].getElementsByClassName('post_title')[0].innerHTML;
		closePost(id.substring(0,id.indexOf('.')));
	}
}
//
function requestAllPosts () {
	var list = document.getElementById('posts_selector').children;
	var id = '';
	for(var i = 0; i < list.length; i++) {
		id = list[i].getElementsByClassName('post_title')[0].innerHTML;
		requestPost(id.substring(0,id.indexOf('.')));
	}
	list = document.getElementById('notes_selector').children;
	for(var i = 0; i < list.length; i++) {
		id = list[i].getElementsByClassName('post_title')[0].innerHTML;
		requestPost(id.substring(0,id.indexOf('.')));
	}
}
//
function createNote() {
	if(!confirm('Создать заметку?')) return;
	var xhr = new XMLHttpRequest();

	xhr.open('POST', '/ideaCreateNote', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if(this.responseText == 'false') {
			alert('Не удалось создать заметку.');
			return;
		}
		var td = document.getElementsByClassName('post_title').length+1;
		document.getElementById('notes_selector').innerHTML += '<div class="post_block"><div class="post_title">' + ('0000'+td).substr(-4) + '. Новая заметка</div><div class="post_inside"></div></div>';
		editPost(td);
	};
	
	xhr.send();
}
//
function moveToPosts(id) {
	if(!confirm('Переместить заметку "'+ document.getElementsByClassName('post_title')[id-1].innerHTML+'" в посты?')) return;
	var xhr = new XMLHttpRequest();

	xhr.open('GET', '/ideaMoveToPosts?id='+id, true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if(this.responseText == 'false') alert('Не удалось переместить заметку в посты.');
		else requestIdeas();
	};
	
	xhr.send();
}
//
function saveChanges(id) {
	if(!confirm('Сохранить изменения в"'+ document.getElementsByClassName('post_title')[id-1].innerHTML+'"?')) return;
	var container = document.getElementsByClassName('post_inside')[id-1];
	console.log(container);
	// get values from forms
	var title = container.getElementsByClassName('title_input')[0].value.trim().replace(/\r/g, '');
	var text = container.getElementsByClassName('text_input')[0].value.trim().replace(/\r/g, '');
	var todo = container.getElementsByClassName('todo_input')[0].value.trim().replace(/\r/g, '');
	var anchors = container.getElementsByClassName('anchor_input')[0].value.trim().replace(/\r/g, '');
	var images = container.getElementsByClassName('image_input')[0].value.trim().replace(/\r/g, '');
	var audios = container.getElementsByClassName('audio_input')[0].value.trim().replace(/\r/g, '');
	var videos = container.getElementsByClassName('video_input')[0].value.trim().replace(/\r/g, '');
	var links = container.getElementsByClassName('link_input')[0].value.trim().replace(/\r/g, '');
	var files = container.getElementsByClassName('file_input')[0].value.trim().replace(/\r/g, '');
	
	var out = retSendStr(id)+'\n';
	out += retSendStr(title)+'\n';
	out += retSendStr(text)+'\n';
	out += retSendStr(todo)+'\n';
	out += retSendStr(anchors)+'\n';
	out += retSendStr(images)+'\n';
	out += retSendStr(audios)+'\n';
	out += retSendStr(videos)+'\n';
	out += retSendStr(links)+'\n';
	out += retSendStr(files);
	
	var xhr = new XMLHttpRequest();

	xhr.open('POST', '/ideaSave', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if(this.responseText == 'false') alert('Не удалось сохранить изменения.');
		else editPost(id);
	};
	console.log(out);
	xhr.send(out);
}
//
function retSendStr(string) {
	string = (''+string).trim();
	string = ('0'+(string.split('\n').length) + '\n' + string).split('\n').join('\n0');
	return string;
}
// TODO:
window.onbeforeunload = function () {
	return 'Закрыть вкладку?';
};

window.onload = init;