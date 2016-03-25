//
// Handle Condition Editor Add Selections
//
var LOGIC = "Logic";
var NOT = "Not";
var OPEN = "(";
var CLOSE = ")";

var BREADCRUMB = 'Breadcrumb';
var EVENT = 'Event';
var INTENT = 'Intent';
var FOLLOWEDBY = 'FollowedBy';
var NOTFOLLOWEDBY = 'NotFollowedBy';

var MetaSessionQueryEditor =(
	function( ) 
	{
		//properties/fields
		var _name = "list";    // Name of this FilterEditor
		var data=[];
		
		var breadcrumbTypeData = null;
		var eventTypeData = null;
		var intentTypeData = null;
		
		var ID=null;
		var attAddAfterElement;
		var hasError = false;
		var validateHasRun = false;
		var callbacks = {};
		var eventEditor = null;
		
		
		function oMetaSessionQueryEditor() 
		{
			$.fn.replaceOptions = function(options) {
				var self, $option;

				this.empty();
				self = this;

				$.each(options, function(index, option) {
				$option = $("<option></option>")
				.attr("value", option.value)
				.attr("disabled", option.disabled? true:false )
				.attr("selected", option.selected? true:false )
				.text(option.text);
				self.append($option);
				});
			};
			loadData();
		}

		oMetaSessionQueryEditor.prototype = 
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
				$('#' + _name +' li.ui-widget-content:not(:first)').remove();
				data=[];
				FilterValidate();
			},
			getFilter: function()
			{
				var Filter=[];
				$("#" + _name +" > .ui-widget-content" ).each(	
					function() 
					{
						var jsonIndex = $(this).attr("dataid");
						
						if( jsonIndex ) 
						{
							var json = data[jsonIndex];
							if( json.DisplayText == "AND_AFTER" || json.DisplayText == "AND_WITHIN")
							{
								// read values from screen
								var countSpan = $(this).find(".variableLBL");
								var periodSpan = $(this).find(".variablePeriodLBL");
								var count = countSpan.html();
								var period = periodSpan.html();
								if( count )
									json.Count = count;
								if( period)
									json.Period = period;
							}
							Filter.push( data[jsonIndex]);
						}
					} 
				);
				return Filter;
			},
			setFilter: function( Filter)
			{
				$.each(Filter, 
					function(i, item) 
					{
						var $li1 = $(".ui-widget-content" ).last();	
						loadAttributeFilter($li1, item);
					}
				);
				FilterValidate();
				
			},
			create: function(name, divName)
			{
				_name = name;
				if( divName )
				{
					$('#' + divName).replaceWith( 
						'<OL id="' + _name + '" class="complexEditor  has-error" >' +
							'<LI class="ui-widget-content ui-selected"> <span class="firstLine ui-selected"> &nbsp; </span> ' +
							'<DIV class="pull-right button-handle btn-group">' +
								getPlusMenu()+
							'</DIV></LI>' + 
						'</OL>'
					);
				}
				
				data=[];
				
				//
				// Events for FilterEditor
				//
				$('#'+_name).on('click',".addEvent",	
					function()
					{
						attAddAfterElement = $(this).parent().parent().parent().parent();
						
						if(eventEditor )
						{
							eventEditor.reset();
						} else {
							eventEditor = new TypeAndFilterModalEditor();
							eventEditor.create("Intent", "IntentEditorDiv");
						}
						eventEditor.setResultType( EVENT);
						eventEditor.setType( eventTypeData );
						
						eventEditor.prepareForAdd(
								// ensure the OK button will be bound to the adding of this element ONLY
								function()
								{
										var json = eventEditor.getJSON();
										json.Period = "contains";
										addAttributeFilter( attAddAfterElement, json);
										FilterValidate();
									
								}
						);
						eventEditor.display( );
					}
				);
				
				$('#'+_name).on('click',".editEvent",	
					function()
					{
						attEditCurrElement = $(this).parent().parent();
						
						var json = getJSONForRow( attEditCurrElement );
											
						// if the element's index in the data array exists and is valid, continue with the edit
						if ( json ) 
						{
							var jsonIndex = json.jsonIndex;
							var option = json.Period;
							
							if(!eventEditor ) {
								eventEditor = new TypeAndFilterModalEditor();
								eventEditor.create("Intent", "IntentEditorDiv");
							}
							eventEditor.setResultType( EVENT );
							eventEditor.setType( eventTypeData );
							
							eventEditor.prepareForEdit(attEditCurrElement, json, 
									// ensure the OK button will be bound to the editing of this element ONLY
									function()
									{
											var json = eventEditor.getJSON();
											json.jsonIndex = jsonIndex;
											json.Period = option;
											updateEventRow(attEditCurrElement, jsonIndex, json);
											FilterValidate();
										
									}
							);
							
							eventEditor.display( );
						} else {
							alert('Error: Invalid element index found in this event condition: ' + dataid);
						}
					}
				);
				
				$('#'+_name).on('click',".addBreadcrumb",	
					function()
					{
						attAddAfterElement = $(this).parent().parent().parent().parent();
						
						if(eventEditor )
						{
							eventEditor.reset();
						} else {
							eventEditor = new TypeAndFilterModalEditor();
							eventEditor.create("Breadcrumb", "IntentEditorDiv");
						}
						eventEditor.setResultType( BREADCRUMB );
						eventEditor.setType( breadcrumbTypeData );
						
						eventEditor.prepareForAdd(
								// ensure the OK button will be bound to the adding of this element ONLY
								function()
								{
										var json = eventEditor.getJSON();
										json.Period = "contains";
										json.Type = BREADCRUMB;
										addAttributeFilter( attAddAfterElement, json);
										FilterValidate();
								}
						);
						eventEditor.display( );
					}
				);
				
				$('#'+_name).on('click',".editBreadcrumb",	
					function()
					{
						attEditCurrElement = $(this).parent().parent();
						
						var json = getJSONForRow( attEditCurrElement );
											
						// if the element's index in the data array exists and is valid, continue with the edit
						if ( json ) 
						{
							var jsonIndex = json.jsonIndex;
							var option = json.Period;
							
							if(!eventEditor ) {
								eventEditor = new TypeAndFilterModalEditor();
								eventEditor.create("Intent", "IntentEditorDiv");
							}
							eventEditor.setResultType( BREADCRUMB );
							eventEditor.setType( breadcrumbTypeData );
							
							eventEditor.prepareForEdit(attEditCurrElement, json, 
									// ensure the OK button will be bound to the editing of this element ONLY
									function()
									{
											var json = eventEditor.getJSON();
											json.jsonIndex = jsonIndex;
											json.Period = option;
											updateBreadcrumbRow(attEditCurrElement, jsonIndex, json);
											FilterValidate();
										
									}
							);
							
							eventEditor.display( );
						} else {
							alert('Error: Invalid element index found in this event condition: ' + dataid);
						}
					}
				);
				
				//$('#'+_name).on('click',".addBreadcrumb",	
				//	function()
				//	{
				//			var $li1 = $(this).parent().parent().parent().parent();
				//			var json=createBreadcrumbJSON();
				//			json.Operation="&&[]";
				//			addAttributeFilter($li1, json, true);
				//			FilterValidate();							
				//	}
				//);
				
				$('#'+_name).on('click','.addIntent', 
					function(event)
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createIntentJSON();
						if( json)
						{
							json.Operation="&&[";
							addAttributeFilter($li1, json, true);
							FilterValidate();
						}
					}
				);
				
				$('#'+_name).on('click','.addAND_FOLLOWEDBY', 
					function(event)
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(FOLLOWEDBY, "FOLLOWED BY");
						json.Operation="&&[";
						addAttributeFilter($li1, json, true);
						FilterValidate();	
					}
				);
				$('#'+_name).on('click','.addAND_NOTFOLLOWEDBY', 
					function(event)
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(NOTFOLLOWEDBY, " NOT FOLLOWED BY");
						json.Operation="!&&[";
						addAttributeFilter($li1, json, true);
						FilterValidate();	
					}
				);
				
				$('#'+_name).on('click',".addOR",	
					function()
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(LOGIC, "OR", false);
						json.Operation="||";
						addAttributeFilter($li1, json);	
						FilterValidate();			
					}
				);
				$('#'+_name).on('click',".addAND",	
					function()
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(LOGIC, "AND", false);
						json.Operation="&&";
						addAttributeFilter($li1, json);	
						FilterValidate();			
					}
				);

				$('#'+_name).on('click',".addPAR",	
					function()
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(CLOSE, ")", false);
						addAttributeFilter($li1, json);
						var json=createJSON(OPEN, "(", false);
						addAttributeFilter($li1, json);
						FilterValidate();
					}
				);
				
				

				
				$('#'+_name).on('click',".delBTN",	
					function()
					{
						$('#'+_name+" li").each(	function() 
											{
												if( $(this).hasClass("ui-selected") )
												{
													$(this).remove();
												}
											} 
						);
						FilterValidate();
					}
				);
				
				$('#'+_name).selectable({
					cancel: '.sort-handle,button,.dropdown-menu,.variable',
					selected: 
						function(event, ui) 
						{ 
								$("*").blur(); 
						} 
				}).sortable({
					items: "> li[dataid]",
					handle: '.sort-handle',
					helper: function(e, item) 
							{
								if ( ! item.hasClass('ui-selected') ) 
								{
									item.parent().children('.ui-selected').removeClass('ui-selected');
									item.addClass('ui-selected');
								}
			 
								var selected = item.parent().children('.ui-selected').clone();
								item.data('multidrag', selected).siblings('.ui-selected').remove();
								return $('<li/>').append(selected);
							},
					stop: 	function(e, ui) 
							{
								var selected = ui.item.data('multidrag');
								ui.item.after(selected);
								ui.item.remove();
								
								FilterValidate();
							}
				});
				
				$('#'+_name).on('blur','.variableTBX',
					function () 
					{
						var lbl = $(this).prev();
						if ($(this).val().trim() != '') 
						{
							$(this).hide();
							var val = $(this).val();
							var num = parseInt(val);
							if( num )
								lbl.html(  num  );
							else
								lbl.html(  30  );
							$(this).val('');
						}
					}
				);
	 
				$('#'+_name).on('click','.variableLBL',
					function () 
					{
						var tbx = $(this).next();
						if ($(this).html().trim() != '') {
							$(tbx).show().focus();
							$(tbx).val($(this).html()); 
							$(this).html('');
						}
					}
				);
				$('#'+_name).on('blur','.variablePeriodSEL',
					function () 
					{
						var lbl = $(this).prev();
						if ($(this).val().trim() != '') 
						{
							$(this).hide();
							var val = $(this).val();
							lbl.html(  val  );
							//$(this).val('');
						}
					}
				);
	 
				$('#'+_name).on('click','.variablePeriodLBL',
					function () 
					{
						var tbx = $(this).next();
						if ($(this).html().trim() != '') {
							$(tbx).show().focus();
							$(tbx).val($(this).html()); 
							$(this).html('');
						}
					}
				);
				
				$('#'+_name).on('blur','.variableTypeSEL',
					function () 
					{
						var lbl = $(this).prev();
						if ($(this).val().trim() != '') 
						{
							$(this).hide();
							var val = $(this).val();
							lbl.html(  val  );
							//$(this).val('');
						}
					}
				);
	 
				$('#'+_name).on('click','.variableTypeLBL',
					function () 
					{
						var tbx = $(this).next();
						if ($(this).html().trim() != '') {
							$(tbx).show().focus();
							$(tbx).val($(this).html()); 
							$(this).html('');
						}
					}
				);
				$('#'+_name).on('blur','.variableChannelSEL',
					function () 
					{
						var lbl = $(this).prev();
						if ($(this).val().trim() != '') 
						{
							$(this).hide();
							var val = $(this).val();
							lbl.html(  val  );
							//$(this).val('');
						}
					}
				);
	 
				$('#'+_name).on('click','.variableChannelLBL',
					function () 
					{
						var tbx = $(this).next();
						if ($(this).html().trim() != '') {
							$(tbx).show().focus();
							$(tbx).val($(this).html()); 
							$(this).html('');
						}
					}
				);
				$('#'+_name).on('blur','.variableOptionSEL',
					function () 
					{
						var lbl = $(this).prev();
						if ($(this).val().trim() != '') 
						{
							$(this).hide();
							var val = $(this).val();
							lbl.html(  val  );
							//$(this).val('');
						}
					}
				);
	 
				$('#'+_name).on('click','.variableOptionLBL',
					function () 
					{
						var tbx = $(this).next();
						if ($(this).html().trim() != '') {
							$(tbx).show().focus();
							$(tbx).val($(this).html()); 
							$(this).html('');
						}
					}
				);
				FilterValidate();
			}
		}			
		//
		// Private Methods for FilterEditor
		//
		function getJSONForRow( row )
		{
			//var row = $("#" + _name +" > .ui-widget-content" ).eq(rowNum);	
			
			var jsonIndex = $(row).attr("dataid");
	
			if( jsonIndex && jsonIndex < data.length) 
			{
				var json = data[jsonIndex];
				json.jsonIndex = jsonIndex;
				if( json.Type == EVENT )
				{
					// read values from screen
					var optionSpan = $(row).find(".variableOptionLBL");
					var option = optionSpan.html();
					json.Period = option;
				}else if( json.Type == BREADCRUMB )
				{
					// read values from screen
					var optionSpan = $(row).find(".variableOptionLBL");
					var option = optionSpan.html();
					json.Period = option;
				}
				return json;	
			}
		}
		function loadData()
		{
			$.ajax({ 
				type: "GET",
				url: 'query/getEventTypes.jsp', 
				dataType: 'json',

				success: function(data, textStatus, jqXHR)
				{
					eventTypeData = data;
				},
				error: function (jqXHR, textStatus, errorThrown)
				{
					alert("getEventType failed.:" + errorThrown + " " + textStatus); 
				}
			});
			$.ajax({ 
				type: "GET",
				url: 'query/getBreadcrumbTypes.jsp', 
				dataType: 'json',

				success: function(data, textStatus, jqXHR)
				{
					breadcrumbTypeData = data;
				},
				
				error: function (jqXHR, textStatus, errorThrown)
				{
					alert("getBreadcrumbTypes failed.:" + errorThrown + " " + textStatus); 
				}
			});
			$.ajax({ 
				type: "GET",
				url: 'query/getAllIntents.jsp', 
				dataType: 'json',

				success: function(data, textStatus, jqXHR)
				{
					intentTypeData = data;
				},
				error: function (jqXHR, textStatus, errorThrown)
				{
					alert("getAllIntents failed.:" + errorThrown + " " + textStatus); 
				}
			});
		}
		
		function getPlusMenu()
		{
					return 	'<BUTTON id="plusButton" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">' +
									'<I class="fa fa-plus fa-lg"></I> ' +
							'</BUTTON>' + 
							'<UL class="dropdown-menu" role="menu">' +
							  '<LI><A class="addBreadcrumb" >Breadcrumb</A></LI>' + 
							  '<LI><A class="addEvent" >Event</A></LI>' + 
							  (getIntentCount() > 0? '<LI><A class="addIntent" >Intent</A></LI>':'') + 
							  '<LI><A class="addReportingCat" >Reporting Catagory</A></LI>' + 
							  '<LI role="presentation" class="divider"></LI>' + 
							  //'<LI><A class="addAND_FOLLOWEDBY" >Followed immediately by</A></LI>' +
							  //'<LI><A class="addAND_NOTFOLLOWEDBY" >not followed immediately by</A></LI>' +
							  '<LI><A class="addOR" >OR Logic Operation </A></LI>' +
							  '<LI><A class="addAND" >AND Logic Operation </A></LI>' +
							  '<LI role="presentation" class="divider"></LI>' + 
							  '<LI><A class="addPAR"  >Parenthesis ( ) </A></LI>' +
							'</UL>';
		}

		function createIntentJSON()
		{
				var json={};
				json.DisplayText=""
				json.Type=INTENT
				json.CanEdit=false;
				var ary = getIntentArray();
				if( ary && ary.length > 0)
				{
					json.IntentType = ary[0];
				}
				else{
					alert("There are no Intent Definitions.")
					return null;
				}
				json.Period = "contains";
				return json;
		}
		function createJSON(type, displayText, canEdit)
		{
				var json={};
				json.DisplayText=displayText
				json.Type=type
				json.CanEdit=canEdit;
				return json;
		}
		
		function createBreadcrumbJSON ()
		{
				var json={};
				json.DisplayText=""
				json.Type=BREADCRUMB
				json.CanEdit=false;
				json.BCType = "LogOn";
				json.Period = "contains";
				json.Channel= "Any";
				return json;
		}
		
		function createComplexJSON(type, displayText, count, period, canEdit)
		{
				var json={};
				json.DisplayText=displayText
				json.Type=type
				json.CanEdit=canEdit;
				json.Count = count;
				json.Period = period;
				return json;
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
		
		function makeSelect(name, list, selectedName)
		{
			var v = "<select  class='" + name + " variable' hidden >"
			var arrayLength = list.length;
			for (var i = 0; i < arrayLength; i++) 
			{
				v += "<option value='" + list[i] +"' " + (selectedName==list[i]?"selected":"") + ">" + list[i] +"</option>";
			}
			v += " </select> ";
			
			return v;
		}

		function getPeriodSelect( type )
		{
			var myStringArray = ["starts with","does not start with", "ends with", "does not ends with", "contains", "does not contain", "followed immediately by", "not followed immediately by"];
			return makeSelect( "variablePeriodSEL", myStringArray, type);

		}
		
		function getOptionSelect( type )
		{
			var myStringArray = [ "contains", "does not contain"];
			return makeSelect( "variableOptionSEL", myStringArray, type);

		}
		function getChannelSelect( type )
		{
			var myStringArray = [ "Any","IVR", "WEB", "MOBILE" ];
			return makeSelect( "variableChannelSEL", myStringArray, type);
		}
		function getIntentCount()
		{
			var v = getIntentArray();
			if( v)
				return v.length;
			return 1;
		}
		
		var intentNameArray;
		function getIntentArray()
		{
			if( !intentNameArray && intentTypeData )
			{
				intentNameArray=[];
				$.each( intentTypeData, function(i, item) {
					intentNameArray.push(item.Name);
				});
			}
			return intentNameArray
		}
		function getIntentTypeSelect( type )
		{
			var aray = getIntentArray()
			return makeSelect( "variableTypeSEL", aray, type);
//			return  "<select  class='variableTypeSEL variable' hidden >" + 
//						"<option value='LogOn' " + (type=="LogOn"?"selected":"") + ">LogOn</option>" + 
//						"<option value='ViewStatement' " + (type=="ViewStatement"?"selected":"") + ">ViewStatement</option>" + 
//					" </select> ";
		}
		
		function addAttributeFilter(afterElement, json, edit)
		{
			loadAttributeFilter(afterElement, json, edit);
		}
		
		function loadAttributeFilter(afterElement, json, edit)
		{
			var displayText=json.DisplayText;

			if( json.Type == INTENT )
			{
				var elem=$(afterElement).after( "<li class='ui-widget-content' dataid='" + 
											data.length+
											 "' ><span class='displayText'>" +
											 "<span class='variableOptionLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.Period  +"</span>" +
											 getOptionSelect( json.Period )+
											  " Intent of Type <span class='variableTypeLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.IntentType  +"</span>" +
											 getIntentTypeSelect( json.IntentType )+
											 "</span>" + 
											 getButtonGroup(json.CanEdit) + 
											 "</li>" 
										  );
			}
			else if( json.Type == BREADCRUMB )
			{
				var elem=$(afterElement).after( getBreadcrumbRow( json, data.length) );
			}
			else if( json.Type == EVENT )
			{
				var elem=$(afterElement).after( getEventRow( json, data.length) );
			}
			else if( json.Type == FOLLOWEDBY )
			{
				var elem=$(afterElement).after( "<li class='ui-widget-content' dataid='" + 
											data.length+
											 "' ><span class='displayText'> followed by " +
											 " Breadcrumb of Type <span class='variableTypeLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.BCType  +"</span>" +
											 getBreadcrumbTypeSelect( json.BCType )+
											 "</span>" + 
											 getButtonGroup(json.CanEdit) + 
											 "</li>" 
										  );
			}
			else if( json.Type == NOTFOLLOWEDBY )
			{
				var elem=$(afterElement).after( "<li class='ui-widget-content' dataid='" + 
											data.length+
											 "' ><span class='displayText'> Not followed by " +
											 " Breadcrumb of Type <span class='variableTypeLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.BCType  +"</span>" +
											 getBreadcrumbTypeSelect( json.BCType )+
											 "</span>" + 
											 getButtonGroup(json.CanEdit) + 
											 "</li>" 
										  );
					
			}
			
			else
			{
				var elem=$(afterElement).after( "<li class=\"ui-widget-content\" dataid='" + 
											data.length+
											 "' ><span class=\"displayText\">"+ 
											 json.DisplayText +
											 "</span>" + 
											 getButtonGroup(json.CanEdit) + 
											 "</li>" 
										  );
			}
			data.push(json);
		}
		
		function getEventRow( json, jsonIndex)
		{
			return "<li class=\"ui-widget-content\" dataid='" + jsonIndex + "' ><span class=\"displayText\">"+ 
											 "<span class='variableOptionLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.Period  +"</span>" +
											 getOptionSelect( json.Period ) +
											 json.DisplayText +
											 "</span>" + 
											 getButtonGroup(json.CanEdit, "editEvent") + 
											 "</li>" 
		}
		
		function getBreadcrumbRow( json, jsonIndex)
		{
			return "<li class=\"ui-widget-content\" dataid='" + jsonIndex + "' ><span class=\"displayText\">"+ 
											 "<span class='variableOptionLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.Period  +"</span>" +
											 getPeriodSelect( json.Period ) +
											 json.DisplayText +
											 "</span>" + 
											 getButtonGroup(json.CanEdit, "editBreadcrumb") + 
											 "</li>" 
		}
		
		
		function updateEventRow(atElement, jsonIndex, json)
		{
			$(atElement).replaceWith( getEventRow(json, jsonIndex) );
			// replace the json in the data array
			data[jsonIndex] = json;
		}
		
		function updateBreadcrumbRow(atElement, jsonIndex, json)
		{
			$(atElement).replaceWith( getBreadcrumbRow(json, jsonIndex) );
			// replace the json in the data array
			data[jsonIndex] = json;
		}
		
		function editAttributeFilter(atElement, jsonIndex, json)
		{
			$(atElement).replaceWith("<li class=\"ui-widget-content\" dataid='" + 
									 jsonIndex +
									 "' ><span class=\"displayText\">"+ 
									 json.DisplayText +
									 "</span>" + 
									 getButtonGroup(json.CanEdit) + 
									 "</li>" 
									);
			
			// replace the json in the data array
			data[jsonIndex] = json;
		}
				
		function addEventFilter(afterElement, json)
		{
			var displayText=json.DisplayTest;
			var elem=$(afterElement).after( "<li class=\"ui-widget-content\" dataid='" + 
											data.length+
											 "' ><span class=\"displayText\">"+ 
											 json.DisplayText +
											 "</span>" + 
											 getButtonGroup(json.CanEdit) + 
											 "</li>"  
										  );
			data.push(json);
		}
		

											
											
		// This gives the HTML for a lines button group ( visible when selected )
		function getButtonGroup(includeEdit, editButtonName)
		{
			var ret = "<div class=\"pull-right button-handle btn-group\">";
			if( includeEdit )
			{
				ret += "<button type=\"button\" class=\"btn btn-default " + editButtonName + "\"><i class=\"fa fa-pencil fa-lg\"></i> </button>"
			}
			ret += 	getPlusMenu() +
					"<button type=\"button\" class=\"delBTN btn btn-default\"><i class=\"fa fa-trash-o fa-lg\"></i> </button> <i class=\"fa fa-arrows-v sort-handle\"></i> </DIV>"; 
			return ret;
		}
		
		function isListOperator( value )
		{
			if( value == "In List" || value == "Not In List" )
				return true;
			
			return false;
		}

		//
		// Validation Routines 
		//
		function FilterValidate()
		{
			hasError = false;

			var array=[];
			if( _name )
			{
				$("#" + _name +" > .ui-widget-content" ).each(	
					function() 
					{
						var jsonIndex = $(this).attr("dataid");
						
						var json=null;
						if( jsonIndex ) 
						{
							json = data[jsonIndex];
						}
						if( json && json.Type)
						{
							array.push( json);
							$(this).children('span').css('color', 'rgb(51, 51, 51)'); 
							$(this).attr('title', "");
						}
					} 
				);
			}
			
			if( array.length == 0 )
			{
				hasError = true;
				EmptyListError();
			}else{
				ClearFirstLineError();
			}
			
			// take list and reduce to single element or log errors
			var prev=null;
			var current={};
			var next=null;
			
			for( i=0;i<array.length;i++)
			{
				current = array[i];
				if( i+1 < array.length)
					next = array[i+1];
				else
					next = null;
				switch( current.Type)
				{
					case LOGIC:
						if( !prev || (prev.Type != BREADCRUMB && prev.Type != CLOSE && prev.Type != INTENT && prev.Type != EVENT ) )
						{
							FilterError( i, "  Syntax Error:'" + current.DisplayText + " unexpected:' :Logic needs an valid BEFORE filter." );
							return;
						}
						if( !next || (next.Type != BREADCRUMB && next.Type != OPEN && next.Type != INTENT && next.Type != EVENT ) )
						{
							FilterError( i, "  Syntax Error:'" + current.DisplayText + " unexpected:' :Logic needs an valid AFTER filter." );
							return;
						}
						break;
					case NOTFOLLOWEDBY:	
					case FOLLOWEDBY:
						if( !prev || (prev.Type != BREADCRUMB && prev.Type != CLOSE && prev.Type != FOLLOWEDBY ) )
						{
							FilterError( i, "  Syntax Error:'" + current.DisplayText + " unexpected:'" );
							return;
						}
						
						break;
		
					case OPEN:
						if( prev && (prev.Type == BREADCRUMB || prev.Type == CLOSE ) )
						{
							FilterError( i, " Syntax Error:'" + current.DisplayText + " unexpected: Invalid before expression " );
							return;
						}
						if( !next || (next.Type != BREADCRUMB && next.Type != CLOSE && next.Type != OPEN ) )
						{
							FilterError( i, " Syntax Error:'" + current.DisplayText + " unexpected : Invalid after expression" );
							return;
						}
						
						
						// has matching close statement
						if( !hasMatchingClose(array, i) )
						{
							FilterError( i, " Syntax Error:'" + current.DisplayText + " unexpected : No closing ')' found." );
							return;
						}
						break;
						
					case CLOSE:
						if( !prev || prev.Type == OPEN|| prev.Type == LOGIC )
						{
							FilterError( i, " Syntax Error;  Invalid expression: ( hint Before)" );
							return;
						}
						
						// has matching open statement
						if( !hasMatchingOpen(array, i) )
						{
							FilterError( i, " Syntax Error:'" + current.DisplayText + " unexpected : No opening ')' found." );
							return;
						}
						break;
						
					case BREADCRUMB:
						if( prev && (prev.Type == BREADCRUMB || prev.Type == FOLLOWEDBY || prev.Type == NOTFOLLOWEDBY ) )
							FilterError( i, "expected Logic statement here" );
						break;

					case EVENT:
						if( prev && prev.Type != LOGIC )
							FilterError( i, "expected Logic statement here" );
						break;
					case INTENT:
						if( prev && prev.Type != LOGIC )
							FilterError( i, "expected Logic statement here" );
						break;
				}
				prev = current;
			}
			
			dispatchEvent('change');
		}

		function hasMatchingClose(array, index)
		{
			for( j=index;j<array.length;j++)
			{
				if( array[j].Type == CLOSE) 
					return true;
			}
			return false;
		}

		function hasMatchingOpen(array, index)
		{
			for( j=index;j>=0;j--)
			{
				if( array[j].Type == OPEN) 
					return true;
			}
			return false;
		}
		
		function EmptyListError()
		{
			var span = $("#" + _name +' >  li.ui-widget-content >span.firstLine').eq(0);
			if( span )
			{
				span.text("You must enter at least one condition ");
				var color = $(span).css('color');
				$(span).css('color', 'red'); //rgb(51, 51, 51)
				$(span).attr('title', "You must enter at least one condition ");
			}
		}
		function ClearFirstLineError()
		{
			var span = $("#" + _name +' >  li.ui-widget-content >span.firstLine').eq(0);
			if( span )
			{
				span.html("&nbsp;");
				$(span).css('color', 'rgb(51, 51, 51)'); 
				$(span).attr('title', "");
			}
		}
		
		function FilterError( i , msg)
		{
			hasError = true;
			var line = $("#" + _name +' >  li.ui-widget-content >span.displayText').eq(i);
			if( line) 
			{
				var color = $(line).css('color');
				$(line).css('color', 'red'); //rgb(51, 51, 51)
				
				$(line).parent().attr('title', msg);
			}
			//console.log( "Error in line " + i + ":" + msg);
			dispatchEvent('change');
		}
		
		return oMetaSessionQueryEditor;
	}
)();


