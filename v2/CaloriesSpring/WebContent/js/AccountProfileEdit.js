//****************************************
//             ID Table JScript Functions
// http://mrbool.com/how-to-add-edit-and-delete-rows-of-a-html-table-with-jquery/26721#
//****************************************

function IdAdd()
{ 
	$("#IDTable tbody").append( "<tr>"+ "<td><input class='IdInput' placeholder='Enter Customer ID'  type='text' style='width: 100%'/></td>"+ "<td><img src='images/save.png' class='btnIdSave'><img src='images/delete.png' class='btnIdDelete'/></td>"+ "</tr>"); 
	$(".btnIdSave").bind("click", IdSave);	
	$(".btnIdDelete").bind("click", IdDelete); 
	$("input.IdInput").focusout( 
			function() 
			{
				IdSave.call(this);
				/*var curVal = $(this).val();
				if( curVal.length > 0)
				{
					
					var parent =$(this).parent();
			    	$(parent).html( curVal );
				}*/
			}
		);	
	$("input.IdInput").focus();
	EnableButton();	
};

function IdSave()
{ 
	var par = $(this).parent().parent(); 
	var tdId = par.children("td:nth-child(1)"); 
	var tdButtons = par.children("td:nth-child(2)");
	
	var idValue = tdId.children("input[type=text]").val();
	if( idValue )
	{
		tdId.html(idValue);
		tdButtons.html("<img src='images/delete.png' class='btnIdDelete'/><img src='images/edit.png' class='btnIdEdit'/>");
		
		$(".btnIdEdit").bind("click", IdEdit); 
		$(".btnIdDelete").bind("click", IdDelete);
	}else{
		par.remove();
	}
	EnableButton();
}; 

function IdEdit()
{ 
	var par = $(this).parent().parent(); 
	var tdId = par.children("td:nth-child(1)"); 
	var tdButtons = par.children("td:nth-child(2)"); 
	tdId.html("<input type='text' id='IdInput' value='"+tdId.html()+"' style='width: 100%'/>"); 
	

	
	tdButtons.html("<img src='images/save.png' class='btnIdSave'/>"); 

	$(par).find('input').focusout( 
			function() 
			{
				IdSave.call(this);
				/*
				var curVal = $(this).val();
				if( curVal.length > 0)
				{
					var parent =$(this).parent();
			    	$(parent).html( curVal );
				}
				*/
			}
		);
	
	$(par).find('input').focus();
	
	$(".btnIdSave").bind("click", IdSave); 
	$(".btnIdEdit").bind("click", IdEdit); 
	$(".btnIdDelete").bind("click", IdDelete); 
};

function IdDelete()
{ 
	var par = $(this).parent().parent(); 
	par.remove(); 
	EnableButton();
}; 

$(function()
{ 
	$(".btnIdEdit").bind("click", IdEdit); 
	$(".btnIdDelete").bind("click", IdDelete); 
	$("#AddIDButton").bind("click", IdAdd); 
});

function EnableButton()
{
	var enable = true;
	var rowCount = $('#IDTable > tbody > tr').length;
	
	if( rowCount == 0){
		enable = false;
	}
	
	
	$('#IDTable > tbody  > tr').each(
		function() 
		{
			$this = $(this)
			var input = $this.find("input");
			if( input.length > 0 ) 
			{
				var inputVal = input.val();
				if( inputVal.length == 0)
				{
					enable = false;
				}
			}
		}
	);
	
	/*
	$('#AttributeTable > tbody  > tr').each(
		function() 
		{
			$this = $(this)
			var value = $this.find("input");
			if( value.length > 0 ) 
			{
				console.log("Found Input in AttributeTable");
				enable = false;
			}
		}
	);
	*/
	if( enable )
	{
		//console.log("Enabling AddProfileBtn");
		$('#AddProfileBtn').removeAttr("disabled");
		$('#UpdateProfileBtn').removeAttr("disabled");
		$('#DeleteProfileBtn').removeAttr("disabled");
		
	}else{
		$('#AddProfileBtn').attr("disabled", "disabled");
		$('#UpdateProfileBtn').attr("disabled", "disabled");
		$('#DeleteProfileBtn').attr("disabled", "disabled");
	}
}

//****************************************
//            Attribute Table JScript Functions
//  http://mrbool.com/how-to-add-edit-and-delete-rows-of-a-html-table-with-jquery/26721#
//****************************************


function GetAccountProfileJson()
{

	var json = {};
	
	json.IDs=[];
	$('#IDTable > tbody  > tr').each(
		function() 
		{		
			var tdKey = $(this).children("td:nth-child(1)");
			
			var txtKey = tdKey.html();

			json.IDs.push(txtKey);
			
			
		}
		
	);
	json.Attributes = attributeTable.getAttributes();
	GetCategoryAttributes(json.Attributes)
			
	return JSON.stringify(json);

	// var json = "{\n  IDs:[";
	// $('#IDTable > tbody  > tr').each(
		// function() 
		// {
			// var tdValue = "";
			
			// var tdKey = $(this).children("td:nth-child(1)");
			
			// var valInput = $(tdKey).find('input');
			// if( valInput.length == 0)
				// tdValue = tdKey.html();
			// else
				// tdValue = valInput.val();
			
			// if( first == true )
			// {
				// first = false;
			// }
			// else
			// {
				// json += ", ";
			// }
			// json += "\"" + tdValue + "\""
		// }
	// );
	// json += "]";
	
	// json += GetAttributeJson();

	// json += "\n}";
	// return json;
}
