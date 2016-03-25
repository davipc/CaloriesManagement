
var AttributeTable =
(
	function( ) 
	{
		//properties/fields
		var hasError = false;
		var includeType = true
		var callbacks = {};
		var _divId = null;
		var _name = null;
		var editingRow = null;
		
		// VALUE NEEDS TO BE THE SAME AS THE ONE DEFINED BY 
		// com.convergys.ihublib.ReportCategory.ReportCategoryConstants.REPORT_CATEGORY_ATTRIBUTES_NAME
		var ignoreAttribute = "ReportingAttributes";
		var keyName = "Key";
		var valueName = "Value";

		var jsoneditor =null;
		var jsonEditorActive = false;
		
		function oTypedAttributeTable() {}

		oTypedAttributeTable.prototype = 
		{
		
			/**
			 * Adds a callback function for an event on the widget
			 * @param strEvent String event name to attach to
			 * @param callback function the method to call when the event fires
			 */
			bind: function(strEvent, callback) 
			{
				if (!callbacks.hasOwnProperty(strEvent)) {
					callbacks[strEvent] = [];
				}
				callbacks[strEvent].push(callback);
			},
			
			isValid : function()
			{
				return !hasError;
			},
			
			reset : function()
			{
				$('#' + _name + ' li.ui-widget-content:not(:first)').remove();
				FilterValidate();
			},
			getAttributes: function()
			{
				var json = {};
				$('#' + _name +' > tbody  > tr').each(
					function() 
					{
						var tdName = $(this).children("td:nth-child(1)");
						var tdType = $(this).children("td:nth-child(2)");
						var tdDesc = $(this).children("td:nth-child(3)"); 	

						var nameValue = tdName.html();
						var typeValue = tdType.html();
						var descValue = tdDesc.html();
						switch( typeValue)
						{
							case "JSON":
								var j = JSON.parse(descValue);
								json[nameValue] =j
								break;
								
							case "boolean":
								json[nameValue] = (descValue=='true'?true:false);
								break;
								
							case "number":
								json[nameValue] = Number(descValue);
								break;
							default:
								json[nameValue] = descValue;
								break;
						}
					}
				);
				return json;
			},
			
			clearTable: function(Attributes)
			{
				clearTable();
				postErrorMessage("");
			},
			
			displayAttributes: function(Attributes) 
			{
				clearTable();
				$.each( Attributes, 
					function( name, json ) 
					{
						if( name != ignoreAttribute )
							addAttribute( name, json)
					}
				);
			},
			
			autoFillAttributes: function(Attributes) 
			{
				clearTable();
				$.each( Attributes, 
					function( name, json ) 
					{
						addTypeAttribute( name, json);
						
					}
				);
			},

			create: function(name, divName)
			{
				_name = name;
				if( divName )
				{
					_divId = divName
					$('#' + _divId).html(
						'<TABLE id="' + _name + '" class="table table-bordered" >' + 
							'<thead>' + 
								'<TR><TH style="width: 20%">' + keyName + '</TH>' +
								(includeType?'<TH style="width: 20%">Type</TH>':'') +
								'<TH style="width: 50%">' + valueName + '</TH> <TH style="width: 10%"></TH></TR>'+
							'</thead>'+
							'<tbody> </tbody> '+
						'</TABLE>'+
						'<BR>'+
						'<button class="AddAttrButton btn btn-default  btn-circle " >+</button>' +
						'<div class="divider"/>' +
						'<button class="btn btn-default" id="AutoCreate">Auto Create Attributes</button>'
					);
					$("#AutoCreate").hide();
				}
				
				//
				// Events for FilterEditor
				//
				$('#'+_divId).on('click','.AddAttrButton', 
					function(event)
					{
						addAttribute( "", "");
						editingRow = $('#' +  _name + ' tbody tr:last')
						InPlaceEditRow();
					}
				);
				
				$('#'+_divId).on('click',"#autoPopulate",	
					function()
					{

					}
				);
	
				$('#'+_divId).on('click',".btnAttrDelete",	
					function()
					{
						var row = $(this).parent().parent();
						$(row).remove();
					}
				);
	
				$('#'+_divId).on('click',".btnAttrEdit",	
					function()
					{
						editingRow = $(this).parent().parent();
						InPlaceEditRow();
					}
				);
				
				$('#'+_divId).on('click',".btnAttrSave",
					function()
					{
						editingRow = $(this).parent().parent();
						StopInPlaceEditRow();
					}
				);
				
				// Start the JSON Editor
				$('#'+_divId).on('focus',"#txtValue",
					function(event) 
					{
						var val = $("#typeSelect").val();
						
						if( $("#typeSelect").val() == "JSON" )
						{
							if( jsoneditor == null){
								
								var container = document.getElementById('jsoneditor');
								  
								var options = {
									mode: 'tree',
									modes: ['code', 'form', 'text', 'tree', 'view'], // allowed modes
									error: function (err) {
									  alert(err.toString());
									}
								  };


								jsoneditor = new JSONEditor(container, options);
								
								$('#jsonCancel').click(
									function() 
									{
										jsonEditorActive = false;
										$('#JsonEditorModal').modal('hide');
										
										$('#txtKey').focus();
									}
								);
								
								$('#jsonOK').click(
									function() 
									{
										$('#txtKey').focus();
										jsonEditorActive = false;
										try{
											var json = jsoneditor.get();
											$('#JsonEditorModal').modal('hide');
											$("#txtValue").val( JSON.stringify(json) );
											$('#txtKey').focus();
										}catch(err)
										{
											postErrorMessage("JSON is not valid.  Please fix it." );
										}
										
									}
								);
							}	
							var jsonString = $("#txtValue").val();
							var json = null;
							if( jsonString == null || jsonString.length == 0)
							{
								json = {};
							}else{
								json = JSON.parse(jsonString);
							}
							
							postErrorMessage("");
							jsoneditor.set(json);
							$('#JsonEditorModal').modal({
							  backdrop: 'static',
							  keyboard: true
							});
							$('#JsonEditorModal').css('z-index', 9999); 
							$('#JsonEditorModal').css('position', 'absolute');
							jsonEditorActive = true;
							
						}
					}
				);
			}
			
			
		}
		
		function getTypeSelect( selectedItem, editable )
		{
			if( editable){
				return '<select style="width: 100%" id="typeSelect">'+
						'<option value="string" ' +(selectedItem=='string'?'selected':'')+ '>String</option>'+
						'<option value="number" ' +(selectedItem=='number'?'selected':'')+ '>Number</option>'+
						'<option value="boolean" ' +(selectedItem=='boolean'?'selected':'')+ '>Boolean</option>'+
						'<option value="JSON" ' +(selectedItem=='JSON'?'selected':'')+ '>Json Object</option>'+
						'</select>'
			}else{
				return "<input type='text' id='typeSelect' disabled='true' value='"+selectedItem+"' style='width: 100%'/>"
			}
		}
		function getBooleanSelect( value )
		{
			 var html = '<select style="width: 100%" id="typeSelect">'+
							'<option value="true" ' + (value=='true'?'selected':'') + '>true</option>'+
							'<option value="false" ' + ( value!='true'?'selected':'') + '>false</option>'+
						'</select>';
			return html;
		}
		//
		// Private Methods for FilterEditor
		//
		
				/**
		* Dispatches events to all the registered handlers
		* @param eventName String the event to handle
		*/
		function dispatchEvent(eventName) 
		{
			// Create an event object to send the callback
			var evt = {};

			if (callbacks.hasOwnProperty(eventName)) 
			{
				var myCallbacks = callbacks[eventName];
				for (var i = 0; i < myCallbacks.length; ++i)
				{
					// Call the attached function with an event object parameter
					myCallbacks[i](evt);
				}
			}
		}
		
		function clearTable()
		{
			$("#" + _name + " tbody").empty(); // clear table
		}
		
		function addTypeAttribute( name, json)
		{
			var type = json.Type;
			var val = null;
			var typeString = "string";
			
			if( type.startsWith("string") )
			{
				typeString = type;
				val="";
			}
			if( type.startsWith("number") )
			{
				typeString = "string"
				val='0';
			}
			if( type.startsWith("JSON") )
			{
				typeString = "JSON"
				val='{}';
			}
			if( type.startsWith("boolean") )
			{
				typeString = "boolean"
				val='false';
			}
			
			var html = "<tr> <td editable='false'>" + name +  "</td>" + 
			(includeType?"<td editable='false'>" + type +  "</td> ":"") + 
			"<td>" + val +  "<td><img src='images/delete.png' class='btnAttrDelete'/><img src='images/edit.png' class='btnAttrEdit'/></td>"+ "</tr>"

			return $("#" + _name + " tbody").append( html ); 

		}
		
		function addAttribute( name, json)
		{
			var description="";
			var type = "string";
			
			var typeString = typeof json;
			
			switch( typeString)
			{
				case "boolean":
					type = "boolean"
					description = json;
					break;
				case "number":
					type = "number"
					description = json;
					break;
				case "object":
					type = "JSON";
					description = JSON.stringify( json );
					break;
				default:
					type = "string";
					description = json;
					break;
			}
			
			var html = "<tr> <td>" + name +  "</td>" + 
						(includeType?"<td editable='true'>" + type +  "</td> ":"") + 
						"<td>" + description +  "<td><img src='images/delete.png' class='btnAttrDelete'/><img src='images/edit.png' class='btnAttrEdit'/></td>"+ "</tr>"

			return $("#" + _name + " tbody").append( html ); 
		}

		
		
		function createAttribute()
		{
				var json={};
				json.Attributes=displayText
				json.Type=type
				json.CanEdit=canEdit;
				
				return json;
		}
	
	    
		function InPlaceEditRow()
		{
			var typeEditable = true;
			var nameEditable = true;
			
			var tdName = $(editingRow).children("td:nth-child(1)");
			var tdType = $(editingRow).children("td:nth-child(2)");
			var tdDesc = $(editingRow).children("td:nth-child(3)"); 	
			var tdButtons = $(editingRow).children("td:nth-child(4)"); 
			
			var editable = tdType.attr("editable");
			if( editable && editable == "false")
			{
				typeEditable = false;
			}
			editable = tdName.attr("editable");
			if( editable && editable == "false")
			{
				nameEditable = false;
			}
			
			var type = tdType.html();
			
			tdName.html("<input type='text' id='txtKey' value='"+tdName.html()+"' style='width: 100%' " + (nameEditable?"":"disabled='true'") +"/>"); 
			
			tdType.html( getTypeSelect( type , typeEditable) ); 
			
			if( type == "boolean")
			{
				tdDesc.html( getBooleanSelect( tdDesc.html() ) );
			}else{
				tdDesc.html("<input type='text' id='txtValue' value='"+tdDesc.html()+"' class 'value' style='width: 100%'/>"); 
			}
			tdButtons.html("<img src='images/save.png' class='btnAttrSave'/>"); 
			
			
			var delayedFn, blurredFrom;
			$('tr').on('blur', 'input', function(event) {
				blurredFrom = event.delegateTarget;
				delayedFn = setTimeout(function() {
					if( !jsonEditorActive)
						StopInPlaceEditRow();
				}, 0);
			});
			$('tr').on('blur', 'select', function(event) {
				blurredFrom = event.delegateTarget;
				delayedFn = setTimeout(function() {
					StopInPlaceEditRow();
				}, 0);
			});
			$('tr').on('focus', 'input', function(event) {
				if (blurredFrom === event.delegateTarget) {
					clearTimeout(delayedFn);
				}
			});
			
			// make sure the attribute name is not the reserved reporting categories attribute name
			$('tr > td > input').on("keyup", function() {
				var attrName = tdName.children(":nth-child(1)").val();
				if (attrName == ignoreAttribute) {
					//alert('Invalid attribute name!');
					tdName.addClass("text-danger");
					tdButtons.html(""); 
				} else {
					tdName.removeClass("text-danger");
					tdButtons.html("<img src='images/save.png' class='btnAttrSave'/>"); 
				}
				
			});
			
			$('#typeSelect').change(
				function(event) 
				{
					var tdDesc = $(editingRow).children("td:nth-child(3)");
					var tdContent = $(tdDesc).children(":nth-child(1)");
					var type = $('#typeSelect').val();
					switch (type)
					{
						case "boolean":
							if( tdContent.is("input") )
							{
								var html = getBooleanSelect( tdContent.val() );
								tdDesc.html( html);
							}
						break;
						
						default:
							if( tdContent.is("select") )
							{
								tdDesc.html("<input type='text' id='txtValue' value='"+tdContent.val()+"' class 'value' style='width: 100%'/>"); 
							}
						
						break;
					}
					
				}
			);
			
			$('tr').on('focus', 'select', 
				function(event) 
				{
					if (blurredFrom === event.delegateTarget) {
						clearTimeout(delayedFn);

				}
			});
			
			if( nameEditable)
				tdName.children(":nth-child(1)").focus();
			else
				tdDesc.children(":nth-child(1)").focus();
		}
		
		function postSuccessMessage(message)
		{
			var msg = $("#jsonmessage");
			$("#jsonmessage").removeClass("text-danger");
			$("#jsonmessage").addClass("text-info");
			$("#jsonmessage").html( message );
		}

		function postErrorMessage(message)
		{
			$("#jsonmessage").addClass("text-danger");
			$("#jsonmessage").removeClass("text-info");
			$("#jsonmessage").html( message );
		}	
		
		function StopInPlaceEditRow()
		{
			var row = editingRow;
			if( ! row) 
				return;
			editingRow = null;
			
			var tdName = $(row).children("td:nth-child(1)");
			var tdType = $(row).children("td:nth-child(2)");
			var tdDesc = $(row).children("td:nth-child(3)"); 	
			var tdButtons = $(row).children("td:nth-child(4)");

	
			var nameValue = tdName.children(":nth-child(1)").val();
			var typeValue = tdType.children(":nth-child(1)").val();
			var descValue = tdDesc.children(":nth-child(1)").val();
			if( nameValue )
			{
				if (tdButtons.html() == '') {
					$(row).remove();
				} else {
					tdName.html(nameValue);
					if( typeValue )
						tdType.html(typeValue);
					tdDesc.html(descValue);
					
					tdButtons.html("<img src='images/delete.png' class='btnAttrDelete'/><img src='images/edit.png' class='btnAttrEdit'/>");
				}
			}else{
				$(row).remove();
			}
			
		}

		
		return oTypedAttributeTable;
	}
)();




