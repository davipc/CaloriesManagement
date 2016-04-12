var app = angular.module("NewUserApp", []);

app.controller("NewUsersCtrl", function($scope, $http) {
	
    $scope.init = function() {

		$scope.chosenUser = 
		{
			login: '',
			password: '',
			name: '', 
			gender: '',
			dailyCalories: '',
			roles: [],
			creationDt: ''		
		};
	
		$('#login').focus();		
    	
        // load the roles
    	getRoles($scope, $http);
    }
    
	$scope.createUser = function(user){
//		if ($scope.userForm.$valid) {
			
			// full calendar uses old version of angular, doesn't have ng-<validation> attributes on input fields
			// validating manually
			var isValid = validateFields(user);
			
			if (isValid) {
				postUser(user, $scope, $http);
			}
//		} else {
//			$scope.userForm.submitted = true;
//		}
	}
});

function validateFields(user) {
	var valid = true;
	if (!user.login || user.login == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Login is required.</p>");	
	} else if (user.login.length > 12) {
		valid = false;
		$("#message").html("<p style=\"color:red\">Login can only be 12 chars long.</p>");
	} else if (!user.password || user.password == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Password is required.</p>");	
	} else if (!user.name || user.name == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Name is required.</p>");	
	} else if (user.name.length > 80) {
		valid = false;
		$("#message").html("<p style=\"color:red\">Name can only be 80 chars long.</p>");
	} else if (!user.gender || user.gender == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Gender is required.</p>");	
	} else if (!user.dailyCalories || user.dailyCalories == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Daily Calories is required.</p>");
	} else if (!/^\d+$/.test(user.dailyCalories)) {
		valid = false;
		$("#message").html("<p style=\"color:red\">Daily Calories needs to be a number.</p>");
	}
	
	return valid;
}

function getRoles($scope, $http) {
	  
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  $http.get('api/v2/roles').
	    success(function(data, status, headers, config) {
	    	$scope.roles = data;
		      
		    // need to find and keep DEFAULT role (for created users)
		    angular.forEach($scope.roles, function(role) {
		    	if (role.name == "DEFAULT") {
		    		$scope.defaultRole = role;
				}
		    });
	    }).
	    error(function(data, status, headers, config) {
	    	alert('Error getting list of roles: ' + status);
		});
}


function postUser(user, $scope, $http){
	
	// assign the creation date time
	if (!user.creationDt || user.creationDt == '')
		user.creationDt = new Date();

	// add the default role so the user can navigate
	user.roles.push($scope.defaultRole);
	
	$http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // POST USER
	  $http.post('api/v2/users/', user)
	  	.success(function(data, status, headers, config) {
			if (status == 201) {
				//alert('User successfully created!');
				$("#message").html("");
				$("#message").append("<p style=\"color:green\">User successfully created!</p>");
				$("#message").append("<p style=\"color:green\"><a href='login.jsp'>Click here </a>to go back to the login page, or wait 5 seconds to be redirected</p>");

				$scope.chosenUser.creationDt = new Date(data.creationDt);
				
				setTimeout(function() {
					  window.location.href = "login.jsp";
				}, 5000);
			}
	  	})
	  	.error(function(data, status, headers, config) {
	  		$("#message").html("");
	  		if (status == 400) {
	  			//alert('That login is being used already.');
	  			$("#message").append("<p style=\"color:red\">That login is being used already.</p>");
	  		} else {
	  			//alert('Error creating user: ' + status);
	  			
	  			$("#message").append("<p style=\"color:red\">Error creating user: " + status + "</p>");
	  		}
	  	});
}

function getUTCDate(date) {
	var _utc = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
    return _utc;
}

