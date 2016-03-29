<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>  

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
                    <div class="col-lg-12">
                        <h1>Meals Calendar</h1>
                        
                		<div ng-controller="MealsCtrl">
                		
                		<br>
                		<div>
                		<label for="userSelect"> Meals for User: </label>
               			<select ng-change="getMealsForUser()"
               			        ng-model="selectedUser" id="userSelect"
						        ng-options="user as user.name for user in users"></select>
						</div>
						<br>
						
					  
					  	<div id="calendarModal" class="modal fade">
							<div class="modal-dialog">
							    <div class="modal-content">
							        <div class="modal-header">
							            <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span> <span class="sr-only">close</span></button>
							            <h4 id="modalTitle" class="modal-title"></h4>
							        </div>
							        <div id="modalBody" class="modal-body"> 
									   <form role="form">
									      <div class="form-group">
									        <label for="description">Description</label>
									        <input ng-model="meal.id" type="hidden" id="mealId">
									        <input ng-model="meal.description" type="text" class="form-control" id="mealDescription" placeholder="Enter description" required/>
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
							        <div class="modal-footer" ng-show="!meal.id">
							            <button ng-click="newMeal(meal)" class="btn btn-success" data-dismiss="modal">New Meal</button>
							        </div>
							        <div class="modal-footer" ng-show="meal.id">
							        	<button ng-click="deleteMeal(meal)" class="btn btn-default" ng-show"" data-dismiss="modal">Remove</button>
							            <button ng-click="updateMeal(meal)" class="btn btn-success" data-dismiss="modal">Edit Meal</button>
							        </div>
							    </div>
							</div>
						</div>
					  </div>


  						<div id='calendar'></div>    
                        
                    </div>
                </div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->

</body>
</html>