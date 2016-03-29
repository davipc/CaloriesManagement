var app = angular.module("MyApp", []);

app.controller("MealsCtrl", function($scope, $http) {
	
    $scope.init = function(user, showUsers) {
    	
    	$scope.chosenUser = user; //{"id":29,"login":"admin1","password":"CxTVAaWURCoBxoWVQbyz6BZNGD0yk3uFGDVEL2nVyU4=","name":"Admin 1","gender":"M","dailyCalories":1800,"roles":[{"id":12,"name":"ADMIN"}],"creationDt":1459263652257};
    	
    	// start by showing an empty calendar
        $scope.events = [];
        showCalendar($scope.events, $scope);
    	
        if (showUsers) {
	        // Load the users
	    	getUsers($scope, $http);
        }
        
    	// Load the meals from currently logged in user
    	getMealsForUser($scope.chosenUser.id, $scope, $http);
    }
    
	$scope.userChanged = function(){
		$scope.chosenUser = $scope.selectedUser;
		getMealsForUser($scope.chosenUser.id, $scope, $http);
	}
	
	$scope.newMeal = function(meal){
		postMeal(meal, $scope, $http);
	}
	
	$scope.deleteMeal = function(meal){
		deleteMeal($scope.originalMeal, $scope, $http);
	}
	
	$scope.updateMeal = function(meal){
		putMeal($scope.originalMeal, meal, $scope, $http);
	}
	
	$scope.showMeal = function(meal){
		$scope.meal = {
				id:meal.id,
		        description: meal.description,
		        calories: meal.calories,
		        mealDate: meal.mealDate,
		        mealTime: meal.mealTime
		};
		$scope.originalMeal = {
				id:meal.id,
		        description: meal.description,
		        calories: meal.calories,
		        mealDate: meal.mealDate,
		        mealTime: meal.mealTime
		};
	}
});

function getUsers($scope, $http){
	  
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  //$http.get('json/users.json'). // for test only
	  $http.get('api/v2/users').
	    success(function(data, status, headers, config) {
	      $scope.users = data;
	      
	      setChosenUser($scope);
	    }).
	    error(function(data, status, headers, config) {
	    	alert('Error getting list of users: ' + status);
		});
}

function setChosenUser($scope){
	angular.forEach($scope.users, function(user, key) {
  	  	if(!$scope.selectedUser && user.id == $scope.chosenUser.id){
  	  		$scope.selectedUser = $scope.users[key];
  	  	}
    });
}

function postMeal(meal, $scope, $http){
	
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // POST MEAL
	  //$http.post('json/meals.json', meal) // for test only
	  $http.post('api/v2/meals/', meal)
	  	.success(function(data, status, headers, config) {
	    	meal = data;
	  	})
	  	.error(function(data, status, headers, config) {
	    	alert('Error creating meal: ' + status);
	  	});

	  putMealOnCalendar(meal, $scope);
}

function putMeal(originalMeal, meal, $scope, $http){
	
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // PUT MEAL
	  $http.put('api/v2/meals/', meal)
	  	.success(function(data, status, headers, config) {
	    	meal = data;
	    })
	  	.error(function(data, status, headers, config) {
	    	alert('Error updating meal: ' + status);
	    });

	  removeMealFromCalendar(originalMeal, $scope)
	  putMealOnCalendar(meal, $scope);

}

function deleteMeal(meal, $scope, $http){
	
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // DELETE MEAL
	  $http.delete('api/v2/meals/' + meal.id, meal.id)
	  	.success(function(data, status, headers, config) {
	    	// delete successful
	    })
	    .error(function(data, status, headers, config) {
	    	alert('Error deleting meal: ' + status);
	    });
	  
	removeMealFromCalendar(meal, $scope);
}

function getMealsForUser(userId, $scope, $http){
	  
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  //$http.get('json/meals'+userId+'.json'). // for tests only
	  $http.get('api/v2/users/'+userId+'/meals')
	  	.success(function(data, status, headers, config) {
	      var meals = data;
	      
	      $scope.events = [];
	      
	      angular.forEach(meals, function(meal, key) {
	    	  
	    	  convertMealToEvent(meal, $scope);
	      });
	      
	      createBackgroundEvents(meals, $scope); 
	      
		  $('#calendar').fullCalendar('removeEvents', function(evt) {
			    return true;
		  });
		  
	      angular.forEach($scope.events, function(event, key) {
	    	  $('#calendar').fullCalendar('renderEvent', event, true);
	      });
	    })
	    .error(function(data, status, headers, config) {
	    	alert("Error getting meals for user " + userId + ": " + status);
	    });
}

function createBackgroundEvent(startDate, caloriesPerDay, $scope){
	var event = new function() {
	      this.title = startDate;
	      this.start = startDate;
	      this.rendering = "background";
	      this.caloriesPerDay = caloriesPerDay;
	      this.color = getBackgroundColor(caloriesPerDay, $scope);
	  }
	
	$scope.events.push(event);
	$scope.backgroundEvents.push(event);
	
	return event;
}

function getBackgroundColor(caloriesPerDay, $scope){
    if(caloriesPerDay <= $scope.chosenUser.dailyCalories){
  	  return "green";
    }else{
  	  return "red";
    }
}

function createBackgroundEvents(meals, $scope){
	var caloriesPerDay = 0;
	var currentDate = null;
	$scope.backgroundEvents = [];
	angular.forEach(meals, function(meal, key) {	
		
		if(currentDate != meal.mealDate){	
			if(currentDate){
				createBackgroundEvent(currentDate, caloriesPerDay, $scope);
			}
			caloriesPerDay = meal.calories;
		}else{
			caloriesPerDay = caloriesPerDay + meal.calories;
		}
		currentDate = meal.mealDate;
    });
	
	createBackgroundEvent(currentDate, caloriesPerDay, $scope);
}

function adjustBackgroundEvent(meal, $scope){
	
	var mealDate = meal.mealDate;
	var backEvent;
	var backEventIndex;
	angular.forEach($scope.backgroundEvents, function(event, key) {	
		if(!backEvent){
			if(event.start == meal.mealDate){	
				backEvent = event;
				backEvent.caloriesPerDay = backEvent.caloriesPerDay + meal.calories;
				backEvent.color = getBackgroundColor(backEvent.caloriesPerDay,$scope);
				backEventIndex = key;
			}
		}
    });
	
	if(!backEvent){
		backEvent = createBackgroundEvent(meal.mealDate, meal.calories, $scope);
	}
	
	// Replace back event
	$('#calendar').fullCalendar('removeEvents', function(evt) {
	    return ((evt.title == meal.mealDate) && (evt.rendering == 'background'));
	})

	if(backEvent.caloriesPerDay > 0){
		$('#calendar').fullCalendar('renderEvent', backEvent, true);
	}else{
		$scope.backgroundEvents.splice(backEventIndex, 1);
	}
	
	return backEvent;
}

function createEvent(eventId, eventTitle, calories, startDate, events){
	      var event = new function() {
	    	   this.id = eventId;
	           this.title = eventTitle;
	           this.description = calories;
	           this.start = startDate;
	        }
	      return event;
}

function convertMealToEvent(meal, $scope){
	var event = createEvent(meal.id, meal.description,meal.calories, meal.mealDate+'T'+meal.mealTime);
	$scope.events.push(event);
	return event;
}

function putMealOnCalendar(meal, $scope){
	var eventMeal = convertMealToEvent(meal, $scope);	
	$('#calendar').fullCalendar('renderEvent', eventMeal, true);
	
	adjustBackgroundEvent(meal, $scope);

}

function removeMealFromCalendar(meal, $scope){
	
	meal.calories = -meal.calories;
	
	adjustBackgroundEvent(meal, $scope);
	
	// Remove event
	$('#calendar').fullCalendar('removeEvents', function(evt) {
	    return (evt.id == meal.id);
	})
}

function showCalendar(events, $scope){
	
	$(document).ready(function() {

		$('#calendar').fullCalendar({
			theme: true,
			header: {
				left: 'prev,next today',
				center: 'title'
			},
			defaultDate: new Date(),
			selectable: true,
			selectHelper: true,
			select: function(start, end) {
				var meal = {
				        description: '',
				        calories: '',
				        originalCalories: 0,
				        mealDate: start.format('YYYY-MM-DD'),
				        mealTime: ''
				}
				
				$scope.$apply(function(){
	                $scope.showMeal(meal)
	              });
				
				$('#modalTitle').html("Enter New Meal");
				$('#calendarModal').modal();
				
				$('#calendar').fullCalendar('unselect');
			},
			editable: true,
			eventLimit: true, // allow "more" link when too many events
			events:  events, //"json/events.json"
			eventClick:  function(event, jsEvent, view) {
				var meal = {
						id: event.id,
				        description: event.title,
				        calories: event.description,
				        originalCalories: event.description,
				        mealDate: event.start.format('YYYY-MM-DD'),
				        mealTime: event.start.format('HH:mm:ss')
				}
				
				$scope.$apply(function(){
	                $scope.showMeal(meal)
	              });
				
	            $('#modalTitle').html(event.title+' '+event.description);
	            $scope.$apply(function(){
	                $scope.showMeal(meal)
	              });
	            $('#calendarModal').modal();
	        },
		});
		
	});	
}

