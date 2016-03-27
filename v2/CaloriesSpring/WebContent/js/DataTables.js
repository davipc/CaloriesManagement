

function hideColumns(tableId, list)
{
	var dataTable = $("#" + tableId ).DataTable();
	jQuery.each( list,
			function ( i , val ) 
			{
				var col = dataTable.column( val);
				col.visible( false);
			}
	);
}
function showColumns(tableId, list)
{
	var dataTable = $("#" + tableId ).DataTable();
	jQuery.each( list,
			function ( i , val ) 
			{
				var col = dataTable.column( val);
				col.visible( true );
			}
	);
}
function CreateColumnChooserMenu( tableId, menuElementId )
{
	var dTable = breadcrumbTable = $("#" + tableId ).DataTable();
	var menuElement = $('#' + menuElementId);
	var insert = "";
	var v = dTable.columns()[0];
	
	jQuery.each( v,
			function ( i , val ) 
			{
				var col = dTable.column(val);
				var header = col.header();
				name = col.header().innerText;
				if( name.length > 0){
					var checked= col.visible()?"checked ":"";
					var row = "<li><input type='checkbox' " + checked + " data-column='" + val + "' onchange='hideBreadcrumbColumn(\"" + tableId + "\", this )'/>" + name + "</li>"
					insert += row +"\n";
				}
			}
	);
	
	menuElement.html(insert);
}

function hideBreadcrumbColumn(tableId, checkbox)
{
	var dataTable = $("#" + tableId ).DataTable();
	var col = $(checkbox).attr('data-column');
	if( col ){
		var column = dataTable.column( col);
		if( $(checkbox).prop('checked') )
		{
			column.visible ( true);
		}
		else
		{
			column.visible ( false);
		}
	}
}