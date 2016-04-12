<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>  

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html lang="en">
<head>
<meta charset='utf-8' />

<title>Calories Management</title>

<link href='lib/cupertino/jquery-ui.min.css' rel='stylesheet' />
<link href='lib/fullcalendar/fullcalendar.css' rel='stylesheet' />
<link href='lib/fullcalendar/fullcalendar.print.css' rel='stylesheet' media='print' />
<link href="lib/bootstrap/bootstrap.min.css" rel="stylesheet" type="text/css" >
<!-- Custom CSS -->
<link href="css/simple-sidebar.css" rel="stylesheet">

<script src='lib/moment.min.js'></script>
<script src='lib/jquery.min.js'></script>
<script src='lib/fullcalendar/fullcalendar.js'></script>
<script src="lib/angular/angular.js"></script>
<script src="js/controllers.js"></script>
<script src="lib/bootstrap/bootstrap.min.js"></script>
<style>
   #calendar {
               max-width: 900px;
               margin: 0 auto;
       }
</style>
</head>
<body ng-app="MyApp">

   <div id="wrapper">

        <%@ include file="Sidebar.jsp" %>

        <!-- Page Content -->
        <div id="page-content-wrapper">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-lg-10">
                        <h1>Meals Calendar</h1>
                        
                		<div ng-controller="MealsCtrl">
                		
	                		<sec:authorize access="hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')">
	                			<div ng-init="init(<sec:authentication property="principal.JSON" />, true)"></div>
	                		</sec:authorize>
	                		<sec:authorize access="!(hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN'))">
	                			<div ng-init="init(<sec:authentication property="principal.JSON" />, false)"></div>
	                		</sec:authorize>
                		
		                    <div class="col-lg-12">
	                		
		                		<div class="col-lg-2">
			                        <br/><br/><br/> 
			                		<sec:authorize access="hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')">
			                		<div class="form-group">
			                		<label for="userSelect"> User: &nbsp;&nbsp;</label>
			               			<select ng-change="userChanged()"
			               			        ng-model="selectedUser" id="userSelect"
									        ng-options="user as user.login for user in users | orderBy:'login'"></select>
									</div>
									</sec:authorize>
										
								    <div>
									    <div class="form-group">
											<label for="fromDateFilter"> From date: </label>
											<input ng-model="filterFromDate" class="form-control" id="fromDateFilter" type="date" required/>
									    </div>
									    <div class="form-group">
											<label for="toDateFilter"> To date: </label>
											<input ng-model="filterToDate" class="form-control" id="toDateFilter" type="date" required/>
									    </div>
									</div>
								    <div>
									    <div class="form-group">
									    	<label for="fromTimeFilter"> From Time: </label>
									     	<input ng-model="filterFromTime" type="time" class="form-control" id="fromTimeFilter" required/>
									    </div>
									    <div class="form-group">
									    	<label for="toTimeFilter"> To Time: </label>
									     	<input ng-model="filterToTime" type="time" class="form-control" id="toTimeFilter" required/>
									    </div>
									</div>
									<button ng-click="filterMealsClicked()" class="btn btn-success">Filter Meals</button>
									<button ng-click="resetClicked()" class="btn btn-default">Reset</button>
									
									
									<br/>
									<br/> 
									<br/> 
									<br/> 
									<br/> 
									<br/> 
									<br/> 
									<br/>  
									<br/>  
									<br/>  
									<br/>
									<br/>  
									<b>Max Daily Calories:</b> {{ chosenUser.dailyCalories | number: 0 }}<br/><br/>
									<b>Average per Day:</b> {{ dailyAvgCalories | number: 0 }}
								</div>
								
		                		<div class="col-lg-10">
								
								  	<div id="calendarModal" class="modal fade">
										<div class="modal-dialog">
										    <div class="modal-content">
										        <div class="modal-header">
										            <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span> <span class="sr-only">close</span></button>
										            <h4 id="modalTitle" class="modal-title"></h4>
										        </div>
										        <div id="modalBody" class="modal-body"> 
												   <form name="form" role="form">
												      <div class="form-group">
												        <label for="description">Description</label>
												        <input ng-model="meal.id" type="hidden" id="mealId">
												        <input ng-model="meal.user.id" type="hidden" id="mealUserId">
												        <input ng-model="meal.description" type="text" class="form-control" id="mealDescription" name="mealDescription" placeholder="Enter description" required />
												        <!-- <span ng-show="meal.description.$dirty && meal.description.$error.required">Description is required</span> -->
												      </div>
												         <div class="form-group">
												        <label for="description">Calories</label>
												        <input ng-model="meal.calories" type="number" class="form-control" id="calories" placeholder="Enter Calories" required/>
												      </div>
												      <div class="form-group">
												        <label for="mealDate">Date</label>
														<input ng-model="meal.mealDate" class="form-control" id="mealDate" type="date" required/>
												       </div>
												      <div class="form-group">
												        <label for="mealTime">Time</label>
												        <input ng-model="meal.mealTime" type="time" class="form-control" id="mealTime" required/>
												      </div>
												    </form>		        
										        </div>
										        <div class="modal-footer">
										            <div ng-show="!meal.id">
											            <button ng-click="newMeal(meal)" class="btn btn-success">New Meal</button>
											        </div>
											        <div ng-show="meal.id">
											        	<button ng-click="deleteMeal(meal)" class="btn btn-default">Remove</button>
											            <button ng-click="updateMeal(meal)" class="btn btn-success">Edit Meal</button>
											        </div>
											        <br/>
											        <div id="message"></div>
											    </div>
										    </div>
										</div>
									</div>
			
			  						<div id='calendar'></div>    
						  		</div>
                    		</div>
                    	</div> <!-- div controller -->
                    </div>
                </div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->

</body>

</html>
