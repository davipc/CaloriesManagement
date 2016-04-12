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
                    <div class="col-lg-12">
						<h1>Meals Manager</h1>
						<br/>  <br/>
						<!-- <h5> -->
						Welcome, <sec:authentication property="principal.name" />! 
						<br/>  <br/>
						On this application you will be able to manage and query your meals.<br/> 
						Click on "Meals Calendar" to the left to add, remove, change or check your meals.
						<sec:authorize access="!hasRole('ROLE_ADMIN')">
						<br/>  <br/>
						You can also manage your user information by clicking on "User Management"
						</sec:authorize>	
						<sec:authorize access="hasRole('ROLE_ADMIN')">
						<br/>  <br/>
						Since you are an administrator, you can also manage the application users by clicking on "User Management"
						</sec:authorize>	
						<br/>  <br/>
						Click on "Meals Manager" at any time to come back to this page.		 
						<!-- </h5> -->
                    </div>
                </div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->

</body>
</html>
