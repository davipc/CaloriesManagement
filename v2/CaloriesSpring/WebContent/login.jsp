<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Calories Management</title>

    <!-- Bootstrap Core CSS -->
    <link href="lib/bootstrap/bootstrap.min.css" rel="stylesheet">
    <link href="css/sb-admin-2.css" rel="stylesheet">
	<script src='lib/jquery.min.js'></script>
    <script src="lib/bootstrap/bootstrap.min.js"></script>
</head>

<body>
    <div class="container">
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <div class="login-panel panel panel-info">
                    <div class="panel-heading">
                        <h3 class="panel-title">Please Sign In</h3>
                    </div>
                    <div class="panel-body">
                        <form role="form" method="POST" action="/CaloriesSpring/login.jsp">
                            <fieldset>
                                <div class="form-group">
                                    <input class="form-control" placeholder="Login" name="username"  autofocus>
                                </div>
                                <div class="form-group">
                                    <input class="form-control" placeholder="Password" name="password" type="password" value="">
                                </div>
                                <input type="submit" class="btn btn-lg btn-success btn-block" value="Login"/>
                            </fieldset>
                        </form>
                        
                        <% 
                        	String errors = (String) request.getParameter("error");
                        	if (errors != null) {
                        		out.println("<p style=\"color:red\">Invalid login or password</p>");
                        	}
                        %>
						<p align="right" font="small"><a align="right" href="userNew.jsp">or click here to Signup</a></p>
                    </div>
                </div>
            </div>
        </div>
    </div>

</body>

</html>
