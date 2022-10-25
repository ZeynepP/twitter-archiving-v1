// Only Firefox
function switchVisibility(elementId) {
	var element = document.getElementById(elementId);
	if(element) {
		var display = element.style.display;
		if(display == "none")
			element.style.display = "inline";
		else
			element.style.display = "none";
	
		var regex = /\#.*$/;
		document.location.href = document.location.href.replace(regex, "")+"#:";
		// change URL to memoize the swith state on history.back
		// add ":" after "#" to avoid scrowlling
	}
}

function reverseDateFormat(dateStr, sep) {
	var reversedDateStr = "";
	var splits = dateStr.split(sep);
	for(var i=splits.length-1; i>=0; i--) {
		reversedDateStr	+= splits[i]+sep;		
	}
	return reversedDateStr.substring(0, reversedDateStr.length - sep.length);
}



function feedHiddenInputs() {
	//$("#hidden_from").attr("value", reverseDateFormat($("#from").attr("value"),"-"));
	//$("#hidden_to").attr("value", reverseDateFormat($("#to").attr("value"),"-"));
}


$(function() {

	$("#hidden_tz").attr("value", jstz.determine().name());

	$.extend($.datepicker, {
	     _doKeyDown: function(event){
	           //copy original source here with different
	           //values and conditions in the switch statement
	     }
	});

	var dates = $( "#from_date, #to_date" ).datepicker({
		monthNames: [ "Janvier", "Fevrier", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" ],
		monthNamesShort: [ "Jan", "Fev", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Dec" ],
		dayNames: [ "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" ],
		dayNamesMin: [ "Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa" ],
		firstDay: 1,
		changeMonth: true,
		changeYear: true,
		// yearRange: '1996:',
		numberOfMonths: 1,
		showAnim: 'slideDown',
		dateFormat: 'dd-mm-yy',

		
		onSelect: function( selectedDate ) {
			
			// apply constraints between the 2 date pickers
			var option = this.id == "from_date" ? "minDate" : "maxDate";
			var instance = $( this ).data( "datepicker" );
			var date = $.datepicker.parseDate(
				instance.settings.dateFormat ||
				$.datepicker._defaults.dateFormat,
				selectedDate, instance.settings
			);
			dates.not( this ).datepicker( "option", option, date );
			
			// Update hidden inputs
			feedHiddenInputs();
		}
	});

	feedHiddenInputs();
	
});


// site input field popup
var pos_set = 0;
$(function() {
	$("#s").focusin(function() {
		
		var relatedPos = $("#s").offset();
		var relatedH = $("#s").outerHeight();
		var relatedW = $("#s").innerWidth();
		if (pos_set == 0) {
			$("#site-popup").offset({top :relatedPos.top + relatedH , left : relatedPos.left });
			pos_set = 1;
		}
		$("#site-popup").width(relatedW-14);
		
		$("#site-popup").show();
	});

	$("#s").focusout(function() {
		$("#site-popup").hide();
	});


	$("#site-popup").hide();
});