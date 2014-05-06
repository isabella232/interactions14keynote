

var currentInteractionId = null;


$(window).load(function(){
	document.addEventListener('tizenhwkey', function(e) {
        if(e.keyName == "back")
            tizen.application.getCurrentApplication().exit();
    });
	
	$( "#logoImage" ).click(function() {
		  connect();
	});
	
	$( "#btnDismiss" ).click(function() {
		$('#alert').hide();
		$('#idle').show();
	});
	
	$( "#btnListen" ).click(function() {
		
		var data = {
				'messageType' : 'listen',
				'interactionId' :  $('#currentInteractionId').text()
		}
		
		SASocket.sendData(CHANNELID, JSON.stringify(data) );
	});
	
	$( "#btnJoin" ).click(function() {
		
		var data = {
				'messageType' : 'join',
				'interactionId' :  $('#currentInteractionId').text()
		}
		
		SASocket.sendData(CHANNELID, JSON.stringify(data) );
	});

	$( "#btnDisconnect" ).click(function() {
		
		var data = {
				'messageType' : 'disconnect',
				'interactionId' : $('#currentInteractionId').text()
		}
		
		$('#call').hide();
		$('#alert').hide();
		$('#idle').show();
		
		SASocket.sendData(CHANNELID, JSON.stringify(data) );
	});

	$( "#btnStopListening" ).click(function() {
		
		var data = {
				'messageType' : 'stoplisten',
				'interactionId' :  $('#currentInteractionId').text()
		}
		
		$('#call').hide();
		$('#alert').hide();
		$('#idle').show();
		
		SASocket.sendData(CHANNELID, JSON.stringify(data) );
	});

	
	
	$('#call').hide();
	$('#alert').hide();
	$('#idle').show();
	
	
	setInterval( function() {
		var currentDate = new Date();
		var options = {weekday: "short", year: "numeric", month: "short", day: "numeric"};
		
		$('#date').text(currentDate.toLocaleDateString("en-US", options))
		
		
		var seconds = currentDate.getSeconds();
		var minutes = currentDate.getMinutes();
		var hours = currentDate.getHours();
		
		if(hours > 12){
			hours = hours % 12;
		} 
		
		$('#ampm').text(( hours < 12 ? "AM" : "PM" ));
		
		if(hours == 0){
			hours = 12;
		}
		
		$('#clock').text(( hours < 10 ? "0" : "" ) + hours + ':' + ( minutes < 10 ? "0" : "" ) + minutes); // ;
		
    }, 1000);	
	
	setError('NOT CONNECTED');
	
});




/*
 * 
 * {
 * 	'messageType': 'newAlert',
 *  'title': 'Some title',
 *  'subTitle': 'Sub title here',
 *  'subTitle2': 'Agent: kevin',
 *  'interactionId': '12345234'
 * }
 * 
 * {
 * 	'messageType': 'clearAlert'
 * }
 * 
 * {
 * 	'messageType':'newCall',
 * 	'remoteName' : 'NAME',
 * 	'remoteNumber' : 'Number',
 *  'interactionId' : '123542',
 *  'isListening': 'true'
 * }
 * 
 * {
 * 	'messageType': 'clearCall'
 * }
 */

function onreceive(channelId, data) {
	var message = JSON.parse(data);
	
	console.log(message);
	
	if(message.messageType == 'newAlert' && message.subTitle != ''){
		
		$('#alert').show();
		$('#idle').hide();
		$('#call').hide();
		$('#alertTitle').html(message.title);
		$('#alertSubtitle').html(message.subTitle);
		$('#alertAgent').html(message.subTitle2);
		currentInteractionId = message.interactionId;

		
		$('#currentInteractionId').text(message.interactionId);
		
		
	}
	else if((message.messageType == 'clearAlert') || 
			(message.messageType == 'clearCall')){
		$('#alert').hide();
		$('#idle').show();
		$('#call').hide();
	}
	else if(message.messageType == 'newCall'){
		$('#alert').hide();
		$('#idle').hide();
		$('#call').show();
		$('#callRemoteName').html(message.remoteName);
		$('#callRemoteNumber').html(message.remoteNumber);
		
		if(message.isListening === true){
			$('#btnJoin').show();
			$('#btnStopListening').show();
			$('#btnDisconnect').hide();
		}
		else{
			$('#btnJoin').hide();
			$('#btnStopListening').hide();
			$('#btnDisconnect').show();
		}
		
		$('#currentInteractionId').text(message.interactionId);
		
		currentInteractionId = message.interactionId;
				
	}
	
}


 
 function setStatus(message){
	 $('#status').html(message);
	 $('#status').css('color', 'green');
 }
 
 function setError(message){
	 $('#status').html(message);
	 $('#status').css('color', 'red');
 }
 