<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>  

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html lang="en">

<head>

	<title>Calories Management</title>

    <!-- Bootstrap Core CSS -->
    <LINK href="css/bootstrap.min.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <LINK href="css/plugins/metisMenu/metisMenu.min.css" rel="stylesheet">

    <!-- Timeline CSS -->
    <LINK href="css/plugins/timeline.css" rel="stylesheet">

    <!-- Custom CSS -->
    <LINK href="css/sb-admin-2.css" rel="stylesheet">

    <!-- Morris Charts CSS -->
    <LINK href="css/plugins/morris.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <LINK href="font-awesome-4.2.0/css/font-awesome.min.css" rel="stylesheet" type="text/css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    
    <LINK rel="stylesheet" href="jquery/jquery-ui.min.css">
	<LINK rel="stylesheet" href="jquery/themes/blue/style.css" type="text/css" media="print, projection, screen" />
    
	<!-- Custom CSS -->
	<link href="css/simple-sidebar.css" rel="stylesheet">
    
    <SCRIPT src="jquery/jquery-1.11.1.min.js" type="text/javascript"></SCRIPT>
    <SCRIPT src="jquery/jquery-ui.min.js" type="text/javascript"></SCRIPT>
	<SCRIPT src="jquery/jquery.json.min.js" type="text/javascript"></SCRIPT>
	
	<SCRIPT src="jquery/jquery-dateFormat.min.js" type="text/javascript"></SCRIPT>
	<SCRIPT src="jquery/jquery.numeric.min.js"  type="text/javascript"></SCRIPT>	
	<SCRIPT src="jquery/jquery.tablesorter.min.js"  type="text/javascript"></SCRIPT>
	
	<SCRIPT src="js/bootstrap.min.js" type="text/javascript"></SCRIPT>
	
	<!-- Metis Menu Plugin JavaScript -->
	<SCRIPT src="js/plugins/metisMenu/metisMenu.min.js"	type="text/javascript"></SCRIPT>
	
	<!-- Custom Theme JavaScript -->
	<SCRIPT src="js/sb-admin-2.js" type="text/javascript"></SCRIPT>
	
	
	<script src="js/plugins/dataTables/jquery.dataTables.js"></script>
    <script src="js/plugins/dataTables/dataTables.bootstrap.js"></script>

	<!-- http://www.daterangepicker.com/-->
	<link rel="stylesheet" type="text/css" href="js/dateRange/daterangepicker-bs3.css" />
	<!--   <script type="text/javascript" src="js/dateRange/moment.js"></script> -->
	<script type='text/javascript' src='js/moment.min.js'></script>
	<script type='text/javascript' src='js/moment-timezone-with-data-2010-2020.js'></script>

	<script type="text/javascript" src="js/dateRange/daterangepicker.js"></script>
</head>

<body ng-app="MyApp">

   <div id="wrapper">

        <%@ include file="Sidebar.jsp" %>

        <!-- Page Content -->
        <div id="page-content-wrapper">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-lg-12">

	<!-- 
    <div id="wrapper">
    -->
		<!-- Navigation -->
        <div id="page-wrapper">
            <div class="row">
                <div class="col-lg-12">
                    <h1 id="pageHeader" class="page-header">New User</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
 
			<div class="navbar">
		     	<div class="row">
		     	
		     		<div id="existingUsersDiv" class="col-lg-12">
	                  <div class="panel panel-info">
	                      <div class="panel-heading">
	                          Existing Users
	                      </div>
						  <div class="panel-body">
						  	<div class="table-responsive">
								<TABLE id="UserTable" class="table table-bordered table-stripped table-hover">
									<THEAD>
										<TR>
											<TH>Login</TH>
											<TH></TH>
										</TR>
									</THEAD> 
									<TBODY> 
									</TBODY> 
								</TABLE>
							</div>
							
							<button class="btn btn-default" id="NewUserBtn" >New User</button>
						  </div>
						</div>
					</div>
					
                	<div class="col-lg-12">
                	
                   		<div class="panel panel-info" id="UserDetailsDiv" hidden="true">
                        	<div class="panel-heading"> User Details</div>
	                        <div class="panel-body">
		                        <p> In this screen you can define the user information, password and the assigned roles.</p>
		                        
		                        <br>

								<div class="panel panel-info">
		                       		<div class="panel-heading"> User Info </div>
	                        		<div class="panel-body">
			                       		<div class="table-responsive">
											<input type='hidden' id='userId' class="form-control" value="XXX">
											<TABLE id="UserDetailsTable" class="table table-bordered" >
												<thead>
													<TR>
														<TH style="width: 25%">Name</TH>
														<TH style="width: 75%">Value</TH>
													</TR>
												</thead>
												<tbody>
													<tr><td align="right">Login</td><td><div id='loginDiv'> <input type='text' id='login' class="form-control" ></div></tr>
													<tr><td align="right"><table><tr><td align="right">Password</td></tr><tr id="passwordUpdateText"><td><sub>* only enter if you want to update it</sub></td></tr></table></td><td><div id='passwordDiv'> <input type='password' id='password' class="form-control" ></div></tr>
													<tr><td align="right">Name</td><td><div id='nameDiv'> <input type='text' id='name' class="form-control" ></div></tr>
													<tr><td align="right">Gender</td><td><div id='surnameDiv'> <select id='gender' class="form-control" ><option value="M">Male</option><option value="F">Female</option></select></div></tr>
													<tr><td align="right">Daily Calories</td><td><div id='dailyCaloriesDiv'> <input type='text' id='dailyCalories' class="form-control" ></div></tr>
												</tbody> 
											</TABLE>
										</div>
									</div>
								</div>
								<div class="panel panel-info">
		                       		<div class="panel-heading"> Role Associations </div>
		                       		<div class="panel-body">
										<p> Here you choose the roles this user will be associated with.</p>
			                      
			                      		<br>
			
										<div class="form-group" >
											<TABLE id="RoleAssociationTable">
												<thead>
													<TR>
														<TH style="width: 45%">Available Roles</TH>
														<TH style="width: 2%"></TH>
														<TH style="width: 6%"></TH>
														<TH style="width: 2%"></TH>
														<TH style="width: 45%">Selected Roles</TH>
													</TR>
												</thead>
												<tbody>
													<!-- <tr><td>&nbsp;</td></tr> -->
													<tr>
														<td>
															<select id="availableRolesId" class="form-control" size="20" multiple>
															</select>
														</td>
														<td></td>
														<td align="center" valign="middle">
															<table>
																<tr><td><input type="button" id="btnRolesRight" class="btn btn-primary margin-bottom: 10px;" value="&gt;&gt;"/></td></tr>
																<tr><td>&nbsp;</td></tr>
																<tr><td><input type="button" id="btnRolesLeft" class="btn btn-primary margin-bottom: 10px;" value="&lt;&lt;"/></td></tr>
																<tr><td>&nbsp;</td></tr>
															</table>
														</td>
														<td></td>
														<td>
															<select id="selectedRolesId" class="form-control" size="20" multiple>
															</select>
														</td>
													</tr>
												</tbody> 
											</TABLE>
										</div>
									</div>
								</div>
					
								<button disabled="disabled" class="btn btn-default" id="AddUserBtn" >Create User</button>
								<button disabled="disabled" class="btn btn-default" id="UpdateUserBtn" >Update User</button>
								<button disabled="disabled" class="btn btn-default" id="DeleteUserBtn" >Delete User</button>

								<br>
								<div id="message"></div>
							</div>
						</div>
					</div>					
				</div>
			</div>
		</div>
	<!-- </div> -->
    
                    </div>
                </div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->
    
	<script>
	    
		$('#NewUserBtn').click(
				function()
				{
					//alert('New User clicked!');
					MakeCreateScreen();
					$('#UserDetailsDiv').show();
					$('#login').focus();
				}
		);
	
		function MakeCreateScreen()
		{
			$('#pageHeader').text("New User");
			
			$('#passwordUpdateText').hide();
			
			var pathname = window.location.pathname;
			$('#AddUserBtn').show();
			$("#UpdateUserBtn").hide();
			$('#DeleteUserBtn').hide();
			$('#AddUserBtn').removeAttr("disabled");
			$('#UpdateUserBtn').attr("disabled", "disabled");
			$('#DeleteUserBtn').attr("disabled", "disabled");
			window.history.replaceState("", "", pathname );
		
	
			$('#message').html( "" );
			
			$('#login').val("");
			$('#password').val("");
			$('#name').val("");
			$('#gender').val("");
			$('#dailyCalories').val("");
			
			// populate the available roles list with all groups and regular roles
			// first merge the group names with the role names - the double merge is essential to not modify the original arrays 
			// (this way it creates a new array in the process)
			populateRolesSelectElement($('#availableRolesId'), allRoles);
			
			// empty the selected roles list
			populateRolesSelectElement($('#selectedRolesId'), []);
		}
	
		function MakeUpdateScreen( id )
		{
			$('#pageHeader').text("Edit User");
			
			$('#passwordUpdateText').show();
			
			$('#AddUserBtn').hide();
			$('#UpdateUserBtn').show();
			$('#DeleteUserBtn').show();
	
			$('#AddUserBtn').attr("disabled", "disabled");
			$('#UpdateUserBtn').removeAttr("disabled");
			$('#DeleteUserBtn').removeAttr("disabled");
	
			var pathname = window.location.pathname;
			window.history.replaceState("", "", pathname+ "?id=" + id);
			$('#message').html( "" );
			
			var requestData = '{ "id" : "' + id + '"}"';
			
			$.ajax({
			    url: "userAdminSecured",//servlet URL that gets first option as parameter and returns JSON of to-be-populated options
			    type: "POST",//request type, can be GET
			    cache: false,//do not cache returned data
			    data: "operation=getUser&data="+requestData,//data to be sent to the server
			    //dataType: "json",//type of data returned
			    beforeSend: function(){
	                   $("#message").html("Processing...");
	            },
				success: function(data, textStatus, jqXHR) {
					$("#message").html("");
					if (data.length > 0 && data != "{ \"responseData\":{},\"errorMsg\":\"\" }") {
						var jsonData = JSON.parse(data);
						
						var errorMsg = jsonData['errorMsg'];
						var userData = jsonData['responseData'];
						
						pushUserDataToFields(userData);
						
						if (errorMsg != "") {
							var decodedErrorMsg = decodeStr(errorMsg);
							$("#message").html("<p style=\"color:red\">" + decodedErrorMsg + "</p>");
						}
					} else {
						$('#UpdateUserBtn').attr('disabled', '');
						$('#DeleteUserBtn').attr('disabled', '');
						$("#message").html("<p style=\"color:red\">User not found!</p>");
					}
		    	},
		    	error: function(data, textStatus, jqXHR) {
		    		alert('Get User Data: CALL FAILED!');
		    	}
			});
			
			$('#requirePwdChange').prop('checked', false);
		}
		
		function populateRolesSelectElement(selectElement, selectedValues) {
			// finally recreate the list of options on the right side (preserving the original order)
			selectElement.empty();
			
			$.each(allRoles, 
				function(index, value) {
					if ($.inArray(value, selectedValues) >= 0) {
						selectElement.append($("<option></option>").attr("value", value).text(value));
					}
				}
			);		
		}
		

		// Moves the selected items on the "from" side to the "to" side 
		function moveSelectedRolesFromTo(from, to) {
			// first gather list of options already present on the "to" side
			var finalSelectedList = [];
			to.find('option').each(
				function() {
					finalSelectedList.push($(this).val());
				}
			)
	
			// then add the lists that were selected on the "from" side
			from.find('option:selected').each(
				function() {
					finalSelectedList.push($(this).val());
					// take the chance to remove the item from the "from" side
					$(this).remove();
				}
			)
			
			// finally recreate the list of options on the "to" side (preserving the original order)
			populateRolesSelectElement(to, finalSelectedList);
		}
		
		$('#btnRolesRight').click(
			function() {
				// move the selected items from left to right
				moveSelectedRolesFromTo($('#availableRolesId'), $('#selectedRolesId'));
			}
		);
	
		$('#btnRolesLeft').click(
				function() {
					// move the selected items from right to left
					moveSelectedRolesFromTo($('#selectedRolesId'), $('#availableRolesId'));
				}
			);
		
	
		function pushUserDataToFields(userData) {
			$('#login').val(userData['login']);
			
			// we are not filling the password field (it comes hashed from the DB)
			//$('#password').val(userData['password']);
			$('#password').val("");
			
			$('#name').val(userData['name']);
			$('#gender').val(userData['gender']);
			$('#dailyCalories').val(userData['dailyCalories']);
			
			// create the array of selected roles - for each group found, include the contained roles in the final list
			var selectedRoles = $.merge([], userData['roles']);
			$.each(userData['roles'], 
				function(index, value) {
					var groupElements = roleGroups[value];
					if (groupElements != null) {
						selectedRoles = $.merge(selectedRoles, groupElements);
					}
				}
			)
	
			// create the array of available roles, by taking all values and removing the ones in the selected list
			var availableRoles = [];
			$.each(allRoles,
				function(index, value) {
					if ($.inArray(value, selectedRoles) < 0) {
						availableRoles.push(value);
					}
				}
			);
			
			// populate the UI elements with the lists
			populateRolesSelectElement($('#availableRolesId'), availableRoles);
			populateRolesSelectElement($('#selectedRolesId'), selectedRoles);
		}
	
		function createJSONUserFromFields() {
			var userData = "{";
			
			if (id != "") {
				userData += "\"id\":" + JSON.stringify($('#userId').val()) + ",";
			}
			userData += "\"login\":" + JSON.stringify($('#login').val()) + ",";
			
			// we only pick the password field if it was filled
			if ($.trim($('#password').val) != "") {
				userData += "\"password\":" + JSON.stringify($('#password').val()) + ",";
			}
			
			userData += "\"name\":" + JSON.stringify($('#name').val()) + ",";
			userData += "\"gender\":" + JSON.stringify($('#gender').val()) + ",";
			userData += "\"dailyCalories\":" + JSON.stringify($('#dailyCalories').val()) + ",";
			
			// include roles
			var selectedRoles = [];
			
			// get all options but the divider
			$('#selectedRolesId option').each(
				function() {
					var currentValue = $(this).val();
					if (currentValue != 'rolesDivider') {
						selectedRoles.push(JSON.stringify(currentValue));
					}
				}
			);
			
			if (!$.isEmptyObject(selectedRoles)) {
				userData += ", \"roles\": [" + selectedRoles.join(',') + "]";
			}		
			
			userData += "}"
			
			return userData;
		}
	
		function validateUserFields(operation) {
			var errors = "";
			if ($.trim($('#login').val()) == "")
				errors += "Login is a mandatory field<br>";
			
			if (operation == 'addUser') {
				if ($.trim($('#password').val()) == "")
					errors += "Password is a mandatory field<br>";
			}
			
			if ($.trim($('#name').val()) == "")
				errors += "Name is a mandatory field<br>";
			if ($.trim($('#gender').val()) == "")
				errors += "Gender is a mandatory field<br>";
			if ($.trim($('#dailyCalories').val()) == "")
				errors += "Daily Calories is a mandatory field<br>";
	
			return errors;
		}
		
		$('#AddUserBtn').click( 
			function()
	    	{
				var errors = validateUserFields('addUser');
				if (errors != "") {
					$('#message').html("<p style=\"color:red\">" + errors + "</p>");
				} else {
				
					var jsonUserData = createJSONUserFromFields();
					
					//$('#message').html("Creating user with data " + jsonUserData);
					
					$.ajax({
					    url: "userAdminSecured",//servlet URL that gets first option as parameter and returns JSON of to-be-populated options
					    type: "POST",//request type, can be GET
					    cache: false,//do not cache returned data
					    data: "operation=addUser&data="+jsonUserData,//data to be sent to the server
					    //dataType: "json",//type of data returned
					    beforeSend: function(){
			            	$("#message").text("Processing...");
			            },
						success: function(data, textStatus, jqXHR) {
							$("#message").html("");
							if (data.length > 0) {
								var jsonData = JSON.parse(data);
								
								var errorMsg = $.trim(jsonData['errorMsg']);
								var responseData = jsonData['responseData'];
								
								// make sure the ID was returned
								var id = responseData['id'];
								
								if (id == "") {
									errorMsg += " [Error creating user: no ID returned. Please contact the Administrator]";
								}
		
								if (errorMsg != "") {
									var decodedErrorMsg = decodeStr(errorMsg);
									$("#message").append("<p style=\"color:red\">" + decodedErrorMsg + "</p>");
								} else {
									$('#message').append("<p style=\"color:green\">User created!</p>");							
		
									// go to edit page with the just created user loaded
									var pathname = window.location.pathname;
									window.location.replace(pathname + "?id="+id );
								}
							} else {
								$("#message").append("<p style=\"color:red\">NO DATA</p>");
							}
				    	},
				    	error: function(data, textStatus, jqXHR) {
				    		alert('Create User: CALL FAILED!');
				    	}
					});
				}
	    	}
	    );
	
		
		$('#UpdateUserBtn').click( 
			function()
		    {
				var errors = validateUserFields('updateUser');
				if (errors != "") {
					$('#message').html("<p style=\"color:red\">" + errors + "</p>");
				} else {
					var jsonUserData = createJSONUserFromFields();
					
					//$('#message').html("Updating user with data " + jsonUserData);
					
					$.ajax({
					    url: "userAdminSecured",//servlet URL that gets first option as parameter and returns JSON of to-be-populated options
					    type: "POST",//request type, can be GET
					    cache: false,//do not cache returned data
					    data: "operation=updateUser&data="+jsonUserData,//data to be sent to the server
					    //dataType: "json",//type of data returned
					    beforeSend: function(){
			            	$("#message").text("Processing...");
			            },
						success: function(data, textStatus, jqXHR) {
							$("#message").html("");
							if (data.length > 0) {
								var jsonData = JSON.parse(data);
								
								var errorMsg = $.trim(jsonData['errorMsg']);
								var responseData = jsonData['responseData'];
								
								// make sure the ID didn't change
								var newId = responseData['newId'];
								if (errorMsg == "" && newId != id) {
									errorMsg = "Unexpected Id change during update: " + id + " changed to " + newId;
								}
								
								if (errorMsg != "") {
									var decodedErrorMsg = decodeStr(errorMsg);
									$("#message").append("<p style=\"color:red\">" + decodedErrorMsg + "</p>");
								} else {
									$('#message').append("<p style=\"color:green\">User updated!</p>");	
									
									if (loginChanged) {
										// refresh edit page so new login is loaded
										var pathname = window.location.pathname;
										window.location.replace(pathname + "?id="+newId );
									}
								}
							} else {
								$("#message").append("<p style=\"color:red\">NO DATA</p>");
							}
				    	},
				    	error: function(data, textStatus, jqXHR) {
				    		alert('Update User: CALL FAILED!');
				    	}
					});
				}		        
		    }
		);
		
		var questionModal = -1;
		
		$('#DeleteUserBtn').click( 
			function()
			{
		        //alert('Delete clicked!');

		        if( questionModal == -1 )
		        {
					questionModal = new QuestionModal();
					questionModal.create("question" );
		        }
		        questionModal.reset();
				questionModal.bind("yes", 
					function()
					{
						//$('#message').html("Deleting user with id " + id);
						
						var requestData = '{ "id" : "' + id + '"}"';
						
						$.ajax({
						    url: "userAdminSecured",//servlet URL that gets first option as parameter and returns JSON of to-be-populated options
						    type: "POST",//request type, can be GET
						    cache: false,//do not cache returned data
						    data: "operation=deleteUser&data="+requestData,//data to be sent to the server
						    //dataType: "json",//type of data returned
						    beforeSend: function(){
				            	$("#message").text("Processing...");
				            },
							success: function(data, textStatus, jqXHR) {
								$("#message").html("");
								if (data.length > 0) {
									var jsonData = JSON.parse(data);
									
									var errorMsg = $.trim(jsonData['errorMsg']);
									var responseData = jsonData['responseData'];
									
									// make sure the deletion was successful
									var deleted = responseData['result'];
		
									if (!deleted) {
										errorMsg += " [Error deleting user: " + deleted + ". Please contact the Administrator]";
									}
		
									if (errorMsg != "") {
										var decodedErrorMsg = decodeStr(errorMsg);
										$("#message").append("<p style=\"color:red\">" + decodedErrorMsg + "</p>");
									} else {
										$('#message').append("<p style=\"color:green\">User deleted!</p>");							
		
										// go to edit page with the just created user loaded
										var pathname = window.location.pathname;
										window.location.replace(pathname);
									}
								} else {
									$("#message").append("<p style=\"color:red\">NO DATA</p>");
								}
					    	},
					    	error: function(data, textStatus, jqXHR) {
					    		alert('Delete User: CALL FAILED!');
					    	}
						});
					}
				);
				questionModal.display( "Are you sure you want to delete this User?");
			}
	    );
			
		// need to handle case where string was encoded with '+' for empty spaces instead of '%20'
		function decodeStr(str) {
			return decodeURIComponent((str+'').replace(/\+/g, '%20'));
		}
		
		$('#login').change(
			function() {
				loginChanged = true;
			}
		);
		
		// when the login is updated we will refresh the page so the table of users is updated
		var loginChanged = false;
		
		// create structures for role lists and groups once here, so we can use it from within several functions
		var allRoles = [];
		
		allRoles.push("Default");
		allRoles.push("Manager");
		allRoles.push("Admin");
		
		// global ID to be used by every function
		var id = "";
	
		if (id != "") {
			MakeUpdateScreen(id);
			$('#UserDetailsDiv').show();
		}

	</script>
</body>

</html>