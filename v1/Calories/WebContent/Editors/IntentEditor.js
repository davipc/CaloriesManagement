//
// Handle Condition Editor Add Selections
//
var LOGIC = "Logic";
var NOT = "Not";
var OPEN = "(";
var CLOSE = ")";
var EVENT = 'Event';

var IntentEditor =(
	function( ) 
	{
		//properties/fields
		var _name = "list";    // Name of this FilterEditor
		var data=[];
		var typeData = null;
		var ID=null;
		var attAddAfterElement;
		var hasError = false;
		var validateHasRun = false;
		var callbacks = {};
		var eventEditor = null;
		
		
		function oIntentEditor() {}

		oIntentEditor.prototype = 
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
			setType: function( type)
			{
				typeData = type;
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
					'<OL id="' + name + '" class="complexEditor  has-error" >' +
						'<LI class="ui-widget-content ui-selected"> <span class="firstLine ui-selected"> &nbsp; </span> ' +
							'<DIV class="pull-right button-handle btn-group">' +
								'<BUTTON id="plusButton" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">' +
									'<I class="fa fa-plus fa-lg"></I> ' +
								'</BUTTON>' + 
								'<UL class="dropdown-menu" role="menu">' +
								  '<LI><A class="addEvent" >Event Condition</A></LI>' + 
								  '<LI role="presentation" class="divider"></LI>' + 
								  '<LI><A class="addAND_WITHIN" >Within X minutes of</A></LI>' +
								  '<LI><A class="addAND_AFTER" > Followed by (Within X minutes)</A></LI>' +
								  '<LI><A class="addOR" >OR Logic Operation </A></LI>' +
								  '<LI role="presentation" class="divider"></LI>' + 
								  '<LI><A class="addPAR"  >Parenthesis ( ) </A></LI>' +
								'</UL>'+
							'</DIV>' +
						'</LI>' + 
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
						
						eventEditor.setType( typeData );
						
						eventEditor.prepareForAdd(
								// ensure the OK button will be bound to the adding of this element ONLY
								function()
								{
										var json = eventEditor.getJSON();
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
							
							// index will be used to replace the element in the json array at save time
							var jsonIndex = $(attEditCurrElement).attr("dataid");
							
							// if the element's index in the data array exists and is valid, continue with the edit
							if (jsonIndex && jsonIndex < data.length) {
								
								if(!eventEditor ) {
									eventEditor = new TypeAndFilterModalEditor();
									eventEditor.create("Intent", "IntentEditorDiv");
								}
								
								eventEditor.setType( typeData );
								
								eventEditor.prepareForEdit(attEditCurrElement, data[jsonIndex], 
										// ensure the OK button will be bound to the editing of this element ONLY
										function()
										{
												var json = eventEditor.getJSON();
												editAttributeFilter(attEditCurrElement, jsonIndex, json);
												FilterValidate();
											
										}
								);
								
								eventEditor.display( );
							} else {
								alert('Error: Invalid element index found in this event condition: ' + dataid);
							}
						}
					);
				
				
				$('#'+_name).on('click','.addAND_WITHIN', 
					function(event)
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createComplexJSON(LOGIC, "AND_WITHIN", 30, "Hours", false);
						json.Operation="&&[]";
						addAttributeFilter($li1, json, true);
						FilterValidate();	
						//$li1.after( "<li class=\"ui-widget-content\"> AND" + getButtonGroup(false) + "</li>" );
					}
				);
	
				$('#'+_name).on('click','.addAND_AFTER', 
					function(event)
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createComplexJSON(LOGIC, "AND_AFTER", 30, "Hours", false);
						json.Operation="&&-";
						addAttributeFilter($li1, json, true);
						FilterValidate();	
						//$li1.after( "<li class=\"ui-widget-content\"> AND" + getButtonGroup(false) + "</li>" );
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
				

				
				$('#'+_name).on('click',".addNOT",	
					function()
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(NOT, "NOT", false);
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
				
				
				// This makes the popup menu appear under the +.  It should be automatic, but for some reason it was not.
				// $('#'+_name).on('click',"#plusButton",
						// function() 
						// {
							// $('[data-toggle="dropdown"]').parent().addClass('open');
//							alert("click");
						// }
				// );
				
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
				FilterValidate();
			}
		}			
		//
		// Private Methods for FilterEditor
		//
		function createJSON(type, displayText, canEdit)
		{
				var json={};
				json.DisplayText=displayText
				json.Type=type
				json.CanEdit=canEdit;
				
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

		function getPeriodSelect( type )
		{
			return  "<select  class='variablePeriodSEL variable' hidden >" + 
						"<option value='Minutes' " + (type=="Minutes"?"selected":"") + ">Minutes</option>" + 
						"<option value='Hours' " + (type=="Hours"?"selected":"") + ">Hours</option>" + 
						"<option value='Days' " + (type=="Days"?"selected":"") + ">Days</option>" + 
					" </select> ";
		}
		
		function addAttributeFilter(afterElement, json, edit)
		{
			loadAttributeFilter(afterElement, json, edit);
		}
		
		function loadAttributeFilter(afterElement, json, edit)
		{
			var displayText=json.DisplayText;

			if( json.Type == LOGIC && displayText == 'AND_WITHIN' )
			{
				var elem=$(afterElement).after( "<li class='ui-widget-content' dataid='" + 
											data.length+
											 "' ><span class='displayText'> Within " +
											 "<span class='variableLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" + json.Count +"</span>" +
											 "<input class='variableTBX variable' type='text' value='" + json.Count + "' hidden /> " +  
											 "<span class='variablePeriodLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.Period  +"</span>" +
											 getPeriodSelect( json.Period )+
											 "of (order unimportant) </span>" + 
											 getButtonGroup(json.CanEdit) + 
											 "</li>" 
										  );
				// var newLI = $(afterElement).next("li");
				// var next = $(newLI).children(":first");
				// var input = $(next).children()[1];
				// if( input) 
					// $(input).focus();
			}
			else if( json.Type == LOGIC && displayText == 'AND_AFTER' )
			{
				var elem=$(afterElement).after( "<li class='ui-widget-content' dataid='" + 
											data.length+
											 "' ><span class='displayText'> Followed by (within " +
											 "<span class='variableLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" + json.Count +"</span>" +
											 "<input class='variableTBX variable' type='text' value='" + json.Count +"' hidden/> "+ 
											 "<span class='variablePeriodLBL variable' style='color: #0000EE; font-size: 14px; font-weight: bold;'>" +  json.Period  +"</span>" +
											 getPeriodSelect( json.Period )+
											 " )</span>" + 
											 getButtonGroup(json.CanEdit) + 
											 "</li>" 
										  );
					
			}else{
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
		function getButtonGroup(includeEdit)
		{
			var ret = "<div class=\"pull-right button-handle btn-group\">";
			if( includeEdit )
			{
				ret += "<button id=\"editButton\" type=\"button\" class=\"btn btn-default editEvent\"><i class=\"fa fa-pencil fa-lg\"></i> </button>"
			}
			ret += 	"<button id=\"plusButton\" type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\"><i class=\"fa fa-plus fa-lg\"></i> </button>"+
						'<UL class="dropdown-menu" role="menu">' +
						  '<LI><A class="addEvent" >Event Condition</A></LI>' + 
						  '<LI role="presentation" class="divider"></LI>' + 
						  '<LI><A class="addAND_WITHIN" >Within X minutes of</A></LI>' +
						  '<LI><A class="addAND_AFTER" > Followed by (Within X minutes)</A></LI>' +
						  '<LI><A class="addOR" >OR Logic Operation </A></LI>' +
						  '<LI role="presentation" class="divider"></LI>' + 
						  '<LI><A class="addPAR"  >Parenthesis ( ) </A></LI>' +
						'</UL>'+
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
						if( !prev || (prev.Type != EVENT && prev.Type != CLOSE) )
						{
							FilterError( i, "  Syntax Error:'" + current.DisplayText + "unexpected:' :Logic needs an valid BEFORE filter." );
							return;
						}
						if( !next || (next.Type != EVENT && next.Type != OPEN) )
						{
							FilterError( i, "  Syntax Error:'" + current.DisplayText + "unexpected:' :Logic needs an valid AFTER filter." );
							return;
						}
						break;
						
					case OPEN:
						
						if( prev && (prev.Type == EVENT || prev.Type == CLOSE ) )
						{
							FilterError( i, " Syntax Error:'" + current.DisplayText + " unexpected: Invalid before expression " );
							return;
						}
						if( !next || (next.Type != EVENT && next.Type != CLOSE && next.Type != OPEN ) )
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
					case EVENT:
						if( prev && prev.Type == EVENT )
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
		
		return oIntentEditor;
	}
)();


