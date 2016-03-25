


$('#AttributeValue').on('input', function() 
{
	AttributeValidate();
});

$('#testRegex').click(	function() 
						{
							pattern = $("#AttributeValue").val();
							var parts = pattern.split('/'),
								regex = pattern,
								options = "";
							if (parts.length > 1) {
								regex = parts[1];
								options = parts[2];
							}
							try {
								var regExp = new RegExp(regex, options);
								test = $("#TestAttribute").val();
								t1 = regExp.test(test)
								if( t1)
								{
									$("#resultsLabel").css("color", "green");
									$("#resultsLabel").html("Match");
								}
								else
								{
									$("#resultsLabel").html("No Match");
									$("#resultsLabel").css("color", "red");
								}
							}
							catch(e) {
								return false;
							}
						}
);


$('#AttributeName').on('change',function() 
								{
									AttributeValidate();
								}
);
$('#AttributeOperation').on('change', function() 
										{
											AttributeValidate();
											
											
											var value = $(this).val();
																	
											if( isListOperator( value ) )
											{
												$("#AttributeValueDiv").hide();
												$("#ListChooser").show();
											}else{
												
												if( value == "Matches Regular Expression")
												{
													$("#TestRegexpDiv").show();
													$("#AttributeValue").attr("placeholder", "Enter regex matching expression here.").val("").focus().blur();;
												}else{
													$("#AttributeValue").attr("placeholder", "Enter a value to compare against attribute value.").val("").focus().blur();;
													$("#TestRegexpDiv").hide();
												}
												
												$("#AttributeValueDiv").show();
												$("#ListChooser").hide();
											}
										  
										}
);

//var $value = $(this).val();	
									var index = $(this).prop("selectedIndex");
									if( index  == 0 )
									{
										$("#AttributeNameDiv").addClass("has-error");
									}else{
										
									}
var completeFunction = null;								
function AttributeReset( type , _completeFunction )
{
	completeFunction = _completeFunction;
	
	var array=[];
	array.push( { text: "Choose Attribute name" , value:"Choose Attribute name", disabled:true , selected:true});
	
	for (var property in type.Attributes) 
	{
		 var prop = type.Attributes[property];
		 array.push( { text: property , value: property} );
	}
	$("#AttributeName").replaceOptions(array);
	
	$("#AttributeName").prop("selectedIndex",0);
	$("#AttributeOperation").prop("selectedIndex",0);
	$("#AttributeList").prop("selectedIndex", 0);
	$("#AttributeList").prop("selectedIndex", 0);
	$("#AttributeValue").val("");
	
	$("#AttributeOperationDiv").hide();
	$("#AttributeValueDiv").hide();
	$("#ListChooser").hide();
	
	AttributeValidate();
}

function AttributeValidate()
{
		var attrIndex = $("#AttributeName").prop("selectedIndex");
		var operIndex = $("#AttributeOperation").prop("selectedIndex");
		var operation=$("#AttributeOperation").val();
		
		if( attrIndex  == 0 )
		{
			$("#AttributeNameDiv").addClass("has-error");
			$("#AttributeOperationDiv").hide();
		}else{
			$("#AttributeNameDiv").removeClass("has-error");
			$("#AttributeOperationDiv").show();
		}
		
		if( operIndex  == 0 )
		{
			$("#AttributeOperationDiv").addClass("has-error");
			
		}else{
			$("#AttributeOperationDiv").removeClass("has-error");
		}
			
		var value="";
		if( isListOperator( operation) )
		{
			var listIndex = $("#AttributeList").prop("selectedIndex");
			if( operIndex  == 0 )
			{
				$("#ListChooserDiv").addClass("has-error");
			}else{
				$("#ListChooserDiv").removeClass("has-error");
			}
			if( attrIndex > 0 && operIndex > 0 && listIndex > 0)
			{
				$('#AttrConstOK').prop("disabled",false);
				//$('#AttrConstOK').enable();
				return;
			}			
		}else{
			value=$("#AttributeValue").val();
			if( value.length > 0)
			{
				$("#AttributeValueDiv").removeClass("has-error");
			}else{
				$("#AttributeValueDiv").addClass("has-error");
			}
			
			if( attrIndex > 0 && operIndex > 0 && value.length > 0)
			{
				$('#AttrConstOK').prop("disabled",false);
				return;
			}
		}
		
		$('#AttrConstOK').prop("disabled",true);
}

$('#AttrConstOK').click(
	function()
	{
		// Construct Filter row
		var attrName =$("#AttributeName").val();
		var operation=$("#AttributeOperation").val();
		var value="";
		if( isListOperator( operation) )
		{
			value=$("#AttributeList").val();
		}else{
			value=$("#AttributeValue").val();
		}
		
		var json=createJSON( ATTRIBUTE, "'" + attrName + "' " + operation + " '" + value+"'", true);
		json.Attribute = attrName;
		json.Operator = operation;
		json.Operand = value;
		
		completeFunction( json );
		//addAttributeFilter(attAddAfterElement, json);
		
	}
);

function isListOperator( value )
{
	if( value == "In List" || value == "Not In List" )
		return true;
	
	return false;
}

function createJSON(type, displayText, canEdit)
{
		var json={};
		json.DisplayText=displayText
		json.Type=type
		json.CanEdit=canEdit;
		
		return json;
}

