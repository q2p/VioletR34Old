var chosen = -1;

var editorDiv;
var contentDiv;
var editorButtons;
var listDiv;
var resSelect;
var resUrl;
var resTitle;
var resBeg;
var resEnd;

var types = []; // [[storageName, name, beg, end],...]

function init() {
	editorDiv = document.getElementById('editor');
	contentDiv = document.getElementById('content');
	editorButtons = document.getElementsByClassName('editor_button');
	listDiv = document.getElementById('list');
	resSelect = document.getElementById('res_input');
	resUrl = document.getElementById('url_input');
	resTitle = document.getElementById('title_input');
	resBeg = document.getElementById('beg');
	resEnd = document.getElementById('end');

	requestTypes();
}

function resChange() {
	var str = resSelect.value;
	for(var i = 0; i < types.length; i++) {
		if(types[i][0] == str) {
			resBeg.innerHTML = types[i][2];
			resEnd.innerHTML = types[i][3];
			resBeg.style.display = (types[i][2] == ''?'none':'table-cell');
			resEnd.style.display = (types[i][3] == ''?'none':'table-cell');
			break;
		}
	}
	checkRes();
}

function requestTypes() {
	var xhr = new XMLHttpRequest();

	xhr.open('GET', 'resourceTypes', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		types = JSON.parse(this.responseText);

		var n = '';
		for(var i = 0; i < types.length; i++) n += '<option value="'+types[i][0]+'">'+types[i][1]+'</option>';
		
		resSelect.innerHTML = n;
		
		resChange();
		
		requestList();
	};
	
	xhr.send();
}

function requestList() {
	editorDiv.style.display = 'none';
	
	var xhr = new XMLHttpRequest();

	xhr.open('GET', 'resourcesList', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		var n = JSON.parse(this.responseText);
				
		var cur;
		var m = '';
		for(var i = 0; i < n.length; i++) {
			cur = n[i];
			for(var j = 0; j < types.length; j++) {
				if(types[j][0] == cur[0]) {
					m += '<div class="item"><div class="resource" style="background-image: url(d/VioletR34/data/resources/types/'+cur[0]+'.png)"><div class="edit_button" onclick="edit('+i+')"></div></div><div class="info"><div class="date" onclick="update(\''+i+'\')">'+cur[3]+'</div><div class="name" onclick="copyConstruct(\''+cur[1]+'\',\''+types[j][2]+'\',\''+types[j][3]+'\')">'+cur[2]+'</div></div></div>';
					break;
				}
			}
		}
		listDiv.innerHTML = m;
		
		contentDiv.style.display = 'block';
	};
	
	xhr.send();
}

function update(id) {
	var xhr = new XMLHttpRequest();

	xhr.open('GET', 'resourceUpdate?id='+id, true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		requestList();
	};
	
	xhr.send();
}

function sendSave(id, address, name, res) {
	var xhr = new XMLHttpRequest();

	xhr.open('POST', 'resourceSave', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		requestList();
	};
	
	xhr.send(id+'\n'+address+'\n'+name+'\n'+res);
}

function sendDelete(id) {
	var xhr = new XMLHttpRequest();

	xhr.open('GET', 'resourceDelete?id='+id, true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		requestList();
	};
	
	xhr.send();
}

function edit(id) {
	chosen = id;
	contentDiv.style.display = 'none';
	editorButtons[2].style.display = 'block';
	editorDiv.style.display = 'block';
	
	var p = document.getElementsByClassName('item')[chosen];
			
	var t = p.getElementsByClassName('name')[0].getAttribute('onclick').trim();
		
	document.getElementById('url_input').value = t.substring(t.indexOf('\'')+1, t.indexOf('\','));
	
	document.getElementById('title_input').value = p.getElementsByClassName('name')[0].innerHTML.trim();
	
	t = p.getElementsByClassName('resource')[0].style.backgroundImage.trim();
		
	resSelect.value = t.substring(t.lastIndexOf('/')+1, t.lastIndexOf('.'));
	
	resChange();
}

function closeFav() {
	chosen = -1;
	editorDiv.style.display = 'none';
	contentDiv.style.display = 'block';
}

function saveFav() {
	if(chosen == -1 && resSelect.value == 'clone') return;
	editorDiv.style.display = 'none';
	sendSave(chosen, resUrl.value, resTitle.value, resSelect.value);
}

function deleteFav() {
	if(chosen == -1) return;
	document.getElementById('editor').style.display = 'none';
	sendDelete(chosen);
}

function checkRes() {
	var xhr = new XMLHttpRequest();

	xhr.open('POST', 'resourceClone', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		if(this.responseText == "fine") editorButtons[1].style.display = 'block';
		else editorButtons[1].style.display = 'none';
	};

	xhr.send(resSelect.value + '\n' + resUrl.value + '\n' + chosen);
}

function createNew() {
	chosen = -1;
	contentDiv.style.display = 'none';
	editorButtons[2].style.display = 'none';
	editorDiv.style.display = 'block';
	resUrl.value = '';
	resTitle.value = '';
}

function copyConstruct(url, beg, end) {
	copy(beg+url+end);
}

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

window.onload = init;