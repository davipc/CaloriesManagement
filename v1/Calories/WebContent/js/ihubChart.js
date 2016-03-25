/**
 * Encapsulates the front-end functionality of a chart for the InteractionHub 
 *
 * Requires: (Load before this file)
 *    Highcharts <version>
 *    JQuery     <version>
 *    JQuery-UI  <version>
 *    ihubChart.css should be linked from the including page
 *    FontAwesome added
 *   font-awesome-4.2.0/css/font-awesome.css.min should be linked from the including page
 */

Highcharts.setOptions({
    colors: [
        '#218221',
        "#514F78",
        "#9B5E4A",
        "#72727F",
        "#1F949A",
        "#82914E",
        "#86777F",
        "#42A07B"
    ],
    global: {
        useUTC: false
    }
});



// Namespace ihub
if (typeof ihub === "undefined") {
	ihub = {};
}

// Unique identifier for charts in order of instantiation.
ihub.chartIndex = 1;


// find the nth significant digit after the decimal pt
ihub.getYPrecision = function(v) 
{
    var precision = 0;
    var value = Math.abs(v);

    if (parseInt(value) != value) {
        for (var i = 0; i < 5; i++) {
            value *= 10;
            if (value >= 1 && parseInt(value) == value) {
                break;
            }
        }
        precision = i + 1;
    }

    return precision;
};

ihub.Chart = function(refid, options, timezone) 
{
    var cls = function(refid, options, timezone) 
    {
        this.id = ihub.chartIndex++;
        this.options = options;
        
        this.timezoneName = timezone;

        // Set chart options, if there are passed in
        if (options.hasOwnProperty("chartOptions")) {
            this.chartOptions = options.chartOptions;
        }
        
        if (options.hasOwnProperty("groupBy")) {
            this.groupByOptions = options.groupBy;
        }
        
        this.refid = refid;
     
    };

    cls.prototype = {
        // UI components
   
        // @var the entire UI that goes inside the container 
        dom: null,
       
        mySliderMin: 1,
        mySliderMax: 1,
        
        // @var the containing element for the widget 
        container: null,
        
        // @var the container for tab elements 
        tabContainer: null,
        
        statusContainer: null,
        typeContainer: null,
        /** @var the container for the highcharts chart */
        chartContainer: null,
        // the identifier for which chart this is, as configured in the XHRChartController class
        refid: "",
        /** @var the container for the slider element */
        sliderContainer: null,
        lastTimestamp: null,
        sliderMin: 1,
        sliderMax: 1,
        sliderValue: 1,
        /** @var Highcharts.Chart instance */
        chart: null,
        /** @var hashtable of event callbacks to execute on the control **/
        callbacks: {},
 
        /** @var Highcharts-compatible options object */
        chartOptions: {},
        
        groupByOptions: {},
		
		loadingDiv: null,
        
		timezoneName : 'America/New_York',
        controlsContainer: null,
        goLeftElm: null,
        goRightElm: null,
        /** @var hashtable of options for the widget, including tabs,
         *  chart, and slider properties */
        options: {},
        /** @var array of tabs to show */
        tabs: null,
        /** @var the function to call to fetch chart data */
        dataFunction: null,
        /** @var fetched/saved chart data */
        chartData: {},
        /** @var the current active series */
        activeDataset: [],
        lodingDiv: null,
        /** @var keep track of series view options on change */
        viewOptions: null,
        sliderNavButtonWidth: 32,
        /** @var current chart period */
        period: ihub.Chart.PERIOD_DAY,
        

        getControls: function() 
        {
            var instance = this;
            var output = '';
//            $.ajax({
//                url: ihub.Chart.XHR_CONTROLLER,
//                type: 'POST',
//                data: {
//                    'action': 'getChartControls',
//                    'refid': instance.refid,
//                    'id'   : this.id
//                },
//                dataType: 'json',
//                success: function(response) {
//                    if (response._return.controlsHTML) {
//                        output = response._return.controlsHTML;
//                    }
//                },
//                async: false
//            });
            return output;
        },
        
        init: function() 
        {
            // initialize the overall container as a JQuery object
            this.container = $('#' + this.options.container);
            // Set up the tab container
            this.tabContainer =
                    $('<div />')
                    .attr('id', 'tabContainer' + this.id)
                    .addClass('ihub-tab-container')
                    .css('margin-top', 10)
                    .css('text-align', 'left');
            var instance = this;

            this.loadingDiv = $('<div />')
                    .addClass('ihub-loading-div')
                    .appendTo(this.container)
                    .hide();


            // Controls for the chart based on refid (these come from the server side)
            this.controlsContainer = $(this.getControls());
            this.controlsContainer.attr('id', this.controlsContainer.attr('id') + this.id);

            if (this.controlsContainer) 
            {
                this.container.append(this.controlsContainer);
                // init events for chart controls
                this.initControlEvents();
            }

            this.statusContainer = $('<div />').attr('id', 'ihub-status-container');

            this.container.append(this.tabContainer);
            this.tabContainer.append(this.statusContainer);

            // Set up the chartContainer
            this.chartContainer =
                    $("<div />")
                    .attr('id', 'chartContainer' + this.id)
                    .addClass('ihub-chart-container');
            this.container.append(this.chartContainer);

            // Set up the slider container
            this.sliderContainer =
                    $('<div />')
                    .attr('id', 'sliderContainer' + this.id)
                    .addClass('ihub-slider-container');
            this.container.append(this.sliderContainer);
        },
        
        updateChartType: function(newType) 
        {
            var chart = this.chart;
            var newPointWidth = 1;

            if (newType == 'column') {
                if (this.period == 'month') {
                    newPointWidth = 10;
                } else if (this.period == 'year') {
                    newPointWidth = 20;
                }
            }
        },
        /**
         * Adds a callback function for an event on the widget
         * @param strEvent String event name to attach to
         * @param callback function the method to call when the event fires
         */
        bind: function(strEvent, callback) 
        {
            if (!this.callbacks.hasOwnProperty(strEvent)) {
                this.callbacks[strEvent] = [];
            }
            this.callbacks[strEvent].push(callback);
        },
        /**
         * Dispatches events to all the registered handlers
         * @param eventName String the event to handle
         */
        dispatchEvent: function(eventName) 
        {
            // Create an event object to send the callback
            var evt = {};

            if (this.callbacks.hasOwnProperty(eventName)) {
                var myCallbacks = this.callbacks[eventName];
                for (var i = 0; i < myCallbacks.length; ++i) {
                    // Call the attached function with an event object parameter
                    myCallbacks[i](evt);
                }
            }
        },
        /**
         * Performs an XHR to get the upper and lower limit
         * for the timeline slider. This should only need to be
         * called once in the life of any given Chart
         *
         * @output object : {min: int, max: int}
         */
        fetchSliderBounds: function() 
        {
        	
            var output = {
                min: null,
                max: null
            };
            
            if( this.mySliderMax > 1 && this.mySliderMin > 1)
            {
            	output.min = this.mySliderMin;
                output.max = this.mySliderMax;
                
            }else{
	            // Call into the Ajax controller to get the bounds
	            var instance = this;
	            $.ajax(
		            {
		                url: ihub.Chart.XHR_CONTROLLER,
		                type: 'POST',
		                dataType: 'json',
		                data: {
		                    'action': 'fetchSliderBounds',
		                    'refid': instance.refid
		                },
		                success: function(response) 
		                {
		                    output.min = response._return.min;
		                    output.max = response._return.max;
		                },
		                async: false
		            }
	            );
            }
            return output;
        },
        /**
         * Shows the widget in the configured container
         */
        show: function() {
            this.dispatchEvent('onBeforeShow');
			
			this.loadingDiv = $('<div />')
                .attr('id', 'ihubLoadingDiv' + this.id)
                .addClass('ihub-loading-div')
                .hide();
            // If the UI isn't built, build it
            if (!this.dom) {
                this.initUI();
                this.container.show();
                this.initChart();

                // Build slider component
                if (this.options.hasOwnProperty('slider')) {
                    this.initSlider();
                }
            }
            this.dispatchEvent('onShow');
        },
        /**
         * Initializes the dom for the widget
         */
        initUI: function() {
            this.dispatchEvent('onBeforeInitUI');
            // Build tab elements
            if (this.options.hasOwnProperty('tabs')) {
                this.tabs = this.options.tabs;
            }

            if (!this.tabs) {
            	if( ihub.Chart.chartsOpt[this.refid] ){
            		this.tabs = ihub.Chart.chartsOpt[this.refid].tabs;
            	}else{
            		this.tabs = ihub.Chart.chartsOpt["defaultChart"].tabs;
            	}
            }
            if (this.tabs) {
                this.initTabs();
            }
            this.dispatchEvent('onInitUI');
        },
        /**
         * Initialize the slider control
         */
        initSlider: function() {
            this.dispatchEvent('onBeforeInitSlider');

            this.sliderContainer.css('position', 'relative');
            this.sliderContainer.css('height', '40px');
            this.sliderContainer.show();

            var instance = this;

            var bounds = this.fetchSliderBounds();
            this.slider =
                    $('<div />')
                    .slider({
                        min: bounds.min,
                        max: bounds.max,
                        step: ihub.Chart.STEP_SIZES[this.period],
                        value: bounds.max,
                        slide: function(event, ui) {
                            instance.sliderTooltip(event, ui, instance);
                        },
                        create: function(event, ui) {
                            instance.sliderTooltip(event, ui, instance);
                        },
                        change: function(event, ui) {
                            instance.sliderTooltip(event, ui, instance);
                            // Open data for the given timestamp
                            // for the current period
                            var timestamp = instance.slider.slider('value');
                            // Go get me some data, and display it
                            instance.fetchChartData(instance.period, timestamp);
                        }
                    })
                    .css("position", "absolute");

            // Programmatically recede the timeline one step with this button
            var goLeftElm = $('<button/>')
                    .attr('id', 'ihub-goLeft' + this.id)
                    .css('position', 'absolute')
                    .mousedown(
                            function() {
                                // need to get the slider element
                                var slider = $($(this).parent().find('#chartSlider'));
                                instance.slider.slider('value', instance.slider.slider('value') - ihub.Chart.STEP_SIZES[instance.period]);
                            })
                    .addClass('ui-corner-all')
                    .css('height', '25px;')
                    .css('width', '25px')
                    .css('text-align', 'center')
                    .css('cursor', 'pointer')
                    .attr('title', 'Go back one ' + instance.period)
                    .html('<i class="fa fa-arrow-circle-left"></i>');

            // Programatically advance the timeline one step with this button
            var goRightElm = $('<button />')
                    .attr('id', 'ihub-goRight' + this.id)
                    .css('position', 'absolute')
                    .mousedown(
                            function() {
                                // need to get the slider element
                                instance.slider.slider('value', instance.slider.slider('value') + ihub.Chart.STEP_SIZES[instance.period]);
                            })
                    .addClass('ui-corner-all')
                    .css('height', '25px;')
                    .css('width', '25px')
                    .css('text-align', 'center')
                    .css('cursor', 'pointer')
                    .attr('title', 'Advance one ' + instance.period)
                    .html('<i class="fa fa-arrow-circle-right"></i>');

            this.goLeftElm = goLeftElm;
            this.goRightElm = goRightElm;


            // Assemble the DOM components here
            this.sliderContainer.append(goLeftElm);
            this.sliderContainer.append(this.slider);
            this.sliderContainer.append(goRightElm);

            // Mod 6/4/2014 - removed the code that sizes the slider elements here, for RM #2644

            this.dom = true;
            this.dispatchEvent('onInitSlider');
        },
        sizeSliderElements: function(instance) {

            // IE8 creates a rendering/DOM attachment race condition on the chart,
            // workaround for the slider is to fit it into the container width itself, rather
            // than the plotBox width
            var hasPlotBox = false;


            var totalWidth = instance.chartContainer.css('width');
            if (instance.chart.plotBox !== undefined) {
                totalWidth = instance.chart.plotBox.width;
                hasPlotBox = true;
            }



            var sliderWidth = totalWidth - (2 * instance.sliderNavButtonWidth);
            var sliderHeight = hasPlotBox ? instance.slider.css('height') : '12';

            instance.slider.css('width', sliderWidth);
            instance.slider.css('left', hasPlotBox ? (instance.chart.plotLeft + instance.sliderNavButtonWidth) : instance.sliderNavButtonWidth);
            instance.goLeftElm.css('left', hasPlotBox ? instance.chart.plotLeft : 0);
            instance.goRightElm.css('left', hasPlotBox ? instance.chart.plotLeft + sliderWidth + (instance.sliderNavButtonWidth * 1.5)
                                                         : 0 + sliderWidth + (instance.sliderNavButtonWidth * 1.5));

        },
        /**
         * Initialize ui tabs for the widget
         */
        initTabs: function() {
            this.dispatchEvent('onBeforeInitTabs');
            // Build the UI for the tabs

            var periodOrder = [
                ihub.Chart.PERIOD_DAY,
                ihub.Chart.PERIOD_WEEK,
                ihub.Chart.PERIOD_MONTH,
                ihub.Chart.PERIOD_YEAR
            ];

            var instance = this;
            for (var i = 0; i < periodOrder.length; ++i) {
                var per = periodOrder[i];
                if (this.tabs.hasOwnProperty(per)) {
                    this.tabContainer.append(
                            $('<a />')
                            .attr('id', per + 'Tab' + this.id)
                            .addClass('ihub-chart-tab')
                            .click(function() {
                                var period = $(this).attr('id').replace(/Tab/, '');
                                var find = instance.id;
                                var re = new RegExp(find, 'g');
                                period = period.replace(re, '');
                                instance.setPeriod(period, true);
                            })
                            .html(this.tabs[per].label));
                }
            }
            this.tabContainer.append(this.typeContainer);
            // This starts a loop on the tabs, then immediately exits the
            // loop after operating on the first one.
            
            var myTabs;
        	if( ihub.Chart.chartsOpt[this.refid] ){
        		myTabs = ihub.Chart.chartsOpt[this.refid].tabs;
        	}else{
        		myTabs = ihub.Chart.chartsOpt["defaultChart"].tabs;
        	}
        	
            for (var t in myTabs ) {
                this.highlightTab(t);
                break;
            }

            this.dom = true;
            this.dispatchEvent('onInitTabs');
        },
        
        initChart: function() 
        {
            this.dispatchEvent('onBeforeInitChart');
            // Fetch the data for the chart
			if (this.options.hasOwnProperty("initialPeriod")) 
			{
				this.period = this.options.initialPeriod;
				this.fetchChartData(this.period );
			}else{
				this.fetchChartData('day');
			}
        },
        /**
         * Set the period for the widget
         *
         * @param newPeriod one of ihub.Chart.DAY, WEEK, MONTH, YEAR
         * @param [doUpdate=true] determines whether to update the chart display
         */
        setPeriod: function(newPeriod, doUpdate) 
        {
            this.dispatchEvent('beforeSetPeriod');

            if (typeof doUpdate === "undefined") 
            {
                doUpdate = true;
            }

            if (newPeriod.match(/^day$|^week$|^month$|^year$/)) 
            {
                this.lastPeriod = this.period;
                this.period = newPeriod;

                this.dispatchEvent('onSetPeriod' + newPeriod);
                if (doUpdate) 
                {
                	try{
                		this.update('period');
                	}catch(err)
                	{}
                }
            }

            // If there is a slider, update it to reflect the new period
            if (this.options.slider) 
            {
                this.slider.slider('option', 'step', ihub.Chart.STEP_SIZES[this.period]);
            }

            // Update controls on the chart (if any)
            this.updateControls();
            this.dispatchEvent('afterSetPeriod');
        },
        updateControls: function() 
        {
            var container = $(this.controlsContainer);
            switch (this.refid) 
            {
                case 'controlsAggregate':
                    if (this.controlID.length) 
                    {
                        if (this.period.match(/month|year/)) 
                        {
                            container.find('#displayTypeSelectionColumn').prop('checked', true);
                            container.find('#displayTypeContainer').show();
                            container.show();
                        } else {
                            container.find('#displayTypeSelectionLine').prop('checked', 'true');
                            container.find('#displayTypeContainer').hide();
                        }
                        container.find('#displayTypeContainer').buttonset('refresh');
                    } else {
                        container.find('#displayTypeSelectionLine').prop('checked', true);
                        container.find('#displayTypeContainer').hide();
                    }

                    break;

                default:  // breadcrumb
                    if (this.period.match(/day|week/)) 
                    {
                        this.controlsContainer.find('#usageTypeContainer').hide();
                        this.controlsContainer.find('#usageTypeKWH').prop('checked', true);
                    } else {
                        this.controlsContainer.find('#usageTypeContainer').show();
                    }

                    if (this.period.match(/day|week/)) 
                    {
                        this.controlsContainer.find('#displayTypeContainer').hide();
                        this.controlsContainer.find('#usageTypeContainer').hide();
                    } else {
                        this.controlsContainer.find('#displayTypeContainer').show();
                        this.controlsContainer.find('#usageTypeContainer').show();
                    }

                    if (this.period.match(/month|year/)) 
                    {
                        this.controlsContainer.find('#displayTypeSelectionColumn').prop('checked', true);
                    }

                    if (container.find('#resourceSelectionWater').prop('checked') || container.find('#resourceSelectionGas').prop('checked') ) 
                    {
                        container.find('#usageTypeContainer').hide();
                    }

                    container.find('#resourceContainer').buttonset('refresh');
                    container.find('#usageTypeContainer').buttonset('refresh');
                    container.find('#displayTypeContainer').buttonset('refresh');
                    if (this.period != "day") 
                    {
                        $('#openAllCircuits_lightBox').hide();
                    } else {
                        $('#openAllCircuits_lightBox').show();
                    }
                    break;
            }

            if (this.sliderContainer) 
            {
                // update titles on the slider buttons
                this.sliderContainer.find('#ihub-goLeft' + this.id).attr('title', 'Go back one ' + this.period);
                this.sliderContainer.find('#ihub-goRight' + this.id).attr('title', 'Advance one ' + this.period);
            }

            // update tab display when controls are updated for any chart with tabs
            this.highlightTab(this.period);
        },
        /**
         * Highlights a tab, while de-emphasizing the others
         */
        highlightTab: function(period) {
            if (this.tabs) {
                // de-emphasize all tabs
                this.tabContainer.find('a').removeClass('ihub-active-tab').css('background-color', 'rgb(206, 242, 254)').css('color', 'black');
                var currentTab = this.tabContainer.find('#' + this.period + 'Tab' + this.id);

                currentTab.addClass('ihub-active-tab');
                currentTab.css('background-color', '#2975b3').css('color', 'white');
            }
        },
        /**
         * Updates the Highcharts Chart component, tabDisplay, etc
         */
        update: function(type) {
            this.dispatchEvent('onBeforeUpdate');

            if (type == 'period') {
                // update tab ui
                this.tabContainer.find('a').removeClass('ihub-active-tab').css('background-color', 'rgb(206, 242, 254)').css('color', 'black');
                var currentTab = this.tabContainer.find('#' + this.period + 'Tab' + this.id);

                this.tabContainer.find('#' + this.period + 'Tab' + this.id).addClass('ihub-active-tab');
                currentTab.css('background-color', '#2975b3').css('color', 'white');

                if (this.period == 'day' || this.period == 'week') {
                    this.controlsContainer.find('#usageTypeSelectionKWH').prop('checked', true);
                }

                this.fetchChartData(this.period);
                if (this.period == 'day' || this.period == 'week') {
                    this.updateChartType('area');
                } else {
                    this.controlsContainer.find('#displayTypeSelectionColumn').prop('checked', true);
                    this.updateChartType('column');

                }

            } else if (type == 'resource') {
                // Update the chart because a resource was selected
                this.fetchChartData(this.period);
            } else if (type == 'displayType') {
                var newType = this.controlsContainer.find("input[name='displayTypeSelection']:checked").val();
                this.updateChartType(newType);
            } else if (type == 'usageType') {
                this.fetchChartData(this.period);
            } else if (type == 'slider') {
                // TODO - is there any reason for this case?
            }

            this.slider.slider("option", "step", ihub.Chart.STEP_SIZES[this.period]);
            this.dispatchEvent('onUpdate');
        },
        /**
         * Attempts to pull data from cache
         * @output mixed object containing chart options or false, if the data is
         *  not yet cached
         */
        getCachedData: function(period, timestamp) {
            var output = false;
            if (!timestamp) {
                timestamp = "tab";
            }
            if (!this.chartData.hasOwnProperty(this.refid)) {
                this.chartData[this.refid] = {};
                return output;
            }
            var cacheKey = this.getCacheKey(period, timestamp);

            if (this.chartData[this.refid].hasOwnProperty(cacheKey)) {
                return this.chartData[this.refid][cacheKey];
            }

            return output;
        },
 
        /*
         * XHR parameters for the controlsAggregate chart type
         */
        controlsAggregateData: function() {
            var output = {
                
            };
            return output;
        },
        /*
         * XHR parameters for the sensorIconPop chart type
         */
        sensorIconPopData: function() {
            var output = {};
            return output;
        },
 
        /*
         * XHR parameters for the channelGroupDetail chart type
         */
        channelGroupDetailData: function() 
        {
            output.costOrKWH = this.controlsContainer.find("input[name='usageTypeSelection']:checked").val();
            return output;
        },
        otherPowerData: function() {
            return {};
        },
        /*
         * XHR parameters for the totalBreadcrumbs chart type
         */
        defaultChartData: function() {
            var output = {
                resource: "breadcrumb"
            };

            return output;
        },
        totalBreadcrumbsData: function() {
            var output = {
                resource: "breadcrumb"
            };

            return output;
        },
        breadcrumbByTypeStackedData: function() {
        	 var output = {
                 resource: "breadcrumb"
             };

             return output;
        },
        breadcrumbByApplStackedData: function() {
       	 var output = {
                resource: "breadcrumb"
            };

            return output;
       },
       breadcrumbByChannelStackedData: function() {
         	 var output = {
                  resource: "breadcrumb"
              };

              return output;
         },
        /**
         * Determines whether cached data exists.
         * Takes an arbitrary number of arguments that combine to make the cachekey for data
         * @return boolean true if cached data exists, false otherwise
         */
        hasCached: function(period, timestamp) {
            var cache = this.chartData;
            var cacheKey = this.getCacheKey(period, timestamp);

            if (!cache.hasOwnProperty(this.refid)) {
                return false;
            }
            return cache[this.refid].hasOwnProperty(cacheKey);
        },
        getCacheKey: function(period, timestamp) {
            output = "" + period + timestamp;
            switch (this.refid) {

                case 'circuitsDetail':
                case 'channelGroupDetail':
                    output = "" + period + timestamp;
                    var usageType = this.controlsContainer.find("input[name='usageTypeSelection']:checked").val();
                    if (!usageType) {
                        usageType = 'kwh';
                    }
                    output += usageType;
                    break;
                case 'controlsAggregate':
                case 'sensorViewPop':
                case 'sensorIconPop':

                case 'otherPower':
                    output = "" + period + timestamp;
                    break;
            	
                default:     // breadcrumb
                    output = "" + period + timestamp;
                    var resource = this.controlsContainer.find("input[name='resourceSelection']:checked").val();
                    if (!resource) {
                        resource = "electricity";
                    }
                    var usageType = this.controlsContainer.find("input[name='usageTypeSelection']:checked").val();
                    if (!usageType) {
                        usageType = "kwh";
                    }
                    output += resource + usageType;
                    break;
            }
            return output;
        },
        // Add an entry to the cached contents for the chart
        // Takes an arbitrary number of arguments, to allow more
        // depth for some charts, less for others.
        addToCache: function(data, period, timestamp) {
            var cache = this.chartData;

            if (!cache.hasOwnProperty(this.refid)) {
                cache[this.refid] = {};
            }

            var cacheKey = this.getCacheKey(period, timestamp);
            // save the data
            cache[this.refid][cacheKey] = data;

            // Write over the cache with the new data appended
            this.chartData = cache;
        },
        // Data provider function
        fetchChartData: function(period, timestamp) 
        {
            var cachedData = this.getCachedData(period, timestamp);

            this.saveViewOptions();
			
			this.showLoading('Loading...');
			
            if (cachedData) {
                this.initChartOptions(cachedData);
                this.setStatus(cachedData.referenceDate);
                this.hideLoading();
                return;
            }

            var d = new Date();
            
            var data;
            if( this[this.refid + "Data"] )
            	data = this[this.refid + "Data"]();
            else
            	data = this["defaultChartData"]();
            // Settings always included in the XHR's from charts
            data.period    = period;
            data.timestamp = timestamp ? timestamp / 1000 : '';
            data.refid     = this.refid;
            data.timezoneOffset = d.getTimezoneOffset();
            
            if (this.groupByOptions.hasOwnProperty( period )) 
            {
            	data.groupPeriod = this.groupByOptions[ period ];
            }
                 
            data.action = 'getSeriesForChart';

            // Call into the XHR controller to get the data we need.
            var instance = this;
            setTimeout(function() {
				$.ajax({
					url: ihub.Chart.XHR_CONTROLLER,
					type: 'POST',
					beforeSend: function() {
						//instance.showLoading('Loading....');
					},
					data: data,
					fail:
						function(response) 
						{ 
								alert ("error"); 
						},
					success: function(response) {

						var dataToCache = {};

						if (response._return.seriesData.hasOwnProperty('categories')) 
						{
							dataToCache = response._return.seriesData.series;
						} else {
							dataToCache = response._return.seriesData;
						}
						// Cache series data
						timestamp = timestamp ? timestamp : 'tab';
						// Refresh the chart with the new data
						instance.initChartOptions(dataToCache);


						instance.lastTimestamp = timestamp;

						// Set the date range status display in the upper right of the chart
						if (response._return.hasOwnProperty('referenceDate')) 
						{
							instance.setStatus(response._return.referenceDate);
							dataToCache.referenceDate = response._return.referenceDate;
						}
						
						if (response._return.hasOwnProperty('sliderBounds')) 
						{
							if( response._return.sliderBounds.hasOwnProperty('max'))
								instance.mySliderMax = response._return.sliderBounds.max;
							if( response._return.sliderBounds.hasOwnProperty('min'))
								instance.mySliderMin = response._return.sliderBounds.min;
						}

						instance.addToCache(dataToCache, period, timestamp);

						instance.restoreViewOptions();
						instance.hideLoading();

					},
					async: false,
					dataType: 'json'
				});
			}, 250);
        },
        showLoading: function(text) {
            if (this.chart) {
				// copies positioning properties into the loading div
				var elm = this.loadingDiv;
				elm.css('top', this.chartContainer.css('top'));
				elm.css('left', this.chartContainer.css('left'));
				elm.css('width', this.chartContainer.css('width'));
				elm.css('height', this.chartContainer.css('height'));
				this.chartContainer.prepend(this.loadingDiv);
				elm.html(text);
				elm.show();
            }
        },
        hideLoading: function() {
            if (this.chart) {
                this.loadingDiv.hide();
            }
        },
        /**
         * Sets up the chart and re-instantiates based on
         * series data received
         *
         * @param data array[JSONObject] an array of series data
         */
        initChartOptions: function(data) {

            var instance = this;
            var newOptions = {
                chart: {
                    zoomType: 'x',
                    marginTop: 50,
                    spacingTop: 0,
                    spacingLeft: 10,
                    spacingRight: 20,
                    height: 400,
                    borderWidth: 0,
                    borderColor: '#787c7d',
                    borderRadius: 0,
                    plotBackgroundColor: '#f1f9ff',
                    plotBorderWidth: 0,
                    backgroundColor: {
                        linearGradient: [0, 300, 0, 0], // top to bottom, 300px height
                        stops: [
                            [0, 'rgb(255, 255, 255)'], // light
                            [1, 'rgb(206, 242, 254)']     // dark
                        ]
                    }
                },
                title: {
                    text: null
                },
                subtitle: {
                    text: null
                },
                // Brian
                // https://gist.github.com/j0nes2k/7316272
                xAxis: {
                    type: 'datetime',
                    maxZoom: 1 * 3600 * 1000, // 2 hours
//                    dateTimeLabelFormats: {// don't display the dummy year
//                        second: '%l:%M%P',
//                        minute: '%l:%M%P',
//                        hour: '%l:%M%P',
//                        day: '%b %e',
//                        week: '%b %e',
//                        month: '%b %e',
//                        year: '%b'
//                    },
                    title: {
                        text: null
                    },
                    labels: {
                        align: 'center',
                        y: 15,
                        enabled: true,
                        style: {
                        	fontSize: '0.8em'
                        },
                        formatter: function()
                        {
                        	          
							//    var utcOffset = 0;
							//	// utcOffset should be set globally to whatever timezone you want to show
							//	if (self.options.utcOffset) {
							//		utcOffset = self.options.utcOffset * 60;
							//	}
							//    
							//	if (utcOffset > 0) {
							//		datetime = datetime.add('minutes', Math.abs(utcOffset));
							//	}
							//	else 
							//	{
							//		datetime = datetime.subtract('minutes', utcOffset);
							//    }
							var time = moment(this.value).tz(instance.timezoneName).format('h:mm a');
							if( time == "12:00 am")
								time = moment(this.value).tz(instance.timezoneName).format('MMM DD');
						    return time;
                        }
                    },
//                    labels: {
//                        enabled: true,
//                        style: {
//                            fontSize: '0.8em'
//                        }
//                    },
                    lineColor: '#444',
                    tickPixelInterval: 75,
                    gridLineDashStyle: 'ShortDash',
                    minorGridLineDashStyle: 'ShortDash',
                    gridLineWidth: 1
                },
                yAxis: []
            };

            // Default tick interval (minutes);
            var minTick = 60 * 1000;
            switch (this.period) {
                case 'day':
                    minTick = 60 * 1000;
                case 'week':
                    minTick = 12 * 60 * 60 * 1000;
                case 'month':
                    minTick = 24 * 60 * 60 * 1000;
                    break;
                case 'year':
                    minTick = 30 * 24 * 60 * 60 * 1000;
                    break;
            }
            // Set the minimum tick size, to keep from showing inappropriate
            // time ticks on the X Axis
//            newOptions.xAxis.minTickInterval = minTick;

            var instance = this;
            var globalOptions = {
                tooltip: {
                    formatter: function() 
                    {
                    	var timeStamp
                    	
                    	var periodLength = this.series.xData[this.series.xData.length - 1] - this.series.xData[this.series.xData.length - 2];
                    	switch (periodLength) {
                    	
	                    	case 60000: // min period
	                			timeStamp = Highcharts.dateFormat('%l:%M%P', this.x);
	                			break;
	                    	case 3600000: // hour period
	                			timeStamp = Highcharts.dateFormat('%b %e, %l:%M%P', this.x);
	                			break;
                    		case 86400000: // day period
                    			timeStamp = Highcharts.dateFormat('%b %e', this.x);
                    			break;
                    		default:
                    			timeStamp = Highcharts.dateFormat('%b %Y', this.x);
                				break;

                    	}
                   		//timeStamp = Highcharts.dateFormat('%b %e, %l:%M%P', this.x);

//                        if (this.series.xData[this.series.xData.length - 1] - this.series.xData[0] <= 24 * 60 * 60 * 1000) {
//                            // avoid having yesterday's series using today's timestamp:
//                            timeStamp = Highcharts.dateFormat('%l:%M%P', this.x);
//                        } else if (this.series.yAxis.options.unit === 'dd') {
//                            timeStamp = Highcharts.dateFormat('%b %e', this.x);
//                        }

                        var precision = instance.getYPrecision(this.y);

                        var prependStr = '';

                        var unit = '';
                        if (this.series.yAxis.options.hasOwnProperty('unit')) {
                            if (this.series.yAxis.options.unit == '$') {
                                prependStr = '$';
                                unit = '';
                            } else {
                                unit = this.series.yAxis.options.unit;
                            }
                        }


                        return '<b>' + timeStamp + '</b><br/>' +
                                 prependStr + this.series.name  + " : "+ Highcharts.numberFormat(this.y, precision);
                    }
                },
                legend: {
                    enabled: true,
                    align: 'left',
                    floating: true,
                    borderWidth: 0,
                    itemStyle: {
                        cursor: 'pointer',
                        color: 'black',
                        fontSize: '11px',
                        textDecoration: 'underline'
                    },
                    verticalAlign: 'top'
                },
                credits: {
                    enabled: false
                },
                plotOptions: {
                    series: {
                        fillOpacity: 0.01
                    },
                    line: {
                        connectNulls: false,
                        shadow: false,
                        lineWidth: 1,
                        marker: {
                            enabled: false,
                            states: {
                                hover: {
                                    enabled: true,
                                    radius: 5
                                }
                            }
                        }
                    },
                    column: {
                        cursor: 'pointer',
                        stacking: 'normal',
                        point: {
                            events: {
                                click: function(e) {
                                    if (!instance.period.match(/month|year/)) {
                                        return;
                                    }

                                    // Fetch drilldown data
                                    var point = e.point;
                                    var fetchPeriod = 'day';
                                    var chartType = 'area';
                                    if (instance.period == 'year') {
                                        fetchPeriod = 'month';
                                        chartType = 'column';
                                    }
                                    instance.setPeriod(fetchPeriod, false);
                                    instance.fetchChartData(fetchPeriod, point.x);
                                    instance.updateChartType(chartType);

                                }
                            }
                        }
                    },
                    spline: {
                        shadow: false,
                        lineWidth: 2,
                        connectNulls: true,
                        marker: {
                            enabled: false,
                            states: {
                                hover: {
                                    enabled: true,
                                    radius: 5
                                }
                            }
                        }
                    },
                    area: {
                        lineWidth: 1,
                        connectNulls: true,
                        marker: {
                            enabled: false,
                            states: {
                                hover: {
                                    enabled: true,
                                    radius: 5
                                }
                            }
                        },
                        shadow: false,
                        states: {
                            hover: {
                                lineWidth: 2
                            }
                        }
                    },
                    areaspline: {
                        lineWidth: 1,
                        connectNulls: true,
                        marker: {
                            enabled: false,
                            states: {
                                hover: {
                                    enabled: true,
                                    radius: 5
                                }
                            }
                        },
                        shadow: false,
                        states: {
                            hover: {
                                lineWidth: 2
                            }
                        }
                    }
                }
            };


            $('body').css('cursor', 'auto');
            var oldSeries = [];
            var yMin = Number.MAX_VALUE;
            var totalNameLength = 0;

            for (var i = 0; i < data.length; i++) {
                if (data[i].yMin) {
                    yMin = Math.min(yMin, data[i].yMin);
                } else {
                    yMin = 1;
                }
                totalNameLength += (data[i].name.length + 4); // 4 for the colored dash and spaces around
            }


            this.globalOptions = $.extend(true, globalOptions, newOptions);
            this.chartOptions = $.extend(true, this.chartOptions, globalOptions);


            this.chartOptions.series = data;
            // Height
            var height = 0;
            var fill = false;
            switch (this.refid) {

                default:    // breadcrumb
                	height = 350;
                    fill = true;
                    break;            
            }


            if (height) {
                this.chartOptions.chart.height = height;
            }

            if (fill) {
                this.chartOptions.plotOptions.series.fillOpacity = 0.4;
            }

            if (data[0].xMax) {
                this.chartOptions.xAxis.max = data[0].xMax;
            } else {
                this.chartOptions.xAxis.max = null;
            }

            if (data[0].xMin) {
                this.chartOptions.xAxis.min = data[0].xMin;
            } else {
                this.chartOptions.xAxis.min = null;
            }

            var legendRows = Math.ceil(totalNameLength * 7 / ($(this.chartContainer).width() - 20)); // 20 for LR margin
            this.chartOptions.chart.marginTop = legendRows * 16 + 10;

            this.chartOptions.yAxis = [];
            for (var i = 0; i < 1; i++) 
            {
            	var yaxis = ihub.Chart.yAxis;
            	
            	if( this.chartOptions.series[i].hasOwnProperty("unit"))
            	{
            		yaxis.title.text = this.chartOptions.series[i].unit;
            	}
            	this.chartOptions.yAxis.push( yaxis);
            }
            
            // Depending on the chart, there are different numbers of yAxes
            // initialize yAxes for the chart
            for (var i = 0; i < this.chartOptions.series.length; i++) 
            {
                if (!this.chartOptions.series[i].yAxis) {
                    break;
                }
                var yAxisNumber = this.chartOptions.series[i].yAxis;

                if (this.chartOptions.yAxis[yAxisNumber].hasOwnProperty('gridLineColor')) {
                    if (fill) {
                        this.chartOptions.yAxis[yAxisNumber].gridLineColor = '#A0A0A0';
                    } else {
                        this.chartOptions.yAxis[yAxisNumber].gridLineColor = '#C0C0C0';
                    }

                    if (yMin != Number.MAX_VALUE &&
                            (typeof this.chartOptions.yAxis[yAxisNumber].min == 'undefined' ||
                                    yMin - 1 > this.chartOptions.yAxis[yAxisNumber].min)) {
                        this.chartOptions.yAxis[yAxisNumber].min = yMin - 1;
                    }
                }
            }

            this.chartOptions.chart.renderTo = this.chartContainer.attr('id');
            this.chart = new Highcharts.Chart(this.chartOptions);

            switch (this.refid) {

                case 'controlsAggregate':
                    if (this.controlID.length
                        && this.period.match(/month|year/)
                    ) {
                        this.updateChartType('column');
                    }
                    break;
                default:    // breadcrumb
                    if (this.period.match(/month|year/)) {
                        this.updateChartType('column');
                    }
                    break;            }

            // RM #2644 - Move slider sizing code here to ensure chart is already rendered
            // and as a result give the slider all kinds of heartburn.
            // Give the browser 100 milliseconds to render things and such before attempting.
            var instance = this;
            setTimeout(function() {
                    instance.sizeSliderElements(instance);
                }, 100);

        },
        initControlEvents: function() {
            var container = this.controlsContainer;
            var instance = this;
            switch (this.refid) {
               default:      // breadcrumb
//                    container.find('#resourceSelectionElectricity').prop('checked', true);
//                    container.find("input[name='resourceSelection']").change(
//                            function() {
//                                container.find('#resourceSelection').buttonset('refresh');
//                                instance.update('resource');
//                            });
//
//                    container.find('#usageTypeSelectionKWH').prop('checked', true);
//                    container.find("input[name='usageTypeSelection']").change(
//                            function() {
//                                instance.update('usageType');
//                                container.find('#usageTypeSelection').buttonset('refresh');
//                            });
//
//                    container.find('#displayTypeSelectionLine').prop('checked', true);
//                    container.find("input[name='displayTypeSelection']").change(
//                            function() {
//                                instance.update('displayType');
//                                container.find('#displayTypeSelection').buttonset('refresh');
//                            });
//
//                    container.find('#resourceContainer').buttonset();
//                    container.find('#usageTypeContainer').buttonset();
//                    container.find('#displayTypeContainer').buttonset();
                    this.updateControls();
                    break;
            }
        },
        getYPrecision: function(val) {
            var v = Math.abs(val) + "";
            if (v.indexOf('.') != -1) {
                return v.split('.')[1].length;
            } else {
                return 0;
            }
        },
        sliderTooltip: function(event, ui, instance) {
            if (!ui.value) {
                return;
            }

            var dt = new Date(ui.value);
            var dateStr = instance.formatTooltipDate(dt);
            var periodStr = "";
            switch (instance.period) {
                case "day":
                    periodStr = "Day";
                    break;
                case "week":
                    var toDate = new Date(ui.value - (7 * ihub.Chart.STEP_SIZES['day']));
                    dateStr = instance.formatTooltipDate(toDate) + " to " + dateStr;
                    periodStr = "Week";
                    break;
                case "month":
                    var toDate = new Date(ui.value + ihub.Chart.STEP_SIZES['month']);
                    // Just show the month
                    dateStr = ihub.Chart.MONTHS[toDate.getMonth()] + " " +
                        dt.getFullYear();
                    periodStr = "Month";
                    break;
                case "year":
                    var toDate = new Date(ui.value + ihub.Chart.STEP_SIZES['year']);
                    periodStr = "Year";
                    break;
            }

            var ttText = "<div class='ihub-slider-tooltip'>" + dateStr + "<br /><strong>" + "(" + periodStr + " View)</strong></div>";
            instance.sliderContainer.find('.ui-slider-handle').html(ttText);
            instance.sliderContainer.find('.ihub-slider-tooltip').show();
            instance.sliderContainer.find('.ihub-slider-tooltip').hide('fade', 1500);
        },
        formatTooltipDate: function(dt) {
            var output = ihub.Chart.DAYS[dt.getDay()] + " " +
                    ihub.Chart.MONTHS[dt.getMonth()].substring(0, 3) + " " +
                    dt.getDate() + ", " +
                    dt.getFullYear();
            return output;
        },
        
        
        /**
         * Sets the status text to the right of the tabs if present,
         * in the top bar if no tabs are present
         */
        setStatus: function(str) 
        {
            this.statusContainer.html(str);
            var instance = this;
            this.sizeInterval = null;

            this.sizeInterval = setInterval(
	            function() 
	            {
	                instance.autofitStatusText();
	            }, '100'
	        );
        },
        /**
         * Uses an autoshrink hack to make sure the status text fits without despoiling
         * our pretty layout.
         */
        autofitStatusText: function() 
        {
            if (!this.container) 
            {
                return;
            }

            var tabWidth = 0;
            if (this.tabs) 
            {
                for (var t in this.tabs) 
                {
                    var tab = this.tabContainer.find($('#' + t + 'Tab'));
                    var tw = parseInt(tab.css('width'))
                            + parseInt(tab.css('padding-left'))
                            + parseInt(tab.css('padding-right'));
                    tabWidth += tw;
                }
            }

            // Set the desired width of the status container

            var desiredWidth = parseInt(this.container.css('width')) - tabWidth - 5;


            var size;
            var c = this.statusContainer;
            while (parseInt(c.css('width')) > desiredWidth) 
            {
                if (parseInt(c.css('width')) == 0) 
                {
                    break;
                }
                size = parseInt(c.css('font-size'));
                c.css('font-size', size - 1);
            }
            // If we are not using all our available space, we should.
            if (parseInt(c.css('width') < desiredWidth)) 
            {
                c.css('width', desiredWidth);
            }
            this.sizeInterval = null;
            this.statusContainer.show();
        },
        controlsAggregateFormatSeriesData: function(data) 
        {
            for (var i = 0; i < data.length; i++) {
                var series = data[i];

                if (series.name == 'Room Temperature') {
                    series.type = "areaspline";
                }
            }
        },
        saveViewOptions: function() 
        {
            var vopts = {};
            var chart = this.chart;

            if (chart == null) 
            {
                return;
            }
            // series being displayed
            vopts.visibleSeries = {};

            for (var i = 0; i < chart.series.length; ++i) 
            {
                var s = chart.series[i];
                var obj = {
                    name    : s.name,
                    visible : s.visible,
                    type    : s.type
                };
                vopts.visibleSeries[s.name] = obj;
            }

            // store the period, so that we can apply the line/chart options appropriately
            vopts.period = this.period;

            this.viewOptions = vopts;
        },
        restoreViewOptions: function() 
        {
            if (this.viewOptions == null) {
                return;
            }
            var chart = this.chart;
            var opts = this.viewOptions;

            // series being displayed
            for (var i = 0; i < chart.series.length; ++i) 
            {
                var s = chart.series[i];

                if (opts.visibleSeries.hasOwnProperty(s.name)) 
                {
                    // update the series properties from saved options
                    var saved = opts.visibleSeries[s.name];
                    if (saved.visible) 
                    {
                        chart.series[i].show();
                    } else {
                        chart.series[i].hide();
                    }
                    if (opts.period == this.period) 
                    {
                        s.update({
                            type: saved.type
                        });
                    }
                }
            }
        }
    };


    var output = new cls(refid, options, timezone);
    output.init();
    return output;
};

// Date formatting lookups
ihub.Chart.MONTHS = [
    'January', 'February', 'March', 'April',
    'May', 'June', 'July', 'August',
    'September', 'October', 'November', 'December'
];

ihub.Chart.DAYS = [
    'Sunday', 'Monday', 'Tuesday', 'Wednesday',
    'Thursday', 'Friday', 'Saturday'
];

// Chart titles
ihub.Chart.chartTitle = {
    'day'  : 'Minute by Minute View for Today',
    'week' : 'Hourly View for the Past Week',
    'month': 'Hourly View for the Past Month',
    'year' : 'Hourly View for the Past Year'
};


// Server-side XHR controller location
ihub.Chart.XHR_CONTROLLER = "GetChartData.jsp";

// Chart static members
ihub.Chart.PERIOD_MINUTE = "minute";
ihub.Chart.PERIOD_HOUR = "hour";
ihub.Chart.PERIOD_DAY = "day";
ihub.Chart.PERIOD_WEEK = "week";
ihub.Chart.PERIOD_MONTH = "month";
ihub.Chart.PERIOD_YEAR = "year";


// Step sizes based on period
ihub.Chart.STEP_SIZES = {
    'day'  : 24 * 60 * 60 * 1000,
    'week' : 7 * 86400 * 1000,
    'month': 30 * 86400 * 1000,
    'year' : (365 * 86400) * 1000
};

ihub.Chart.chartsOpt = {
		
	defaultChart: {
        tabs: {
            day: {label: 'Day'},
            week: {label: 'Week'},
            month: {label: 'Month'},
            year: {label: 'Year'}
        }
    },
	totalBreadcrumbs: {
        tabs: {
            day: {label: 'Day'},
            week: {label: 'Week'},
            month: {label: 'Month'},
            year: {label: 'Year'}
        }
    },
    breadcrumbByTypeStacked: {
        tabs: {
            day: {label: 'Day'},
            week: {label: 'Week'},
            month: {label: 'Month'},
            year: {label: 'Year'}
        }
    },
    breadcrumbByChannelStacked: {
        tabs: {
            day: {label: 'Day'},
            week: {label: 'Week'},
            month: {label: 'Month'},
            year: {label: 'Year'}
        }
    },
    breadcrumbByApplStacked: {
        tabs: {
            day: {label: 'Day'},
            week: {label: 'Week'},
            month: {label: 'Month'},
            year: {label: 'Year'}
        }
    }
};


// Configured yAxis settings
ihub.Chart.yAxis =
           {/***** 0. Breadcrumbs *****/
                title: {
                    text: null
                },
                labels: {
                    enabled: true,
                    style: {
                        fontSize: '0.8em'
                    },
                
                    formatter: function() {
                        var precision = ihub.getYPrecision(this.value);
                        return Highcharts.numberFormat(this.value, precision) + "";
                    }
                },
                plotOptions: {
                    series: {
                        stacking: 'normal'
                    }
                },
                lineColor: '#444',
                lineWidth: 1,
                unit: 'Breadcrumbs created:',
                min: 0,
                //startOnTick: false,
                tickPixelInterval: 50,
                showFirstLabel: false,
                minorTickInterval: null,
                minorGridLineDashStyle: 'ShortDash',
                gridLineDashStyle: 'ShortDash'
            };

