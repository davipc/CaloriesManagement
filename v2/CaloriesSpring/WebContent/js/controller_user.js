var app = angular.module("UserApp", []);

app.controller("UsersCtrl", function($scope, $http) {
	
    $scope.init = function(user, adminUser) {

    	// $scope.chosenUser will always hold a copy to the data in the DB... when changes are made, 
    	// the list will still hold the value as in the DB and chosen user will point to a copy of it
    	// the reason for that is so the list of users doesn't change while the form is being edited (login is changeable), 
    	// and when the form changes, the user can still be found in the list users list by ID 
    	
    	$scope.chosenUser = user; 
    	$scope.adminUser = adminUser;
    	
    	// clear the password field (users will only populate if they want to
		// change it)
		$scope.chosenUser.password = '';
		// change date format (from ms to real date)
		$scope.chosenUser.creationDt = new Date($scope.chosenUser.creationDt);
    	
        // load the roles
    	getRoles($scope, $http);

        if (adminUser) {
        	// Load the users
	    	getUsers($scope, $http);
        }
    }
    
	$scope.userChanged = function() {
		$scope.chosenUser = angular.copy($scope.selectedUser);
    	// clear the password field (users will only populate if they want to change it)
		$scope.chosenUser.password = '';
		// change date format (from ms to real date)
		$scope.chosenUser.creationDt = new Date($scope.chosenUser.creationDt);
		updateRoleSelections($scope);
		$("#message").html("");
	}

	$scope.rolesChanged = function() {
		$scope.chosenUser.roles = $scope.selectedRoles;
	}
	
	$scope.newUserClicked = function() {
		$scope.chosenUser = 
			{
				login: '',
				password: '',
				name: '', 
				gender: '',
				dailyCalories: '',
				roles: [$scope.defaultRole],
				creationDt: ''			
			};
		
		$scope.selectedUser = $scope.chosenUser;
		updateRoleSelections($scope);
		
		$("#message").html("");
		
		$('#login').focus();
	}
	
	$scope.createUser = function(user){
//		if ($scope.userForm.$valid) {
			// full calendar uses old version of angular, doesn't have ng-<validation> attributes on input fields
			// validating manually
			var isValid = validateFields('create', user);
			
			if (isValid) {
				postUser(user, $scope, $http);
			}
//		} else {
//			$scope.userForm.submitted = true;
//		}
	}
	
	$scope.updateUser = function(user){
//		if ($scope.userForm.$valid) {
			// full calendar uses old version of angular, doesn't have ng-<validation> attributes on input fields
			// validating manually
			var isValid = validateFields('update', user);
			
			if (isValid) {
				putUser(user, $scope, $http);
			}
//		} else {
//			$scope.userForm.submitted = true;
//		}
	}

	$scope.deleteUser = function(user){
		deleteUser(user, $scope, $http);
	}
});


function validateFields(operation, user) {
	var valid = true;
	if (!user.login || user.login == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Login is required.</p>");	
	} else if (user.login.length > 12) {
		valid = false;
		$("#message").html("<p style=\"color:red\">Login can only be 12 chars long.</p>");
	} else if ((!user.password || user.password == '') && operation == 'create') {
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


function getUsers($scope, $http) {
	  
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // $http.get('json/users.json'). // for test only
	  $http.get('api/v2/users').
	    success(function(data, status, headers, config) {
		      $scope.users = data;
		      
		      setChosenUser($scope);
	    }).
	    error(function(data, status, headers, config) {
	    	alert('Error getting list of users: ' + status);
		});
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
		      
		    updateRoleSelections($scope);
	    }).
	    error(function(data, status, headers, config) {
	    	alert('Error getting list of roles: ' + status);
		});
}

function setChosenUser($scope){
	angular.forEach($scope.users, function(user, key) {
  	  	if(!$scope.selectedUser && user.id == $scope.chosenUser.id){
  	  		$scope.selectedUser = $scope.users[key];
  	  	}
    });
}

function updateRoleSelections($scope) {
    if ($scope.adminUser) {
    	$scope.selectedRoles = [];  

    	// mark the user roles in the select
		angular.forEach($scope.roles, function(role) {
			// then check if this role is in the set of user roles (for existing user)
			var found = false;
			angular.forEach($scope.chosenUser.roles, function(chosenRole) {
				if(role.name == chosenRole.name) {
			  		found = true;
			  	}
			});
			
			if (found) 
				$scope.selectedRoles.push(role);
		});
    } 
}

function postUser(user, $scope, $http){

	// for new users whe only assign the date at save time
	if (!user.creationDt || user.creationDt == '')
		user.creationDt = new Date();
	
	$("#message").html("");
	
	$http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // POST USER

	$http.post('api/v2/users/', user)
	  	.success(function(data, status, headers, config) {
	    	$scope.chosenUser = angular.copy(data);
	    	
	    	// add the user to the list of users
	    	if ($scope.users) {
	    		$scope.users.push(data);
	    		// select the new user in the list
	    		// redefine as the new object has an ID
	    		$scope.selectedUser = null;
	    		setChosenUser($scope);
	    	}
	    	
			$scope.chosenUser.password = '';
			// change date format (from ms to real date)
			$scope.chosenUser.creationDt = new Date($scope.chosenUser.creationDt);

			//alert('User successfully created!');
			$("#message").append("<p style=\"color:green\">User successfully created!</p>");
	  	})
	  	.error(function(data, status, headers, config) {
	  		if (status == 400) {
	  			//alert('That login is being used already.');
				$("#message").append("<p style=\"color:red\">That login is being used already.</p>");
	  		} else {
	  			//alert('Error creating user: ' + status);
				$("#message").append("<p style=\"color:red\">Error creating user: " + status + "</p>");
	  		}
	  	});
}

function putUser(user, $scope, $http){
	var userForPost = angular.copy(user);
		
	if (userForPost.password == '')
		delete userForPost.password;

	$("#message").html("");
	
	$http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // PUT MEAL
	  $http.put('api/v2/users/', userForPost)
	  	.success(function(data, status, headers, config) {
	    	//alert('User successfully updated!');
			$("#message").append("<p style=\"color:green\">User successfully updated!</p>");
			
			$scope.chosenUser = angular.copy(data);
			
			// replace the user in the list of users, as the login name might have changed
	    	if ($scope.users) {
	    		var index = findUserById($scope.users, user);
	    		if (index >= 0) {
	    			$scope.users[index] = angular.copy(data);
	    		}

	    		// find the user again in the list
	    		$scope.selectedUser = null;
	    		setChosenUser($scope);
	    	}
			
			$scope.chosenUser.password = '';
			// change date format (from ms to real date)
			$scope.chosenUser.creationDt = new Date($scope.chosenUser.creationDt);

			// now update the user in session if it's the logged user that got updated
			// will only update "harmless" attributes (not the roles list or ID)
			
			$http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
			  // call JSP
			  $http.post('updateUserInSession.jsp', data)
			  	.success(function(data, status, headers, config) {
					//alert('Received ' + data + ' back from the JSP');
			    })
			  	.error(function(data, status, headers, config) {
					//alert('Received status ' + status + ' back from the JSP');
			    });
			
			
	    })
	  	.error(function(data, status, headers, config) {
	  		if (status == 400) {
	  			//alert('That login is being used already.');
				$("#message").append("<p style=\"color:red\">That login is being used already.</p>");
	  		} else {
		    	//alert('Error updating user: ' + status);
				$("#message").append("<p style=\"color:red\">Error updating user: " + status + "</p>");
	  		}
	    });

}

function deleteUser(user, $scope, $http){

	$("#message").html("");
	
	var userId = user.id;
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // DELETE USER
	  $http.delete('api/v2/users/' + userId)
	  	.success(function(data, status, headers, config) {
	    	//alert('User successfully deleted!');
			$("#message").append("<p style=\"color:green\">User successfully deleted!</p>");
	    	
	    	// remove user from list of users
	    	if ($scope.users) {
	    		var index = findUserById($scope.users, user);
	    		if (index >= 0) {
	    			$scope.users.splice(index, 1);
	    		}
	    		
	    		// select a new user on the list
	    		$scope.selectedUser = $scope.users[0];
	    		$scope.userChanged();
	    	}
	    	
	    })
	    .error(function(data, status, headers, config) {
	    	//alert('Error deleting user: ' + status);
			$("#message").append("<p style=\"color:red\">Error deleting user: " + status + "</p>");
	    });
}

function findUserById(userList, user) {
	var index = -1;
	var count = 0;
	angular.forEach(userList, function(userInList) {
		if (index == -1 && userInList.id == user.id) {
			index = count; 
		}
		count++;
	});
	
	return index;
}

function getUTCDate(date) {
	var _utc = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
    return _utc;
}

