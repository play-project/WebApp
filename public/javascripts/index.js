$(document).ready(function() {
	$("#available-topics").accordion({
			autoHeight: false,
			navigation: true,
			collapsible: true
	});
    $("#topic-tabs").sortable({
            distance: 10,
            opacity: 0.7,
            revert: 100,
            scroll: true,
            helper: 'clone',
            zIndex: 50000
	});
	$("#tabs").tabs().addClass('ui-tabs-vertical ui-helper-clearfix');
	$("#tabs li").removeClass('ui-corner-top').addClass('ui-corner-left');
	$(".ui-tabs-vertical .ui-tabs-panel").height($(".ui-tabs-nav").first().height());
	
	$("#error1, #error2, #error3").dialog({
		resizable: false,
		draggable: false,
		autoOpen: false,
		modal: true,
		height: 120,
		buttons: {
			Ok: function() {
				$(this).dialog("close");
			}
		}
	});
	$("#error4").dialog({
		resizable: false,
		draggable: false,
		autoOpen: false,
		modal: true,
		height: 120,
		buttons: {
			Ok: function() {
				location.href = "/";
			}
		}
	});
	
	var subAction = #{jsAction @Application.subscribe(':topicId') /}
	$("a.subscribe-button").live('click', function() {
		var $id = $(this).parent().attr('id');
		var $sid = $id.substring($id.lastIndexOf('-') + 1);
	    $.ajax({
            url: subAction({topicId: $sid}),
            type: 'POST',
	        dataType: 'json',
	        success: function(results) {
	            $(results).each(function() {
		            if(this.id != "-1"){
						$("#available-topics").accordion("destroy");
						$('#topic-'+this.id+'').remove();
						$('#desc-topic-'+this.id+'').remove();
						$("#available-topics").accordion({
							autoHeight: false,
							navigation: true,
							collapsible: true
						});
						$('#tabs').tabs('add','#tab-topic-'+this.id+'','<span id="unread-'+this.id+'" class="unread-count">0</span><span class="subscribed-topic-title">'+this.title+'</span><img src="'+this.icon+'" class="tab-topic-icon">');
						$('#tab-topic-'+this.id).html('<h2>'+this.title+'<a href="#" id="unsubscribe-button-'+this.id+'" class="unsubscribe-button">Unsubscribe</a></h2><div id="topic-events-'+this.id+'"></div>');
					} else {
						$("#error2").dialog("open");
					}
				})
	        },
	        error: function() {
				$("#error1").dialog("open");
				$("#available-topics").accordion( "option", "disabled", false );
	        }
	    });
	});
	
	var unsubAction = #{jsAction @Application.unsubscribe(':topicId') /}
	$("a.unsubscribe-button").live('click', function() {
		var $id = $(this).attr('id');
		var $sid = $id.substring($id.lastIndexOf('-') + 1);
	    $.ajax({
            url: unsubAction({topicId: $sid}),
            type: 'POST',
	        dataType: 'json',
	        success: function(results) {
	            $(results).each(function() {
		            if(this.id != "-1"){
						$("#available-topics")
							.accordion("destroy")
							.append('<h3 id="topic-'+this.id+'"><a href="#"><span class="available-topic-title">'+this.title+'</span><img src="'+this.icon+'" class="available-topic-icon"/></a><a href="#" class="subscribe-button">Sub</a></h3><div id="desc-topic-'+this.id+'"><p>'
									+ this.content + '<br/><br/><span class="topic-tech-desc"><br/><b>Id: </b>'+this.id+'<br/><b>Path: </b>'+this.path+'</span></p></div>')
							.accordion({
									autoHeight: false,
									navigation: true,
									collapsible: true
							});
						$("#tabs").tabs("remove", $("#tabs").tabs("option", "selected"));
					} else {
						$("#error1").dialog("open");
					}
				})
	        },
	        error: function() {
				$("#error3").dialog("open");
	        }
	    });
	});

	var $lastReceived = 0
	var waitEvents = #{jsAction @waitEvents(':lastReceived') /}
	/*
	%{
		String txt = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus ullamcorper laoreet fermentum. Cras placerat dui pharetra arcu ultricies sit amet commodo tortor molestie.";
	}%
	*/
	//var send0 = #{jsAction @sendEvent('Event !', txt, 'internalns_rootTopic1') /}
	//var send1 = #{jsAction @sendEvent('Event !', txt, 'internalns_childTopic1') /}
	//var send2 = #{jsAction @sendEvent('Event !', txt, 'internalns_grandChildTopic21') /}
	//var send3 = #{jsAction @sendEvent('Event !', txt, 'internalns_childTopic3') /}
	
	var sendFbStatusEvent = #{jsAction @WebService.testFacebookStatusFeedEvent() /}
	
	$("#send0").click(function(e) {
		$.post(sendFbStatusEvent())
	});
	/*
	$("#send1").click(function(e) {
		$.post(send1())
	});
	$("#send2").click(function(e) {
		$.post(send2())
	});
	$("#send3").click(function(e) {
		$.post(send3())
	});
	*/
	
	var getEvents = function() {
		$.ajax({
			url: waitEvents({lastReceived: $lastReceived}),
			type: 'GET',
			dataType: 'json',
			success: function(events) {
				if(events.error == "disconnected"){
					$("#error4").dialog("open")
				}
				var k = events.length;
				$(events).each(function() {
					k--;
					display(this, (k == 0));
					$lastReceived = this.id;
				})
				getEvents()
			},
			error: function() {
				getEvents();
			}
		});
	}
	
	var blinking = false;
	var unblinking = false;
	var display = function(event, blink) {
		if(blink && !blinking){
			blinking = true;
			unblinking = false;
			$('#unread-'+event.data.topicId).parent().animate({
				"color": "#FF0"
			}, 200);
		}
		
		var cptall = parseInt($('#unread-all').html());
		$('#unread-all').html(cptall+1);
		var $topicunreadspan = $('#unread-'+event.data.topicId);
		var cpt = parseInt($topicunreadspan.html());
		$topicunreadspan.html(cpt+1);
		
		if(blink && blinking && !unblinking){
			blinking = false;
			unblinking = true;
			$topicunreadspan.parent().animate({
				"color": "#4F7425"
			}, 200);
		}
		
		$('#topic-events-'+event.data.topicId)
			.prepend('<div id="event-'+event.id+'" class="event"><h3>'+event.data.title+'</h3><p>'+event.data.content+'</p></div>');
		//$('#event-'+event.id).hide().fadeIn();
		
		$('#alltopics-events')
			.prepend('<div id="eventall-'+event.id+'" class="event"><h3>'+event.data.title+'</h3><p>'+event.data.content+'</p></div>');
		//$('#eventall-'+event.id).hide().fadeIn();
	}
	
	var searchAction = #{jsAction @searchTopics() /}
	$('#search-button').bind("click", function(){
		search();
	});
	$('#search').bind('keyup', function(e) {
		var charCode = (e.which) ? e.which : e.keyCode;
		if (charCode == 27){
			$('#search').prop("value", "");
		}
		if (charCode == 13){
			search();
		}
	});
	
	
	var search = function() {
		var searchtitle = $('#searchin-title').hasClass('search-option-checked');
		var searchdesc = $('#searchin-desc').hasClass('search-option-checked');
		$.post(
			searchAction(),
			{search:$("#search").prop("value"), title:searchtitle, desc:searchdesc},
			function(data) {
				$("#available-topics")
					.accordion("destroy")
					.html(data)
					.accordion({
						autoHeight: false,
						navigation: true,
						collapsible: true
					});
			},
			'html');
	}
	
	$('.search-option').bind('click', function(){
		if($(this).hasClass('search-option-checked')){
			$(this).removeClass('search-option-checked');
		} else {
			$(this).addClass('search-option-checked');
		}
		search();
	});
	
	getEvents();
	alert("ok");
});