//////////////////////////////////////////////////////
// Attribute compare Editor
//////////////////////////////////////////////////////
var LOGIC = "Logic";
var NOT = "Not";
var OPEN = "(";
var CLOSE = ")";
var ATTRIBUTE = 'Attribute';

$('#AttributeValue').on('input', function() {
	AttributeValidate();
});

$('#testRegex').click(function() {
	pattern = $("#AttributeValue").val();
	try {
		// we use ^ and $ because we want the matcher to behave like Java's, that is, to match the entire string.
		// in JS if we don't put those 2 special chars, it will accept partial matches, while in Java it won't.
		// for instance, in JS matching "abc123def" against "\d+" will succeed (matching the "123" piece), while in Java it would fail.
		var regExp = new RegExp('^' + pattern + '$');
		test = $("#TestAttribute").val();
		t1 = regExp.test(test)
		if (t1) {
			$("#resultsLabel").css("color", "green");
			$("#resultsLabel").html("Match");
		} else {
			$("#resultsLabel").html("No Match");
			$("#resultsLabel").css("color", "red");
		}
	} catch (e) {
		return false;
	}
});


$('#AttributeName').on('change', function() {
	AttributeValidate();
	
	//alert(JSON.stringify(localTypeData));
	
	// change operations list to be according to attribute type
	setOperationsForAttribute($('#AttributeName').val());	
});

$('#AttributeOperation').on('change', function() {
	AttributeValidate();

	var value = $(this).val();

	if (isListOperator(value)) {
		$("#AttributeValueDiv").hide();
		$("#ListChooser").show();
	} else {

		if (value == "Matches Regular Expression") {
			$("#TestRegexpDiv").show();
			$("#AttributeValue").attr("placeholder",
					"Enter regex matching expression here.")
					.val("").focus().blur();
			;
		} else {
			$("#AttributeValue")
					.attr("placeholder",
							"Enter a value to compare against attribute value.")
					.val("").focus().blur();
			;
			$("#TestRegexpDiv").hide();
		}

		$("#AttributeValueDiv").show();
		$("#ListChooser").hide();
	}

});

//var $value = $(this).val();	
var index = $(this).prop("selectedIndex");
if (index == 0) {
	$("#AttributeNameDiv").addClass("has-error");
} else {

}

var completeFunction = null;

// so the JS has access to the type data structure in the files using it
var localTypeData = null;

function AttributeReset(type, _completeFunction) {
	localTypeData = instrumentTypeData(type);
	completeFunction = _completeFunction;

	//alert("Attribute Reset: " + JSON.stringify(type));
	
	var array = [];
	array.push({
		text : "Choose Attribute name",
		value : "Choose Attribute name",
		disabled : true,
		selected : true
	});

	for ( var property in type.Attributes) {
		var prop = type.Attributes[property];
		array.push({
			text : property,
			value : property
		});
	}
	
	array.push({text: "Base.Status", value: "Base.Status"});
	array.push({text: "Base.ChannelType", value: "Base.ChannelType"});
	array.push({text: "Base.ApplicationName", value: "Base.ApplicationName"});
	
	$("#AttributeName").replaceOptions(array);

	$("#AttributeName").prop("selectedIndex", 0);

	// change operations list to be according to attribute type
	setOperationsForAttribute($('#AttributeName').val());	
		
	$("#AttributeOperation").prop("selectedIndex", 0);
	$("#AttributeValue").val("");
	$("#TestAttribute").val("");
	$("#resultsLabel").html("");
	$("#AttributeList").prop("selectedIndex", 0);

	$("#AttributeOperationDiv").hide();
	$("#AttributeValueDiv").hide();
	$("#TestRegexpDiv").hide();
	$("#ListChooser").hide();

	AttributeValidate();
}

function instrumentTypeData(type) {
	var newTypeData = jQuery.extend(true, {}, type);
	
	/**
	if (newTypeData.Attributes) {
		newTypeData.Attributes.BaseStatus = {Type: "string", Description: "The base status"};
		newTypeData.Attributes.BaseChannelType = {Type: "string", Description: "The base channel type"};
		newTypeData.Attributes.BaseApplicationName = {Type: "string", Description: "The base application name"};
	}
	**/
	
	return newTypeData;
}

function AttributeLoadForEdit(type, currJson, _completeFunction) {
	localTypeData = instrumentTypeData(type);
	completeFunction = _completeFunction;

	var array = [];
	array.push({
		text : "Choose Attribute name",
		value : "Choose Attribute name",
		disabled : true,
		selected : true
	});

	for ( var property in type.Attributes) {
		var prop = type.Attributes[property];
		array.push({
			text : property,
			value : property
		});
	}

	array.push({text: "Base.Status", value: "Base.Status"});
	array.push({text: "Base.ChannelType", value: "Base.ChannelType"});
	array.push({text: "Base.ApplicationName", value: "Base.ApplicationName"});
	
	$("#AttributeName").replaceOptions(array);

	$("#AttributeName").val(currJson.Attribute);
	
	// change operations list to be according to attribute type
	setOperationsForAttribute($('#AttributeName').val());	
	
	$("#AttributeOperation").val(currJson.Operator);
	$("#AttributeValue").val(currJson.Operand);
	$("#TestAttribute").val("");
	$("#resultsLabel").html("");
	$("#AttributeList").prop("selectedIndex", 0);

	$("#AttributeOperationDiv").show();
	$("#AttributeValueDiv").show();

	if (currJson.Operator == "Matches Regular Expression") {
		$("#TestRegexpDiv").show();
	} else {
		$("#TestRegexpDiv").hide();
	}
	
	$("#ListChooser").hide();

	AttributeValidate();
}


function setOperationsForAttribute(attributeName) {
	if (attributeName && localTypeData) {
		// find type for attribute name
		var attribute = localTypeData.Attributes[attributeName];
		var attributeType = '';
		
		if (attribute && attribute.Type) {
			attributeType = attribute.Type;
		} else {
			if (attributeName.indexOf('Base.') == 0) {
				attributeType = 'string';
			}
		}
		
		var operations = [];
		operations.push({
			text : "Select Operation",
			value : "",
			disabled : true,
			selected : true
		});			

		if (attributeType == 'string') {
			operations.push({text: "==", value: "==" });
			operations.push({text: "!=", value: "!=" });
			operations.push({text: "StartsWith", value: "StartsWith" });
			operations.push({text: "Does not StartsWith", value: "Does not StartsWith" });
			operations.push({text: "EndsWith", value: "EndsWith" });
			operations.push({text: "Does not EndsWith", value: "Does not EndsWith" });
			operations.push({text: "Matches Regular Expression", value: "Matches Regular Expression" });
		} else if (attributeType == 'number') {
			operations.push({text: "==", value: "==" });
			operations.push({text: "!=", value: "!=" });
			operations.push({text: ">", value: ">" });
			operations.push({text: "<", value: "<" });
			operations.push({text: ">=", value: ">=" });
			operations.push({text: "<=", value: "<=" });
		} else {
			operations.push({text: "==", value: "==" });
			operations.push({text: "!=", value: "!=" });
			operations.push({text: ">", value: ">" });
			operations.push({text: "<", value: "<" });
			operations.push({text: ">=", value: ">=" });
			operations.push({text: "<=", value: "<=" });
			operations.push({text: "StartsWith", value: "StartsWith" });
			operations.push({text: "Does not StartsWith", value: "Does not StartsWith" });
			operations.push({text: "EndsWith", value: "EndsWith" });
			operations.push({text: "Does not EndsWith", value: "Does not EndsWith" });
			operations.push({text: "Matches Regular Expression", value: "Matches Regular Expression" });
		}
		
		$('#AttributeOperation').replaceOptions(operations);
	}
}


function AttributeValidate() {
	var attrIndex = $("#AttributeName").prop("selectedIndex");
	var operIndex = $("#AttributeOperation").prop("selectedIndex");
	var operation = $("#AttributeOperation").val();

	if (attrIndex == 0) {
		$("#AttributeNameDiv").addClass("has-error");
		$("#AttributeOperationDiv").hide();
	} else {
		$("#AttributeNameDiv").removeClass("has-error");
		$("#AttributeOperationDiv").show();
	}

	if (operIndex == 0) {
		$("#AttributeOperationDiv").addClass("has-error");

	} else {
		$("#AttributeOperationDiv").removeClass("has-error");
	}

	var value = "";
	if (isListOperator(operation)) {
		var listIndex = $("#AttributeList").prop("selectedIndex");
		if (operIndex == 0) {
			$("#ListChooserDiv").addClass("has-error");
		} else {
			$("#ListChooserDiv").removeClass("has-error");
		}
		if (attrIndex > 0 && operIndex > 0 && listIndex > 0) {
			$('#AttrConstOK').prop("disabled", false);
			//$('#AttrConstOK').enable();
			return;
		}
	} else {
		value = $("#AttributeValue").val();
		if (value.length > 0) {
			$("#AttributeValueDiv").removeClass("has-error");
		} else {
			$("#AttributeValueDiv").addClass("has-error");
		}

		if (attrIndex > 0 && operIndex > 0 && value.length > 0) {
			$('#AttrConstOK').prop("disabled", false);
			return;
		}
	}

	$('#AttrConstOK').prop("disabled", true);
}

$('#AttrConstOK').click(
		function() {
			// Construct Filter row
			var attrName = $("#AttributeName").val();
			var operation = $("#AttributeOperation").val();
			var value = "";
			if (isListOperator(operation)) {
				value = $("#AttributeList").val();
			} else {
				value = $("#AttributeValue").val();
			}

			var json = createJSON(ATTRIBUTE, "'" + attrName + "' " + operation
					+ " '" + value + "'", true);
			json.Attribute = attrName;
			json.Operator = operation;
			json.Operand = value;

			completeFunction(json);
			//addAttributeFilter(attAddAfterElement, json);

		}
);

function isListOperator(value) {
	if (value == "In List" || value == "Not In List")
		return true;

	return false;
}

function createJSON(type, displayText, canEdit) {
	var json = {};
	json.DisplayText = displayText
	json.Type = type
	json.CanEdit = canEdit;

	return json;
}
