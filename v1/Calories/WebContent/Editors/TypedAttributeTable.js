
var TypedAttributeTable =
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
						json[nameValue] = {};
						json[nameValue].Type = typeValue;
						json[nameValue].Description = descValue;
					}
				);
				return json;
			},
			
			clearTable: function(Attributes)
			{
				clearTable();
			},
			
			displayAttributes: function(Attributes) 
			{
				clearTable();
				$.each( Attributes, 
					function( name, json ) 
					{
						addAttribute( name, json)
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
								'<TR><TH style="width: 20%">Name</TH>' +
								(includeType?'<TH style="width: 20%">Type</TH>':'') +
								'<TH style="width: 50%">Description</TH> <TH style="width: 10%"></TH></TR>'+
							'</thead>'+
							'<tbody> </tbody> '+
						'</TABLE>'+
						'<BR>'+
						'<button class="AddAttrButton btn btn-default  btn-circle " >+</button>'
						//+'<button class="btn btn-default" id="autoPopulate" hidden >Auto Populate from existing Breadcrumbs</button>	'
					);
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
			}
			
			
		}
		
		function getTypeSelect( selectedItem )
		{
			return '<select style="width: 100%">'+
						'<option value="string" ' +(selectedItem=='string'?'selected':'')+ '>String</option>'+
						'<option value="number" ' +(selectedItem=='number'?'selected':'')+ '>Number</option>'+
						'<option value="boolean" ' +(selectedItem=='boolean'?'selected':'')+ '>Boolean</option>'+
						'<option value="JSON" ' +(selectedItem=='JSON'?'selected':'')+ '>Json Object</option>'+
						'<option value="string:DateYYYYMMDD" ' +(selectedItem=='string:DateYYYYMMDD'?'selected':'')+ '>String Date YYYYMMDD</option>' + 
						'</select>'
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
		
		function addAttribute( name, json)
		{
			var description="";
			var type = "string";
			if( Object.prototype.toString.call(json) == '[object String]' ) 
			{
				description = json;
			}else {
				if( json && json.Description) 
					description = json.Description;
				if( json && json.Type )
					type = json.Type;
			}	
			
			var html = "<tr> <td>" + name +  "</td>" + 
						(includeType?"<td>" + type +  "</td> ":"") + 
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
			var tdName = $(editingRow).children("td:nth-child(1)");
			var tdType = $(editingRow).children("td:nth-child(2)");
			var tdDesc = $(editingRow).children("td:nth-child(3)"); 	
			var tdButtons = $(editingRow).children("td:nth-child(4)"); 
			
			tdName.html("<input type='text' id='txtKey' value='"+tdName.html()+"' style='width: 100%'/>"); 
			tdType.html( getTypeSelect( tdType.html() ) ); 
			tdDesc.html("<input type='text' id='txtValue' value='"+tdDesc.html()+"' style='width: 100%'/>"); 
			tdButtons.html("<img src='images/save.png' class='btnAttrSave'/>"); 
			
			
			var delayedFn, blurredFrom;
			$('tr').on('blur', 'input', function(event) {
				blurredFrom = event.delegateTarget;
				delayedFn = setTimeout(function() {
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
			$('tr').on('focus', 'select', function(event) {
				if (blurredFrom === event.delegateTarget) {
					clearTimeout(delayedFn);
				}
			});
			
			tdName.children(":nth-child(1)").focus();
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
				tdName.html(nameValue);
				tdType.html(typeValue);
				tdDesc.html(descValue);
				
				tdButtons.html("<img src='images/delete.png' class='btnAttrDelete'/><img src='images/edit.png' class='btnAttrEdit'/>");
			}else{
				$(row).remove();
			}
			
		}

		
		return oTypedAttributeTable;
	}
)();


