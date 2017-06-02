var errorsDiv;
var errorsSelector;

var id;
var isPost;
var title;
var text;
var todo;
var anchors;
var images;
var audios;
var videos;
var links;
var files;

var title_el;
var v_text_el;
var v_todo_el;
var v_anchors_el;
var v_images_el;
var v_audios_el;
var v_videos_el;
var v_links_el;
var v_files_el;
var v_port;

var e_title_el;
var e_text_el;
var e_todo_el;
var e_anchors_el;
var e_images_el;
var e_audios_el;
var e_videos_el;
var e_links_el;
var e_files_el;
var e_port;

var local_path;

var try_to_view; // true = view; false = edit;

//
function init() {
	id = window.location.search;
	id = parseInt(id.substring(id.indexOf('?id=')+4));
	v_port = document.getElementById('view_port');
	e_port = document.getElementById('edit_port');
	title_el = document.getElementById('title');
	v_text_el = document.getElementById('v_text').getElementsByTagName('p')[0];
	v_todo_el = document.getElementById('v_todo').getElementsByTagName('p')[0];
	v_anchors_el = document.getElementById('v_anchors').getElementsByClassName('container')[0];
	v_images_el = document.getElementById('v_images').getElementsByClassName('container')[0];
	v_audios_el = document.getElementById('v_audios').getElementsByClassName('container')[0];
	v_videos_el = document.getElementById('v_videos').getElementsByClassName('container')[0];
	v_links_el = document.getElementById('v_links').getElementsByClassName('container')[0];
	v_files_el = document.getElementById('v_files').getElementsByClassName('container')[0];
	e_title_el = document.getElementById('e_title');
	e_text_el = document.getElementById('e_text');
	e_todo_el = document.getElementById('e_todo');
	e_anchors_el = document.getElementById('e_anchors');
	e_images_el = document.getElementById('e_images');
	e_audios_el = document.getElementById('e_audios');
	e_videos_el = document.getElementById('e_videos');
	e_links_el = document.getElementById('e_links');
	e_files_el = document.getElementById('e_files');
	requestLocalPath();
	viewIdea();
}
//
function requestLocalPath() {
	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/localPath', true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		local_path = this.responseText;
	};
	xhr.send();
}
//
function viewIdea() {
	try_to_view = true;
	requestIdea();
}
//
function editIdea() {
	try_to_view = false;
	requestIdea();
}
//
function viewIdeaLoaded() {
	e_port.style.display = 'none';
	// title
	document.title = id;
	title_el.innerHTML = '<p>'+rightTitle(id, title)+'</p>';
	// text
	v_text_el.innerHTML = '<p>'+text+'</p>';
	hideIf('v_text', text.trim() == '');
	// todo
	v_todo_el.innerHTML = '<p>'+todo+'</p>';
	hideIf('v_todo', todo.trim() == '');
	// anchors
	var n = '';
	for(var i = 0; i < anchors.length; i++) {
		n += '<a class="piece line" href="idea?id='+anchors[i]+'">'+anchors[i]+'.</a>';
		requestAnchor(anchors[i]);
	}
	v_anchors_el.innerHTML = n;
	hideIf('v_anchors', anchors.length == 0);
	// images
	n = '';
	for(var i = 0; i < images.length; i++)
		n += '<div class="piece"><div class="title_menu"><p>'+images[i]+'</p><div class="left" onclick=copy(\''+localLink(images[i])+'\')>Локальная ссылка</div><div class="right">Тэггер</div></div><div class="box image" style="background-image: url(d/'+images[i]+'?thumb=398)"></div></div>';
	v_images_el.innerHTML = n;
	hideIf('v_images', images.length == 0);
	// audios
	n = '';
	for(var i = 0; i < audios.length; i++)
		n += '<div class="piece"><div class="title_menu"><p>'+audios[i]+'</p><div class="left" onclick=copy(\''+localLink(audios[i])+'\')>Локальная ссылка</div><div class="right">Тэггер</div></div><audio preload="auto" controls loop src="d/'+audios[i]+'"></audio></div>';
	v_audios_el.innerHTML = n;
	hideIf('v_audios', audios.length == 0);
	// videos
	n = '';
	for(var i = 0; i < videos.length; i++)
		n += '<div class="piece"><div class="title_menu"><p>'+videos[i]+'</p><div class="left" onclick=copy(\''+localLink(videos[i])+'\')>Локальная ссылка</div><div class="right">Тэггер</div></div><video class="box" preload="auto" controls loop src="d/'+videos[i]+'"></video></div>';
	v_videos_el.innerHTML = n;
	hideIf('v_videos', videos.length == 0);
	// links
	n = '';
	for(var i = 0; i < links.length; i++)
		n += '<div class="piece line" onclick=copy('+links[i]+'\')>'+links[i]+'</div>';
	v_links_el.innerHTML = n;
	hideIf('v_links', links.length == 0);
	// files
	n = '';
	for(var i = 0; i < files.length; i++)
		n += '<div class="title_menu piece"><p>'+files[i]+'</p><div class="left" onclick=copy(\''+localLink(files[i])+'\')>Локальная ссылка</div><div class="right">Тэггер</div></div>';
	v_files_el.innerHTML = n;
	hideIf('v_files', files.length == 0);
	v_port.style.display = 'block';
}
//
function editIdeaLoaded() {
	v_port.style.display = 'none';
	hideIf('move_to_posts', isPost);
	// title
	document.title = id;
	title_el.innerHTML = '<p>'+rightTitle(id, title)+'</p>';
	e_title_el.value = title;
	// text
	e_text_el.value = text;
	// todo
	e_todo_el.value = todo;
	// anchors
	var n = '';
	for(var i = 0; i < anchors.length; i++) n += anchors[i]+'\n';
	e_anchors_el.value = n.trim();
	// images
	n = '';
	for(var i = 0; i < images.length; i++) n += images[i]+'\n';
	e_images_el.value = n.trim();
	// audios
	n = '';
	for(var i = 0; i < audios.length; i++) n += audios[i]+'\n';
	e_audios_el.value = n.trim();
	// videos
	n = '';
	for(var i = 0; i < videos.length; i++) n += videos[i]+'\n';
	e_videos_el.value = n.trim();
	// links
	n = '';
	for(var i = 0; i < links.length; i++) n += links[i]+'\n';
	e_links_el.value = n.trim();
	// files
	n = '';
	for(var i = 0; i < files.length; i++) n += files[i]+'\n';
	e_files_el.value = n.trim();
	
	e_port.style.display = 'block';
	
	// insideOfPost.removeAttribute('ondragover'); TODO:
	// insideOfPost.removeAttribute('ondragenter'); TODO:
	// insideOfPost.removeAttribute('ondrop'); TODO:
}
//
function requestIdea() {
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/ideaData?id='+id, true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		console.log(this.responseText);
		var m = JSON.parse(this.responseText);
		isPost = m[1]=='post'?true:false;
		title = m[2];
		text = m[3];
		todo = m[4];
		anchors = m[5];
		images = m[6];
		audios = m[7];
		videos = m[8];
		links = m[9];
		files = m[10];
		if(try_to_view) viewIdeaLoaded();
		else editIdeaLoaded();
	};
	
	xhr.send();
}
//
function copy(url) {
	var a = document.createElement("textarea");
	
	a.style.position = 'fixed';
	a.style.top = '0px';
	a.style.left = '0px';
	a.style.width = '2em';
	a.style.height = '2em';
	a.style.padding = '0px';
	a.style.border = 'none';
	a.style.outline = 'none';
	a.style.boxShadow = 'none';
	a.style.background = 'transparent';
	a.value = url;
	document.body.appendChild(a);
	a.select();
	try { document.execCommand('copy'); } catch (e) {}
	document.body.removeChild(a);
}
function hideIf(id, bool) {
	document.getElementById(id).style.display = bool?'none':'block';
}
function localLink(url) {
	return 'file://'+local_path+url;
}
function rightTitle(id, title) {
	return id+'. '+title;
}
function cancel(e) {
	if (e.preventDefault) e.preventDefault();
	return false;
}
//
function requestAnchor(id) {
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/ideaData?id='+id, true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		var m = JSON.parse(this.responseText);
		
		var childs = v_anchors_el.children;
		
		for(var i = 0; i < childs.length; i++) {
			if(anchors[i] == m[0]) {
				childs[i].innerHTML = rightTitle(m[0], m[2]);
				break;
			}
		}
	};
	
	xhr.send();
}
//
function removePost() {
	if(!confirm('Удалить пост "'+rightTitle(id, title)+'" навсегда?')) return;
	var xhr = new XMLHttpRequest();

	xhr.open('GET', '/ideaRemove?id='+id, true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.responseCode != 200) alert('Не удалось удалить пост.');
		else document.location.assign('ideas?page=1');
	};
	
	xhr.send();
}
//
function moveToPosts() {
	if(!confirm('Переместить заметку "'+rightTitle(id, title)+'" в посты?')) return;
	var xhr = new XMLHttpRequest();

	xhr.open('GET', '/ideaMoveToPosts?id='+id, true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if(this.responseCode != 200) alert('Не удалось переместить заметку в посты.');
		else document.location.assign('idea?id='+this.responseText);
	};
	
	xhr.send();
}
//
function saveChanges() {
	if(!confirm('Сохранить изменения в "'+rightTitle(id,title)+'"?')) return;
	// get values from forms
	var l_title = e_title_el.value.trim().replace(/\r/g, '');
	var l_text = e_text_el.value.replace(/\r/g, '');
	var l_todo = e_todo_el.value.replace(/\r/g, '');
	var l_anchors = e_anchors_el.value.trim().replace(/\r/g, '');
	var l_images = e_images_el.value.trim().replace(/\r/g, '');
	var l_audios = e_audios_el.value.trim().replace(/\r/g, '');
	var l_videos = e_videos_el.value.trim().replace(/\r/g, '');
	var l_links = e_links_el.value.trim().replace(/\r/g, '');
	var l_files = e_files_el.value.trim().replace(/\r/g, '');
	
	var out = splitToSend(id)+'\n';
	out += splitToSend(l_title)+'\n';
	out += splitToSend(l_text)+'\n';
	out += splitToSend(l_todo)+'\n';
	out += splitToSend(l_anchors)+'\n';
	out += splitToSend(l_images)+'\n';
	out += splitToSend(l_audios)+'\n';
	out += splitToSend(l_videos)+'\n';
	out += splitToSend(l_links)+'\n';
	out += splitToSend(l_files);
	
	var xhr = new XMLHttpRequest();

	xhr.open('POST', '/ideaSave', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		console.log(this.status);
		console.log(this.responseText);
		if (this.status != 200) alert('Не удалось сохранить изменения.');
		else requestIdea();
	};
	console.log(out);
	xhr.send(out);
}
//
function splitToSend(string) {
	string = (''+string).split('\n');
	return '0'+string.length+'\n'+string.join('\n');
}
//
window.onbeforeunload = function () {
	if(!try_to_view) return 'Закрыть вкладку?';
};
//
window.onload = init;