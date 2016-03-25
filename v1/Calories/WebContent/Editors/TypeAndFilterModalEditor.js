//
// Handle Condition Editor Add Selections
//


var TypeAndFilterModalEditor =(
	function( ) 
	{
		//properties/fields
		var _name = "list";    // Name of this FilterEditor
		var data=[];
		var typeData = null;
		var ID=null;
		var attAddAfterElement;
		var hasError = false;
		var callbacks = {};
		var resultName = "Event";
		var filterEditor = null;
		
		var completeFunction = null;
		
		function oTypeAndFilterModalEditor() {}

		oTypeAndFilterModalEditor.prototype = 
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
			
			reset: function()
			{
				if( filterEditor)
					filterEditor.reset();
				$("#" + _name + "Type").prop("selectedIndex",0);
				data=[];
				validateForm();
			},
			
			prepareForAdd: function(_completeFunction) {
				completeFunction = _completeFunction;
				
				createFilterEditorIfNull();
				filterEditor.reset();
			},

			prepareForEdit: function(attEditCurrElement, currJson, _completeFunction) {
				completeFunction = _completeFunction;
				
				$("#" + _name + "Type").val(currJson.EventType);
				createFilterEditorIfNull();
				filterEditor.reset();
				filterEditor.setFilter(currJson.Filter);

				var eventTypeObj = getEventType( currJson.EventType );
				filterEditor.setType( eventTypeObj );

				validateForm();
			},

			setType: function( type)
			{
				typeData = type;
				var array=[];
				array.push( { text: "Select Type" , value:"Select Type ", disabled:true , selected:true});
				for (var i = 0; i < typeData.length; i++) 
				{
					var object = typeData[i];
					array.push( { text: object.Type , value: object.Type} );
				}
				$("#" + _name + "Type").replaceOptions(array);

			},
			setResultType( name)
			{
				resultName = name;
			},
			
			getJSON: function()
			{
				var filterDisplay = "";
				var json = {};
				json.Type = resultName;
				json.EventType = $('#'+ _name + 'Type').val();
				json.Filter=filterEditor.getFilter();
				
				for (var i = 0; i < json.Filter.length; i++) 
				{
					var object = json.Filter[i];
					if( object.DisplayText )
					{
						filterDisplay += object.DisplayText + " ";
					}
				}
				
				if( filterDisplay.length > 0)
				{
					json.DisplayText = resultName +" '" +json.EventType + "' WHERE " + filterDisplay;
				}else{
					json.DisplayText = resultName +" '" +json.EventType + "'";
				}
				json.CanEdit = true;
				return json;
			},
			
			display: function()
			{
				$('#' + _name + 'EventEditor').modal({
				  backdrop: 'static',
				  keyboard: true
				});
				$('#' + _name + 'EventEditor').css('z-index', 9998); 
				$('#' + _name + 'EventEditor').css('position', 'absolute');
				validateForm();
			},
			
			// Create a ModalEditor in the "divName" passing in the TypeDB to use
			create: function(name, divName)
			{
				_name = name;
				if( divName )
				{
					$('#' + divName).replaceWith(					
						'<div class="modal fade" id="' + _name + 'EventEditor" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">' +
							'<div class="modal-dialog panel-info">' +
								'<div class="modal-content">' +
									'<div class="modal-header">' +
										'<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
										'<h4 class="modal-title" id="myModalLabel"> Type and Attribute Editor</h4>' +
									'</div>' +
									'<div class="modal-body">' +
										'<div class="panel panel-info">' +
											'<div class="col-lg-12">' +
												'<form role="form">' +												
													'<div id="' + _name + 'TypeDiv" class="form-group  has-error" >' +
														'<label class="control-label" for="Type">Type:</label>' +
														'<select id="' + _name + 'Type" class="form-control">' +
															'<option value="" disabled selected>Select Type </option>' +
														'</select>' +
													'</div>' +
													
													'<div id="' + _name + 'bcFilterDiv" class="form-group ">' +
														'<label>Event Filter:</label>' +
														'<div id="' + _name + 'filterDiv"></div>' +
														'<p class="help-block">Press the + button to add a condition to filter to select the desired breadcrumbs</p>' +
													'</div>' +
													
													'<BR>' +

												'</form>' +
											'</DIV>' +
										'</DIV>' +
										'<div class="modal-footer">' +
											'<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>' +
											'<button type="button" class="btn btn-primary"  data-dismiss="modal" id="' + _name + 'SaveBtn">OK</button>' +
										'</div>' +
									'</div>' +
								'</div>' +
							'</div>' +
						'</div>' 
					);
				}
				
				data=[];
				
				$("#" + _name + "SaveBtn").on('click',
					function(event)
					{
						completeFunction();
					}
				);
				
				$('#'+ _name + 'Type').bind('change',
					function() 
					{
						var val = $('#'+ _name + 'Type').val();
						
						var typeData = getEventType( val );
						
						if( typeData && filterEditor )
						{
							filterEditor.reset();
							filterEditor.setType( typeData );
						}
						
						validateForm();
					}
				);
			
			}
		}			
		//
		// Private Methods for TypeAndFilterModalEditor
		//
		
		function createFilterEditorIfNull() {
			
			if( filterEditor == null) {
				filterEditor = new FilterEditor();
				filterEditor.create("eventList",  _name + 'filterDiv');
				filterEditor.bind('change',
				  function() 
				  {
					validateForm();
				  });
			}
		}
		
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
		
		function getEventType( type) 
		{
			for (var i = 0; i < typeData.length; i++) 
			{
				var object = typeData[i];
				if( object.Type == type)
				{
					return object;
				}
			}
			return null;
		}
		
		
		function createJSON(type, displayText, canEdit)
		{
				var json={};
				json.DisplayText=displayText
				json.Type=type
				json.CanEdit=canEdit;
				
				return json;
		}
		
		
		//
		// Validation Routines 
		//
		function validateForm()
		{
			var hasError = false;
			var index = $('#'+ _name + 'Type').prop("selectedIndex");
			if( index == 0)
			{
				hasError = true;
				$( "#" + _name + 'TypeDiv').addClass("has-error");
			}else{
				$( "#" + _name + 'TypeDiv').removeClass("has-error");
			}
			
			if( !filterEditor || !filterEditor.isValid() ) 
			{
				hasError = true;
			}
			
			if( hasError) 
			{
				$('#' + _name + 'SaveBtn').attr("disabled", true );
			}
			else
			{
				$('#' + _name + 'SaveBtn').attr("disabled", false );
			}
		}
		
		return oTypeAndFilterModalEditor;
	}
)();


