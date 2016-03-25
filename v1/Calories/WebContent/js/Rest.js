$body = $("body");

function SendRESTJson( method, url, json, responseFunction )
{
	var realJson = null;
	try{
		realJson = JSON.parse(json);
	}catch( error )
	{
	}
	var resq = "<table> <tr> <td> <strong>" + method + "</strong> " + url + "</td></tr>";
	
	resq +="<tr><td><pre>" + (realJson? syntaxHighlight( JSON.stringify( realJson , undefined, 4) ):"") + "</pre></td></tr>";

	
	$body.addClass("loading");
	
	resq += "<table>";
	
	$("#Request").html( resq );
	
	$.ajax({
		url : url,
		type: method,
		data : json,
		contentType: 'application/json; charset=UTF-8',
		dataType :"json",
		complete: function(jqXHR, var1 , var2 )
		{
			var statusCode = jqXHR.status;
			var statusText = jqXHR.statusText
			var tmp = jqXHR.getAllResponseHeaders();
			var headers = tmp.split("\n");
			var contentType = jqXHR.getResponseHeader("Content-Type");
			var json = null;
			if( contentType == "application/json")
			{
				json=jqXHR.responseJSON;
			}
			var resp = "<table> <tr> <td>HTML <strong>" + statusCode + "</strong> " + statusText + "</td></tr>";
			headers.forEach(function(entry) {
				resp += "<tr><td>" + entry + "</td></tr>";
			});
			
			if( json ) 
			{
				resp += "<tr><td><pre>" + syntaxHighlight(JSON.stringify( json , undefined, 4)) + "</pre></td></tr>";
			}
			resp += "<table>";
			$("#Response").html( resp );
			
			$body.removeClass("loading");
			
			responseFunction( jqXHR , statusCode, statusText, headers, json)
		},
	});
}
function syntaxHighlight(json) 
{
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

function formatTime(t)
{
	myDate = new Date(t);
	return formatDate(myDate);
}

function formatDate(d)
{
    // padding function
    var s = function(a,b){return(1e15+a+"").slice(-b)};

    // default date parameter
    if (typeof d === 'undefined'){
        d = new Date();
    };

    // return ISO datetime
    return d.getFullYear() + '-' +
        s(d.getMonth()+1,2) + '-' +
        s(d.getDate(),2) + ' ' +
        s(d.getHours(),2) + ':' +
        s(d.getMinutes(),2) + ':' +
        s(d.getSeconds(),2)+ ':' +
		s(d.getMilliseconds(),3);
}

function formatShortDate(d)
{
    // padding function
    var s = function(a,b){return(1e15+a+"").slice(-b)};

    // default date parameter
    if (typeof d === 'undefined'){
        d = new Date();
    };

    // return ISO datetime
    return  s(d.getHours(),2) + ':' +
        s(d.getMinutes(),2);	
}