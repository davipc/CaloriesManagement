
//****************************************
//            Attribute Table JScript Functions
//  http://mrbool.com/how-to-add-edit-and-delete-rows-of-a-html-table-with-jquery/26721#
//****************************************

function DisplayAttributes(Attributes)
{
	$.each( Attributes, AddAttributeRow);
}

function GetAttributeJson(  )
{
	var json = {};
	var attributes = {};
	
	var rowCount = $('#AttributeTable > tbody > tr').length;
	
	if( rowCount > 0)
	{
		first = true;	
		
			$('#AttributeTable > tbody  > tr').each(
				function() 
				{
					var keyValue = "";
					var valueValue = "";
					var tdKey = $(this).children("td:nth-child(1)");
					var tdValue = $(this).children("td:nth-child(2)");
					
					var keyInput = $(tdKey).find('input');
					if( keyInput.length == 0)
						keyValue = tdKey.html();
					else
						keyValue = keyInput.val();
					
					var valInput = $(tdValue).find('input');
					if( valInput.length == 0)
						valueValue = tdValue.html();
					else
						valueValue = valInput.val();
					
					if( keyValue.length > 0)
					{
						if( first == true )
						{
							first = false;
						}
						else
						{
							json += ",  ";
						}
						attributes[keyValue] = valueValue;
					}
				}
			);
			
		json += "}";
	}
	attributes = GetCategoryAttributes(attributes);
	
	if( Object.keys(attributes ).length > 0)
	{
		//json["Attributes"] = attributes;
		return ", \"Attributes\":" + JSON.stringify(attributes);
	}
	return "";
}

function AddAttributeRow( attribName, attribValue, edit)
{
	edit = (typeof edit === "undefined") ? false : edit;
	
	
	if( edit ){
		$("#AttributeTable tbody").append( "<tr>"+ "<td><input type='text' value='" + attribName + "' placeholder='Enter Attribute " +getColumnHeader(0) + "' style='width: 100%'/></td>"+ 
												   "<td><input type='text' value='" + attribValue + "' placeholder='Enter Attribute " +getColumnHeader(1) + "' style='width: 100%'/></td>"+
												   "<td><img src='images/save.png' class='btnAttrSave'><img src='images/delete.png' class='btnAttrDelete'/></td>"+ 
										   "</tr>");
		$(".btnAttrSave").bind("click", AttSave);	
		$(".btnAttrDelete").bind("click", AttDelete); 
	}
	else
	{
		$("#AttributeTable tbody").append( "<tr><td>" + attribName +  "</td>" +
											   "<td>" + attribValue +  "</td> " + 
											   "<td><img src='images/delete.png' class='btnAttrDelete'/><img src='images/edit.png' class='btnAttrEdit'/></td>"+ 
										   "</tr>");
		$(".btnAttrEdit").bind("click", AttEdit); 	
		$(".btnAttrDelete").bind("click", AttDelete); 
	}
	/*
	$("#AttributeTable tbody").append( "<tr><td class=liveEdit>" + attribName +  "</td>" + "<td class=liveEdit >" + attribValue +  "</td> " + "<td><img src='images/delete.png' class='btnAttrDelete'/></td>");
	$(".btnAttrDelete").bind("click", AttDelete); 
	InplaceEdit();
	*/
}
function getColumnHeader( column)
{
	var tdKey = $("#AttributeTable >thead").find('th').eq( column );
	return tdKey.html();
}

function InplaceEdit()
{
	$("td.liveEdit").click(
		function()	
		{
			var input = $(this).find('input');
			if( input.length == 0)
			{
				var curValue = $(this).html();
				//$(this).attr('class', 'edited');
				$(this).html("<input type='text' class='editInput' value='" + curValue + "' style='width: 100%' autofocus/>");
				$(this).find('input').focus();
				
				$("input.editInput").focusout( 
						function() 
						{
							var curVal = $(this).val();
							var parent =$(this).parent();
					    	$(parent).html( curVal );
						}
					);
			}
		}
	);

}

function AttAdd()
{ 
	AddAttributeRow( "", "", true);
	EnableButton();
};

function AttSave()
{ 
	var par = $(this).parent().parent(); 
	var tdKey = par.children("td:nth-child(1)"); 
	var tdValue = par.children("td:nth-child(2)"); 
	var tdButtons = par.children("td:nth-child(3)");
	
	var keyValue = tdKey.children("input[type=text]").val();
	var valueValue = tdValue.children("input[type=text]").val();
	if( keyValue )
	{
		tdKey.html(keyValue);
		tdValue.html(valueValue);
		tdButtons.html("<img src='images/delete.png' class='btnAttrDelete'/><img src='images/edit.png' class='btnAttrEdit'/>");
		
		$(".btnAttrEdit").bind("click", AttEdit); 
		$(".btnAttrDelete").bind("click", AttDelete);
	}else{
		par.remove();
	}
	
	EnableButton();
}; 

function AttEdit()
{ 
	var par = $(this).parent().parent(); 
	var tdKey = par.children("td:nth-child(1)");
	var tdValue = par.children("td:nth-child(2)"); 	
	var tdButtons = par.children("td:nth-child(3)"); 
	
	tdKey.html("<input type='text' id='txtKey' value='"+tdKey.html()+"' style='width: 100%'/>"); 
	tdValue.html("<input type='text' id='txtValue' value='"+tdValue.html()+"' style='width: 100%'/>"); 
	tdButtons.html("<img src='images/save.png' class='btnAttrSave'/>"); 
	
	$(".btnAttrSave").bind("click", AttSave); 
	$(".btnAttrEdit").bind("click", AttEdit); 
	$(".btnAttrDelete").bind("click", AttDelete); 
	
	$(par).focusout(
		function() 
		{
			AttSave();
		}
	);
	$(par).blur(
		function() 
		{
			AttSave();
		}
	);
};

function AttDelete()
{ 
	var par = $(this).parent().parent(); 
	par.remove(); 
	EnableButton();
}; 

$(function()
{
	//$(".btnAttrEdit").bind("click", AttEdit); 
	//$(".btnAttrDelete").bind("click", AttDelete); 
	$("#AddAttrButton").bind("click", AttAdd); 
});
