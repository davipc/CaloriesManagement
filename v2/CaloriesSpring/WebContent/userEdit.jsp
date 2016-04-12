<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>  

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html lang="en">
<head>

<title>Calories Management</title>

<link href='lib/cupertino/jquery-ui.min.css' rel='stylesheet' />
<link href='lib/fullcalendar/fullcalendar.css' rel='stylesheet' />
<link href='lib/fullcalendar/fullcalendar.print.css' rel='stylesheet' media='print' />
<link href="lib/bootstrap/bootstrap.min.css" rel="stylesheet" type="text/css" >
<link href="css/simple-sidebar.css" rel="stylesheet">

<script src='lib/moment.min.js'></script>
<script src='lib/jquery.min.js'></script>
<script src='lib/fullcalendar/fullcalendar.js'></script>
<script src="lib/angular/angular.js"></script>
<script src="js/controller_user.js"></script>
<script src="lib/bootstrap/bootstrap.min.js"></script>
<style>
   #calendar {
               max-width: 900px;
               margin: 0 auto;
       }
</style>
</head>
<body ng-app="UserApp">

   <div id="wrapper">

        <%@ include file="Sidebar.jsp" %>

        <!-- Page Content -->
        <div id="page-content-wrapper">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-lg-11">
                        <h1>User Management</h1>
                        	<br/><br/>
							<div ng-controller="UsersCtrl">
							                		
		                		<sec:authorize access="hasRole('ROLE_ADMIN')">
		                			<div ng-init="init(<sec:authentication property="principal.JSON" />, true)"></div>
		                		</sec:authorize>
		                		<sec:authorize access="!hasRole('ROLE_ADMIN')">
		                			<div ng-init="init(<sec:authentication property="principal.JSON" />, false)"></div>
		                		</sec:authorize>		                        

			                    <div class="col-lg-12">
		                		
			                		<sec:authorize access="hasRole('ROLE_ADMIN')">
	               						<div class="col-lg-2">
					                		<div class="form-group">
					                		<label for="userSelect"> Edit a User: &nbsp;&nbsp;</label>
					               			<select ng-change="userChanged()"
					               			        ng-model="selectedUser" id="userSelect"
											        ng-options="user as user.login for user in users | orderBy:'login'"></select>
											</div>
											<br/><br/>
											<label>Or create one:</label> <button class="btn btn-success" ng-click="newUserClicked()">Create Now!</button>  
										</div>
									</sec:authorize>
			                    
			                		<div class="col-lg-9">
	
		                		    <div class="panel panel-info">
		                        	<div class="panel-heading"> User Details</div>
			                        <div class="panel-body">
			                        <form name="userForm">
										<sec:authorize access="hasRole('ROLE_ADMIN')">				                        
											<p> In this screen you can define the user information, password and the assigned roles.</p>
										</sec:authorize>
										<sec:authorize access="!hasRole('ROLE_ADMIN')">				                        
											<p> In this screen you can define the user information and password.</p>
										</sec:authorize>
				                        
				                        <br>
		
										<div class="panel panel-info">
				                       		<div class="panel-heading"> User Info </div>
			                        		<div class="panel-body">
					                       		<div class="table-responsive">
													<input ng-model="chosenUser.id" type='hidden' id='userId'>
													<TABLE id="UserDetailsTable" class="table table-bordered" >
														<thead>
															<TR>
																<TH style="width: 25%">Name</TH>
																<TH style="width: 75%">Value</TH>
															</TR>
														</thead>
														<tbody>
															<tr><td align="right">Login</td><td><input type="text" ng-model="chosenUser.login" id="login" class="form-control" ></td></tr>
															<tr><td align="right"><table><tr><td align="right">Password</td></tr><tr id="passwordUpdateText"><td><sub>* only enter if you want to update it</sub></td></tr></table></td><td><input type="password" ng-model="chosenUser.password" id="password" class="form-control" ></td></tr>
															<tr><td align="right">Name</td><td><input type="text" ng-model="chosenUser.name" id="name" class="form-control" ></td></tr>
															<tr><td align="right">Gender</td><td><select id="gender" ng-model="chosenUser.gender" class="form-control" ><option value="M">Male</option><option value="F">Female</option></select></td></tr>
															<tr><td align="right">Daily Calories</td><td><input type="number" id="dailyCalories" ng-model="chosenUser.dailyCalories" class="form-control" ></td></tr>
															<tr><td align="right">Created on</td><td><input type="datetime" id="creationDt" ng-model="chosenUser.creationDt" class="form-control" disabled></td></tr>
														</tbody> 
													</TABLE>
												</div>
											</div>
										</div>
				                		<sec:authorize access="hasRole('ROLE_ADMIN')">
											<div class="panel panel-info">
					                       		<div class="panel-heading"> Role Associations </div>
					                       		<div class="panel-body">
													<p> Here you choose the roles this user will be associated with.</p>
						                      
						                      		<br>
													<div class="col-lg-5">
													<div class="form-group">
														<select id="selectedRolesId" ng-model="selectedRoles" ng-options="role as role.name for role in roles" ng-change="rolesChanged()" class="form-control" size="5" multiple>
														</select>
														<!-- {{ selectedRoles | json }} -->		
														
													</div>
													</div>
													<div class="col-lg-5">
													</div>
												</div>
											</div>
										</sec:authorize>
																	
										<button class="btn btn-success" ng-click="createUser(chosenUser)" ng-show="!chosenUser || !chosenUser.id">Create User</button>
										<button class="btn btn-success" ng-click="updateUser(chosenUser)" ng-show="chosenUser && chosenUser.id">Update User</button>
				                		<sec:authorize access="hasRole('ROLE_ADMIN')">
											<button class="btn btn-default" ng-click="deleteUser(chosenUser)" ng-show="chosenUser && chosenUser.id">Delete User</button>
										</sec:authorize>
										<br/> <br/>
										<div id="message"></div>										
									</form>
		
									</div>
								</div>
							
							</div> <!-- User details  -->
	                		<div class="col-lg-1">
							</div>
							
							</div>
						</div>
						</div>	
						
  
                        
                    </div>
                </div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

</body>
</html>
