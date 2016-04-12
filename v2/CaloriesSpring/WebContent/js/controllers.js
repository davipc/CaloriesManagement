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
    	getMealsForUser($scope.chosenUser.id, null, null, null, null, $scope, $http);
    }
    
	$scope.userChanged = function(){
		$scope.chosenUser = $scope.selectedUser;

		getMealsForUser($scope.chosenUser.id, $scope.filterFromDate, $scope.filterToDate, $scope.filterFromTime, $scope.filterToTime, $scope, $http);
	}
	
	$scope.filterMealsClicked = function(){
		//alert('from Date: ' + $scope.filterFromDate + '; to Date: ' + $scope.filterToDate + 'from Time: ' + $scope.filterFromTime + '; to Time: ' + $scope.filterToTime);
//		if ($scope.filterFromDate || $scope.filterToDate || $scope.filterFromTime || $scope.filterToTime) {
			//alert('At least one is defined');
			getMealsForUser($scope.chosenUser.id, $scope.filterFromDate, $scope.filterToDate, $scope.filterFromTime, $scope.filterToTime, $scope, $http);
//		} else {
//			alert('No changes to filter, so no action needed');
//		}
	}
	
	
	$scope.resetClicked = function(){
//		if ($scope.filterFromDate || $scope.filterToDate || $scope.filterFromTime || $scope.filterToTime) {
			//alert('At least one was cleaned');
			// clear date/time fields and redo the search 
			$scope.filterFromDate = $scope.filterToDate = $scope.filterFromTime = $scope.filterToTime = null;
			getMealsForUser($scope.chosenUser.id, $scope.filterFromDate, $scope.filterToDate, $scope.filterFromTime, $scope.filterToTime, $scope, $http);
//		} else {
//			alert('No changes to filter, so no action needed');
//		}
	}
	
	$scope.newMeal = function(meal){
		// full calendar uses old version of angular, doesn't have ng-<validation> attributes on input fields
		// validating manually
		var isValid = validateFields(meal);
		
		if (isValid) {
			postMeal(meal, $scope, $http);
		}
	}
	
	$scope.deleteMeal = function(meal){
		deleteMeal($scope.originalMeal, $scope, $http);
	}
	
	$scope.updateMeal = function(meal){
		// full calendar uses old version of angular, doesn't have ng-<validation> attributes on input fields
		// validating manually
		var isValid = validateFields(meal);
		
		if (isValid) {
			putMeal($scope.originalMeal, meal, $scope, $http);
		}
	}
	
	$scope.showMeal = function(meal){
		$scope.meal = {
				id:meal.id,
				user: {id: meal.user.id},
				description: meal.description,
		        calories: meal.calories,
		        mealDate: meal.mealDate,
		        mealTime: meal.mealTime
		};
		$scope.originalMeal = {
				id:meal.id,
				user: {id: meal.user.id},
		        description: meal.description,
		        calories: meal.calories,
		        mealDate: meal.mealDate,
		        mealTime: meal.mealTime
		};
	}
});

function validateFields(meal) {
	var valid = true;
	if (!meal.description || meal.description == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Description is required.</p>");	
	} else if (meal.description.length > 200) {
		valid = false;
		$("#message").html("<p style=\"color:red\">Description can only be 200 chars long.</p>");
	} else if (!meal.calories || meal.calories == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Calories is required.</p>");
	} else if (!/^\d+$/.test(meal.calories)) {
		valid = false;
		$("#message").html("<p style=\"color:red\">Calories needs to be a number.</p>");
	} else if (!meal.mealDate || meal.mealDate == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Meal date is required.</p>");	
	} else if (!meal.mealTime || meal.mealTime == '') {
		valid = false;
		$("#message").html("<p style=\"color:red\">Meal time is required.</p>");	
	}
	
	return valid;
}


function getUsers($scope, $http) {
	  
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

function calculateAvgCalories($scope){
	if ($scope.totalCalories && $scope.backgroundEvents.length > 0) {
		$scope.dailyAvgCalories = $scope.totalCalories / $scope.backgroundEvents.length;
	} else {
		$scope.dailyAvgCalories = 0;
	} 
}


function postMeal(meal, $scope, $http){
	
	$("#message").html("");
	
	// format dates and times for submission
	var date = getUTCDate(new Date(meal.mealDate+'T'+meal.mealTime));
	var time = getUTCDate(new Date(meal.mealDate+'T'+meal.mealTime));
	
	var postedMeal = angular.copy(meal);
	postedMeal.mealDate = date;
	postedMeal.mealTime = time;
	
	$http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // POST MEAL
	  //$http.post('json/meals.json', meal) // for test only
	  $http.post('api/v2/meals/', postedMeal)
	  	.success(function(data, status, headers, config) {
	    	meal = data;
	    	
	    	$scope.totalCalories += meal.calories; 
	    	
	    	// need to format the date and time since those come back from the post and put methods as longs, 
	    	// and the calendar events are stored as string dates 
	    	meal.mealDate = moment(new Date(meal.mealDate)).format('YYYY-MM-DD');
	       	meal.mealTime = moment(new Date(meal.mealTime)).format('HH:mm');
	       	
	  	  	putMealOnCalendar(meal, $scope);
	  	  	
	  	  	$('#calendarModal').modal('hide');
	  	  
	  	  	// recalculate avg
	  	  	calculateAvgCalories($scope);	  	  	
	  	})
	  	.error(function(data, status, headers, config) {
	  		if (status == 400) {
	  			$("#message").append("<p style=\"color:red\">A meal exists for that date and time. Please edit the existing meal instead.</p>");
	  		} else {
	  			$("#message").append("<p style=\"color:red\">Error creating meal: " + status + "</p>");
	  		}
	  	});
}

function putMeal(originalMeal, meal, $scope, $http){
	
	$("#message").html("");
	
	// format dates and times for submission
	var date = getUTCDate(new Date(meal.mealDate+'T'+meal.mealTime));
	var time = getUTCDate(new Date(meal.mealDate+'T'+meal.mealTime));
	
	var putMeal = angular.copy(meal);
	putMeal.mealDate = date;
	putMeal.mealTime = time;
	
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // PUT MEAL
	  $http.put('api/v2/meals/', putMeal)
	  	.success(function(data, status, headers, config) {
	    	meal = data;
	  	  	
	    	// need to format the date and time since those come back from the post and put methods as longs, 
	    	// and the calendar events are stored as string dates 
	    	meal.mealDate = moment(new Date(meal.mealDate)).format('YYYY-MM-DD');
	       	meal.mealTime = moment(new Date(meal.mealTime)).format('HH:mm');
	       	
	       	// update calories total
	       	$scope.totalCalories -= originalMeal.calories; 
	       	$scope.totalCalories += meal.calories; 
	       	
	    	// change meal in the calendar (need to remove then reinsert)
	    	removeMealFromCalendar(originalMeal, $scope);
	  	  	putMealOnCalendar(meal, $scope);
	  	  	
	  	  	$('#calendarModal').modal('hide');
	  	  	
	  	  	// recalculate avg
	  	  	calculateAvgCalories($scope);	  	  	
	    })
	  	.error(function(data, status, headers, config) {
	  		if (status == 400) {
	  			$("#message").append("<p style=\"color:red\">A meal exists for that date and time. Please edit the existing meal instead.</p>");
	  		} else {
	  			$("#message").append("<p style=\"color:red\">Error updating meal: " + status + "</p>");
	  		}
	    });

}

function deleteMeal(meal, $scope, $http){
	
	$("#message").html("");
	
	var mealId = meal.id;
	  $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	  // DELETE MEAL
	  $http.delete('api/v2/meals/' + mealId)
	  	.success(function(data, status, headers, config) {
	    	// delete successful
	  		$scope.totalCalories -= meal.calories; 
	  		removeMealFromCalendar(meal, $scope);
	  		
	  		$('#calendarModal').modal('hide');
	  		
	  	  	// recalculate avg
	  	  	calculateAvgCalories($scope);	  	  	
	    })
	    .error(function(data, status, headers, config) {
  			$("#message").append("<p style=\"color:red\">Error deleting meal: " + status + "</p>");
	    });
}

function getMealsForUser(userId, filterFromDate, filterToDate, filterFromTime, filterToTime, $scope, $http){
	 
	// this will force the interface not to show anything (empty string)
	$scope.dailyAvgCalories = 'Processing...';

	 $http.defaults.headers.common["X-Custom-Header"] = "Angular.js";
	 // $http.get('json/meals'+userId+'.json'). // for tests only
	 $http.get('api/v2/users/'+userId+'/meals', {
	 params: {fromDate: filterFromDate, toDate: filterToDate, fromTime: filterFromTime, toTime: filterToTime} 
	 	})
	 	.success(function(data, status, headers, config) {
	 		var meals = data;
		    meals.sort(function(a, b){
			   	 // return getUTCDate(new Date(a.mealDate+'T'+a.mealTime)) -
					// new
					// getUTCDate(Date(b.mealDate+'T'+b.mealTime));
			   	 return new Date(a.mealDate) - new Date(b.mealDate);
		     });
		     
		     $scope.events = [];
		     $scope.totalCalories = 0;
		     
		     angular.forEach(meals, function(meal, key) {
		    	 $scope.totalCalories += meal.calories; 
		    	 convertMealToEvent(meal, $scope);
		     });
	     
		     createBackgroundEvents(meals, $scope); 

		     calculateAvgCalories($scope);
		     
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


function getUTCDate(date) {
	var _utc = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
    return _utc;
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
	
	if (meals && meals.length > 0) {
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
				center: 'title' //,
				//right: 'month,agendaWeek,agendaDay' // disabled since the meals only have a start time, and the week/day views allow to setup end times we would not be able to save 
			},
			defaultDate: new Date(),
			selectable: true,
			selectHelper: true,
			select: function(start, end) {
				var meal = {
				        user: {id: $scope.chosenUser.id},
						description: '',
				        calories: '',
				        originalCalories: 0,
				        mealDate: start.format('YYYY-MM-DD'),
				        mealTime: ''
				}

//				// only to be used when week or day views are enabled
//				if($('#calendar').fullCalendar('getView').name !='month'){
//					meal.mealTime = start.format('HH:mm');
//				}
				
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
				        user: {id: $scope.chosenUser.id},
						description: event.title,
				        calories: event.description,
				        originalCalories: event.description,
				        mealDate: event.start.format('YYYY-MM-DD'),
				        mealTime: event.start.format('HH:mm')
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

