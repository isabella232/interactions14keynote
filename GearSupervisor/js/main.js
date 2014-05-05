

var currentInteractionId = null;


$(window).load(function(){
	document.addEventListener('tizenhwkey', function(e) {
        if(e.keyName == "back")
            tizen.application.getCurrentApplication().exit();
    });
	
	$( "#btnReset" ).click(function() {
		  connect();
	});
	
	$( "#btnDismiss" ).click(function() {
		$('#alert').hide();
		$('#idle').show();
	});
	
	$( "#btnListen" ).click(function() {
		
		var data = {
				'messageType' : 'listen',
				'interactionId' : $('#call').data('interactionId')
		}
		
		SASocket.sendData(CHANNELID, JSON.stringify(data) );
	});

	$( "#btnDisconnect" ).click(function() {
		
		var data = {
				'messageType' : 'disconnect',
				'interactionId' : $('#call').data('interactionId')
		}
		
		$('#call').hide();
		$('#alert').hide();
		$('#idle').show();
		
		SASocket.sendData(CHANNELID, JSON.stringify(data) );
	});

	$( "#btnStopListening" ).click(function() {
		
		var data = {
				'messageType' : 'stoplisten',
				'interactionId' : $('#call').data('interactionId')
		}
		
		$('#call').hide();
		$('#alert').hide();
		$('#idle').show();
		
		SASocket.sendData(CHANNELID, JSON.stringify(data) );
	});

	
	
	$('#call').hide();
	$('#alert').hide();
	$('#idle').show();
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
	
	if(message.messageType == 'newAlert'){
		$('#alert').show();
		$('#idle').hide();
		$('#call').hide();
		$('#alertTitle').html(message.title);
		$('#alertSubtitle').html(message.subTitle);
		$('#alertAgent').html(message.subTitle2);
		currentInteractionId = message.interactionId;
		$('#call').data('interactionId', message.interactionId);
		
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
		
		$('#call').data('interactionId', message.interactionId);
		
		currentInteractionId = message.interactionId;
				
	}
	
}


 
 function setStatus(message){
	 $('#status').html(message);
	 $('#status').css('color', 'black');
 }
 
 function setError(message){
	 $('#status').html(message);
	 $('#status').css('color', 'red');
 }
 