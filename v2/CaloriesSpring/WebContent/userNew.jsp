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
<script src="js/controller_newuser.js"></script>
<script src="lib/bootstrap/bootstrap.min.js"></script>
<style>
   #calendar {
               max-width: 900px;
               margin: 0 auto;
       }
</style>
</head>
<body ng-app="NewUserApp">

   <div id="wrapper">

        <%@ include file="SidebarUserNew.jsp" %>

        <!-- Page Content -->
        <div id="page-content-wrapper">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-lg-11">
                        <h1>Create your new user</h1>
                        	<br/><br/>
							<div ng-controller="NewUsersCtrl">
							                		
	                			<div ng-init="init()"></div>

			                    <div class="col-lg-12">
		                		
			                		<div class="col-lg-11">
	
		                		    <div class="panel panel-info">
		                        	<div class="panel-heading"> User Details</div>
			                        <div class="panel-body">
			                        <form name="userForm">
				                        <p> In this screen you can define the user information and password.</p>
				                        
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
															<tr><td align="right">Password</td><td><input type="password" ng-model="chosenUser.password" id="password" class="form-control" ></td></tr>
															<tr><td align="right">Name</td><td><input type="text" ng-model="chosenUser.name" id="name" class="form-control" ></td></tr>
															<tr><td align="right">Gender</td><td><select id="gender" ng-model="chosenUser.gender" class="form-control"><option value="M">Male</option><option value="F">Female</option></select></td></tr>
															<tr><td align="right">Daily Calories</td><td><input type="number" id="dailyCalories" ng-model="chosenUser.dailyCalories" class="form-control" ></td></tr>
															<tr><td align="right">Created on</td><td><input type="datetime" id="creationDt" ng-model="chosenUser.creationDt" class="form-control" disabled></td></tr>
														</tbody> 
													</TABLE>
												</div>
											</div>
										</div>
																	
										<button class="btn btn-success" ng-click="createUser(chosenUser)" ng-show="!chosenUser || !chosenUser.id">Create User</button>
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
