var search;
var create_tag;
var results;

var tagsList;

function init() {
	search = document.getElementById('search');
	create_tag = document.getElementById('create_tag');
	results = document.getElementById('results');
	
	loadTagsList();
}

function searchFor() {
	var input = search.value;
	
	var out = '';
	
	var haveExact = input == '';
	
	for(var i = 0; i < tagsList.length; i++) {
		if(tagsList[i].indexOf(input) != -1)
			out += '<div class="tag_bar blue" onclick="editTag(\''+tagsList[i]+'\')">'+tagsList[i]+'</div>';
		if(tagsList[i] == input) haveExact = true;
	}
	
	results.innerHTML = out;
	
	if(!haveExact) {
		create_tag.innerHTML = 'Создать тэг "'+input+'"';
		create_tag.style.display = 'block';
	} else create_tag.style.display = 'none';
}

function editTag(name) {
	var o = prompt('Переименовать тэг "'+name+'" в...', name);
	if(o == null) return;
	if(o == '') {
		if(!confirm('Удалить тэг "'+name+'"?')) return;
		
		var xhr = new XMLHttpRequest();
		xhr.open('POST', '/taggerDeleteTag', true);
		
		xhr.onreadystatechange = function() {
			if(this.readyState != 4) return;
			loadTagsList();
			if(this.status != 200) alert(this.responseText);
		};
		
		xhr.send(name);
		return;
	}
	var xhr = new XMLHttpRequest();
	xhr.open('POST', '/taggerRenameTag', true);
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		loadTagsList();
		if(this.status != 200) alert(this.responseText);
	};
	
	xhr.send(name+'\n'+o);
}

function createTag() {
	if(!confirm('Создать тэг "'+search.value+'"?')) return;
	
	var xhr = new XMLHttpRequest();
	xhr.open('POST', '/taggerCreateTag', true);
	
	xhr.onreadystatechange = function() {
		if(this.readyState != 4) return;
		loadTagsList();
		if(this.status != 200) alert(this.responseText);
	};
	
	xhr.send(search.value);
}

function loadTagsList() {
	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/taggerTagsList', true);
	xhr.onreadystatechange = function() {
		if(this.readyState != 4) return;
		tagsList = JSON.parse(this.responseText);
		searchFor();
	};
	
	xhr.send();
}

window.onload = init;