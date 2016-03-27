var app = angular.module("MyApp", []);

app.controller("MealsCtrl", function($scope, $http) {
	
	// Get from selectedUser
	$scope.maxCaloriesPerDay = 1000;
	$scope.loggedUserId = 1;
	
	// USERS
	getUsers($scope, $http);

	// MEALS
	getMealsAndPutOnCalendar($scope.loggedUserId, $scope, $http);
   
	$scope.getMealsForUser = function(){
		getMealsForUser($scope.selectedUser.id, $scope, $http);
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
	  $http.get('json/users.json').
	    success(function(data, status, headers, config) {
	      $scope.users = data;
	      
	      setLoggedUser($scope);
	    });
}

function setLoggedUser($scope){
	angular.forEach($scope.users, function(user, key) {
  	  	if(!$scope.selectedUser && user.id == $scope.loggedUserId){
  	  		$scope.selectedUser = $scope.users[key];
  	  	}
    });
}

function postMeal(meal, $scope, $http){
	
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // TODO POST MEAL
	  /*
	  $http.post('json/meals.json', meal).
	    success(function(data, status, headers, config) {
	    	meal = data;
	    });
	  */
	  // TODO fake id while does not post
	  meal.id = (new Date()).getTime();
	  putMealOnCalendar(meal, $scope);

}

function putMeal(originalMeal, meal, $scope, $http){
	
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // TODO PUT MEAL
	  /*
	  $http.put('json/meals.json', meal).
	    success(function(data, status, headers, config) {
	    	meal = data;
	    });
	  */
	  removeMealFromCalendar(originalMeal, $scope)
	  putMealOnCalendar(meal, $scope);

}

function deleteMeal(meal, $scope, $http){
	
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // TODO DELETE MEAL
	  /*
	  $http.delete('json/meals.json', meal).
	    success(function(data, status, headers, config) {
	    	
	    });
	  */
	removeMealFromCalendar(meal, $scope);

}

function getMealsAndPutOnCalendar(userId, $scope, $http){
	  
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  $http.get('json/meals'+userId+'.json').
	    success(function(data, status, headers, config) {
	      var meals = data;
	      
	      $scope.events = [];
	      angular.forEach(meals, function(meal, key) {
	    	  
	    	  convertMealToEvent(meal, $scope);
	      });
	      
	      createBackgroundEvents(meals, $scope); 
	      
	      showCalendar($scope.events, $scope)
	      
	    });
}

function getMealsForUser(userId, $scope, $http){
	  
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  $http.get('json/meals'+userId+'.json').
	    success(function(data, status, headers, config) {
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
    if(caloriesPerDay <= $scope.maxCaloriesPerDay){
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

