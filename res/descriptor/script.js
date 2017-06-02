var filePath = null;

var file_el;

var container_el;

var text_el;
var todo_el;
var search_el;
var ideas_el; // TODO:

var tags_u;
var tags_a;
var tags_i;

var tagsAll;

function init() {
	container_el = document.getElementById('container');
	
	file_el = document.getElementById('file');
	
	text_el = document.getElementById('text');
	todo_el = document.getElementById('todo');
	search_el = document.getElementById('search');
	ideas_el = document.getElementById('ideas');
	
	tags_u = document.getElementById('tags_u');
	tags_a = document.getElementById('tags_a');
	tags_i = document.getElementById('tags_i');
		
	var xhr = new XMLHttpRequest();

	xhr.open('GET', '/localPath', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		localPath = this.responseText;
		requestTags();
	};
	
	xhr.send();
}

function requestTags() {
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/taggerTagsList', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState == 4) tagsAll = JSON.parse(this.responseText);
	};
	
	xhr.send();
}

function randomFile() {
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/descriptorRandom', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		if (this.status != 200) alert(this.responseText);
		else fileByPath(this.responseText);
	};
	
	xhr.send();
}

function fileByPath(path) {
	file_el.style.display = 'block';
	if(path == null) path = prompt('Путь:', '');
	if(path == null || path == '') return;
	
	var xhr = new XMLHttpRequest();
	
	xhr.open('POST', '/descriptorGetInfo', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		if (this.status != 200) {
			alert(this.responseText);
			return;
		}
				
		var t = JSON.parse(this.responseText);
		
		filePath = t[0];
		
		switch (t[1]) {
			case 'image':
				container_el.innerHTML = '<img src="d/'+filePath+'"></img>';
				break;
			case 'audio':
				container_el.innerHTML = '<audio preload="auto" controls loop src="d/'+filePath+'"></audio>';
				break;
			case 'video':
				container_el.innerHTML = '<video preload="auto" controls loop src="d/'+filePath+'"></video>';
				break;
			default:
				container_el.innerHTML = '<div>'+filePath+'</div>';
		}
		
		text_el.value = t[2];
		todo_el.value = t[3];
		tags_u.innerHTML = tags_a.innerHTML = tags_i.innerHTML = '';
		
		var tags = t[4];
		for(var i = 0; i < tags.length; i++) tagToUse(tags[i]);
		
		var m = '';
		var list = tags_u.getElementsByTagName('p');
		var inside;
		for(var i = 0; i != tagsAll.length; i++) {
			inside = false;
			for(var j = 0; j != list.length; j++) {
				if(list[j].innerHTML == tagsAll[i][0]) {
					inside = true;
					break;
				}
			}
			if(!inside) m += generateUnitAveilable(tagsAll[i]);
		}
		tags_a.innerHTML = m;
		file_el.style.display = 'block';
	};
	
	xhr.send(path);
}

function generateUnitUsed(name) {
	return '<div class="tag_bar"><p>'+name+'</p><div class="select center green" onclick="tagToIgnore(\''+name+'\')">Игнорировать</div></div>';
}
function generateUnitIgnored(name) {
	return '<div class="tag_bar"><p>'+name+'</p><div class="select center blue" onclick="tagToUse(\''+name+'\')">Использовать</div></div>';
}
function generateUnitAveilable(name) {
	return '<div class="tag_bar"><p>'+name+'</p><div class="select left blue" onclick="tagToUse(\''+name+'\')">Использовать</div><div class="select right green" onclick="tagToIgnore(\''+name+'\')">Игнорировать</div></div>';
}

function getTag(name) {
	for(var i = 0; i < tagsAll.length; i++)
		if(tagsAll[i][0] == name)
			return tagsAll[i];
}

function tagToUse(name) {
	removeUnit(tags_a, name);
	removeUnit(tags_i, name);
	tags_u.innerHTML += generateUnitUsed(name);
}

function tagToIgnore(name) {
	removeUnit(tags_u, name);
	removeUnit(tags_a, name);
	tags_i.innerHTML += generateUnitIgnored(name);
}

function removeUnit(from, name) {
	var list = getList(from);
	for(var i = 0; i != list.length; i++)
		if(list[i].innerHTML == name) from.removeChild(list[i--].parentElement);
}

function getList(from) {
	return from.getElementsByTagName('p');
}

function saveFile() {
	if(!confirm('Сохранить изменения в "'+filePath+'"?')) return;
	
	var ret = '1\n'+filePath+'\n'+splitToSend(text_el.value)+'\n'+splitToSend(todo_el.value)+'\n1\n';
		
	var list = tags_u.getElementsByClassName('tag_bar');
	
	for(var i = 0; i != list.length; i++) {
		if(i != 0) ret += ' ';
		ret += list[i].getElementsByTagName('p')[0].innerHTML;
	}
		
	var xhr = new XMLHttpRequest();

	xhr.open('POST', '/descriptorSave', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) alert(this.responseText);
		else editPost();
	};
	
	xhr.send(ret);
}

function searchFor() {
	var input = search_el.value;
	
	var out = '';
	
	resultsIn(tags_u, input);
	resultsIn(tags_a, input);
	resultsIn(tags_i, input);
}

function resultsIn(inside, input) {
	var list = getList(inside);
	for(var i = 0; i != list.length; i++)
		list[i].parentElement.style.display = (list[i].innerHTML.indexOf(input) == -1)?'none':'block';
}

function splitToSend(string) {
	string = (''+string).split('\n');
	return '0'+string.length+'\n'+string.join('\n');
}

function localLink() {
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
	a.value = "file:///"+localPath+filePath;
	document.body.appendChild(a);
	a.select();
	try { document.execCommand('copy'); } catch (e) {}
	document.body.removeChild(a);
}

window.onbeforeunload = function () {
	if(filePath != null) return 'Закрыть вкладку?';
};

window.onload = init;