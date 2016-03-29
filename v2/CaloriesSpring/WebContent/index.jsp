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
						
						Logged user: <sec:authentication property="principal.login" /> with id <sec:authentication property="principal.id" />
						<br/>
						Whole info: <sec:authentication property="principal.JSON" />
                    </div>
                </div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->

</body>
</html>
