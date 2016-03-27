
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
				//FilterValidate();
				return !hasError;
			},
			
			reset : function()
			{
				$('#' + _name + ' li.ui-widget-content:not(:first)').remove();
				FilterValidate();
			},
			

			create: function(name, divName)
			{
				_name = name;
				if( divName )
				{
					_divId = divName
					$('#' + divName).html(
						'<TABLE id="' + _name + " class="table table-bordered" >' + 
							'<thead>' + 
								'<TR><TH style="width: 25%">Name</TH>' +
								(includeType?"<TH style="width: 25%">Type</TH>":"") +
								'<TH style="width: 50%">Description</TH> <TH style="width: 10%"></TH></TR>'+
							'</thead>'+
							'<tbody> </tbody> '+
						'</TABLE>'+
						'<BR>'+
						'<button class="AddAttrButton" >+</button>	'+
						'<button class="btn btn-default" id="autoPopulate" hidden >Auto Populate from existing Breadcrumbs</button>	'
					);
				}
				
				//
				// Events for FilterEditor
				//
				$('#'+_name).on('click','.AddAttrButton', 
					function(event)
					{

					}
				);
				
				$('#'+_name).on('click',"#autoPopulate",	
					function()
					{

					}
				);
			}
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

		
		
		function createAttribute()
		{
				var json={};
				json.Attributes=displayText
				json.Type=type
				json.CanEdit=canEdit;
				
				return json;
		}
	
		
		return oFilterEditor;
	}
)();


