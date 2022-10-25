
var StateManager = (function(){
	
		var thisStateManager = {	
		
			currentStateJson : null,
			previousStateJson : null,
		    defaultState : {},
			getPreviousState : function() {
						if(this.previousStateJson) {
							try {
								return JSON.parse(this.previousStateJson); 
							} catch(e) {
								console.error(e);
								return null;
							}
						}
						return null;
					},
			
			
			getState : function() {
						if(this.currentStateJson) {
							try {
								return JSON.parse(this.currentStateJson);
							} catch(e) {
								console.error(e);
								return null;
							}
						}
						return null;	
					},
					
			

			getStateFromUrl : function() {
						var hash = decodeURIComponent(window.location.hash);
						
						if(hash) {
							console.log("hash",hash);
							hash = hash.substr("#".length);
						} else {
							hash = null;
						}
						var state = {};
						
						try {
							state =  JSON.parse(hash);
							
						} catch(e) {
						}
							
						var mergedState = $.extend({}, this.defaultState, state);
						// fill form data
/*						
						$.each(mergedState["query"], function(key, value) {
								if(key != "retweet" && key !="quote" && key!="source")
								{
									$("#searchform [name='"+key+"']").val(value);
								}
								else
								{
									$("input[name='"+key+"'][value='" + value+"']").prop("checked",true);
								}

						});
*/						
						
						return mergedState;			
					},
			
			setState : function(state, replace) {
				
				
						
				
						var mergedState = $.extend({}, this.getState(), state);
					
						var newStateJson = JSON.stringify(mergedState);
						
						this.previousStateJson = this.currentStateJson;
						
						this.currentStateJson = newStateJson;
						
						
						console.error("setState", newStateJson, this.currentStateJson, replace);
			
						var url = window.location.protocol+"//"+window.location.host+window.location.pathname;
						if(replace) 
						{	
							if(window.history.pushState) 
								window.history.replaceState(null, null, url+"#"+newStateJson);
						
							
						}
						else if(window.history.pushState) 
							window.history.pushState(null, null, url+"#"+newStateJson);

					
					    if((this.previousStateJson == null))
						{
							$(this).trigger("statechange");		
						}
						// we do not want trigger to work if only analyze is changed
						else 
						{
							var q1  = JSON.stringify(JSON.parse(this.currentStateJson)["query"]);
							var q2  = JSON.stringify(JSON.parse(this.previousStateJson)["query"]);
							console.log(q1, q2)
							//if(q1!= q2)
							if(tabstats[1] != true)// for analyze
							{
								console.log("Different state so trigger");
								
								{$(this).trigger("statechange");		}
							}
							else
							{
								if(q1!= q2)
									{$(this).trigger("statechange");		}
							}
						}
						
						
					//	console.log("setState trigger statechnage");				
					}
		};
		return thisStateManager;
})();



