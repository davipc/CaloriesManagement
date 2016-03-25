var BreadcrumbEventEditor =(
	function( ) 
	{
		var callbacks = {};
		var hasError = false;
		//properties/fields
		var typeListDB = null;
		var id = null;
		var currentFilter = null;
		var filterEditor = null;
		var filterToEdit = null;
		
		function oBreadcrumbEventEditor() {}

		oBreadcrumbEventEditor.prototype = 
		{
			create: function(divName, filter)
			{
				filterToEdit = filter;
				if( divName )
				{
					$('#' + divName).replaceWith(
						'<div class="panel panel-info" > ' +
							'<div class="panel-heading"> Conditions for creating Event </div>' +
							'<div class="panel-body">' +
								'<div id="SingleBreadcrumbDiv" class="form-group" hidden>' +
									'<div id="BCTypeDiv" class="form-group  has-error">' +
										'<label class="control-label" for="Type">When a breadcrumb of type:</label>'+
										'<select id="bcType" class="form-control">'+
											'<option value="" disabled selected>Select a Breadcrumb type</option>' +
										'</select>'+
									'</div>'+
									'<div id="FilterDiv" class="form-group" >' +
										'<label>Breadcrumb Filter:</label>' +
										'<div id="FilterContainerDiv" class="form-group">' +
										
										'</div>' +
										'<p class="help-block">Press the + button to add a condition to filter to select the desired breadcrumbs</p>' +
									'</div>' +
								'</div>' +
								'<p><p>' +
							'</div>' +
						'</div>' +
									
						'<div id="EventDiv" class="panel panel-info" hidden >' +
							'<div class="panel-heading"> Event To Create </div>' +
							'<div class="panel-body">' +
								'<div  class="form-group" >' +
									'<div id="EventNameDiv" class="form-group  has-error" >' +
										'<Label>New Event Name</Label> ' +
										'<div id="eventNameDiv"><input type="text" id="eventName" class="form-control" ></div>' +
										'<br>' +
										
									'</div>' +
									'<div class="panel panel-info" id="FieldsDiv"  >' +
										'<div class="panel-heading"> Select which Attributes from the original breadcrumb you`d like to include in the Event' +    
											'<div class="pull-right" > <input id="SelectAllLink" type="checkbox">Select  All</div></div>' +
										'<div id="FieldsToSelect" class="panel-body"> </div>' +
									'</div>' +
								'</div>' +
							'</div>' +
						'</div> '
					);
				}
				
				//
				// Events for FilterEditor
				//
				var savedTypeIndex = 0;
				$("#bcType").bind("click", 
					function(e) 
					{
						savedTypeIndex = $("#Type").prop("selectedIndex");
					}
				);

				$("#bcType").bind('change',
					function()
					{
						var filterSize = $('#list li.ui-widget-content').length;
						if( filterSize > 1) 
						{
							var result = confirm("Changing the type will remove your filter below.  Are you sure you want to proceed?");

							if( result ) 
							{
								$('#list li.ui-widget-content:not(:first)').remove();
							}else{
								$("#Type").prop("selectedIndex",savedTypeIndex );
							}	
						}
						
						var val = $('#bcType').val();
						
						var typeData = getBranchType( val );
						
						updateBCType( typeData);
						
						$("#EventDiv").show();
						
						validateForm();
					}
				);
				
				$("#eventName").bind('input',
					function()
					{
						validateForm();
					}
				);

				$("#SelectAllLink").click( 
					function()
					{
						var checked = $("#SelectAllLink").is(':checked');
						$('#FieldsToSelect').find(':checkbox').each(
							function()
							{
								$(this).prop('checked', checked );
							}
						);
					}
				);
				
				$.ajax({ 
					type: "GET",
					url: 'query/getBreadcrumbTypes.jsp', 
					dataType: 'json',

					success: function(data, textStatus, jqXHR)
					{
						typeListDB = data;

						setBreadcrumbTypes(  );
						
						$("#SingleBreadcrumbDiv").show();
						
						if( filterEditor == null)
						{
							filterEditor = new FilterEditor();
							filterEditor.create("list", "FilterContainerDiv");
							filterEditor.bind('change',
							  function() 
							  {
								validateForm();
							  });
						}else{
							filterEditor.reset();
						}
						
						
						
						if( filterToEdit )
						{
							filterEditor.setFilter( filterToEdit.Filter );
							
							setBreadcrumbTypes( filterToEdit.BreadcrumbType );
							
							var typeData = getBranchType( filterToEdit.BreadcrumbType );

							updateBCType( typeData );
							$("#EventDiv").show();
							$("#eventName").val(filterToEdit.EventName);
						}
						else
						{
							$("#idDiv").hide();		
							$("#EventDiv").hide();
							$("#eventId").text("");
							$("#FieldsToSelect").empty();
							$("#eventName").val("");
							
						}
						
						
						
						
						
						validateForm();
					},
					error: function (jqXHR, textStatus, errorThrown)
					{
						alert("Filter lookup failed.:" + errorThrown + " " + textStatus); 
					}
				});
			},
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
			getEventJson: function()
			{
				var filterName = $("#eventName").val();
				
				json = {};
				json.EventName= filterName;
				json.Attributes=[];
				json.Active = false;
				json.FilterType="SingleBreadcrumb";
				json.Filter=[];
				
				if( filterEditor )
				{	
					json.Filter=filterEditor.getFilter();
				}
				
				json.BreadcrumbType = $('#bcType').val();
				
				$('#FieldsToSelect').find(':checkbox').each(
					function()
					{
						if( $(this).is(':checked') )
						{
							var propName = $(this).attr("propName");
							var propType = $(this).attr("propType");
							
							if( propName && propType ) 
							{
								var attrib = {};
								attrib.AttribType = propType;
								attrib.Name = propName;
								
								json.Attributes.push( attrib );
							}

						}
					}
				);
				
				return json;

			}
		}
		
		function validateForm()
		{
			hasError = false;
			var index = $("#bcType").prop("selectedIndex");
			if( index == 0)
			{
				$("#BCTypeDiv").addClass("has-error");
				hasError = true;
			}else{
				$("#BCTypeDiv").removeClass("has-error");
			}

			//Check filter is valid
			if( filterEditor && !filterEditor.isValid() )
			{	
				hasError = true;
			}
			
			//Check Event Name
			var evntName = $("#eventName").val();
			if( evntName.length == 0)
			{
				$("#EventNameDiv").addClass("has-error");
				hasError = true;
			}
			else
			{
				$("#EventNameDiv").removeClass("has-error");
			}
			dispatchEvent('change');
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

		function updateBCType( typeData)
		{
			if( filterEditor )
			{
				filterEditor.setType( typeData );
			
				$("#FieldsToSelect").empty();
				
				for (var propertyName in typeData.Attributes) 
				{
					 var prop = typeData.Attributes[propertyName];
					 
					 var checkbox = '<div class="checkbox"> <label>	<input propName="' + propertyName + '" propType="Attribute" type="checkbox" ' + (shouldCheck(propertyName)?"checked":"") + '>' + propertyName + '	</label> </div>';
					 
					 $("#FieldsToSelect").append(checkbox);
				}
			}
		};
		
		function shouldCheck( propName)
		{
			if( filterToEdit )
			{
				var attr = filterToEdit.Attributes;
				
				for(var i in attr)
				{
					if( attr[i].Name == propName ) 
						return true;
				}
			}
			return false;
		};


		function getBranchType( type) 
		{
			for (var i = 0; i < typeListDB.length; i++) 
			{
				var object = typeListDB[i];
				if( object.Type == type)
				{
					return object;
				}
			}
			return null;
		};

		function setBreadcrumbTypes( selectedItem )
		{
			var array=[];
			array.push( { text:"Select which Breadcrumb type to filter out", value:"Select which Breadcrumb type to filter out", disabled:true });
			for (var i = 0; i < typeListDB.length; i++) 
			{
				var object = typeListDB[i];
				if( selectedItem && object.Type == selectedItem )
				{
					array.push( { text: object.Type , value: object.Type, selected:true} );
				}else{
					array.push( { text: object.Type , value: object.Type} );
				}
			}
			$("#bcType").replaceOptions(array);
			
			if( ! selectedItem )
				$("#bcType").prop("selectedIndex", 0);
						
		}
		return oBreadcrumbEventEditor;
	}
)();

