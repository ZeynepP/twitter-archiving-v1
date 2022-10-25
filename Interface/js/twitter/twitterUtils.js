/**
 * http://usejsdoc.org/
 * zpehlivan@ina.fr
 */
 
$.fn.serializeObject = function()
{
   var o = {};
   var a = this.serializeArray();

   $.each(a, function() {
       if (o[this.name]) {
           if (!o[this.name].push) {
				
               o[this.name] = [o[this.name]];
           }
			// je ne veux pas des arrays mais string pour collections
			if(this.name=="collection")	
			{
					   o[this.name] =  o[this.name] + ";" + this.value;

			}
			else
			{
				o[this.name].push(this.value || '');
			}

       } else {
           o[this.name] = this.value || '';
			
       }
   });
   return o;
};


$.fn.dataTable.Api.register( 'column().title()', function () {
    var colheader = this.header();
    return $(colheader).text().trim();
} );

////// NEWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW 


function dashboardStats()
{
	 $('[id^="container_dashboard_"]').hide();
	 dashboardClick("retweet", "dashboard", "Distribution des retweets");
	 dashboardClick("lang", "dashboard","Distribution par langage");
	 dashboardClick("place", "dashboard","Distribution par pays");
	 dashboardClick("city", "dashboard","Distribution par ville");
	
}

function dashboardClick(type, timelinetype,title)
{
	//$('[id^="container_dashboard_"]').hide();

	$("#container_dashboard_" + type).show();
	var dchart = new timeline("#container_dashboard_" + type, timelinetype,title);
	dchart.init();
	dchart.update(false, type);
	
	
}

function createPromiseRequest(url, data, dtype)
{
	data["index"] = index;
	console.log(index);
	var datajson = JSON.stringify(data)
	
	
	return	$.ajax({
				url: url,
				contentType: "application/json; charset=utf-8",
				dataType: dtype,
				type: "POST",
				data: datajson
				
			//	success : function() {console.log("ajax ok");},
			//	error : function(test, textStatus, errorThrown) {throw errorThrown;},
				
			});
						
}

function displayError()
{
	error= "Une erreur s'est produite ! Veuillez réessayer ultérieurement ";
	$("#resultsbanner").html(error);
	$("#searchform :input").prop("disabled", false);
	$("#all").removeClass("diary");  
}




function isIframe() {
			try {
				return window.self !== window.top;
			} catch (e) {
				return true;
			}
}



function cleanPage()
{
	
		
		$( "#content" ).hide();
		$('[id^="container_"]').empty();
		$('[id^="container_dashboard_"]').hide();

		analyzeDataTable.table.rows().remove().draw( );

		return false;
	
}
function numberWithSpaces(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

function updateHitsText(div, time, total )
{
	totalresults = total;
	var data = StateManager.getState()["query"];
	var text = "";
	if(total == 0)
	{
		text+= "Aucun résultat  " 
	}
	else if(total == null)
	{
		text+= "Chargement des données " 
	}
	else
	{
		text+= numberWithSpaces(total) + " résultats ";
	}
	

	if( data["query"] != "")
	{
		text+= " pour " + data["query"].bold();
	}
	text+= " parmi les tweets archivés" ;

	text+= 	dateText(data);
	

	if(data["hashtags"] != "")
	{
		text+= " pour le(s) hashtag(s) : " + data["hashtags"].bold();
	}
	if(data["mentions"] != "")
	{
		text+= " pour le(s) mention(s) : " + data["mentions"].bold();
	}
	if(data["users"] != "")
	{
		text+= " pour le(s) utilisateur(s) : " + data["users"].bold() ;
	}
	if(data["retweet"] != undefined)
	{
		if(data["retweet"] == -1)
		{
			text+= " dans tous les tweets   " ;
		}
		else if(data["retweet"] == 0)
		{
			text+= " sans les retweets   " ;
		}
		else
		{
			text+= " dans les retweets   " ;
		}
	}
	
	if(data["quote"] != undefined)
	{
		if(data["quote"] == -1)
		{
			text+= " dans toutes les citations   " ;
		}
		else if(data["quote"] == 0)
		{
			text+= " sans les citations   " ;
		}
		else
		{
			text+= " dans les citations   " ;
		}
	}
	var d = new Date();
	if(data["collection"]!= undefined)
	{
		text+= " dans les collections " + data["collection"].replace(";"," et ");
	}
	text+= " (" + numberWithSpaces(time/1000) + " secondes)";
			
	
	$(div).html(text);
	

}


function dateText(data)
{
	var text = "";
	if(data["from_date"] == '' && data["to_date"] == '')
	{
		text+= " à toutes les dates";
	}
	else if(data["from_date"] != '' && data["to_date"] != '')
	{
		datetext = " entre le " + (data["from_date"] + " 00:00:00").bold()
		console.log(data["date_start"]);
		if( data["date_start"]!=undefined)
		{
			var createdat = new Date( data["date_start"] * 1);
			// createdat = createdat.toISOString().slice(0,10).replace("T"," ") + " " + createdat.toLocaleTimeString();
			createdat = createdat.getFullYear() + "-" +  ('0' + (createdat.getMonth()+1)).slice(-2) + "-" + ('0' + createdat.getDate()).slice(-2)  + " " + createdat.toLocaleTimeString();
			datetext = " entre le " + createdat.bold();
			
		}
		text+= datetext;

		datetext = " et le " + (data["to_date"] + " 23:59:59").bold()
		if( data["date_stop"]!=undefined)
		{
			var createdat = new Date( data["date_stop"] * 1);
			// createdat = createdat.toISOString().slice(0,10).replace("T"," ") + " " + createdat.toLocaleTimeString();
			createdat = createdat.getFullYear() + "-" +  ('0' + (createdat.getMonth()+1)).slice(-2) + "-" + ('0' + createdat.getDate()).slice(-2)  + " " + createdat.toLocaleTimeString();
			datetext = " et le " + createdat.bold()
			
		}
		text+= datetext;
	}
	else if(data["from_date"] == '' && data["to_date"] != '')
	{
	    datetext = " jusqu'au " + (data["to_date"] + " 23:59:59").bold()
		if( data["date_stop"]!=undefined)
		{
			var createdat = new Date( data["date_stop"] * 1);
			// createdat = createdat.toISOString().slice(0,10).replace("T"," ") + " " + createdat.toLocaleTimeString();
			createdat = createdat.getFullYear() + "-" +  ('0' + (createdat.getMonth()+1)).slice(-2) + "-" + ('0' + createdat.getDate()).slice(-2)  + " " + createdat.toLocaleTimeString();
			datetext = " jusqu'au " + createdat.bold()
			
		}
		text+= datetext;
	}
	else if(data["from_date"] != '' && data["to_date"] == '')
		{
			datetext = " since le " + (data["from_date"] + " 00:00:00").bold()
			console.log(data["date_start"]);
			if( data["date_start"]!=undefined)
			{
				var createdat = new Date( data["date_start"] * 1);
				// createdat = createdat.toISOString().slice(0,10).replace("T"," ") + " " + createdat.toLocaleTimeString();
				createdat = createdat.getFullYear() + "-" +  ('0' + (createdat.getMonth()+1)).slice(-2) + "-" + ('0' + createdat.getDate()).slice(-2)  + " " + createdat.toLocaleTimeString();
				datetext = " since le " + createdat.bold();
				
			}
			text+= datetext;
		}
	return text;
	


}

function loadTabs(index)
{
	console.log("Activated tab", index);
	if( index== 1) // analyzes 
	{
		updateAnalyzes();
	} 
	else if(index == 2) // dashboard 
	{
		dashboardStats();
		
	} 
	 else if(index == 3) // wordcount 
	{
			if(totalresults < 80000000)
			{
				if(wordcloudDataTable.table)
					wordcloudDataTable.update();
				else 
					wordcloudDataTable.init();
			
				
			}
			else
			{
				wordcloudDataTable.clean();
			}
	} 
}

function updateSearchbyInterval(timelinechart)
{
	
	var extremes = timelinechart.xAxis[0].getExtremes();
	var max = Math.round(extremes.max);
	var min = Math.round(extremes.min);
	
	var state = StateManager.getState()
	var query = state["query"];
	var timestat = {"date_start" : min, "date_stop": max}
	query["date_start"] = min
	query["date_stop"] = max
	
	var time = new Date(min);
	$("#from_date").datepicker("setDate", time);
	query["from_date"] = $("#from_date").val();
	
	time = new Date(max);
	$("#to_date").datepicker("setDate", time);
	query["to_date"] = $("#to_date").val();
	
	
	StateManager.setState({"query" : query}, false);
	console.log("set state true updateSearchbyInterval")

	return false;
}

////// NEWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW 


/* ========================================================================
 * Bootstrap: collapse.js v3.1.1
 * http://getbootstrap.com/javascript/#collapse
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */


+function ($) {
  'use strict';

  // COLLAPSE PUBLIC CLASS DEFINITION
  // ================================

  var Collapse = function (element, options) {
    this.$element      = $(element)
    this.options       = $.extend({}, Collapse.DEFAULTS, options)
    this.transitioning = null

    if (this.options.parent) this.$parent = $(this.options.parent)
    if (this.options.toggle) this.toggle()
  }

  Collapse.DEFAULTS = {
    toggle: true
  }

  Collapse.prototype.dimension = function () {
    var hasWidth = this.$element.hasClass('width')
    return hasWidth ? 'width' : 'height'
  }

  Collapse.prototype.show = function () {
    if (this.transitioning || this.$element.hasClass('in')) return

    var startEvent = $.Event('show.bs.collapse')
    this.$element.trigger(startEvent)
    if (startEvent.isDefaultPrevented()) return

    var actives = this.$parent && this.$parent.find('> .panel > .in')

    if (actives && actives.length) {
      var hasData = actives.data('bs.collapse')
      if (hasData && hasData.transitioning) return
      actives.collapse('hide')
      hasData || actives.data('bs.collapse', null)
    }

    var dimension = this.dimension()

    this.$element
      .removeClass('collapse')
      .addClass('collapsing')
      [dimension](0)

    this.transitioning = 1

    var complete = function () {
      this.$element
        .removeClass('collapsing')
        .addClass('collapse in')
        [dimension]('auto')
      this.transitioning = 0
      this.$element.trigger('shown.bs.collapse')
    }

    if (!$.support.transition) return complete.call(this)

    var scrollSize = $.camelCase(['scroll', dimension].join('-'))

    this.$element
      .one($.support.transition.end, $.proxy(complete, this))
      .emulateTransitionEnd(350)
      [dimension](this.$element[0][scrollSize])
  }

  Collapse.prototype.hide = function () {
    if (this.transitioning || !this.$element.hasClass('in')) return

    var startEvent = $.Event('hide.bs.collapse')
    this.$element.trigger(startEvent)
    if (startEvent.isDefaultPrevented()) return

    var dimension = this.dimension()

    this.$element
      [dimension](this.$element[dimension]())
      [0].offsetHeight

    this.$element
      .addClass('collapsing')
      .removeClass('collapse')
      .removeClass('in')

    this.transitioning = 1

    var complete = function () {
      this.transitioning = 0
      this.$element
        .trigger('hidden.bs.collapse')
        .removeClass('collapsing')
        .addClass('collapse')
    }

    if (!$.support.transition) return complete.call(this)

    this.$element
      [dimension](0)
      .one($.support.transition.end, $.proxy(complete, this))
      .emulateTransitionEnd(350)
  }

  Collapse.prototype.toggle = function () {
    this[this.$element.hasClass('in') ? 'hide' : 'show']()
  }


  // COLLAPSE PLUGIN DEFINITION
  // ==========================

  var old = $.fn.collapse

  $.fn.collapse = function (option) {
    return this.each(function () {
      var $this   = $(this)
      var data    = $this.data('bs.collapse')
      var options = $.extend({}, Collapse.DEFAULTS, $this.data(), typeof option == 'object' && option)

      if (!data && options.toggle && option == 'show') option = !option
      if (!data) $this.data('bs.collapse', (data = new Collapse(this, options)))
      if (typeof option == 'string') data[option]()
    })
  }

  $.fn.collapse.Constructor = Collapse


  // COLLAPSE NO CONFLICT
  // ====================

  $.fn.collapse.noConflict = function () {
    $.fn.collapse = old
    return this
  }


  // COLLAPSE DATA-API
  // =================

  $(document).on('click.bs.collapse.data-api', '[data-toggle=collapse]', function (e) {
    var $this   = $(this), href
    var target  = $this.attr('data-target')
        || e.preventDefault()
        || (href = $this.attr('href')) && href.replace(/.*(?=#[^\s]+$)/, '') //strip for ie7
    var $target = $(target)
    var data    = $target.data('bs.collapse')
    var option  = data ? 'toggle' : $this.data()
    var parent  = $this.attr('data-parent')
    var $parent = parent && $(parent)

    if (!data || !data.transitioning) {
      if ($parent) $parent.find('[data-toggle=collapse][data-parent="' + parent + '"]').not($this).addClass('collapsed')
      $this[$target.hasClass('in') ? 'addClass' : 'removeClass']('collapsed')
    }

    $target.collapse(option)
  })

}(jQuery);
