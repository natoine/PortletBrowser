/*$(document).ready(function(){
	   alert("page chargée avec jquery");
	 });
*/
function browserHREF(_href)
{
	//alert(_href);	
	document.getElementById("url_setUrl").value = _href;
	document.forms['setUrl'].submit();
}

function getDatas4Annotation()
{
	var _current_url = getCurrentURL();
	//alert("URL : " + _current_url );
	var xpointer_start ;
	var xpointer_end ;

	if (window.getSelection)
	{
      var selected_text = window.getSelection();
      var selRange = window.getSelection().getRangeAt(0);
      //Toujours bon quelquesoit le sens de la sélection (gauche droite ou droite gauche)
	  var start_node = selRange.startContainer;
	  var end_node = selRange.endContainer;
	  var start_offset = selRange.startOffset;
	  var end_offset = selRange.endOffset;
	  //alert("start_node : " + start_node + " end_node : " + end_node + " start_offset : " + start_offset + " end_offset : " + end_offset);
      xpointer_start = makeXpointer(_current_url , start_node , start_offset);
      xpointer_end = makeXpointer(_current_url , end_node , end_offset);
    }
	else if (document.getSelection)
    {
		var selected_text = document.getSelection();
		var selRange = selected_text.getRangeAt(O);
		var start_node = selRange.startContainer;
		var end_node = selRange.endContainer;
		var start_offset = selRange.startOffset;
		var end_offset = selRange.endOffset;
		//alert("start_node : " + start_node + " end_node : " + end_node + " start_offset : " + start_offset + " end_offset : " + end_offset);
	    xpointer_start = makeXpointer(_current_url , start_node , start_offset);
	    xpointer_end = makeXpointer(_current_url , end_node , end_offset);
    }
    else if(document.selection)
    {
    	//ie
        var userSelection = document.selection.createRange(); 
        var selected_text = userSelection.text;
        var range = getRangeObject(userSelection);
        var start_node = range.startContainer;
        var end_node = selected_text.endContainer;
        var start_offset = selected_text.startOffset;
        var end_offset = selected_text.endOffset;
        //alert("start_node : " + start_node + " end_node : " + end_node + " start_offset : " + start_offset + " end_offset : " + end_offset);
        xpointer_start = makeXpointer(_current_url , start_node , start_offset);
        xpointer_end = makeXpointer(_current_url , end_node , end_offset);
    }	
//	alert("xpointer start: " + xpointer_start);
//	alert("xpointer end: " + xpointer_end);
	if(xpointer_start != -1 && xpointer_end != -1)
	{
		//Poster le formulaire
		document.getElementById("url_doSelection").value = _current_url;
		document.getElementById("text_selection").value = selected_text;
		document.getElementById("xpointer_start").value = xpointer_start;
		document.getElementById("xpointer_end").value = xpointer_end;
		document.forms['doSelection'].submit();
	}
	else alert("Votre sélection n'est pas annotable.");
}

function makeXpointer(_url , _node , _offset)
{
	var id_node = -1 ;
	var _tree = "";
	var _nb_previous_node = 1;
	var _previous_node;
	var xpointer = _url + "#xpointer(";
	var original_node = _node ;
	
	var _node_test = _node;
	//tester si on est bien dans la fenêtre d'annotation
	while(_node_test.id != "PortletBrowserContent")
	{
		if(_node_test.nodeName == "HTML")
		{
			return -1 ;
		}
		_node_test = _node_test.parentNode;
	}

	//alert("node : " + _node.nodeName);
	//récupérer l'id du node
	while(id_node == -1 || id_node == "" )
	{
		//décompte de la position dans l'arbre
		_previous_node = _node ;
		while(_previous_node.previousSibling != null)
		{
			//alert("while previous node : " + _previous_node.previousSibling.nodeName);
			//ignorer les noeuds text créés par le navigateur
			//if( _previous_node.nodeName != "#text")
			if( _previous_node.previousSibling.nodeName != "#text" && _previous_node.previousSibling.className!= "annotation")
			{
				_nb_previous_node =  _nb_previous_node + 1;
				//alert("incrémente _nb_previous_node : " + _nb_previous_node);
			}
			_previous_node = _previous_node.previousSibling ;
		}
		//alert("sortie du while");
		if(_node.nodeName != "#text" && _node.className != "annotation")// && _nb_previous_node!= 0)
		{
			//_tree = _tree + "/" + _nb_previous_node ;
			_tree = "/" + _nb_previous_node + _tree;
			//alert("modif de tree nodeName : " + _node.nodeName + "tree : " + _tree);
		}
		//alert(" tree : " + _tree);
		_nb_previous_node = 1 ;
		//recherche de l'id du parent
		_node = _node.parentNode;
		//alert("parentNode : " + _node.nodeName );
		if(_node.id.match("^"+"annotation")!="annotation") id_node = _node.id ;
	}
	if(id_node == "PortletBrowserContent") 
	{
		xpointer = xpointer + "body";
	}
	else
	{
		xpointer = xpointer + "id(\"" + id_node +"\")";
	}
	//calcul du véritable offset
	_previous_node = original_node.previousSibling ;
	while(_previous_node != null)
	{
		//alert(_previous_node);
		if(_previous_node.nodeName == "#text")
		{
			_offset = _offset + _previous_node.nodeValue.length; 
			//_previous_node = _previous_node.previousSibling ;
		}
		else if(_previous_node.className == "annotation")
		{
			//ajout de la longueur du texte contenu à l'offset
			_offset = _offset + computeOffset( 0 , _previous_node);
			//_previous_node = _previous_node.previousSibling ;
		}
		_previous_node = _previous_node.previousSibling ;
		//else _previous_node = null ;
	}
	_parent_node = original_node.parentNode ;
	while(_parent_node.className == "annotation")
	{
		_previous_node = _parent_node.previousSibling ;
		while(_previous_node != null)
		{
			if(_previous_node.nodeName == "#text")
			{
				_offset = _offset + _previous_node.nodeValue.length; 
				_previous_node = _previous_node.previousSibling ;
			}
			else if(_previous_node.className == "annotation")
			{
				//ajout de la longueur du texte contenu à l'offset
				_offset = _offset + computeOffset( 0 , _previous_node);
				_previous_node = _previous_node.previousSibling ;
			}
			else _previous_node = null ;
		}
		_parent_node = _parent_node.parentNode ;
	}
	
	xpointer = xpointer + _tree + "," + _offset + ")";
	//alert("xpointer : " + xpointer);
	return xpointer;
}

function processPreviousNodes(_previous_node , _offset)
{
	while( _previous_node != null )
	{
		//alert("previous node is not null !!!");
		if(_previous_node.className == "annotation")
		{
			//ajout de la longueur du texte contenu à l'offset
			_offset = _offset + computeOffset( 0 , _previous_node);
			//parcours de tous les fils de cette annotation et ajout de la longueur du texte des fils annotations
			_previous_node = _previous_node.previousSibling ;
		}
		else if(_previous_node.nodeName == "#text")
		{
			_offset = _offset + _previous_node.nodeValue.length; 
			_previous_node = _previous_node.previousSibling ;
		}
		else
		{
			 //_previous_node = null ;
			if(_previous_node.parentNode.className == "annotation") 
			{
				 _previous_node = _previous_node.parentNode;
				// alert("previous_node is an annotation");
			}
			else _previous_node = null ;
		}
	}
	return _offset ;
}

function computeOffset( _offset , _node)
{
	//alert("computeOffset : _offset " + _offset);
	var current_node = _node.firstChild ;
	while(current_node != null)
	{
		if(current_node.nodeName == "#text")
		{
			_offset = _offset + current_node.nodeValue.length;
		}
		else if(current_node.className == "annotation")	_offset = computeOffset( _offset , current_node);
		current_node = current_node.nextSibling ;
	}
	return _offset ;
}


function getCurrentURL()
{
	//ne marche pas si domaine différent de celui de la page contenant l'iframe
	//var CurrentUrl = document.getElementById('browserPortletIframe').contentDocument.URL;
	return document.getElementById('browserPortletDiv').getAttribute('src');
}

function getRangeObject(selectionObject)
{
	if(selectionObject.getRangeAt)
			return selectionObject.getRangeAt(0);
	else
	{
		var range = document.createRange();
		range.setStart(selectionObject.anchorNode , selectionObject.anchorOffset);
		range.setEnd(selectionObject.focusNode , selectionObject.focusOffset);
		return range;
	}	
}