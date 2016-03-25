//
// Handle Condition Editor Add Selections
//
var LOGIC = "Logic";
var NOT = "Not";
var OPEN = "(";
var CLOSE = ")";
var ATTRIBUTE = 'Attribute';

var FilterEditor =(
	function( ) 
	{
		//properties/fields
		var _name = "list";    // Name of this FilterEditor
		var data=[];
		var typeData = null;
		var ID=null;
		var attAddAfterElement;
		var attEditCurrElement;
		var hasError = false;
		var callbacks = {};
		
		function oFilterEditor() {}

		oFilterEditor.prototype = 
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
			// Send the type that has been choosen by the user.
			setType: function( type)
			{
				typeData = type;
			},
			
			isValid : function()
			{
				//FilterValidate();
				return !hasError;
			},
			
			reset : function()
			{
				$('#' + _name + ' li.ui-widget-content:not(:first)').remove();
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
				
			},
			create: function(name, divName)
			{
				_name = name;
				if( divName )
				{
					$('#' + divName).replaceWith(
					'<OL id="' + name + '" class="complexEditor has-error" >' +
						'<LI class="ui-widget-content ui-selected">&nbsp; ' +
							'<DIV class="pull-right button-handle btn-group">' +
								'<BUTTON id="plusButton" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">' +
									'<I class="fa fa-plus fa-lg"></I> ' +
								'</BUTTON>' + 
								'<UL class="dropdown-menu" role="menu">' +
								  '<LI><A class="addCONDITION" >Attribute Compare Condition</A></LI>' + 
								  '<LI role="presentation" class="divider"></LI>' + 
								  '<LI><A class="addAND" >AND Logic Operation </A></LI>' +
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
				$('#'+_name).on('click','.addAND', 
					function(event)
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(LOGIC, "AND", false);
						json.Operation="&&";
						addAttributeFilter($li1, json);
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
						//$li1.after( "<li class=\"ui-widget-content\"> OR" + getButtonGroup(false) + "</li>" );
					}
				);
				
				$('#'+_name).on('click',".addNOT",	
					function()
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(NOT, "NOT", false);
						addAttributeFilter($li1, json);
						FilterValidate();	
						//$li1.after( "<li class=\"ui-widget-content\"> NOT" + getButtonGroup(false) + "</li>" );
					}
				);
				
				$('#'+_name).on('click',".addNOT",	
					function()
					{
						var $li1 = $(this).parent().parent().parent().parent();
						var json=createJSON(NOT, "NOT", false);
						addAttributeFilter($li1, json);
						FilterValidate();	
						//$li1.after( "<li class=\"ui-widget-content\"> NOT" + getButtonGroup(false) + "</li>" );
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
						//$li1.after( "<li class=\"ui-widget-content\"> )" + getButtonGroup(false) + "</li>" );
						//$li1.after( "<li class=\"ui-widget-content\"> (" + getButtonGroup(false) + "</li>" );
					}
				);
				
				$('#'+_name).on('click',".editBTN",	
					function()
					{
						attEditCurrElement = $(this).parent().parent();
						
						// index will be used to replace the element in the json array at save time
						var jsonIndex = $(attEditCurrElement).attr("dataid");
						
						// if the element's index in the data array exists and is valid, continue with the edit
						if (jsonIndex && jsonIndex < data.length) {
						
							// send the current json configuration (data[i]) for loading the form
							AttributeLoadForEdit(typeData, data[jsonIndex], function( json)
								{
									// send the html element for replacing in the page and the index for updating the json in the array
									editAttributeFilter(attEditCurrElement, jsonIndex, json);
									FilterValidate();
								}
							);
							$('#AttributeCompareModal').modal({
							  backdrop: 'static',
							  keyboard: true
							});
							$('#AttributeCompareModal').css('z-index', 9999); 
							$('#AttributeCompareModal').css('position', 'absolute');
							//$li1.after( "<li class=\"ui-widget-content\"> )" + getButtonGroup(false) + "</li>" );
						} else {
							alert('Error: Invalid element index found in this attribute condition: ' + dataid);
						}
					}
				);

				$('#'+_name).on('click',".delBTN",	
					function()
					{
						$('#'+_name + ' li').each(	function() 
											{
												if( $(this).hasClass("ui-selected") )
												{
													$(this).remove();
												}
											} 
						);
						FilterValidate();
						//var $li1 = $(this).parent().parent();
						//$li1.remove();
					}
				);
				
				$('#'+_name).on('click',".addCONDITION",	
					function()
					{
						if (typeData) {
							attAddAfterElement = $(this).parent().parent().parent().parent();
							AttributeReset(typeData, function( json)
												{
													addAttributeFilter(attAddAfterElement, json);
													FilterValidate();
												}
							);
							$('#AttributeCompareModal').modal({
							  backdrop: 'static',
							  keyboard: true
							});
							$('#AttributeCompareModal').css('z-index', 9999); 
							$('#AttributeCompareModal').css('position', 'absolute');
							//$li1.after( "<li class=\"ui-widget-content\"> )" + getButtonGroup(false) + "</li>" );
						}
					}
				);
				
				// This makes the popup menu appear under the +.  It should be automatic, but for some reason it was not.
				// $('#'+_name).on('click',"#plusButton",
						// function() 
						// {
							// $('[data-toggle="dropdown"]').parent().addClass('open');
							//alert("click");
						// }
				// );
				
				$('#'+_name).selectable({
					cancel: '.sort-handle,button,.dropdown-menu'
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
		
		function loadAttributeFilter(afterElement, json) {
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
		
		function addAttributeFilter(afterElement, json)
		{
			loadAttributeFilter(afterElement, json);
		}

		function editAttributeFilter(atElement, jsonIndex, json)
		{
			var displayText=json.DisplayTest;

			$(atElement).replaceWith("<li class=\"ui-widget-content\" dataid='" + 
									 jsonIndex +
									 "' ><span class=\"displayText\">" + 
									 json.DisplayText +
									 "</span>" + 
									 getButtonGroup(json.CanEdit) + 
									 "</li>" 
			);
				
			// replace the json in the data array
			data[jsonIndex] = json;
		}
		
		// This gives the HTML for a lines button group ( visible when selected )
		function getButtonGroup(includeEdit)
		{
			var ret = "<div class=\"pull-right button-handle btn-group\">";
			if( includeEdit )
			{
				ret += "<button id=\"editButton\" type=\"button\" class=\"editBTN btn btn-default\"><i class=\"fa fa-pencil fa-lg\"></i> </button>"
			}
			ret += 	"<button id=\"plusButton\" type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\"><i class=\"fa fa-plus fa-lg\"></i> </button>"+
						"<ul class=\"dropdown-menu\" role=\"menu\">"+
							"<li><a class=\"addCONDITION\" >Attribute Condition</a></li>"+
							"<li role=\"presentation\" class=\"divider\"></li>"+
							"<li><a class=\"addAND\" >AND Logic Operation </a></li>"+
							"<li><a class=\"addOR\" >OR Logic Operation </a></li>"+
							//"<li><a class=\"addNOT\" >NOT Logic Operation </a></li>"+
							"<li role=\"presentation\" class=\"divider\"></li>"+
							"<li><a class=\"addPAR\"  >Parenthesis ( ) </a></li>"+
						"</ul>"+
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
						if( !prev || (prev.Type != ATTRIBUTE && prev.Type != CLOSE) )
						{
							FilterError( i, "  Syntax Error:'" + current.DisplayText + "unexpected:' :Logic needs an valid BEFORE filter." );
							return;
						}
						if( !next || (next.Type != ATTRIBUTE && next.Type != OPEN) )
						{
							FilterError( i, "  Syntax Error:'" + current.DisplayText + "unexpected:' :Logic needs an valid AFTER filter." );
							return;
						}
						break;
						
					case OPEN:
						
						if( prev && (prev.Type == ATTRIBUTE || prev.Type == CLOSE ) )
						{
							FilterError( i, " Syntax Error:'" + current.DisplayText + " unexpected: Invalid before expression " );
							return;
						}
						if( !next || (next.Type != ATTRIBUTE && next.Type != CLOSE && next.Type != OPEN ) )
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
					case ATTRIBUTE:
						if( prev && prev.Type == ATTRIBUTE )
							FilterError( i, "expected Logic statement here" );
						break;
				}
				prev = current;
			}
			
			if( hasError == false )
			{
					$("#SaveBtn").attr("disabled",  false );
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

		function FilterError( i , msg)
		{
			hasError = true;
			var line = $("#" + _name +' >  li.ui-widget-content >span.displayText').eq(i);
			if( line) 
			{
				var color = $(line).css('color');
				$(line).css('color', 'red'); //rgb(51, 51, 51)
				$(line).attr('title', msg);
			}
			//console.log( "Error in line " + i + ":" + msg);
			dispatchEvent('change');
		}
		
		return oFilterEditor;
	}
)();


